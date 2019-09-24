package org.dync.bean;

import java.io.Serializable;
import java.util.List;

/***
 * zhouzhongqing
 * 2019年9月23日16:26:06
 * 视频分组
 * */
public class VideoGroup implements Serializable {

    private String group;

    private List<Video> videoList;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }
}
