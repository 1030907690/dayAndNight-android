package org.dync.ijkplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.dync.adapter.WatchHistoryRecyclerViewAdapter;
import org.dync.bean.MovieBaseModel;
import org.dync.bean.MovieDataModel;
import org.dync.bean.OtherBaseModel;
import org.dync.crash.MyCrashHandler;
import org.dync.db.SQLiteOperationHelper;
import org.dync.utils.BaseTools;
import org.dync.utils.Constant;
import org.dync.utils.CustomDecoration;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.dync.utils.Utils;
import org.dync.utils.VideoType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jaygoo.library.m3u8downloader.bean.M3U8Task;

/***
 * zhouzhongqing
 * 2020年02月19日11:25:42
 * 观看历史记录activity
 * */
public class WatchHistoryActivity extends AppCompatActivity {


    private Activity context = this;


    //步骤1：创建一个SharedPreferences对象
    private SharedPreferences sharedPreferences;

    private final int dataSourceOption = GlobalConfig.getInstance().getSharedPreferences().getInt(Constant.DATA_SOURCE_OPTION, 0);


    @BindView(R.id.mTitleBar)
    TitleBar mTitleBar;
    @BindView(R.id.loading_view_ll)
    LinearLayout loading_view_ll;
    @BindView(R.id.loading_view)
    ImageView mLoadingView;
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;
    @BindView(R.id.rvMovieList)
    RecyclerView rvMovieList;

    private boolean refreshType;
    private int page;
    private int oldListSize;
    private int newListSize;
    private int addListSize;
    private String viewType;
    private WatchHistoryRecyclerViewAdapter adapter;

    private List<MovieDataModel> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCrashHandler.instance().init(getApplicationContext());
        setContentView(getLayoutId());
        initView();
        initData();
    }

    protected int getLayoutId() {
        return R.layout.watch_history_activity;
    }

    protected void initView() {

        ButterKnife.bind(this);

        Utils.applyPermission(context);


        Intent intent = getIntent();
        viewType = "NoDividingLine";//intent.getStringExtra("ViewType");
        mTitleBar.setOnTitleBarListener(new OnTitleBarListener() {

            @Override
            public void onLeftClick(View v) {
                finish();
            }

            @Override
            public void onTitleClick(View v) {
            }

            @Override
            public void onRightClick(View v) {
            }
        });
        AnimationDrawable anim = (AnimationDrawable) mLoadingView.getDrawable();
        anim.start();

    }

    protected void initData() {

        // 开启自动加载功能（非必须）
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshType = true;
                        //page = 1;
                        page = 0;
                        parsingMovieListJson();
                        refreshLayout.finishRefresh();
                        refreshLayout.resetNoMoreData();//setNoMoreData(false);
                    }
                }, 2000);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshType = false;
                        //TODO 先设置成100
                        if (page > 100) {
                            ToastUtil.showToast(context, "暂无更多的数据啦");
                            // 将不会再次触发加载更多事件
                            refreshLayout.finishLoadMoreWithNoMoreData();
                            return;
                        }
                        parsingMovieListJson();
                        refreshLayout.setEnableLoadMore(true);
                        refreshLayout.finishLoadMore();
                    }
                }, 2000);
            }
        });
        //触发自动刷新
        refreshLayout.autoRefresh();

    }

    private void parsingMovieListJson() {

        try {
            // 从assets目录中获取json数据，在真实的项目开发中需要通过网络请求从服务器json数据
            //String jsonData = BaseTools.getAssetsJson(this, "movie" + page + ".json");
            if (refreshType && mList != null) {
                mList.clear();
                oldListSize = 0;
            } else {
                oldListSize = mList.size();
            }
            // 使用Google的Gson开始解析json数据
          /*  Gson gson = new Gson();
            MovieBaseModel movieBaseModel = gson.fromJson(jsonData, MovieBaseModel.class);
            List<MovieDataModel> movieDataModelList = movieBaseModel.getData();
            for (MovieDataModel movieDataModel : movieDataModelList) {
                MovieDataModel data = new MovieDataModel();
                data.setMovClass(movieDataModel.getMovClass());
                data.setDownLoadName(movieDataModel.getDownLoadName());
                data.setDownimgurl(movieDataModel.getDownimgurl());
                data.setDownLoadUrl(movieDataModel.getDownLoadUrl());
                data.setMvdesc(movieDataModel.getMvdesc());
                OtherBaseModel otherModelDesc = gson.fromJson(movieDataModel.getMvdesc(), OtherBaseModel.class);
                List<String> headerList = otherModelDesc.getHeader();
                data.setDirector(headerList.get(1));
                data.setStarring(headerList.get(2));
                data.setType(headerList.get(3));
                data.setRegions(headerList.get(4));
                mList.add(data);
            }*/

            //zhouzhongqing 2020年02月20日11:54:30

            SQLiteOperationHelper sqLiteOperationHelper = new SQLiteOperationHelper(WatchHistoryActivity.this);
            SQLiteDatabase db = sqLiteOperationHelper.getReadableDatabase();

            int num[] = new int[2];
            int pagesSize = 100;
            num[0] = page * pagesSize;// 0*50 1*50 2*50
            num[1] = pagesSize;
            String sql = "select id,name ,data_source,url,url_item,duration,create_time,name_node from " + SQLiteOperationHelper.WATCH_TABLE_NAME + " where 1=1 and data_source=" + dataSourceOption + " order by id desc limit " + num[0] + "," + num[1];

            Cursor cursor = db.rawQuery(sql, null);
            //ToastUtil.showToast(context,"数据条" + cursor.getCount());
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    MovieDataModel data = new MovieDataModel();
                    //暂时写是多少集
                    data.setMovClass(cursor.getString(7));
                    data.setDownLoadName(cursor.getString(1));
                    //暂时写视频地址
                    data.setDownimgurl(cursor.getString(3));
                    //暂时写剧集地址
                    data.setDownLoadUrl(cursor.getString(4));
                    data.setMvdesc(cursor.getColumnName(3));
                    //暂时写是多少集
                    data.setDirector(cursor.getString(7));
                    //暂时写时间
                    data.setStarring(cursor.getString(6));
                    data.setType("视频");
                    data.setRegions("观看至:" + String.valueOf((cursor.getInt(5)) / (34748*2)));
                    mList.add(data);
                }
            }
            db.close();
            sqLiteOperationHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        newListSize = mList.size();
        addListSize = newListSize - oldListSize;

        if (refreshType) {
            // 设置RecyclerView样式为竖直线性布局
            rvMovieList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            adapter = new WatchHistoryRecyclerViewAdapter(this, mList);
            if (viewType.equals("NoDividingLine")) {
                //mTitleBar.setTitle("线性布局样式");
                mTitleBar.setTitle("观看历史");
            } else {
                mTitleBar.setTitle("线性布局(有分割线)样式");
                // 设置分割线
                rvMovieList.addItemDecoration(new CustomDecoration(
                        this, LinearLayoutManager.VERTICAL, R.drawable.divider_mileage, 15));
            }
            rvMovieList.setAdapter(adapter);
        } else {
            adapter.notifyItemRangeInserted(mList.size() - addListSize, mList.size());
            adapter.notifyItemRangeChanged(mList.size() - addListSize, mList.size());
        }
        page++;

        rvMovieList.setVisibility(View.VISIBLE);
        loading_view_ll.setVisibility(View.GONE);

        // item条目的点击事件回调
        adapter.setItemClikListener(new WatchHistoryRecyclerViewAdapter.OnItemClikListener() {

            // 短按点击事件回调
            @Override
            public void onItemClik(View view, int position) {
                //String videoTitle = mList.get(position).getDownLoadName();
                //ToastUtil.showToast(context, videoTitle);
                MovieDataModel movieDataModel = mList.get(position);
                VideoActivity.intentTo(context, movieDataModel.getDownLoadUrl(), movieDataModel.getMovClass(), movieDataModel.getDownimgurl(), movieDataModel.getDownLoadName(), VideoType.WATCH_HISTORY.getCode());

            }

            // 长按点击事件回调
            @Override
            public void onItemLongClik(View view, int position) {

            }
        });

    }

}
