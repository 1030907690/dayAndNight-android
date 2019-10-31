package org.dync.datasourcestrategy.strategy;

import com.alibaba.fastjson.JSONObject;

import org.dync.bean.DataSource;
import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
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


/***
 *  KuKuzy的处理
 * */
public class KuKuZYDataSourceHandle implements IDataSourceStrategy {


    private final String KEY = "KuKuZY";

    private String domain = "http://www.kukuzy.com";
    private final Map<String, String> urlMap = new HashMap<>();

    private final int maxPage = 1;

    public KuKuZYDataSourceHandle() {
        List<DataSource> dataSourceList = GlobalConfig.getInstance().getVersionUpdate().getDataSource();
        for (DataSource dataSource : dataSourceList) {
            urlMap.put(dataSource.getKey(), dataSource.getSearchUrl());
            if (KEY.equals(dataSource.getKey())) {
                domain = dataSource.getDomain();
            }
        }
        //urlMap.put(KEY, "http://www.kukuzy.com/index.php/vod/search.html?wd=%s&submit=");
        //urlMap.put(KEY, "http://www.kukuzy.com/index.php/vod/search/page/%s/wd/%s.html");
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
        /*Connection connect = Jsoup.connect(String.format(urlMap.get(KEY), page, key)).userAgent("Mozilla");//获取连接对象
        Document document = connect.get();//获取url页面的内容并解析成document对象
        Elements classElements = document.body().select("ul[class=\"stui-vodlist clearfix\"]");
        if (null != classElements && classElements.size() > 0) {
            Elements titleElements = classElements.get(0).getElementsByClass("title");
            if (null != titleElements && titleElements.size() > 0) {
                for (Element titleElement : titleElements) {
                    Elements aElements = titleElement.getElementsByTag("a");
                    for (Element aElement : aElements) {
                        String href = aElement.attr("href");
                        String title = aElement.attr("title");
                        VideoSearch videoSearch = new VideoSearch();
                        videoSearch.setName(aElement.text());
                        videoSearch.setUrl(domain + href);
                        videoDescribe(videoSearch);
                        videoList.add(videoSearch);
                    }
                }
                searchKey(videoList, key, page + 1);
            }
        }*/

        if (page <= maxPage) {
            Connection connect = Jsoup.connect(String.format(urlMap.get(KEY), page, key)).userAgent("Mozilla");//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Elements classElements = document.body().select("ul[class=\"stui-vodlist clearfix\"]");
            if (null != classElements && classElements.size() > 0) {
                Elements liClassElements = classElements.get(0).select("li[class=\"clearfix\"]");
                if (null != liClassElements && liClassElements.size() > 0) {
                    for (Element liClassElement : liClassElements) {
                        Elements titleElements = liClassElement.getElementsByClass("title");
                        Elements typeElements = liClassElement.getElementsByClass("type");
                        if (!Arrays.asList(GlobalConfig.getInstance().getVersionUpdate().getFilterClass()).contains(typeElements.get(0).text())) {
                            if (null != titleElements && titleElements.size() > 0) {
                                for (Element titleElement : titleElements) {
                                    Elements aElements = titleElement.getElementsByTag("a");
                                    for (Element aElement : aElements) {
                                        String href = aElement.attr("href");
                                        String title = aElement.attr("title");
                                        VideoSearch videoSearch = new VideoSearch();
                                        videoSearch.setName(aElement.text());
                                        videoSearch.setUrl(domain + href);
                                        videoDescribe(videoSearch);
                                        videoList.add(videoSearch);
                                        //System.out.println(JSONObject.toJSONString(videoSearch));
                                    }
                                }
                            }
                        }
                    }
                    searchKey(videoList, key, page + 1);
                }
            }

        }
    }

    /***
     * 获取视频详情
     * */
    private void videoDescribe(VideoSearch videoSearch) {
        // 设置默认值
        videoSearch.setPhoto("http://www.605zy.cc/upload/vod/2019-08/15666429251.jpg");
        if (null != videoSearch && null != videoSearch.getUrl()) {
            try {
                Connection connect = Jsoup.connect(String.format(videoSearch.getUrl())).userAgent("Mozilla");//获取连接对象
                Document document = connect.get();//获取url页面的内容并解析成document对象
                // 获取图片url
                Elements imageElements = document.body().select("img[class=\"img-responsive\"]");
                if (null != imageElements && imageElements.size() > 0) {
                    String imageSrc = imageElements.get(0).attr("src");
                    if (null != imageSrc && !"".equals(imageSrc.trim())) {
                        if (imageSrc.startsWith("http://") || imageSrc.startsWith("https://")) {
                            videoSearch.setPhoto(imageSrc);
                        }

                        //System.out.println(imageSrc);
                    }
                }
               Map<String,String> result = videoDetailInfo(document.body());
                videoSearch.setPerformer(result.get("leadPerformer"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Video> playList(String url) {
        List<Video> videoList = new ArrayList<>();
        try {
            String $str = "$";
            Connection connect = Jsoup.connect(url);//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Elements classElements = document.body().select("div[id=\"playlist\"]");
            int i = 0;
            for (Element classElement : classElements) {
                i++;
                Elements titleElements = classElement.select("h3[class=\"title\"]");
                if (titleElements.html().contains("m3u8")) {
                    Elements videoElements = classElement.getElementsByTag("li");
                    for (Element videoElement : videoElements) {
                        Elements videoInputElements = videoElement.select("input[type=\"checkbox\"]");
                        for (Element videoInputElement : videoInputElements) {
                            //System.out.println(" videoElement  " + videoInputElement.attr("value"));
                            String[] videoArray = videoInputElement.attr("value").split("\\" + $str);
                            Video video = new Video();
                            video.setName("视频源" + i + videoArray[0].replace($str, ""));
                            video.setUrl(videoArray[1]);
                            videoList.add(video);
                        }

                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoList;
    }


    @Override
    public List<VideoGroup> playList(String url, int page) {
        List<VideoGroup> videoGroupList = new ArrayList<>();
        try {
            String $str = "$";
            Connection connect = Jsoup.connect(url);//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Elements classElements = document.body().select("div[id=\"playlist\"]");
            int i = 0;
            for (Element classElement : classElements) {
                i++;
                Elements titleElements = classElement.select("h3[class=\"title\"]");
                if (titleElements.html().contains("m3u8")) {

                    List<Video> videoList = new ArrayList<>();
                    Elements videoElements = classElement.getElementsByTag("li");
                    for (Element videoElement : videoElements) {
                        Elements videoInputElements = videoElement.select("input[type=\"checkbox\"]");
                        for (Element videoInputElement : videoInputElements) {
                            //System.out.println(" videoElement  " + videoInputElement.attr("value"));
                            String[] videoArray = videoInputElement.attr("value").split("\\" + $str);
                            Video video = new Video();
                            video.setName(videoArray[0].replace($str, ""));
                            video.setUrl(videoArray[1]);
                            videoList.add(video);
                        }

                    }
                    VideoGroup videoGroup = new VideoGroup();
                    videoGroup.setGroup(titleElements.text().replace("播放类型", "").replace(":", "").replace("：", "").trim());
                    videoGroup.setVideoList(videoList);
                    videoGroupList.add(videoGroup);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoGroupList;
    }


    @Override
    public VideoDetail videoDetail(String url) {
        VideoDetail videoDetail = new VideoDetail();
        List<VideoGroup> videoGroupList = new ArrayList<>();
        try {
            String $str = "$";
            Connection connect = Jsoup.connect(url).userAgent("Mozilla");
            ;//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象

            Element body = document.body();
            //1、解析视频图片、名称 剧情等等
            String videoName = "";
            try {
                //图片
                String imageUrl = body.getElementsByClass("img-responsive").attr("src");
                videoDetail.setImageUrl(imageUrl);
                //名称
                videoName = body.select("h1[class=\"title\"]").text();
                videoName = videoName.replace(body.select("h1[class=\"title\"]").select("small[class=\"text-red\"]").text(), "");
                videoDetail.setName(videoName);

                // 剧情
                String plot = body.getElementsByClass("stui-content__desc col-pd clearfix").text();
                videoDetail.setPlot(plot);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //2、解析剧集
            Elements classElements = body.select("div[id=\"playlist\"]");
            for (Element classElement : classElements) {
                Elements titleElements = classElement.select("h3[class=\"title\"]");
                if (titleElements.html().contains("m3u8")) {
                    List<Video> videoList = new ArrayList<>();
                    Elements videoElements = classElement.getElementsByTag("li");
                    for (Element videoElement : videoElements) {
                        Elements videoInputElements = videoElement.select("input[type=\"checkbox\"]");
                        for (Element videoInputElement : videoInputElements) {
                            //System.out.println(" videoElement  " + videoInputElement.attr("value"));
                            String[] videoArray = videoInputElement.attr("value").split("\\" + $str);
                            Video video = new Video();
                            video.setName(videoArray[0].replace($str, ""));
                            video.setUrl(videoArray[1]);
                            videoList.add(video);
                        }

                    }
                    VideoGroup videoGroup = new VideoGroup();
                    videoGroup.setGroup(titleElements.text().replace("播放类型", "").replace(":", "").replace("：", "").trim());
                    videoGroup.setVideoList(videoList);
                    videoGroupList.add(videoGroup);
                }

            }

            Map<String, String> result = videoDetailInfo(body);
            videoDetail.setPerformer(result.get("leadPerformer"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        videoDetail.setVideoGroupList(videoGroupList);
        return videoDetail;
    }

    /***
     * 获取视频详情
     * */
    public Map<String, String> videoDetailInfo(Element body) {


        Map<String, String> result = new HashMap<>();
        //解析主演等等
        Elements videoDetailInfo = body.getElementsByClass("stui-content__detail");

        try {
            //主演
            Elements leadPerformer = videoDetailInfo.get(0).getElementsByClass("data");
            String leadPerformerText = leadPerformer.get(2).text();
            result.put("leadPerformer",leadPerformerText);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

        }


        try {
            //图片
            String imageUrl = body.getElementsByClass("img-responsive").attr("src");

            //名称
            String videoName = body.select("h1[class=\"title\"]").text();
            videoName = videoName.replace(body.select("h1[class=\"title\"]").select("small[class=\"text-red\"]").text(), "");

            // 剧情
            String plot = body.getElementsByClass("stui-content__desc col-pd clearfix").text();
            result.put("plot",plot);
            result.put("videoName",videoName);
            result.put("imageUrl",imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public List<VideoSearch> homeRecommend() {
        List<VideoSearch> videoSearchList = new ArrayList<>();
        try {
            Connection connect = Jsoup.connect(this.domain).userAgent("Mozilla");
            ;//获取连接对象
            Document document = connect.get();//获取url页面的内容并解析成document对象
            Element body = document.body();
            Elements ulElements = body.getElementsByClass("stui-vodlist clearfix");
            if (null != ulElements && ulElements.size() > 0) {
                Element ulElement = ulElements.get(0);
                Elements liElements = ulElement.select("li[class=\"clearfix\"]");
                if (null != liElements) {
                    int i = 0;
                    for (Element liElement : liElements) {
                        if(i > 10){
                            continue;
                        }
                        i++;
                        Elements aTags = liElement.getElementsByClass("title").get(0).getElementsByTag("a");
                        Elements typeElements = liElement.getElementsByClass("type");
                        if (!Arrays.asList(GlobalConfig.getInstance().getVersionUpdate().getFilterClass()).contains(typeElements.get(0).text())) {
                            Element aTag = aTags.get(0);
                            VideoSearch videoSearch = new VideoSearch();
                            videoSearch.setName(aTag.text());
                            videoSearch.setUrl(domain + aTag.attr("href"));
                            videoDescribe(videoSearch);
                            videoSearchList.add(videoSearch);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoSearchList;
    }

    public static void main(String[] args) {
        KuKuZYDataSourceHandle handle = new KuKuZYDataSourceHandle();
       /* handle.search("海上", 1);
        VideoSearch videoSearch = new VideoSearch();*/
        // videoSearch.setUrl("http://www.kukuzy.com/index.php/vod/detail/id/39261.html");
        //handle.videoDescribe(videoSearch);

        handle.videoDetailInfo(null);


    }
}
