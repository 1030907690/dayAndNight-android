package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

/***
 * zhouzhongqing
 * 2019年10月22日14:59:54
 * 视频详情页面
 * */
public class VideoDetailTvActivity extends AppCompatActivity {

    private ImageView videoImg;

    private TextView videoName;

    private TextView videoInfo;

    private final String TAG = VideoDetailTvActivity.class.getSimpleName();

    private final Activity content = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail_tv_activity);
        initView();
        listener();
        initData();
    }


    private void listener() {


    }

    private void initData() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (null != url && !"".equals(url.trim())) {
            GlobalConfig.getInstance().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IDataSourceStrategy dataSourceStrategy = GlobalConfig.getInstance().getDataSourceStrategy();
                        /*List<Video> videoList = dataSourceStrategy.playList(mDatas.get(position).getUrl());
                        Message msg = searchVideoHandler.obtainMessage();
                        msg.what = 1;
                        Bundle data = new Bundle();
                        String videoPath = "https://meigui.qqqq-kuyun.com/20190627/9918_47cdf731/index.m3u8";

                        if (null != videoList && videoList.size() > 0) {
                            videoPath = videoList.get(0).getUrl();
                        }
                        data.putString("videoPath", videoPath);
                        data.putString("url", mDatas.get(position).getUrl());
                        msg.setData(data);
                        videoDetailHandler.sendMessage(msg);*/

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            ToastUtil.showToast(content, "地址参数异常!");
        }
    }


    private Handler videoDetailHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
             switch (msg.what){
                 case 0:
                     break;
                 default:
                     break;
             }
        }
    };

    private void initView() {
        videoImg = findViewById(R.id.video_detail_image_tv);
        videoName = findViewById(R.id.video_detail_name_tv);
        videoInfo = findViewById(R.id.video_detail_plot_info_tv);
    }

}
