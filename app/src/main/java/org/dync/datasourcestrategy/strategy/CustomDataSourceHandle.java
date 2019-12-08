package org.dync.datasourcestrategy.strategy;

import android.os.Bundle;
import android.os.Message;

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



    @Override
    public List<VideoSearch> search(String key, Integer page) {

        CountDownLatch cdl = new CountDownLatch(1);

        boolean isRun = true;

        List<VideoSearch> videoList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        String a = "";
        String apiPrefix = "https://gitee.com/apple_1030907690/weiXin/raw/master/VersionManager.json";//GlobalConfig.getInstance().getSharedPreferences().getString(Constant.CUSTOM_API_PREFIX, null);
        //构造Request对象
        //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
        Request request = new Request.Builder().get().url(apiPrefix + "").build();
        Call call = client.newCall(request);
        //异步调用并设置回调函数
        call.enqueue(new Callback() {
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
        });


        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("done ");
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
        return videoDetail;
    }


    @Override
    public List<VideoSearch> homeRecommend() {
        List<VideoSearch> videoSearchList = new ArrayList<>();

        return videoSearchList;
    }


    public static void main(String[] args) {
        IDataSourceStrategy handle = new CustomDataSourceHandle();
        handle.search("庆余年12", 1);
        //handle.homeRecommend();
        //handle.videoDetail("https://wolongzy.net/detail/295032.html");

    }
}
