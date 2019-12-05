package org.dync.bean;

import java.io.Serializable;

public class VideoSearch implements Serializable {

    public VideoSearch() {

    }

    public VideoSearch(String name, String photo, String url, String performer) {
        this.name = name;
        this.photo = photo;
        this.url = url;
        this.performer = performer;
    }

    private String name;

    private String photo;

    private String url;
    /** 演员**/
    private String performer;

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
