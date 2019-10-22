package org.dync.ijkplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.dync.adapter.DividerItemDecoration;
import org.dync.adapter.RecyclerSearchAdapter;
import org.dync.adapter.RecyclerSearchTvAdapter;
import org.dync.bean.Video;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


/***
 * zhouzhongqing
 * 2019年10月22日10:42:18
 * 视频搜索结果页面
 * */
public class VideoSearchListTvActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<VideoSearch> mDatas;
    private RecyclerSearchTvAdapter mAdapter;
    private Context context = this;

    private TextView searchNoDataTips;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_search_list_tv_activity);

        initData();

    }


    private void initView() {
        if (null == mDatas) {
            mDatas = new ArrayList<>();
        }
        searchNoDataTips = findViewById(R.id.id_search_tips_tv);
        if(null == mDatas || mDatas.size() <= 0){
            searchNoDataTips.setVisibility(View.VISIBLE);
        }
        mRecyclerView = findViewById(R.id.id_recyclerview_tv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new RecyclerSearchTvAdapter(context, this.mDatas));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

    }


    private void listener() {
        mAdapter.setOnItemClickListener(new RecyclerSearchTvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //ToastUtil.showToast(VideoSearchListActivity.this,"点击 " + mDatas.get(position).getName() +  " - " +mDatas.get(position).getUrl());
                //String videoPath = "https://meigui.qqqq-kuyun.com/20190627/9918_47cdf731/index.m3u8";

                GlobalConfig.getInstance().executorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String implName = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(0).getKey() + "DataSourceHandle";
                            IDataSourceStrategy dataSourceStrategy = (IDataSourceStrategy) Class.forName("org.dync.datasourcestrategy.strategy." + implName).newInstance();
                            List<Video> videoList = dataSourceStrategy.playList(mDatas.get(position).getUrl());
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
                            searchVideoHandler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


            }

            @Override
            public void onItemLongClick(View view, int position) {
                ToastUtil.showToast(VideoSearchListTvActivity.this, "长按 " + mDatas.get(position).getName());
            }
        });
    }


    private void initData() {
        Intent intent = getIntent();
        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String implName = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(0).getKey() + "DataSourceHandle";
                    IDataSourceStrategy dataSourceStrategy = (IDataSourceStrategy) Class.forName("org.dync.datasourcestrategy.strategy." + implName).newInstance();
                    List<VideoSearch> videoSearchList = dataSourceStrategy.search(intent.getStringExtra("key"), 1);
                    if (null == videoSearchList || videoSearchList.size() <= 0) {
                        ToastUtil.showToast(VideoSearchListTvActivity.this, "抱歉,没有数据,请切换关键字!");
                    }
                    // Log.d(TAG,"videoSearchList size " + videoSearchList.size() );
                    Message msg = new Message();
                    msg.what = 0;
                    Bundle data = new Bundle();
                    data.putString("json", JSONObject.toJSONString(videoSearchList));
                    msg.setData(data);
                    searchVideoHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private Handler searchVideoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String json = msg.getData().getString("json");
                    if (null != json && json.length() > 0) {
                        List<VideoSearch> videoSearchList = JSONArray.parseArray(json, VideoSearch.class);
                        mDatas = new ArrayList<>();
                        if (null != videoSearchList) {
                            for (VideoSearch videoSearch : videoSearchList) {
                                mDatas.add(videoSearch);
                            }
                        }
                    }

                    initView();
                    listener();
                    break;

                case 1:
                    Bundle data = msg.getData();
                    VideoActivity.intentTo(context, data.getString("videoPath"), "测试", data.getString("url"));
                    break;
                default:
                    break;
            }
        }
    };

    public static Intent newIntent(Context context, String key) {
        Intent intent = new Intent(context, VideoSearchListTvActivity.class);
        intent.putExtra("key", key);
        return intent;
    }


    public static void intentTo(Context context, String key) {
        context.startActivity(newIntent(context, key));
    }


}
