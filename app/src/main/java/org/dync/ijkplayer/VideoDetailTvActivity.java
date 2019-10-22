package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.dync.adapter.RecyclerVideoSourceDramaSeriesAdapter;
import org.dync.adapter.RecyclerVideoSourceDramaSeriesTvAdapter;
import org.dync.bean.Video;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoGroup;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.utils.DownLoadTask;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * zhouzhongqing
 * 2019年10月22日14:59:54
 * 视频详情页面
 * */
public class VideoDetailTvActivity extends AppCompatActivity {

    private ImageView videoImg;

    private TextView videoName;

    private TextView videoInfo;

    private RecyclerView ijkplayerVideoNavigationInfoRecyclerView;

    private TabLayout tabLayoutTitle;

    private RecyclerVideoSourceDramaSeriesTvAdapter recyclerVideoSourceDramaSeriesAdapter;

    private Map<String, List<Video>> videoGroupMap = new HashMap<String, List<Video>>();


    private final String TAG = VideoDetailTvActivity.class.getSimpleName();

    private final Activity content = this;


    private final int spanCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail_tv_activity);
        initView();

        initData();
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
                        VideoDetail videoDetail = dataSourceStrategy.videoDetail(url);
                        Message msg = videoDetailHandler.obtainMessage();
                        msg.what = 0;
                        msg.obj = videoDetail;
                        videoDetailHandler.sendMessage(msg);
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
            switch (msg.what) {
                case 0:
                    VideoDetail videoDetail = (VideoDetail) msg.obj;
                    videoName.setText(videoDetail.getName());
                    DownLoadTask downLoadTask = new DownLoadTask(videoImg, content);
                    downLoadTask.execute(videoDetail.getImageUrl());
                    videoInfo.setText(Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot() + videoDetail.getPlot()));

                    List<VideoGroup> videoGroupList = videoDetail.getVideoGroupList();
                    // 初始化剧集
                    ijkplayerVideoNavigationInfoRecyclerView = findViewById(R.id.ijkplayer_video_navigation_info_tv);
                    //纵向线性布局
                    GridLayoutManager layoutManagerInfo = new GridLayoutManager(content, spanCount);
                    ijkplayerVideoNavigationInfoRecyclerView.setLayoutManager(layoutManagerInfo);
                    if (null == videoGroupList || videoGroupList.size() <= 0) {
                        videoGroupList = new ArrayList<VideoGroup>();
                        VideoGroup tempVideoGroup = new VideoGroup();
                        tempVideoGroup.setGroup("无");
                        tempVideoGroup.setVideoList(new ArrayList<>());
                        videoGroupList.add(tempVideoGroup);
                    }
                    recyclerVideoSourceDramaSeriesAdapter = new RecyclerVideoSourceDramaSeriesTvAdapter(content, videoGroupList.get(0).getVideoList());
                    ijkplayerVideoNavigationInfoRecyclerView.setAdapter(recyclerVideoSourceDramaSeriesAdapter);

                    recyclerVideoSourceDramaSeriesAdapter.setOnItemClickListener(new RecyclerVideoSourceDramaSeriesTvAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (view instanceof Button) {
                                Button videoItemBtn = (Button) view;
                                //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                                videoItemBtn.setTextColor(0xFFFFFFFF);
                                VideoTvActivity.intentTo(content, videoItemBtn.getTag().toString(), "测试", "");
                            }

                        }

                        @Override
                        public void onItemLongClick(View view, int position) {
                            ToastUtil.showToast(content, "长按");
                        }
                    });

                    initTab(videoGroupList);
                    break;
                default:
                    break;
            }
        }
    };

    private void initTab(List<VideoGroup> videoGroups) {
        tabLayoutTitle = findViewById(R.id.tab_layout_data_tv);
        tabLayoutTitle.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (VideoGroup videoGroup : videoGroups) {
            tabLayoutTitle.addTab(tabLayoutTitle.newTab().setText(videoGroup.getGroup()).setTag(videoGroup.getGroup()));
            videoGroupMap.put(videoGroup.getGroup(), videoGroup.getVideoList());
        }

        tabLayoutTitle.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String group = (String) tab.getTag();
                //纵向线性布局
                GridLayoutManager layoutManagerInfo = new GridLayoutManager(content, spanCount);
                ijkplayerVideoNavigationInfoRecyclerView.setLayoutManager(layoutManagerInfo);
                ijkplayerVideoNavigationInfoRecyclerView.setAdapter(recyclerVideoSourceDramaSeriesAdapter = new RecyclerVideoSourceDramaSeriesTvAdapter(content, videoGroupMap.get(group)));
                recyclerVideoSourceDramaSeriesAdapter.notifyDataSetChanged();

                recyclerVideoSourceDramaSeriesAdapter.setOnItemClickListener(new RecyclerVideoSourceDramaSeriesTvAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (view instanceof Button) {
                            Button videoItemBtn = (Button) view;
                            //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                            videoItemBtn.setTextColor(0xFFFFFFFF);
                            VideoTvActivity.intentTo(content, videoItemBtn.getTag().toString(), "测试", "");
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        ToastUtil.showToast(content, "长按");
                    }
                });


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void initView() {
        videoImg = findViewById(R.id.video_detail_image_tv);
        videoName = findViewById(R.id.video_detail_name_tv);
        videoInfo = findViewById(R.id.video_detail_plot_info_tv);
    }

}
