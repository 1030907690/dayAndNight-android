package org.dync.bean;

import java.io.Serializable;
import java.util.List;

/***
 * 视频详情
 */
public class VideoDetail implements Serializable {

    /** 视频剧集分组 **/
    private List<VideoGroup> videoGroupList;

    /** 视频封面 **/
    private String imageUrl;

    /** 视频名称 **/
    private String name;

    /**剧情**/
    private String plot;


    /**主演**/
    private String performer;

    /** 视频的详情网页地址**/
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public List<VideoGroup> getVideoGroupList() {
        return videoGroupList;
    }

    public void setVideoGroupList(List<VideoGroup> videoGroupList) {
        this.videoGroupList = videoGroupList;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
