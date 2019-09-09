package org.dync.datasourcestrategy.strategy;

import org.dync.bean.Video;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
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

    public KuKuZYDataSourceHandle() {
       /* List<DataSource> dataSourceList = GlobalConfig.getInstance().getVersionUpdate().getDataSource();
        for (DataSource dataSource : dataSourceList) {
            urlMap.put(dataSource.getKey(), dataSource.getSearchUrl());
            if(KEY.equals(dataSource.getKey())){
                domain = dataSource.getDomain();
            }
        }*/
        // urlMap.put(KEY, "http://www.kukuzy.com/index.php/vod/search.html?wd=%s&submit=");
        urlMap.put(KEY, "http://www.kukuzy.com/index.php/vod/search/page/%s/wd/%s.html");
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


    private void searchKey(List<VideoSearch> videoList, String key, Integer page) throws Exception {
        Connection connect = Jsoup.connect(String.format(urlMap.get(KEY), page, key)).userAgent("Mozilla");//获取连接对象
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
                        videoSearch.setPhoto("http://www.605zy.cc/upload/vod/2019-08/15666429251.jpg");
                        videoSearch.setUrl(domain + href);
                        videoList.add(videoSearch);
                    }
                }
                searchKey(videoList, key, page + 1);
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
                            System.out.println(" videoElement  " + videoInputElement.attr("value"));
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

    public static void main(String[] args) {
        KuKuZYDataSourceHandle handle = new KuKuZYDataSourceHandle();
        handle.search("海上",1);
    }
}
