package org.dync.datasourcestrategy.strategy;

import org.dync.bean.DataSource;
import org.dync.bean.Video;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.ijkplayer.VideoActivity;
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
    public List<VideoSearch> search(String key) {
        List<VideoSearch> videoList = new ArrayList<>();
        if (urlMap.containsKey(KEY)) {
            try {
                Connection connect = Jsoup.connect(String.format(urlMap.get(KEY), "1",key)).userAgent("Mozilla");//获取连接对象
                Document document = connect.get();//获取url页面的内容并解析成document对象
                Elements classElements = document.body().select("ul[class=\"stui-vodlist clearfix\"]");
                if (null != classElements && classElements.size() > 0) {
                    Elements titleElements = classElements.get(0).getElementsByClass("title");
                    for (Element titleElement : titleElements) {
                        Elements aElements = titleElement.getElementsByTag("a");
                        for (Element aElement : aElements) {
                            /*String href = aElement.attr("href");
                            String title = aElement.attr("title");*/
                            VideoSearch videoSearch = new VideoSearch();
                            videoSearch.setName(aElement.text());
                            videoList.add(videoSearch);
                          //  System.out.println( aElement.text());
                        }

                    }
                }
                //System.out.println(" body " + classElements.html());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return videoList;
    }


    public static void main(String[] args) {
        KuKuZYDataSourceHandle handle = new KuKuZYDataSourceHandle();
        handle.search("海上");

    }
}
