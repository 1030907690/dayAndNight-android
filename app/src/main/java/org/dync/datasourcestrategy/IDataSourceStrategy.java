package org.dync.datasourcestrategy;

import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;

import java.util.List;

/*
* 视频源策略
* */
public interface IDataSourceStrategy {

    /***
     * 搜索 获取资源策略
     * @param key 关键字
     * */
    public List<VideoSearch> search(String key,Integer page);

    /***
     * 获取播放列表 具体的集
     * @param url
     * */
    public List<Video> playList(String url);


    /***
     * 获取播放列表 具体的集
     * @param url
     * */
    public List<VideoGroup> playList(String url,int page);


    /***
     * 获取视频的详情
     * */
    public VideoDetail videoDetail(String url);


}
