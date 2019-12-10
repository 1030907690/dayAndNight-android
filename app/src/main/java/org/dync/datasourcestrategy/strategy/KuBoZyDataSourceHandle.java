package org.dync.datasourcestrategy.strategy;

import org.dync.bean.DataSource;
import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.queue.DelayOrderTask;
import org.dync.queue.DelayOrderWorker;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;


/***
 *  酷播资源的处理
 * */
public class KuBoZyDataSourceHandle implements IDataSourceStrategy {


    private final String KEY = "KuBoZy";

    private String domain = "http://kubozy.net";
    private final Map<String, String> urlMap = new HashMap<>();

    private final int maxPage = 1;

    public KuBoZyDataSourceHandle() {
        //urlMap.put(KEY, "http://kubozy.net/index.php?m=vod-search-pg-%s-wd-%s.html");
        List<DataSource> dataSourceList = GlobalConfig.getInstance().getVersionUpdate().getDataSource();
        for (DataSource dataSource : dataSourceList) {
            urlMap.put(dataSource.getKey(), dataSource.getSearchUrl());
            if (KEY.equals(dataSource.getKey())) {
                domain = dataSource.getDomain();
            }
        }


    }

    @Override
    public List<VideoSearch> search(String key, Integer page) {
        List<VideoSearch> videoList = new ArrayList<>();
        if (urlMap.containsKey(KEY)) {
            try {
                searchKey(videoList, key, page);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return videoList;
    }


    /***
     * 递归查询所有视频
     * */
    private void searchKey(List<VideoSearch> videoList, String key, Integer page) throws Exception {
        if (page <= maxPage) {
            videoSearch(videoList, String.format(urlMap.get(KEY), page, key), page);
        }
    }

    private void videoSearch(List<VideoSearch> videoList, String url, Integer page) throws Exception {

        if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_SEARCH + url)) {
            DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_SEARCH + url);
            if (null != delayOrderTask) {
                DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                List<VideoSearch> videoListTemp = (List<VideoSearch>) delayOrderWorker.getObj();
                videoList.addAll(videoListTemp);
                return;
            }
        }
        final Map<String, VideoSearch> info = new HashMap<>();
        Connection connect = Jsoup.connect(url).userAgent(Constant.USER_AGENT);//获取连接对象
        Document document = connect.get();//获取url页面的内容并解析成document对象
        Element bodyElement = document.body();
        Elements videoContent = bodyElement.getElementsByClass("xing_vb");
        if (null != videoContent && videoContent.size() > 0) {
            Elements uls = videoContent.get(0).getElementsByTag("ul");
            if (null != uls && uls.size() > 1) {
                int j = 0;
                for (int i = 1; i < uls.size(); i++) {
                    Element ul = uls.get(i);
                    Elements spans = ul.getElementsByTag("span");
                    if (null != spans && spans.size() >= 4) {

                        String type = spans.get(2).text();
                        if (null != type && !Arrays.asList(GlobalConfig.getInstance().getVersionUpdate().getFilterClass()).contains(type)) {
                            j++;
                            if (j > 20) {
                                continue;
                            }
                            Element aTag = spans.get(1).getElementsByTag("a").get(0);
                            String detailUrl = domain + aTag.attr("href");
                            String name = aTag.text();
                            VideoSearch videoSearch = new VideoSearch(name, "photo", detailUrl, "performer");
                            info.put(videoSearch.getUrl(), videoSearch);
                        }
                    }
                }
            }
        }


        long startTime = System.currentTimeMillis();
        //使用CompletionService并行执行
        CompletionService<VideoDetail> completionService = new ExecutorCompletionService<>(GlobalConfig.getInstance().executorService());
        for (Map.Entry<String, VideoSearch> videoSearch : info.entrySet()) {
            completionService.submit(new Callable<VideoDetail>() {
                public VideoDetail call() {
                    VideoDetail videoAttributes = null;
                    try {
                        videoAttributes = videoDetailInfo(videoSearch.getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return videoAttributes;
                }
            });

        }
        try {
            int size = info.size();
            for (int t = 0; t < size; t++) {
                Future<VideoDetail> f = completionService.take();
                VideoDetail videoData = f.get();
                if (null != videoData) {
                    VideoSearch videoSearch = info.get(videoData.getUrl());
                    videoSearch.setPerformer(videoData.getPerformer());
                    videoSearch.setPhoto(videoData.getImageUrl());
                    videoList.add(videoSearch);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("CompletionService Execution");
        }

        long endTime = System.currentTimeMillis();

        System.out.println("runTime " + (endTime - startTime));
        //加入缓存
        DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_SEARCH + url, videoList);
        GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
    }


    @Override
    public List<Video> playList(String url) {
        return null;
    }


    @Override
    public List<VideoGroup> playList(String url, int page) {
        return null;
    }


    @Override
    public VideoDetail videoDetail(String url) {
        VideoDetail videoDetail = new VideoDetail();
        try {
            if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_VIDEO_DETAIL + url)) {
                DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_VIDEO_DETAIL + url);
                if (null != delayOrderTask) {
                    DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                    videoDetail = (VideoDetail) delayOrderWorker.getObj();
                    return videoDetail;
                }
            }
            videoDetail = videoDetailInfo(url);
            DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_VIDEO_DETAIL + url, videoDetail);
            GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoDetail;
    }

    /***
     * 获取视频详情
     * */
    public VideoDetail videoDetailInfo(String url) {

        VideoDetail videoDetail = new VideoDetail();
        videoDetail.setUrl(url);
        try {
            Element body = Jsoup.connect(url).userAgent(Constant.USER_AGENT).get().body();
            Elements videoName = body.getElementsByClass("vodh");
            if (null != videoName && videoName.size() > 0) {
                videoDetail.setName(videoName.get(0).text().replace("名称：", ""));
            }
            Elements videoDetailEl = body.getElementsByClass("vodinfobox");
            if (null != videoDetailEl && videoDetailEl.size() > 0) {
                Element performer = videoDetailEl.get(0).getElementsByTag("li").get(2);
                videoDetail.setPerformer(performer.text().replace("主演：", ""));
            }

            Elements images = body.select("img[class=\"lazy\"]");
            if (null != images && images.size() > 0) {
                videoDetail.setImageUrl(images.get(0).attr("src"));
            }

            //剧情介绍
            String plot = body.select("div[class=\"vodplayinfo\"]").get(0).text().replace("剧情介绍 ", "");
            videoDetail.setPlot(plot.trim());


            //剧集
            Element episode = body.select("div[style=\"padding-left:10px;word-break: break-all; word-wrap:break-word;\"]").get(0);
            Elements divs = episode.getElementsByTag("div");
            List<VideoGroup> videoGroupList = new ArrayList<>();
            String split = "\\$";
            for (Element div : divs) {
                String group = div.getElementsByTag("h3").get(0).getElementsByTag("span").get(0).text();
                if (null != group && group.contains("m3u8")) {

                    Elements uls = div.getElementsByTag("ul");
                    if (null != uls && uls.size() > 0) {
                        Elements lis = uls.get(0).getElementsByTag("li");
                        List<Video> videoList = new ArrayList<>();
                        if (null != lis && lis.size() > 0) {
                            for (Element li : lis) {
                                String[] epiArr = li.text().split(split);
                                String number = epiArr[0];
                                String m3u8 = epiArr[1];
                                Video video = new Video();
                                video.setName(number);
                                video.setUrl(m3u8);
                                videoList.add(video);
                            }
                        }
                        VideoGroup videoGroup = new VideoGroup();
                        videoGroup.setGroup(group);
                        videoGroup.setVideoList(videoList);
                        videoGroupList.add(videoGroup);
                    }

                }
            }

            videoDetail.setVideoGroupList(videoGroupList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return videoDetail;
    }


    @Override
    public List<VideoSearch> homeRecommend() {
        List<VideoSearch> videoSearchList = new ArrayList<>();
        try {
            videoSearch(videoSearchList, domain, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoSearchList;
    }


    public static void main(String[] args) {
        IDataSourceStrategy handle = new KuBoZyDataSourceHandle();
        handle.search("庆余年", 1);
        handle.homeRecommend();
        handle.videoDetail("http://kubozy.net/?m=vod-detail-id-30397.html");

    }
}
