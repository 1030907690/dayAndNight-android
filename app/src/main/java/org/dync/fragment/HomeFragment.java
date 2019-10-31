package org.dync.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.dync.adapter.RecyclerHomeRecommendAdapter;
import org.dync.adapter.RecyclerHomeRecommendTvAdapter;
import org.dync.adapter.RecyclerLiveRecommendAdapter;
import org.dync.adapter.RecyclerLiveRecommendTvAdapter;
import org.dync.bean.Live;
import org.dync.bean.VersionUpdate;
import org.dync.bean.VideoDetail;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.ijkplayer.BottomNavigationViewActivity;
import org.dync.ijkplayer.ExoActivity;
import org.dync.ijkplayer.R;
import org.dync.ijkplayer.SettingActivity;
import org.dync.ijkplayer.VideoActivity;
import org.dync.ijkplayer.VideoDetailTvActivity;
import org.dync.ijkplayer.VideoTvActivity;
import org.dync.ijkplayerlib.widget.receiver.NetWorkControl;
import org.dync.ijkplayerlib.widget.receiver.NetworkChangedReceiver;
import org.dync.ijkplayerlib.widget.util.Settings;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.dync.utils.VideoType;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/***
 * 首页
 * */
public class HomeFragment extends Fragment {


    private Button btnSetting;
    private final String TAG = "HomeFragment";

    Button btnExoPlayer;
    private Button btnPlayer;
    private TextView tv;

    /**
     * 首页推荐
     **/
    private RecyclerView recommendRecyclerView;

    /**
     * 首页推荐
     **/
    private RecyclerView recommendLiveRecyclerView;
    private RecyclerHomeRecommendAdapter recyclerHomeRecommendTvAdapter;
    private RecyclerLiveRecommendAdapter recyclerLiveRecommendTvAdapter;

    private List<VideoSearch> videoSearchList;
    private List<Live> liveList;


    public static HomeFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString("name", name);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.bind(getActivity());
        NetworkChangedReceiver register = NetWorkControl.register(TAG, getActivity());
        //设置默认的播放器
        final Settings settings = new Settings(getActivity());
        settings.setPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //tv = (TextView) view.findViewById(R.id.fragment_test_tv);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
            //tv.setText(name);
        }
        btnExoPlayer = view.findViewById(R.id.btn_exoPlayer);

        btnSetting = (Button) view.findViewById(R.id.btn_setting);

        // 隐藏
        btnExoPlayer.setVisibility(View.GONE);
        btnSetting.setVisibility(View.GONE);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });


        btnPlayer = (Button) view.findViewById(R.id.btn_ijkPlayer);
        btnPlayer.setText("测试");
        //隐藏
        btnPlayer.setVisibility(View.GONE);
        btnPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoPath = "https://meigui.qqqq-kuyun.com/20190627/9918_47cdf731/index.m3u8";
                VideoActivity.intentTo(getActivity(), videoPath, "测试", "");
            }
        });


        recommendRecyclerView = view.findViewById(R.id.home_lately_recommend_view);
        recommendLiveRecyclerView = view.findViewById(R.id.home_live_recommend_view);
        loadingRecommend();

    }


    private Handler mainActivityHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    VideoDetail videoDetail = (VideoDetail) msg.obj;
                    VideoActivity.intentTo(getActivity(), videoDetail.getVideoGroupList().get(0).getVideoList().get(0).getUrl(), "测试", videoDetail.getPerformer(), videoDetail.getName(), VideoType.HOME.getCode());
                    break;
                case 1:
                    List<VideoSearch> videoSearchList = (List<VideoSearch>) msg.obj;

                    //纵向线性布局
                    GridLayoutManager layoutManagerInfo = new GridLayoutManager(getActivity(), 3);
                    recommendRecyclerView.setLayoutManager(layoutManagerInfo);
                    recyclerHomeRecommendTvAdapter = new RecyclerHomeRecommendAdapter(getActivity(), videoSearchList);
                    recommendRecyclerView.setAdapter(recyclerHomeRecommendTvAdapter);


                    recyclerHomeRecommendTvAdapter.setOnItemClickListener(new RecyclerHomeRecommendAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (view instanceof ImageButton) {
                                ImageButton videoItemBtn = (ImageButton) view;
                                //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());


                                GlobalConfig.getInstance().executorService().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // String implName = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(0).getKey() + "DataSourceHandle";
                                            IDataSourceStrategy dataSourceStrategy = GlobalConfig.getInstance().getDataSourceStrategy();
                                            VideoDetail videoDetail = new VideoDetail();
                                            if (null != videoItemBtn.getTag().toString() && !"".equals(videoItemBtn.getTag().toString().trim())) {
                                                videoDetail = dataSourceStrategy.videoDetail(videoItemBtn.getTag().toString());
                                            }

                                            videoDetail.setPerformer(videoItemBtn.getTag().toString());
                                            videoDetail.setName(videoSearchList.get(position).getName() + " " + videoDetail.getVideoGroupList().get(0).getVideoList().get(0).getName());
                                            Message msg = mainActivityHandle.obtainMessage();
                                            msg.obj = videoDetail;
                                            msg.what = 0;
                                            mainActivityHandle.sendMessage(msg);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    });

                    break;

                case 2:
                    liveList = (List<Live>) msg.obj;
                    //纵向线性布局
                    GridLayoutManager layoutManagerInfoLive = new GridLayoutManager(getActivity(), 3);
                    recommendLiveRecyclerView.setLayoutManager(layoutManagerInfoLive);
                    recyclerLiveRecommendTvAdapter = new RecyclerLiveRecommendAdapter(getActivity(), liveList);
                    recommendLiveRecyclerView.setAdapter(recyclerLiveRecommendTvAdapter);


                    recyclerLiveRecommendTvAdapter.setOnItemClickListener(new RecyclerLiveRecommendAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (view instanceof ImageButton) {
                                ImageButton videoItemBtn = (ImageButton) view;
                                //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                                VideoActivity.intentTo(getActivity(), videoItemBtn.getTag().toString(), "测试", "", liveList.get(position).getName(), VideoType.HOME.getCode());
                            }
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    });

                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 加载推荐内容
     **/
    private void loadingRecommend() {

        if (null != GlobalConfig.getInstance().getVersionUpdate()) {
            //加载推荐视频
            GlobalConfig.getInstance().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    videoSearchList = GlobalConfig.getInstance().getDataSourceStrategy().homeRecommend();
                    if (null == videoSearchList) {
                        videoSearchList = new ArrayList<>();
                    }
                    Message msg = mainActivityHandle.obtainMessage();
                    msg.what = 1;
                    msg.obj = videoSearchList;
                    mainActivityHandle.sendMessage(msg);
                }
            });

            //加载直播列表
            GlobalConfig.getInstance().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    //构造Request对象
                    //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
                    Request request = new Request.Builder().get().url(GlobalConfig.LIVE_URL).build();
                    Call call = client.newCall(request);
                    //异步调用并设置回调函数
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            //e.printStackTrace();
                            ToastUtil.showToast(getActivity(), "获取直播列表失败!");
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            final String responseStr = response.body().string();

                            try {
                                List<Live> liveList = JSONObject.parseArray(responseStr, Live.class);
                                if (null == liveList) {
                                    liveList = new ArrayList<>();
                                }
                                Message msg = mainActivityHandle.obtainMessage();
                                msg.what = 2;
                                msg.obj = liveList;
                                mainActivityHandle.sendMessage(msg);
                            } catch (Exception e) {
                                Log.d("main live exception", e.getMessage());
                            }

                        }
                    });
                }
            });

        } else {
            ToastUtil.showToast(getActivity(), "连接直播服务器失败,请稍后再试!");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetWorkControl.unRegister(TAG, getActivity());
    }
}