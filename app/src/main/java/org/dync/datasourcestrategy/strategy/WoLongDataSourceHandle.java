package org.dync.datasourcestrategy.strategy;

import org.dync.bean.DataSource;
import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.MappedByteBuffer;
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
 *  卧龙资源的处理
 * */
public class WoLongDataSourceHandle implements IDataSourceStrategy {


    private final String KEY = "WoLong";

    private String domain = "https://wolongzy.net";
    private final Map<String, String> urlMap = new HashMap<>();

    private final int maxPage = 1;

    public WoLongDataSourceHandle() {
        //urlMap.put(KEY, "https://wolongzy.net/search.html?searchword=%s");
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
            videoSearch(videoList, String.format(urlMap.get(KEY), key), page);
        }
    }

    private void videoSearch(List<VideoSearch> videoList, String url, Integer page) throws Exception {
        final Map<String, VideoSearch> info = new HashMap<>();
        Connection connect = Jsoup.connect(url).userAgent(Constant.USER_AGENT);//获取连接对象
        Document document = connect.get();//获取url页面的内容并解析成document对象
        Element bodyElement = document.body();
        Elements videoContent = bodyElement.getElementsByClass("videoContent");
        if (null != videoContent && videoContent.size() > 0) {
            Elements videoContentLis = videoContent.get(0).getElementsByTag("li");
            if (null != videoContentLis && videoContentLis.size() > 0) {
                int line = 0;
                for (Element videoContentLi : videoContentLis) {

                    Elements typeElements = videoContentLi.select("span[class=\"category\"]");
                    if (null != typeElements && typeElements.size() > 0 && !Arrays.asList(GlobalConfig.getInstance().getVersionUpdate().getFilterClass()).contains(typeElements.get(0).text())) {
                        line++;
                        if (line > 20) {
                            continue;
                        }
                        Elements videoDetails = videoContentLi.select("a[class=\"videoName\"]");
                        if (null != videoDetails && videoDetails.size() > 0) {
                            Element videoDetail = videoDetails.get(0);
                            String videoName = videoDetail.text();
                            String href = videoDetail.attr("href");
                            VideoSearch videoSearch = new VideoSearch(videoName, "photo", domain + href, "performer");
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
        List<VideoGroup> videoGroupList = new ArrayList<>();
        try {
            videoDetail = videoDetailInfo(url);

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
            Elements videoName = body.getElementsByClass("whitetitle");
            if (null != videoName && videoName.size() > 0) {
                videoDetail.setName(videoName.get(0).text().replace("名称：", ""));
            }
            Elements videoDetailEl = body.getElementsByClass("right");
            if (null != videoDetailEl && videoDetailEl.size() > 0) {
                Element performer = videoDetailEl.get(0).getElementsByTag("p").get(2);
                videoDetail.setPerformer(performer.text().replace("演员：", ""));
            }

            Elements images = body.select("div[class=\"left\"]");
            if (null != images && images.size() > 0) {
                Element image = images.get(0).getElementsByTag("img").get(0);
                videoDetail.setImageUrl(image.attr("src"));
            }

            //剧情介绍
            String plot = body.select("div[style=\"margin-left:10px;\"]").text().replace("剧情介绍 ", "");
            videoDetail.setPlot(plot);


            //剧集
            Elements episode = body.select("div[class=\"playlist wbox\"]");
            Element content = body.getElementsByClass("width1200 white").get(2);
            Elements elementsH4 = content.getElementsByTag("h4");
            String videoTypes[] = new String[null != elementsH4 ? elementsH4.size() : 0];
            for (int i = 0; i < elementsH4.size(); i++) {
                String videoType = elementsH4.get(i).getElementsByTag("div").get(0).text();
                videoTypes[i] = videoType;
            }

            List<VideoGroup> videoGroupList = new ArrayList<>();

            String split = "\\$";
            if (null != episode && episode.size() > 0) {
                int j = 0;
                for (Element epi : episode) {
                    Elements epiLis = epi.getElementsByTag("li");
                    List<Video> videoList = new ArrayList<>();
                    for (int i = 0; i < epiLis.size(); i++) {
                        Element inputM3u8 = epiLis.get(i).getElementById("m3u8");
                        if (null != inputM3u8) {
                            String value = inputM3u8.attr("value");
                            if (null != value) {
                                String[] epis = value.split(split);
                                if (epis.length >= 2) {
                                    Video video = new Video();
                                    video.setName(epis[0]);
                                    video.setUrl(epis[1]);
                                    videoList.add(video);
                                }
                            }

                        } else {
                            //System.out.println("break " +epiLis.get(i).text());
                            break;
                        }
                    }

                    if (j < videoTypes.length && videoTypes[j].contains("m3u8")) {
                        VideoGroup videoGroup = new VideoGroup();
                        videoGroup.setGroup(videoTypes[j]);
                        videoGroup.setVideoList(videoList);
                        videoGroupList.add(videoGroup);
                    }
                    j++;
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
        IDataSourceStrategy handle = new WoLongDataSourceHandle();
        //handle.search("庆余年12", 1);
        //handle.homeRecommend();
        handle.videoDetail("https://wolongzy.net/detail/295032.html");

    }
}
