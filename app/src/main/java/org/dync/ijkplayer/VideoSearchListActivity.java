package org.dync.ijkplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;

import org.dync.adapter.RecyclerSearchAdapter;
import org.dync.bean.VideoSearch;

import java.util.ArrayList;
import java.util.List;

public class VideoSearchListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private RecyclerSearchAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_search_list);

        initData();
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new RecyclerSearchAdapter(VideoSearchListActivity.this, this.mDatas));
    }


    private void initData() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        if (null != json && json.length() > 0) {
            List<VideoSearch> videoSearchList = JSONArray.parseArray(json, VideoSearch.class);
            mDatas = new ArrayList<>();
            if (null != videoSearchList) {
                for (VideoSearch videoSearch : videoSearchList) {
                    mDatas.add(videoSearch.getName());
                }
            }
        }

    }

    public static Intent newIntent(Context context, String json) {
        Intent intent = new Intent(context, VideoSearchListActivity.class);
        intent.putExtra("json", json);
        return intent;
    }


    public static void intentTo(Context context, String json) {
        context.startActivity(newIntent(context, json));
    }


}
