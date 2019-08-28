package org.dync.bean;

import java.io.Serializable;

/***
 * 数据源地址
 * */
public class DataSource implements Serializable {

    /**  网站 **/
    private String domain;

    /** 网站key**/
    private String key;

    /** 搜索的地址  有%s号好作替换  如 http://www.kukuzy.com/index.php/vod/search.html?wd=海上&submit=   http://www.kukuzy.com/index.php/vod/search.html?wd=%s&submit= **/
    private String searchUrl;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }
}
