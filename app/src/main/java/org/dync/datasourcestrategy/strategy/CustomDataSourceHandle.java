package org.dync.datasourcestrategy.strategy;

import android.os.Bundle;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;

import org.dync.bean.DataSource;
import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.ijkplayer.MainActivity;
import org.dync.queue.DelayOrderTask;
import org.dync.queue.DelayOrderWorker;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.json.JSONArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/***
 *  自定义私服资源的处理
 * */
public class CustomDataSourceHandle implements IDataSourceStrategy {


    private final int maxPage = 1;

    public CustomDataSourceHandle() {


    }


/*

    @Override
    public List<VideoSearch> search(String key, Integer page) {

        CountDownLatch cdl = new CountDownLatch(1);

        boolean isRun = true;

        List<VideoSearch> videoList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        String apiPrefix = "https://gitee.com/apple_1030907690/weiXin/raw/master/VersionManager.json";//GlobalConfig.getInstance().getSharedPreferences().getString(Constant.CUSTOM_API_PREFIX, null);
        //构造Request对象
        //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
        Request request = new Request.Builder().get().url(apiPrefix + "").build();
        Call call = client.newCall(request);
        String responBody = null;
        try {
            Response execute = call.execute();
            responBody = execute.body().source().readUtf8();
        }catch (IOException e){
            e.printStackTrace();
        }


        //异步调用并设置回调函数
       */
/* call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                cdl.countDown();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseStr = response.body().string();
                System.out.println(responseStr);
                cdl.countDown();
            }
        });*//*


     */
/*

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*//*


        System.out.println(responBody);


        System.out.println("done ");
        return videoList;
    }
*/


    @Override
    public List<VideoSearch> search(String key, Integer page) {
        List<VideoSearch> videoList = new ArrayList<>();
        String apiPrefix = GlobalConfig.getInstance().getSharedPreferences().getString(Constant.CUSTOM_API_PREFIX, null);
        if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_SEARCH + apiPrefix + "/search?keyWord="+key)) {
            DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_SEARCH + apiPrefix + "/search?keyWord="+key);
            if (null != delayOrderTask) {
                DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                List<VideoSearch> videoListTemp = (List<VideoSearch>) delayOrderWorker.getObj();
                videoList.addAll(videoListTemp);
                return videoList;
            }
        }


        OkHttpClient client = new OkHttpClient();
        try {
            //构造Request对象
            //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
            Request request = new Request.Builder().get().url(apiPrefix + "/search?keyWord="+key).build();
            Call call = client.newCall(request);
            Response execute = call.execute();
            String responseBody = execute.body().source().readUtf8();
            if (null != responseBody && !"".equals(responseBody)) {
                videoList = JSONObject.parseArray(responseBody, VideoSearch.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //加入缓存
        DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_SEARCH + apiPrefix + "/search?keyWord="+key, videoList);
        GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
        return videoList;
    }

    @Override
    public List<Video> playList(String url) {
        return null;
    }


    @Override
    public List<VideoGroup> playList(String url, int page) {
        return null;
    }


    @Override
    public VideoDetail videoDetail(String url) {
        VideoDetail videoDetail = new VideoDetail();
        String apiPrefix = GlobalConfig.getInstance().getSharedPreferences().getString(Constant.CUSTOM_API_PREFIX, null);

        if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_VIDEO_DETAIL + apiPrefix + "/videoDetail?url="+url)) {
            DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_VIDEO_DETAIL + apiPrefix + "/videoDetail?url="+url);
            if (null != delayOrderTask) {
                DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                videoDetail = (VideoDetail) delayOrderWorker.getObj();
                return videoDetail;
            }
        }

        OkHttpClient client = new OkHttpClient();


        try {

            //构造Request对象
            //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
            Request request = new Request.Builder().get().url(apiPrefix + "/videoDetail?url="+url).build();
            Call call = client.newCall(request);
            Response execute = call.execute();
            String responseBody = execute.body().source().readUtf8();
            if (null != responseBody && !"".equals(responseBody)) {
                videoDetail = JSONObject.parseObject(responseBody, VideoDetail.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 加入缓存
        DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_VIDEO_DETAIL + apiPrefix + "/videoDetail?url="+url, videoDetail);
        GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
        return videoDetail;
    }


    @Override
    public List<VideoSearch> homeRecommend() {
        List<VideoSearch> videoSearchList = new ArrayList<>();
        String apiPrefix = GlobalConfig.getInstance().getSharedPreferences().getString(Constant.CUSTOM_API_PREFIX, null);
        if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_SEARCH + apiPrefix + "/homeRecommend")) {
            DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_SEARCH + apiPrefix + "/homeRecommend");
            if (null != delayOrderTask) {
                DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                List<VideoSearch> videoListTemp = (List<VideoSearch>) delayOrderWorker.getObj();
                videoSearchList.addAll(videoListTemp);
                return videoSearchList;
            }
        }


        OkHttpClient client = new OkHttpClient();


        try {
            //构造Request对象
            //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
            Request request = new Request.Builder().get().url(apiPrefix + "/homeRecommend").build();
            Call call = client.newCall(request);
            Response execute = call.execute();
            String responseBody = execute.body().source().readUtf8();
            if (null != responseBody && !"".equals(responseBody)) {
                videoSearchList = JSONObject.parseArray(responseBody, VideoSearch.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //加入缓存
        DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_SEARCH + apiPrefix + "/homeRecommend", videoSearchList);
        GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
        return videoSearchList;
    }


    public static void main(String[] args) {
        IDataSourceStrategy handle = new CustomDataSourceHandle();
        handle.search("庆余年12", 1);
        //handle.homeRecommend();
        //handle.videoDetail("https://wolongzy.net/detail/295032.html");

    }
}
