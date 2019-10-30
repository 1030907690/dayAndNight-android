package org.dync.utils;

public enum VideoType {

    SEARCH(0,"搜索进入"),
    DOWNLOAD(1,"下载的视频");


    VideoType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private Integer code;

    private String name;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 根据code获取去
     * @param code
     * @return
     */
    public static VideoType get(Integer code){
        for(VideoType platformFree:VideoType.values()){
            if(code.equals(platformFree.getCode())){
                return platformFree;
            }
        }
        return  null;
    }
}
