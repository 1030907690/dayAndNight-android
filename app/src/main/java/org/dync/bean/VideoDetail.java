package org.dync.bean;

import java.io.Serializable;
import java.util.List;

/***
 * 视频详情
 */
public class VideoDetail implements Serializable {

    private List<VideoGroup> videoGroupList;

    private String imageUrl;

    private String name;

    private String plot;

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
