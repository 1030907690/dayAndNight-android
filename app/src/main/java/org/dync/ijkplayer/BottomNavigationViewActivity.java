package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSONObject;

import org.dync.adapter.ViewPagerAdapter;
import org.dync.bean.VersionUpdate;
import org.dync.dialog.UpdataDialog;
import org.dync.fragment.AboutFragment;
import org.dync.fragment.HomeFragment;
import org.dync.fragment.MeFragment;
import org.dync.fragment.SearchFragment;
import org.dync.fragment.TestFragment;
import org.dync.utils.BaseUtils;
import org.dync.utils.BottomNavigationViewHelper;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
* https://blog.csdn.net/Afanbaby/article/details/79240620
* */
public class BottomNavigationViewActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private MenuItem menuItem;
    private UpdataDialog updataDialog;
    //上次按下返回键的系统时间
    private long lastBackTime = 0;
    //当前按下返回键的系统时间
    private long currentBackTime = 0;

    private Activity context = this;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //捕获返回键按下的事件
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //获取当前系统时间的毫秒数
            currentBackTime = System.currentTimeMillis();
            //比较上次按下返回键和当前按下返回键的时间差，如果大于2秒，则提示再按一次退出
            if(currentBackTime - lastBackTime > 2 * 1000){
                ToastUtil.showToast(this, "再按一次返回键退出");
                lastBackTime = currentBackTime;
            }else{ //如果两次按下的时间差小于2秒，则退出程序
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     *
     * @param initUrl 初始化配置地址

     * **/
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
                if( GlobalConfig.reCount < GlobalConfig.getInstance().getRemoteServer().length){
                    ToastUtil.showToast(BottomNavigationViewActivity.this, "获取服务信息失败!正在重试第"+GlobalConfig.reCount+"次");
                    checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                }else{
                    ToastUtil.showToast(BottomNavigationViewActivity.this, "获取服务信息失败!");
                }

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseStr = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // VersionUpdate versionUpdate = JSONObject.parseObject(responseStr, VersionUpdate.class);
                        try{
                            JSONObject.parseObject(responseStr, VersionUpdate.class);
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("json", responseStr);
                            msg.setData(data);
                            msg.what = 0;
                            mainActivityHandle.sendMessage(msg);
                        }catch (Exception e){
                            Log.d("main exception",e.getMessage());
                            GlobalConfig.reCount++;
                            if( GlobalConfig.reCount <= GlobalConfig.getInstance().getRemoteServer().length){
                                ToastUtil.showToast( context, "获取服务信息失败!正在重试第"+GlobalConfig.reCount+"次");
                                checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                            }else{
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
    private void initView() {
        //初始化弹窗 布局 点击事件的id
        updataDialog = new UpdataDialog(this, R.layout.dialog_version_update,
                new int[]{R.id.dialog_sure});
    }

    /***
     * 去下载
     * */
    private void toDownLoad(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }


    private Handler mainActivityHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    VersionUpdate versionUpdate = JSONObject.parseObject(msg.getData().getString("json"), VersionUpdate.class);
                    GlobalConfig.getInstance().setVersionUpdate(versionUpdate);
                    comparison();
                default:
                    break;
            }
        }
    };

    /***
     * 对比版本
     * */
    private void comparison() {
        String currentVersion = GlobalConfig.getInstance().getVersionUpdate().getCurrentVersion();
        String url = GlobalConfig.getInstance().getVersionUpdate().getDownloadUrl();
        //String version = sharedPreferences.getString("version", "1.0.0");
        String version = BaseUtils.getAppVersionName(BottomNavigationViewActivity.this);
        if (0 != version.compareTo(currentVersion)) {
            ToastUtil.showToast(BottomNavigationViewActivity.this, "需要更新");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_view);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        List<Fragment> list = new ArrayList<>();
        list.add(HomeFragment.newInstance("首页"));
        list.add(SearchFragment.newInstance("搜索"));
        list.add(MeFragment.newInstance("Me"));
        list.add(AboutFragment.newInstance("关于"));
        viewPagerAdapter.setList(list);

        initView();
        if(null == GlobalConfig.getInstance().getVersionUpdate()){
            checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            menuItem = item;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_person:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };


}
