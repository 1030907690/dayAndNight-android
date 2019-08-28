package org.dync.bean;

import java.io.Serializable;

public class VideoSearch implements Serializable {

    private String name;

    private String photo;


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
