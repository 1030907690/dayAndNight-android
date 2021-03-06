package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSONObject;

import org.dync.adapter.RecyclerHomeRecommendTvAdapter;
import org.dync.adapter.RecyclerLiveRecommendTvAdapter;
import org.dync.adapter.RecyclerVideoSourceDramaSeriesAdapter;
import org.dync.bean.Live;
import org.dync.bean.VersionUpdate;
import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.crash.MyCrashHandler;
import org.dync.dialog.UpdataDialog;
import org.dync.ijkplayerlib.widget.util.Settings;
import org.dync.queue.DelayOrderTask;
import org.dync.queue.DelayOrderWorker;
import org.dync.utils.BaseUtils;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/***
 * zhouzhongqing
 * 2019年10月21日19:13:15
 * tv版主界面
 * */
public class MainTvActivity extends AppCompatActivity {


    private EditText searchEdit;

    private ImageButton searchBtn;

    private ImageButton tvBtnMenu;

    private Activity content = this;

    private UpdataDialog updataDialog;

    private final Activity context = this;
    private final Map<String, List<Live>> liveGroupMap = new HashMap<>();

    /**
     * 首页推荐
     **/
    private RecyclerView recommendRecyclerView;

    /**
     * 首页推荐
     **/
    private RecyclerView recommendLiveRecyclerView;
    private RecyclerHomeRecommendTvAdapter recyclerHomeRecommendTvAdapter;
    private RecyclerLiveRecommendTvAdapter recyclerLiveRecommendTvAdapter;

    //上次按下返回键的系统时间
    private long lastBackTime = 0;
    //当前按下返回键的系统时间
    private long currentBackTime = 0;

    private TabLayout tabLayoutLiveData;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //捕获返回键按下的事件
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //获取当前系统时间的毫秒数
            currentBackTime = System.currentTimeMillis();
            //比较上次按下返回键和当前按下返回键的时间差，如果大于2秒，则提示再按一次退出
            if (currentBackTime - lastBackTime > 2 * 1000) {
                ToastUtil.showToast(this, "再按一次返回键退出");
                lastBackTime = currentBackTime;
            } else { //如果两次按下的时间差小于2秒，则退出程序
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCrashHandler.instance().init(getApplicationContext());
        setContentView(R.layout.tv_main_activity);

        initView();
        onListener();
        if (null == GlobalConfig.getInstance().getVersionUpdate()) {
            checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
        }


    }


    /**
     * 加载推荐内容
     **/
    private void loadingRecommend() {

        if (null != GlobalConfig.getInstance().getVersionUpdate()) {
            //加载推荐视频
            GlobalConfig.getInstance().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    List<VideoSearch> videoSearchList = GlobalConfig.getInstance().getDataSourceStrategy().homeRecommend();
                    if (null == videoSearchList) {
                        videoSearchList = new ArrayList<>();
                    }
                    Message msg = mainActivityHandle.obtainMessage();
                    msg.what = 1;
                    msg.obj = videoSearchList;
                    mainActivityHandle.sendMessage(msg);
                }
            });

            if (GlobalConfig.getInstance().getDelayOrderQueueManager().containsKeyTask(Constant.CACHE_LIVE + GlobalConfig.LIVE_URL)) {

                DelayOrderTask delayOrderTask = GlobalConfig.getInstance().getDelayOrderQueueManager().getTask(Constant.CACHE_LIVE + GlobalConfig.LIVE_URL);

                if (null != delayOrderTask) {
                    DelayOrderWorker delayOrderWorker = (DelayOrderWorker) delayOrderTask.getTask();
                    Message msg = mainActivityHandle.obtainMessage();
                    msg.what = 2;
                    msg.obj = (List<Live>) delayOrderWorker.getObj();
                    mainActivityHandle.sendMessage(msg);
                }
            } else {
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
                                ToastUtil.showToast(MainTvActivity.this, "获取直播列表失败!");
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                final String responseStr = response.body().string();

                                try {
                                    List<Live> liveList = JSONObject.parseArray(responseStr, Live.class);
                                    if (null == liveList) {
                                        liveList = new ArrayList<>();
                                    }

                                    // 加入缓存
                                    DelayOrderWorker delayOrderWorker = new DelayOrderWorker(Constant.CACHE_LIVE + GlobalConfig.LIVE_URL, liveList);
                                    GlobalConfig.getInstance().getDelayOrderQueueManager().put(delayOrderWorker);
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

            }

        } else {
            ToastUtil.showToast(content, "连接直播服务器失败,请稍后再试!");
        }

    }


    /**
     * @param initUrl 初始化配置地址
     **/
    public void checkVersionGet(String initUrl) {
        OkHttpClient client = new OkHttpClient();
        //构造Request对象
        //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
        Request request = new Request.Builder().get().url(initUrl).build();
        Call call = client.newCall(request);
        //异步调用并设置回调函数
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //e.printStackTrace();
                GlobalConfig.reCount++;
                if (GlobalConfig.reCount < GlobalConfig.getInstance().getRemoteServer().length) {
                    ToastUtil.showToast(context, "获取服务信息失败!正在重试第" + GlobalConfig.reCount + "次");
                    checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                } else {
                    ToastUtil.showToast(context, "获取服务信息失败!");
                }

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseStr = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // VersionUpdate versionUpdate = JSONObject.parseObject(responseStr, VersionUpdate.class);

                        try {
                            JSONObject.parseObject(responseStr, VersionUpdate.class);
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("json", responseStr);
                            msg.setData(data);
                            msg.what = 0;
                            mainActivityHandle.sendMessage(msg);
                        } catch (Exception e) {
                            Log.d("main exception", e.getMessage());
                            GlobalConfig.reCount++;
                            if (GlobalConfig.reCount <= GlobalConfig.getInstance().getRemoteServer().length) {
                                ToastUtil.showToast(context, "获取服务信息失败!正在重试第" + GlobalConfig.reCount + "次");
                                checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                            } else {
                                ToastUtil.showToast(context, "获取服务信息失败!");
                            }
                        }


                        // initTextView.setText("当前版本 :" + currentVersion);
                        //comparison(currentVersion, jsonObject.getString("downloadUrl"));
                    }
                });
            }
        });
    }


    /***
     * 去下载
     * */
    private void toDownLoad(String url) {
        /*Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);*/
        ToastUtil.showToast(content, "TV版请手动更新!");
    }


    private Handler mainActivityHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    VersionUpdate versionUpdate = JSONObject.parseObject(msg.getData().getString("json"), VersionUpdate.class);
                    GlobalConfig.getInstance().setVersionUpdate(versionUpdate);
                    comparison();

                    //设置数据源
                    int dataSourceOption = GlobalConfig.getInstance().getSharedPreferences().getInt(Constant.DATA_SOURCE_OPTION, 0);
                    GlobalConfig.getInstance().setOptionDataSourceStrategy(dataSourceOption);
                    /**
                     * 加载推荐内容
                     * **/
                    loadingRecommend();
                    break;
                case 1:
                    List<VideoSearch> videoSearchList = (List<VideoSearch>) msg.obj;

                    //纵向线性布局
                    GridLayoutManager layoutManagerInfo = new GridLayoutManager(content, 4);
                    recommendRecyclerView.setLayoutManager(layoutManagerInfo);
                    recyclerHomeRecommendTvAdapter = new RecyclerHomeRecommendTvAdapter(content, videoSearchList);
                    recommendRecyclerView.setAdapter(recyclerHomeRecommendTvAdapter);


                    recyclerHomeRecommendTvAdapter.setOnItemClickListener(new RecyclerHomeRecommendTvAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (view instanceof ImageButton) {
                                ImageButton videoItemBtn = (ImageButton) view;
                                //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                                Intent intent = new Intent(context, VideoDetailTvActivity.class);
                                intent.putExtra("url", videoItemBtn.getTag().toString());
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    });

                    break;

                case 2:
                    List<Live> liveList = (List<Live>) msg.obj;
                    String firstGroup = initLiveGroup(liveList);
                    if (null != liveList && liveList.size() > 0 && null != firstGroup) {
                        createLiveView(liveGroupMap.get(firstGroup));
                    }
                    initTabLiveData(liveList);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 初始化直播分类数据
     **/
    private void initTabLiveData(List<Live> liveList) {
        tabLayoutLiveData.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (Map.Entry<String, List<Live>> entry : liveGroupMap.entrySet()) {
            tabLayoutLiveData.addTab(tabLayoutLiveData.newTab().setText(entry.getKey()).setTag(entry.getKey()));
        }
        tabLayoutLiveData.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String group = (String) tab.getTag();
                createLiveView(liveGroupMap.get(group));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    /***
     * 初始化直播分组
     * @return String 返回第一个分组
     * */
    private String initLiveGroup(List<Live> liveList) {

        for (Live live : liveList) {
            if (liveGroupMap.containsKey(live.getGroup())) {
                List<Live> tempLiveList = liveGroupMap.get(live.getGroup());
                tempLiveList.add(live);
            } else {
                liveGroupMap.put(live.getGroup(), new ArrayList<Live>() {{
                    add(live);
                }});
            }
        }
        String firstGroup = null;
        if (null != liveGroupMap && liveGroupMap.size() > 0) {
            firstGroup = liveGroupMap.keySet().iterator().next();
        }
        return firstGroup;
    }

    private void createLiveView(List<Live> localLiveList) {
        //纵向线性布局
        GridLayoutManager layoutManagerInfoLive = new GridLayoutManager(content, 4);
        recommendLiveRecyclerView.setLayoutManager(layoutManagerInfoLive);
        recyclerLiveRecommendTvAdapter = new RecyclerLiveRecommendTvAdapter(content, localLiveList);
        recommendLiveRecyclerView.setAdapter(recyclerLiveRecommendTvAdapter);


        recyclerLiveRecommendTvAdapter.setOnItemClickListener(new RecyclerLiveRecommendTvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (view instanceof ImageButton) {
                    ImageButton videoItemBtn = (ImageButton) view;
                    //ToastUtil.showToast(VideoActivity.this, videoItemBtn.getText() +videoItemBtn.getTag().toString());
                    VideoTvActivity.intentTo(content, videoItemBtn.getTag().toString(), "测试", "", localLiveList.get(position).getName());
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    /***
     * 对比版本
     * */
    private void comparison() {
        String currentVersion = GlobalConfig.getInstance().getVersionUpdate().getCurrentVersion();
        String url = GlobalConfig.getInstance().getVersionUpdate().getDownloadUrl();
        //String version = sharedPreferences.getString("version", "1.0.0");
        String version = BaseUtils.getAppVersionName(context);
        if (0 != version.compareTo(currentVersion)) {
            ToastUtil.showToast(context, "需要更新");
            updataDialog.show();

            updataDialog.setOnCenterItemClickListener(new UpdataDialog.OnCenterItemClickListener() {
                @Override
                public void OnCenterItemClick(UpdataDialog dialog, View view) {
                    switch (view.getId()) {
                        case R.id.dialog_sure:
                            /**调用系统自带的浏览器去下载最新apk*/
                            toDownLoad(url);
                            break;
                    }
                    updataDialog.dismiss();
                }
            });

        }
    }

    private void initView() {
        //初始化弹窗 布局 点击事件的id
        updataDialog = new UpdataDialog(this, R.layout.dialog_version_update,
                new int[]{R.id.dialog_sure});
        searchEdit = findViewById(R.id.tv_search_editText);
        searchBtn = findViewById(R.id.tv_btn_search);

        //设置默认的播放器
        final Settings settings = new Settings(context);
        settings.setPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);

        recommendRecyclerView = findViewById(R.id.home_lately_recommend_view);
        recommendLiveRecyclerView = findViewById(R.id.home_live_recommend_view);

        tabLayoutLiveData = findViewById(R.id.tab_layout_live_data);

        tvBtnMenu = findViewById(R.id.tv_btn_menu);
    }


    private void onListener() {
        //searchEdit.setText("http://pili-live-hdl.qhmywl.com/dsdtv/4677cf6625ce01b236bbb58f99094d51.flv");
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editable = searchEdit.getText();
                if (null != editable && null != editable.toString() && !"".equals(editable.toString())) {
                    if (editable.toString().startsWith("http://") || editable.toString().startsWith("https://") || editable.toString().endsWith(".m3u8") || editable.toString().startsWith("rmpt://") || editable.toString().endsWith(".fly")) {
                        //ToastUtil.showToast(content, "协议暂不支持!");
                        VideoTvActivity.intentTo(content, editable.toString(), "测试", "", "");
                    } else {
                        Intent intent = new Intent(context, VideoSearchListTvActivity.class);
                        intent.putExtra("key", editable.toString());
                        startActivity(intent);
                    }
                } else {
                    ToastUtil.showToast(content, "请输入关键字");
                }
            }
        });

        searchBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        //选中动画
                        ViewCompat.animate(v).scaleX(1.17f).scaleY(1.17f).translationZ(1).start();
                    } else {
                        ViewCompat.animate(v).scaleX(1.17f).scaleY(1.17f).start();
                        ViewGroup parent = (ViewGroup) v.getParent();
                        parent.requestLayout();
                        parent.invalidate();
                    }
                } else {
                    ViewCompat.animate(v).scaleX(1f).scaleY(1f).start();
                }
            }
        });

        tvBtnMenu.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        //选中动画
                        ViewCompat.animate(v).scaleX(1.17f).scaleY(1.17f).translationZ(1).start();
                    } else {
                        ViewCompat.animate(v).scaleX(1.17f).scaleY(1.17f).start();
                        ViewGroup parent = (ViewGroup) v.getParent();
                        parent.requestLayout();
                        parent.invalidate();
                    }
                } else {
                    ViewCompat.animate(v).scaleX(1f).scaleY(1f).start();
                }
            }
        });

        tvBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,MenuTvActivity.class);
                startActivity(intent);
            }
        });

    }


}
