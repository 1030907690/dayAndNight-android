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
        urlMap.put(KEY, "https://wolongzy.net/search.html?searchword=%s");
        /*List<DataSource> dataSourceList = GlobalConfig.getInstance().getVersionUpdate().getDataSource();
        for (DataSource dataSource : dataSourceList) {
            urlMap.put(dataSource.getKey(), dataSource.getSearchUrl());
            if (KEY.equals(dataSource.getKey())) {
                domain = dataSource.getDomain();
            }
        }*/


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
            final Map<String, VideoSearch> info = new HashMap<>();
            Connection connect = Jsoup.connect(String.format(urlMap.get(KEY), key)).userAgent(Constant.USER_AGENT);//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Element bodyElement = document.body();
            Elements videoContent = bodyElement.getElementsByClass("videoContent");
            if (null != videoContent && videoContent.size() > 0) {
                Elements videoContentLis = videoContent.get(0).getElementsByTag("li");
                if (null != videoContentLis && videoContentLis.size() > 0) {
                    for (Element videoContentLi : videoContentLis) {
                        Elements videoDetails = videoContentLi.select("a[class=\"videoName\"]");
                        if (null != videoDetails && videoDetails.size() > 0) {
                            Element videoDetail = videoDetails.get(0);
                            String videoName = videoDetail.text();
                            String href = videoDetail.attr("href");
                            VideoSearch videoSearch = new VideoSearch(videoName, "photo", domain + href, "主演");
                            info.put(videoSearch.getUrl(), videoSearch);
                        }

                    }
                }
            }

            long startTime = System.currentTimeMillis();

            CompletionService<Map<String, String>> completionService = new ExecutorCompletionService<>(GlobalConfig.getInstance().executorService());
            for (Map.Entry<String, VideoSearch> videoSearch : info.entrySet()) {
                completionService.submit(new Callable<Map<String, String>>() {
                    public Map<String, String> call() {
                        Map<String, String> videoAttributes = new HashMap<>();
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
                for (int t = 0; t < info.size(); t++) {
                    Future<Map<String, String>> f = completionService.take();
                    Map<String, String> videoDate = f.get();
                    System.out.println("");

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new IllegalArgumentException("CompletionService Execution");
            }

            long endTime = System.currentTimeMillis();

            System.out.println("runTime " + (endTime - startTime));
        }
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        videoDetail.setVideoGroupList(videoGroupList);
        return videoDetail;
    }

    /***
     * 获取视频详情
     * */
    public Map<String, String> videoDetailInfo(String url) {
        Map<String, String> result = new HashMap<>();
        try {
            Element body = Jsoup.connect(url).userAgent(Constant.USER_AGENT).get().body();
            Elements videoName = body.getElementsByClass("whitetitle");
            result.put("videoName", videoName.get(0).text().replace("名称：", ""));
            System.out.println(" 视频名称 " + videoName.get(0).text());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public List<VideoSearch> homeRecommend() {
        List<VideoSearch> videoSearchList = new ArrayList<>();
        try {
            Connection connect = Jsoup.connect(this.domain).userAgent(Constant.USER_AGENT);
            ;//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Element body = document.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoSearchList;
    }


    public static void main(String[] args) {
        WoLongDataSourceHandle handle = new WoLongDataSourceHandle();
        handle.search("九州", 1);
        System.exit(0);

    }
}
