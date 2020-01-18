package org.dync.ijkplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.exoplayer2.C;

import org.dync.adapter.RecyclerVideoSourceDramaSeriesAdapter;
import org.dync.bean.Video;
import org.dync.bean.VideoGroup;
import org.dync.crash.MyCrashHandler;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.ijkplayer.utils.GlideUtil;
import org.dync.ijkplayer.utils.NetworkUtils;
import org.dync.ijkplayer.utils.StatusBarUtil;
import org.dync.ijkplayer.utils.ThreadUtil;
import org.dync.ijkplayerlib.widget.media.AndroidMediaController;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.util.IjkWindowVideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;
import org.dync.ijkplayerlib.widget.util.Settings;
import org.dync.ijkplayerlib.widget.util.WindowManagerUtil;
import org.dync.subtitleconverter.SubtitleView;
import org.dync.subtitleconverter.subtitleFile.FatalParsingException;
import org.dync.subtitleconverter.subtitleFile.FormatASS;
import org.dync.subtitleconverter.subtitleFile.FormatSRT;
import org.dync.subtitleconverter.subtitleFile.FormatSTL;
import org.dync.subtitleconverter.subtitleFile.FormatTTML;
import org.dync.subtitleconverter.subtitleFile.TimedTextFileFormat;
import org.dync.subtitleconverter.subtitleFile.TimedTextObject;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.dync.utils.VideoType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import jaygoo.library.m3u8downloader.server.EncryptM3U8Server;
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedDurationMilli;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSize;
import static org.dync.ijkplayerlib.widget.util.PlayerController.formatedSpeed;

public class VideoTvActivity extends BaseActivity {

    //public static String videoUrl = "http://www.kukuzy.com/index.php/vod/detail/id/364.html";
    private String videoUrl = "http://www.kukuzy.com/index.php/vod/detail/id/430.html";
    private static final String TAG = "VideoTvActivity";
    private String mVideoPath;
    private Uri mVideoUri;

    private AndroidMediaController mMediaController;
    private PlayerController mPlayerController;

    private boolean mBackPressed;
    private String mVideoCoverUrl;


    @BindView(R.id.video_view)
    IjkVideoView videoView;
    @BindView(R.id.subtitleView)
    SubtitleView subtitleView;
    @BindView(R.id.video_cover)
    ImageView videoCover;
    @BindView(R.id.app_video_status_text)
    TextView appVideoStatusText;
    @BindView(R.id.app_video_replay_icon)
    ImageView appVideoReplayIcon;
    @BindView(R.id.app_video_replay)
    LinearLayout appVideoReplay;
    @BindView(R.id.app_video_netTie_icon)
    TextView appVideoNetTieIcon;
    @BindView(R.id.app_video_netTie)
    LinearLayout appVideoNetTie;
    @BindView(R.id.app_video_freeTie_icon)
    TextView appVideoFreeTieIcon;
    @BindView(R.id.app_video_freeTie)
    LinearLayout appVideoFreeTie;
    @BindView(R.id.app_video_speed)
    TextView appVideoSpeed;
    @BindView(R.id.app_video_loading)
    LinearLayout appVideoLoading;
    @BindView(R.id.app_video_volume_icon)
    ImageView appVideoVolumeIcon;
    @BindView(R.id.app_video_volume)
    TextView appVideoVolume;
    @BindView(R.id.app_video_volume_box)
    LinearLayout appVideoVolumeBox;
    @BindView(R.id.app_video_brightness_icon)
    ImageView appVideoBrightnessIcon;
    @BindView(R.id.app_video_brightness)
    TextView appVideoBrightness;
    @BindView(R.id.app_video_brightness_box)
    LinearLayout appVideoBrightnessBox;
    @BindView(R.id.app_video_fastForward)
    TextView appVideoFastForward;
    @BindView(R.id.app_video_fastForward_target)
    TextView appVideoFastForwardTarget;
    @BindView(R.id.app_video_fastForward_all)
    TextView appVideoFastForwardAll;
    @BindView(R.id.app_video_fastForward_box)
    LinearLayout appVideoFastForwardBox;
    @BindView(R.id.app_video_center_box)
    FrameLayout appVideoCenterBox;
    @BindView(R.id.play_icon)
    ImageView playIcon;
    @BindView(R.id.tv_current_time)
    TextView tvCurrentTime;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;
    @BindView(R.id.img_change_screen)
    ImageView imgChangeScreen;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.rl_video_view_layout)
    RelativeLayout rlVideoViewLayout;
    @BindView(R.id.btn_ratio)
    Button btnRatio;
    @BindView(R.id.btn_rotation)
    Button btnRotation;
    @BindView(R.id.btn_ijk_player)
    Button btnIjkPlayer;
    @BindView(R.id.btn_exo_player)
    Button btnExoPlayer;
    @BindView(R.id.sp_speed)
    Spinner spSpeed;
    @BindView(R.id.btn_window_player)
    Button btnWindowPlayer;
    @BindView(R.id.btn_app_player)
    Button btnAppPlayer;
    @BindView(R.id.horizontalScrollView)
    HorizontalScrollView horizontalScrollView;
    @BindView(R.id.fps)
    TextView fps;
    @BindView(R.id.v_cache)
    TextView vCache;
    @BindView(R.id.a_cache)
    TextView aCache;
    @BindView(R.id.seek_load_cost)
    TextView seekLoadCost;
    @BindView(R.id.tcp_speed)
    TextView tcpSpeed;
    @BindView(R.id.bit_rate)
    TextView bitRate;
    @BindView(R.id.iv_preview)
    ImageView ivPreview;
    @BindView(R.id.ll_video_info)
    LinearLayout llVideoInfo;
    @BindView(R.id.fl_video_url)
    FrameLayout flVideoUrl;
    @BindView(R.id.fl_app_window)
    FrameLayout flAppWindow;
    @BindView(R.id.app_video_box)
    RelativeLayout appVideoBox;

    @BindView(R.id.video_title_name_tip_tv)
    RelativeLayout titleNameTip;

    @BindView(R.id.video_name_tip_tv)
    TextView videoNameTipTv;

    private RecyclerView ijkplayerVideoNavigationInfoRecyclerView;

    private TabLayout tabLayoutTitle;

    private RecyclerVideoSourceDramaSeriesAdapter recyclerVideoSourceDramaSeriesAdapter;

    private Map<String, List<Video>> videoGroupMap = new HashMap<String, List<Video>>();

    //上次按下返回键的系统时间
    private long lastBackTime = 0;
    //当前按下返回键的系统时间
    private long currentBackTime = 0;

    private AtomicInteger hideTipCount = new AtomicInteger(0);
    private VideoType videoType;
    private EncryptM3U8Server m3u8Server = new EncryptM3U8Server();


    /**
     * 快进快退秒数 大概是30秒
     **/
    private int income = 34748;

    public static Intent newIntent(Context context, String videoPath, String videoTitle, String videoUrl, String name, int videoTypeCode) {
        Intent intent = new Intent(context, VideoTvActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("name", name);
        intent.putExtra("videoTypeCode", videoTypeCode);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle, String videoUrl, String name) {
        context.startActivity(newIntent(context, videoPath, videoTitle, videoUrl, name, VideoType.SEARCH.getCode()));
    }

    public static void intentTo(Context context, String videoPath, String videoTitle, String videoUrl, String videoFullName, int videoTypeCode) {
        context.startActivity(newIntent(context, videoPath, videoTitle, videoUrl, videoFullName, videoTypeCode));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCrashHandler.instance().init(getApplicationContext());
        setContentView(R.layout.activity_video_tv);
        ButterKnife.bind(this);

        mContext = this;

        //隐藏 2019年8月31日16:45:43
        btnIjkPlayer.setVisibility(View.GONE);
        btnExoPlayer.setVisibility(View.GONE);

        videoUrl = getIntent().getStringExtra("videoUrl");

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");
        Intent tempIntent = getIntent();
        this.videoType = VideoType.get(tempIntent.getIntExtra("videoTypeCode", 0));


        if (videoType == VideoType.DOWNLOAD) {
            m3u8Server.execute();
            //转换本地url为网络地址url
            mVideoPath = m3u8Server.createLocalHttpUrl(mVideoPath);
            new Thread(){

                @Override
                public void run() {
                    try {
                        BufferedReader tempReader = new BufferedReader(new InputStreamReader(new URL(mVideoPath).openStream()));
                        Log.d("MUtils", mVideoPath+ "可以访问");
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        Log.d("MUtils", mVideoPath + "不能处理的url");
                    }
                }
            }.start();

            Message msg = videoHandle.obtainMessage();
            msg.what = 2;
            videoHandle.sendMessage(msg);
        } else if (videoType == VideoType.HOME) {
            Message msg = videoHandle.obtainMessage();
            msg.what = 2;
            videoHandle.sendMessage(msg);
        }

        mVideoCoverUrl = "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3120404212,3339906847&fm=27&gp=0.jpg";
        mVideoCoverUrl = "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2973320425,1464020144&fm=27&gp=0.jpg";

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // String implName = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(0).getKey() + "DataSourceHandle";
                  /*  IDataSourceStrategy dataSourceStrategy = GlobalConfig.getInstance().getDataSourceStrategy();
                    List<VideoGroup> videoList = new ArrayList<>();
                    if (null != videoUrl && !"".equals(videoUrl.trim())) {
                        videoList = dataSourceStrategy.playList(videoUrl, 1);
                    }*/
                    Message msg = videoHandle.obtainMessage();
                    msg.what = 0;
                  /*  Bundle data = new Bundle();
                    data.putString("json", JSONObject.toJSONString(videoList));
                    msg.setData(data);*/
                    videoHandle.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public static void main(String[] args) throws Exception {

        List<Video> videoList = new ArrayList<>();
        String $str = "$";
        Connection connect = Jsoup.connect("http://www.kukuzy.com/index.php/vod/detail/id/430.html");//获取连接对象
        Document document = connect.get();//获取url页面的内容并解析成document对象
        Elements classElements = document.body().select("div[id=\"playlist\"]");
        for (Element classElement : classElements) {
            Elements titleElements = classElement.select("h3[class=\"title\"]");
            if (titleElements.html().contains("rem3u8")) {
                Elements videoElements = classElement.getElementsByTag("li");
                for (Element videoElement : videoElements) {
                    Elements videoInputElements = videoElement.select("input[type=\"checkbox\"]");
                    for (Element videoInputElement : videoInputElements) {
                        //System.out.println(" videoElement  " + videoInputElement.attr("value"));
                        String[] videoArray = videoInputElement.attr("value").split($str);
                        Video video = new Video();
                        video.setName(videoArray[0].replace($str, ""));
                        video.setUrl(videoArray[1]);
                        videoList.add(video);
                    }

                }

            }

        }

        //System.out.printf(" 打印 " + classElements.html());

    }


    private Handler videoHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initVideoControl();
                    initPlayer();
                    //initFragment(JSONArray.parseArray(msg.getData().getString("json"), VideoGroup.class));
                    initListener();
                    initVideoListener();
//
                    StatusBarUtil.setColor(VideoTvActivity.this, getResources().getColor(R.color.colorPrimary));

                    //2019年10月22日19:09:56 tv版 默认设置为横屏
                    updateFullScreenBg(true);
                    if (mPlayerController.isPortrait()) {
                        //转换屏幕方向
                        mPlayerController.toggleScreenOrientation();
                    }

                    videoNameTipTv.setText(getIntent().getStringExtra("name"));
                    break;
                case 1:
                    if (hideTipCount.getAndDecrement() < 2) {
                        llBottom.setVisibility(View.GONE);
                        titleNameTip.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    String videoFullName = getIntent().getStringExtra("videoFullName");
                    if (null != videoFullName && !"".equals(videoFullName)) {
                        videoNameTipTv.setText(videoFullName);
                    }
                    break;
            }
        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ijk_player:
                mPlayerController.switchPlayer(Settings.PV_PLAYER__IjkMediaPlayer);
                break;
            case R.id.btn_exo_player:
                mPlayerController.switchPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);
                break;
            case R.id.btn_rotation:
                mPlayerController.toogleVideoRotation();
//                mPlayerController.setPlayerRotation(90);
                break;
            case R.id.btn_ratio:
                mPlayerController.toggleAspectRatio();
                break;
            case R.id.btn_window_player:
                WindowManagerUtil.createSmallWindow(mContext, videoView.getMediaPlayer());
                videoView.setRenderView(null);
                WindowManagerUtil.setWindowCallBack(new IjkWindowVideoView.CallBack() {
                    @Override
                    public void removeSmallWindow(IMediaPlayer mediaPlayer) {
                        WindowManagerUtil.removeSmallWindow(mContext);
                        videoView.setMediaPlayer(mediaPlayer);
                        videoView.resetRenders();
                    }
                });
//                AndPermission.with(mContext)
//                        .requestCode(100)
//                        .permission(Manifest.permission.SYSTEM_ALERT_WINDOW)
//                        .rationale(new RationaleListener() {
//                            @Override
//                            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
//                                AndPermission.rationaleDialog(mContext, rationale).show();
//                            }
//                        })
//                        .callback(new PermissionListener() {
//                            @Override
//                            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
//                                WindowManagerUtil.createSmallApp(mContext, videoView.getMediaPlayer());
//                                videoView.setRenderView(null);
//                                WindowManagerUtil.setWindowCallBack(new IjkWindowVideoView.CallBack() {
//                                    @Override
//                                    public void removeSmallWindow(IMediaPlayer mediaPlayer) {
//                                        videoView.setMediaPlayer(mediaPlayer);
//                                        videoView.resetRenders();
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
//                                Toast.makeText(mContext,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .start();
                break;
            case R.id.btn_app_player:
                WindowManagerUtil.createSmallApp(flAppWindow, videoView.getMediaPlayer());
                videoView.setRenderView(null);
                WindowManagerUtil.setAppCallBack(new WindowManagerUtil.AppCallBack() {
                    @Override
                    public void removeSmallApp(IMediaPlayer mediaPlayer) {
                        WindowManagerUtil.removeSmallApp(flAppWindow);
                        videoView.setMediaPlayer(mediaPlayer);
                        videoView.resetRenders();
                    }
                });
                break;
        }
    }

    private void initPlayer() {
        //        ActionBar actionBar = getSupportActionBar();
//        mMediaController = new AndroidMediaController(this, false);
//        mMediaController.setSupportActionBar(actionBar);
//        mVideoView.setMediaController(mMediaController);

        showVideoLoading();
        mPlayerController = null;

        mPlayerController = new PlayerController(this, videoView)
                .setVideoParentLayout(findViewById(R.id.rl_video_view_layout))//建议第一个调用
                .setVideoController((SeekBar) findViewById(R.id.seekbar))
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setPortrait(true)
                .setKeepScreenOn(true)
                .setAutoControlListener(llBottom)//触摸以下控件可以取消自动隐藏布局的线程
                .setPanelControl(new PlayerController.PanelControlListener() {
                    @Override
                    public void operatorPanel(boolean isShowControlPanel) {
                        if (isShowControlPanel) {
                            llBottom.setVisibility(View.VISIBLE);
                            // 新增  2019年10月23日09:16:13
                            titleNameTip.setVisibility(View.VISIBLE);
                        } else {
                            llBottom.setVisibility(View.GONE);
                            // 新增  2019年10月23日09:16:13
                            titleNameTip.setVisibility(View.GONE);
                        }
                    }
                })
                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
                    @Override
                    public void syncTime(long position, long duration) {
                        tvCurrentTime.setText(mPlayerController.generateTime(position));
                        tvTotalTime.setText(mPlayerController.generateTime(duration));
                        if (subtitleView != null) {
                            subtitleView.seekTo(position);
                        }
                    }
                })
                .setGestureListener(new PlayerController.GestureListener() {
                    @Override
                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
                        if (showDelta != 0) {
                            appVideoFastForwardBox.setVisibility(View.VISIBLE);
                            appVideoBrightnessBox.setVisibility(View.GONE);
                            appVideoVolumeBox.setVisibility(View.GONE);
                            appVideoFastForwardTarget.setVisibility(View.VISIBLE);
                            appVideoFastForwardAll.setVisibility(View.VISIBLE);
                            appVideoFastForwardTarget.setText(mPlayerController.generateTime(newPosition) + "/");
                            appVideoFastForwardAll.setText(mPlayerController.generateTime(duration));

                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                            appVideoFastForward.setVisibility(View.VISIBLE);
                            appVideoFastForward.setText(String.format("%ss", text));
                        }
                    }

                    @Override
                    public void onVolumeSlide(int volume) {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.GONE);
                        appVideoVolumeBox.setVisibility(View.VISIBLE);
                        appVideoVolume.setVisibility(View.VISIBLE);
                        appVideoVolume.setText(volume + "%");
                    }

                    @Override
                    public void onBrightnessSlide(float brightness) {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.VISIBLE);
                        appVideoVolumeBox.setVisibility(View.GONE);
                        appVideoBrightness.setVisibility(View.VISIBLE);
                        appVideoBrightness.setText((int) (brightness * 100) + "%");
                    }

                    @Override
                    public void endGesture() {
                        appVideoFastForwardBox.setVisibility(View.GONE);
                        appVideoBrightnessBox.setVisibility(View.GONE);
                        appVideoVolumeBox.setVisibility(View.GONE);
                    }
                });

        // prefer mVideoPath
//        Settings settings = new Settings(this);
//        settings.setPlayer(Settings.PV_PLAYER__IjkMediaPlayer);
//        if (mVideoPath != null)
//            videoView.setVideoPath(mVideoPath);
//        else if (mVideoUri != null)
//            videoView.setVideoURI(mVideoUri);
//        else {
//            Log.e(TAG, "Null Data Source\n");
//            finish();
//            return;
//        }
//        videoView.start();
        onDestroyVideo();
        if (mVideoPath != null) {
            showVideoLoading();
            videoView.setVideoPath(mVideoPath);
            videoView.start();
        }
    }

    private void initFragment(List<VideoGroup> videoGroupList) {
       /*
       2019年9月23日16:34:17
       SampleMediaListFragment videoUrlFragment = new SampleMediaListFragment(videoList);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_video_url, videoUrlFragment);
        fragmentTransaction.commit();

        videoUrlFragment.setOnItemClickListener(new SampleMediaListFragment.OnItemClickListener() {
            @Override
            public void OnItemClick(Context context, String videoPath, String videoTitle) {
                onDestroyVideo();
                mVideoPath = videoPath;
                Log.d(TAG, "OnItemClick: mVideoPath: " + mVideoPath);
                if (mVideoPath != null) {
                    showVideoLoading();
                    videoView.setVideoPath(mVideoPath);
                    videoView.start();
                }
            }
        });*/


        // 初始化可播放的数据源
    /*    ijkplayerVideoNavigation = findViewById(R.id.ijkplayer_video_navigation);
        //ijkplayerVideoNavigation.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManagerNavigation = new LinearLayoutManager(this);
        linearLayoutManagerNavigation.setOrientation(LinearLayoutManager.HORIZONTAL);
        ijkplayerVideoNavigation.setLayoutManager(linearLayoutManagerNavigation);
        //ijkplayerVideoNavigation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        ijkplayerVideoNavigation.setAdapter(recyclerVideoSourceAdapter = new RecyclerVideoSourceAdapter(VideoActivity.this, videoGroupList));
        ijkplayerVideoNavigation.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
*/


        // 初始化剧集
        ijkplayerVideoNavigationInfoRecyclerView = findViewById(R.id.ijkplayer_video_navigation_info);
        //纵向线性布局
        GridLayoutManager layoutManagerInfo = new GridLayoutManager(this, 4);
        ijkplayerVideoNavigationInfoRecyclerView.setLayoutManager(layoutManagerInfo);
        if (null == videoGroupList || videoGroupList.size() <= 0) {
            videoGroupList = new ArrayList<VideoGroup>();
            VideoGroup tempVideoGroup = new VideoGroup();
            tempVideoGroup.setGroup("无");
            tempVideoGroup.setVideoList(new ArrayList<>());
            videoGroupList.add(tempVideoGroup);
        }
        recyclerVideoSourceDramaSeriesAdapter = new RecyclerVideoSourceDramaSeriesAdapter(VideoTvActivity.this, videoGroupList.get(0).getVideoList());
        ijkplayerVideoNavigationInfoRecyclerView.setAdapter(recyclerVideoSourceDramaSeriesAdapter);
        //ijkplayerVideoNavigationInfoRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));

        recyclerVideoSourceDramaSeriesAdapter.setOnItemClickListener(new RecyclerVideoSourceDramaSeriesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (view instanceof Button) {
                    Button videoItemBtn = (Button) view;
                    //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                    videoItemBtn.setTextColor(0xFFFFFFFF);

                    onDestroyVideo();
                    mVideoPath = videoItemBtn.getTag().toString();
                    Log.d(TAG, "OnItemClick: mVideoPath: " + mVideoPath);
                    if (mVideoPath != null) {
                        showVideoLoading();
                        videoView.setVideoPath(mVideoPath);
                        videoView.start();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                ToastUtil.showToast(VideoTvActivity.this, "--");
            }
        });


        initTab(videoGroupList);
    }


    private void initTab(List<VideoGroup> videoGroups) {
        tabLayoutTitle = findViewById(R.id.tab_layout_data);
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
                GridLayoutManager layoutManagerInfo = new GridLayoutManager(VideoTvActivity.this, 4);
                ijkplayerVideoNavigationInfoRecyclerView.setLayoutManager(layoutManagerInfo);
                ijkplayerVideoNavigationInfoRecyclerView.setAdapter(recyclerVideoSourceDramaSeriesAdapter = new RecyclerVideoSourceDramaSeriesAdapter(VideoTvActivity.this, videoGroupMap.get(group)));
                recyclerVideoSourceDramaSeriesAdapter.notifyDataSetChanged();

                recyclerVideoSourceDramaSeriesAdapter.setOnItemClickListener(new RecyclerVideoSourceDramaSeriesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (view instanceof Button) {
                            Button videoItemBtn = (Button) view;
                            //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                            videoItemBtn.setTextColor(0xFFFFFFFF);

                            onDestroyVideo();
                            mVideoPath = videoItemBtn.getTag().toString();
                            Log.d(TAG, "OnItemClick: mVideoPath: " + mVideoPath);
                            if (mVideoPath != null) {
                                showVideoLoading();
                                videoView.setVideoPath(mVideoPath);
                                videoView.start();
                            }
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        ToastUtil.showToast(VideoTvActivity.this, "1");
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

    private void initListener() {
        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    updatePlayBtnBg(true);
                } else {
                    updatePlayBtnBg(false);
                }
            }
        });
        imgChangeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerController != null) {
                    if (mPlayerController.isPortrait()) {
                        updateFullScreenBg(true);
                    } else {
                        updateFullScreenBg(false);
                    }
                    mPlayerController.toggleScreenOrientation();
                }
            }
        });
        appVideoReplayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayer();
            }
        });
        Spinner sp_speed = (Spinner) findViewById(R.id.sp_speed);
        final String[] speeds = {"倍速播放", "0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_speed.setAdapter(adapter);
        sp_speed.setSelection(0, true);
        sp_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {
                    return;
                }
                try {
                    float parseFloat = Float.parseFloat(speeds[pos]);
                    videoView.setSpeed(parseFloat);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        Spinner sp_subtitle = (Spinner) findViewById(R.id.sp_subtitle);
        final String[] subtitles = {
                "字幕",
                "ass",
                "srt",
                "stl",
                "xml"
        };
        final String[] subtitleList = {
                "字幕",
                "standards/ASS/Oceans.Eight.2018.1080p.BluRay.x264-SPARKS.简体.ass",
                "standards/SRT/哆啦A梦：伴我同行.1080P.x264.Hi10P.flac.chs.srt",
                "standards/STL/Aquí no hay quien viva 1.STL",
                "standards/XML/Debate0_03-03-08.dfxp.xml"
        };
        ArrayAdapter<String> subtitleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subtitles);
        subtitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_subtitle.setAdapter(subtitleAdapter);
        sp_subtitle.setSelection(0, true);
        sp_subtitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int pos, long id) {
                ThreadUtil.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pos == 0) {
                            return;
                        }
                        String subtitle = subtitleList[pos];
                        final TimedTextObject tto;
                        TimedTextFileFormat ttff = null;
                        try {
                            InputStream is = getAssets().open(subtitle);
                            switch (subtitles[pos]) {
                                case "ass":
                                    ttff = new FormatASS();
                                    break;
                                case "srt":
                                    ttff = new FormatSRT();
                                    break;
                                case "stl":
                                    ttff = new FormatSTL();
                                    break;
                                case "xml":
                                    ttff = new FormatTTML();
                                    break;
                                default:

                                    break;
                            }

                            tto = ttff != null ? ttff.parseFile("test", is) : null;
//                        IOClass.writeFileTxt("test.srt", tto.toSRT());

                            ThreadUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "加载字幕成功", Toast.LENGTH_SHORT).show();
                                    if (subtitleView != null) {
                                        subtitleView.setData(tto);
                                        subtitleView.setLanguage(SubtitleView.LANGUAGE_TYPE_CHINA);
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (FatalParsingException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    private void initVideoListener() {
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                appVideoReplay.setVisibility(View.GONE);
                appVideoReplayIcon.setVisibility(View.GONE);
                videoCover.setImageDrawable(new ColorDrawable(0));
                videoView.startVideoInfo();
                if (videoView.getMediaPlayer() instanceof IjkExoMediaPlayer) {
                    ArrayList<Integer> trackGroup = ((IjkExoMediaPlayer) videoView.getMediaPlayer()).getTrackGroup();
                    if (!trackGroup.contains(C.TRACK_TYPE_VIDEO)) {
                        if (!TextUtils.isEmpty(mVideoCoverUrl)) {
                            GlideUtil.showImg(mContext, mVideoCoverUrl, videoCover);
                        }
                    }
                }

                mPlayerController
                        .setGestureEnabled(true)
                        .setAutoControlPanel(true);//视频加载后才自动隐藏操作面板
                mPlayerController.setSpeed(1.0f);
            }
        });
        videoView.setVideoInfoListener(new IjkVideoView.VideoInfoListener() {
            @Override
            public void updateVideoInfo(IMediaPlayer mMediaPlayer) {
                showVideoInfo(mMediaPlayer);
            }
        });
        final Settings mSettings = new Settings(mContext);
        final ArrayList<Integer> audios = new ArrayList<>();
        //音频软解成功通知
        audios.add(IMediaPlayer.MEDIA_INFO_OPEN_INPUT);
        audios.add(IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO);
        audios.add(IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN);
        audios.add(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START);
        final ArrayList<Integer> temp_audios = new ArrayList<>();
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                Log.d(TAG, "onInfo: what= " + what + ", extra= " + extra);
                if (what == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                    temp_audios.clear();
                    temp_audios.add(what);
                } else if (temp_audios.size() < 6) {
                    temp_audios.add(what);
                }
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT://播放下一条
                        Log.d(TAG, "MEDIA_INFO_STARTED_AS_NEXT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始整备中
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        videoCover.setImageDrawable(new ColorDrawable(0));
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始整备中
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        if (!temp_audios.contains(IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)) {
                            if (!TextUtils.isEmpty(mVideoCoverUrl)) {
                                GlideUtil.showImg(mContext, mVideoCoverUrl, videoCover);
                            }
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN:
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(false);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        if (!NetworkUtils.isNetworkConnected(mContext)) {
                            updatePlayBtnBg(true);
                        }
                        showVideoLoading();
                        if (mSettings.getPlayer() == Settings.PV_PLAYER__IjkMediaPlayer) {
                            ThreadUtil.runInThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (temp_audios.get(0) == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                                        for (int i = 0; i < temp_audios.size(); i++) {
                                            if (!audios.get(i).equals(temp_audios.get(i))) {
                                                onDestroyVideo();
                                                ThreadUtil.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (mVideoPath != null) {
                                                            videoView.setVideoPath(mVideoPath);
                                                            videoView.start();
                                                        }
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        hideVideoLoading();
                        seekbar.setEnabled(true);
                        playIcon.setEnabled(true);
                        updatePlayBtnBg(!videoView.isPlaying());
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://视频日志跟踪
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
//                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://网络带宽
//                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING://
//                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE://不可设置播放位置，直播方面
//                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE://视频数据更新
//                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE: " + extra);
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR://
//                        Log.d(TAG, "MEDIA_INFO_TIMED_TEXT_ERROR:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE://不支持字幕
//                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT://字幕超时
//                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://
//                        Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED:");
//                        break;
//                    case IMediaPlayer.MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE://
//                        Log.d(TAG, "MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE:");
//                        break;
////                    case IMediaPlayer.MEDIA_ERROR_UNKNOWN://
////                        Log.d(TAG, "MEDIA_ERROR_UNKNOWN:");
////                        break;
//                    case IMediaPlayer.MEDIA_INFO_UNKNOWN://未知信息
//                        Log.d(TAG, "MEDIA_INFO_UNKNOWN or MEDIA_ERROR_UNKNOWN:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED://服务挂掉
//                        Log.d(TAG, "MEDIA_ERROR_SERVER_DIED:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK://数据错误没有有效的回收
//                        Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_IO://IO 错误
//                        Log.d(TAG, "MEDIA_ERROR_IO :");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED://数据不支持
//                        Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED :");
//                        break;
//                    case IMediaPlayer.MEDIA_ERROR_TIMED_OUT://数据超时
//                        Log.d(TAG, "MEDIA_ERROR_TIMED_OUT :");
//                        break;
                }
                return true;
            }
        });
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                updatePlayBtnBg(true);
//                videoView.release(false);
                videoView.stopVideoInfo();
                initVideoControl();
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
                hideVideoLoading();
                Toast.makeText(mContext, "播放出错", Toast.LENGTH_SHORT).show();
                appVideoReplay.setVisibility(View.VISIBLE);
                appVideoReplayIcon.setVisibility(View.VISIBLE);

                if (mPlayerController != null) {
                    mPlayerController
                            .setGestureEnabled(false)
                            .setAutoControlPanel(false);
                }
                videoView.stopVideoInfo();
                return true;
            }
        });
//        videoView.setOnNativeInvokeListener(new IjkVideoView.OnNativeInvokeListener() {
//            @Override
//            public boolean onNativeInvoke(IMediaPlayer mediaPlayer, int what, Bundle bundle) {
//                Log.w(TAG, "onNativeInvoke: what= " + what + ", bundle= " + bundle);
//                int error, http_code;
//                switch (what) {
//                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_WILL_HTTP_OPEN:
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=0}]
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=0, http_code=0}]
//                        //what= 1, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=0, http_code=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.EVENT_DID_HTTP_OPEN:
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, error=0, http_code=200}]
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, error=-101, http_code=0}]
//                        //what= 2, bundle= Bundle[{offset=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, error=-5, http_code=0}]
//                        error = bundle.getInt("error");
//                        http_code = bundle.getInt("http_code");
//                        if (error == -101) {//断网了
//
//                        }
//                        if(http_code == 200) {
//                            hideVideoLoading();
//                        }
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_TCP_OPEN:
//                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
//                        //what= 131073, bundle= Bundle[{family=0, fd=0, ip=, port=0, error=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_DID_TCP_OPEN:
//                        //what= 131074, bundle= Bundle[{family=2, fd=64, ip=118.178.143.146, port=20480, error=0}]
//                        break;
//                    case IjkMediaPlayer.OnNativeInvokeListener.CTRL_WILL_HTTP_OPEN:
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000007.ts, retry_counter=0}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000012.ts, retry_counter=1}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=1}]
//                        //what= 131075, bundle= Bundle[{segment_index=0, url=http://f.rtmpc.cn/thatthatthat/mJGuqyHMpnVQNRoA/hls/000013.ts, retry_counter=0}]
//                        break;
//                }
//                return true;
//            }
//        });
    }

    private void showVideoLoading() {
        if (appVideoLoading != null) {
            appVideoLoading.setVisibility(View.VISIBLE);
            appVideoSpeed.setVisibility(View.VISIBLE);
            appVideoSpeed.setText("");
        }
    }

    private void hideVideoLoading() {
        if (appVideoLoading != null) {
            appVideoLoading.setVisibility(View.GONE);
            appVideoSpeed.setVisibility(View.GONE);
            appVideoSpeed.setText("");
        }
    }

    private void initVideoControl() {
//        playIcon.setEnabled(false);
        seekbar.setEnabled(false);
        seekbar.setProgress(0);
    }

    private void showVideoInfo(IMediaPlayer mMediaPlayer) {
//        LinearLayout ll_video_info = (LinearLayout) findViewById(R.id.ll_video_info);
//        if(mVideoView != null) {
//            ITrackInfo[] trackInfos = mVideoView.getTrackInfo();
//            for(ITrackInfo trackInfo: trackInfos) {
//                final CheckBox checkBox = new CheckBox(ll_video_info.getContext());
//
//                String infoInline = String.format(Locale.US, "%s", trackInfo.getInfoInline());
//                final int trackType = trackInfo.getTrackType()-1;//不知道为什么不跟ITrackInfo类中的参数一致，而是减1
//                checkBox.setText(infoInline);
//                checkBox.setChecked(true);
//                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                        if(mVideoView != null) {
//                            if(b) {
//                                mVideoView.selectTrack(trackType);
//                            }else {
//                                mVideoView.deselectTrack(trackType);
//                            }
//                        }
//                    }
//                });
//                ll_video_info.addView(checkBox);
//            }
//        }

        if (mMediaPlayer != null && mMediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer mp = (IjkMediaPlayer) mMediaPlayer;

            float fpsOutput = mp.getVideoOutputFramesPerSecond();
            float fpsDecode = mp.getVideoDecodeFramesPerSecond();
            long videoCachedDuration = mp.getVideoCachedDuration();
            long audioCachedDuration = mp.getAudioCachedDuration();
            long videoCachedBytes = mp.getVideoCachedBytes();
            long audioCachedBytes = mp.getAudioCachedBytes();
            long tcpSpeeds = mp.getTcpSpeed();
            long bitRates = mp.getBitRate();
            long seekLoadDuration = mp.getSeekLoadDuration();

            mPlayerController.setVideoInfo(fps, String.format(Locale.US, "%.2f / %.2f", fpsDecode, fpsOutput));
            mPlayerController.setVideoInfo(vCache, String.format(Locale.US, "%s, %s", formatedDurationMilli(videoCachedDuration), formatedSize(videoCachedBytes)));
            mPlayerController.setVideoInfo(aCache, String.format(Locale.US, "%s, %s", formatedDurationMilli(audioCachedDuration), formatedSize(audioCachedBytes)));
            mPlayerController.setVideoInfo(seekLoadCost, String.format(Locale.US, "%d ms", seekLoadDuration));
            mPlayerController.setVideoInfo(tcpSpeed, String.format(Locale.US, "%s", formatedSpeed(tcpSpeeds)));
            mPlayerController.setVideoInfo(bitRate, String.format(Locale.US, "%.2f kbs", bitRates / 1000f));

            if (tcpSpeeds == -1) {
                return;
            }
            if (appVideoSpeed != null) {
                String formatSize = formatedSpeed(tcpSpeeds);
                appVideoSpeed.setText(formatSize);
            }
//            if (videoCachedDuration == 0) {//没有缓存了，如果断网
//                if (NetworkUtils.isNetworkConnected(mContext)) {
//                    int currentPosition = videoView.getCurrentPosition();
//                    mPlayerController.seekTo(currentPosition);
//                    updatePlayBtnBg(false);
//                    playIcon.setEnabled(true);
//                } else {
//                    updatePlayBtnBg(true);
//                    playIcon.setEnabled(false);
//                }
//            }
        } else if (mMediaPlayer != null && mMediaPlayer instanceof IjkExoMediaPlayer) {
            IjkExoMediaPlayer mp = (IjkExoMediaPlayer) mMediaPlayer;

            long tcpSpeeds = mp.getTotalRxBytes(mActivity);
            if (appVideoSpeed != null) {
                String formatSize = formatedSpeed(tcpSpeeds);
                appVideoSpeed.setText(formatSize);
            }
        }
    }

    /**
     * 更新播放按钮的背景图片，正在播放
     */
    private void updatePlayBtnBg(boolean isPlay) {
        try {
            int resid;
            if (isPlay) {
                // 暂停
                resid = R.drawable.simple_player_center_play;
                videoView.pause();
            } else {
                // 播放
                resid = R.drawable.simple_player_center_pause;
                videoView.start();
            }
            playIcon.setImageResource(resid);
        } catch (Exception e) {

        }

    }

    /**
     * 更新全屏按钮的背景图片
     */
    private void updateFullScreenBg(boolean isFullSrceen) {
        try {
            int resid;
            if (isFullSrceen) {
                // 全屏
                resid = R.drawable.simple_player_icon_fullscreen_shrink;
            } else {
                // 非全屏
                resid = R.drawable.simple_player_icon_fullscreen_stretch;
            }
            imgChangeScreen.setBackgroundResource(resid);
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!videoView.isBackgroundPlayEnabled()) {
            updatePlayBtnBg(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !videoView.isBackgroundPlayEnabled()) {
//            mVideoView.stopPlayback();
//            mVideoView.release(true);
//            mVideoView.stopBackgroundPlay();
            updatePlayBtnBg(true);
        } else {
            videoView.enterBackground();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyVideo();
        WindowManagerUtil.removeSmallWindow(mContext);
        WindowManagerUtil.removeSmallApp(flAppWindow);
        if (videoType == VideoType.DOWNLOAD) {
            //关闭服务
            m3u8Server.finish();
        }
    }

    private void onDestroyVideo() {
        if (appVideoReplay != null) {
            appVideoReplay.setVisibility(View.GONE);
        }
        if (appVideoReplayIcon != null) {
            appVideoReplayIcon.setVisibility(View.GONE);
        }
        if (mPlayerController != null) {
            mPlayerController.onDestroy();
        }
        if (videoView != null) {
            videoView.stopPlayback();
            videoView.release(true);
            videoView.stopBackgroundPlay();
            videoView.stopVideoInfo();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPlayerController != null) {
            mPlayerController.onConfigurationChanged();
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (videoType == VideoType.DOWNLOAD) {
            m3u8Server.decrypt();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoType == VideoType.DOWNLOAD) {
            m3u8Server.encrypt();
        }
    }



    private void showBottom() {
        llBottom.setVisibility(View.VISIBLE);
        titleNameTip.setVisibility(View.VISIBLE);
        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    hideTipCount.getAndIncrement();
                    Thread.sleep(3000);
                    Message msg = videoHandle.obtainMessage();
                    msg.what = 1;
                    videoHandle.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        showBottom();
        //捕获返回键按下的事件
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, String.valueOf(llBottom.getVisibility()));

            //获取当前系统时间的毫秒数
            currentBackTime = System.currentTimeMillis();
            //比较上次按下返回键和当前按下返回键的时间差，如果大于2秒，则提示再按一次退出
            if (currentBackTime - lastBackTime > 2 * 1000) {
                ToastUtil.showToast(this, "再按一次返回键退出");
                lastBackTime = currentBackTime;
            } else { //如果两次按下的时间差小于2秒，则退出程序
                finish();
                //System.exit(0);
            }
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:     //确定键enter
                Log.d(TAG, "enter1--->");

                if (videoView.isPlaying()) {
                    updatePlayBtnBg(true);
                    ToastUtil.showToast(this, "暂停");
                } else {
                    updatePlayBtnBg(false);
                    ToastUtil.showToast(this, "播放");
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "enter2--->");
                if (videoView.isPlaying()) {
                    updatePlayBtnBg(true);
                    ToastUtil.showToast(this, "暂停");
                } else {
                    updatePlayBtnBg(false);
                    ToastUtil.showToast(this, "播放");
                }
                //ToastUtil.showToast(this,"enter2");
                break;

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG, "back--->");
                return true;   //这里由于break会退出，所以我们自己要处理掉 不返回上一层
            case KeyEvent.KEYCODE_SETTINGS: //设置键
                Log.d(TAG, "setting--->");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:   //向下键

                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    Log.d(TAG, "down--->");
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                Log.d(TAG, "up--->");
                break;
            case KeyEvent.KEYCODE_0:   //数字键0
                Log.d(TAG, "0--->");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                Log.d(TAG, "left--->");
                fastRetreat();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                Log.d(TAG, "right--->");
                fastForward();
                break;
            case KeyEvent.KEYCODE_INFO:    //info键
                Log.d(TAG, "info--->");
                break;

            case KeyEvent.KEYCODE_PAGE_DOWN:     //向上翻页键
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                Log.d(TAG, "page down--->");

                break;


            case KeyEvent.KEYCODE_PAGE_UP:     //向下翻页键
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                Log.d(TAG, "page up--->");

                break;

            case KeyEvent.KEYCODE_VOLUME_UP:   //调大声音键
                Log.d(TAG, "voice up--->");

                break;

            case KeyEvent.KEYCODE_VOLUME_DOWN: //降低声音键
                Log.d(TAG, "voice down--->");

                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE: //禁用声音
                Log.d(TAG, "voice mute--->");
                break;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void fastRetreat() {
        mPlayerController.setDragging(true);
        mPlayerController.getmHandler().removeMessages(PlayerController.MESSAGE_SHOW_PROGRESS);
        if (mPlayerController.getmAutoControlPanelRunnable() != null) {
            mPlayerController.getmAutoControlPanelRunnable().stop();
        }


        long duration = videoView.getDuration();
        long newPosition = (long) ((duration * mPlayerController.getSeekBar().getProgress() * 1.0) / mPlayerController.getSeekBarMaxProgress());
        //快进大概10秒
        newPosition = (newPosition + (-income));
        Log.d(TAG, "newPosition " + newPosition);
        mPlayerController.setNewPosition(newPosition);
        mPlayerController.setDragging(false);
        // if (!isMaxTime && newPosition >= 0) {
        mPlayerController.getmHandler().removeMessages(PlayerController.MESSAGE_SEEK_NEW_POSITION);
        mPlayerController.getmHandler().sendEmptyMessage(PlayerController.MESSAGE_SEEK_NEW_POSITION);
        // }
        mPlayerController.getmHandler().sendEmptyMessageDelayed(PlayerController.MESSAGE_SHOW_PROGRESS, 1000);
        if (mPlayerController.getmAutoControlPanelRunnable() != null) {
            mPlayerController.getmAutoControlPanelRunnable().start(5000);
        }


    }


    private void fastForward() {


        mPlayerController.setDragging(true);
        mPlayerController.getmHandler().removeMessages(PlayerController.MESSAGE_SHOW_PROGRESS);
        if (mPlayerController.getmAutoControlPanelRunnable() != null) {
            mPlayerController.getmAutoControlPanelRunnable().stop();
        }


        long duration = videoView.getDuration();
        long newPosition = (long) ((duration * mPlayerController.getSeekBar().getProgress() * 1.0) / mPlayerController.getSeekBarMaxProgress());
        //快进大概10秒
        newPosition = (newPosition + income);
        Log.d(TAG, "newPosition " + newPosition);
        mPlayerController.setNewPosition(newPosition);
        mPlayerController.setDragging(false);
        // if (!isMaxTime && newPosition >= 0) {
        mPlayerController.getmHandler().removeMessages(PlayerController.MESSAGE_SEEK_NEW_POSITION);
        mPlayerController.getmHandler().sendEmptyMessage(PlayerController.MESSAGE_SEEK_NEW_POSITION);
        // }
        mPlayerController.getmHandler().sendEmptyMessageDelayed(PlayerController.MESSAGE_SHOW_PROGRESS, 1000);
        if (mPlayerController.getmAutoControlPanelRunnable() != null) {
            mPlayerController.getmAutoControlPanelRunnable().start(5000);
        }

    }
}
