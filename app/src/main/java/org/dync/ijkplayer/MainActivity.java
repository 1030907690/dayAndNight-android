package org.dync.ijkplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;

import org.dync.bean.VersionUpdate;
import org.dync.dialog.UpdataDialog;
import org.dync.ijkplayerlib.widget.receiver.NetWorkControl;
import org.dync.ijkplayerlib.widget.receiver.NetworkChangedReceiver;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String INIT_URL = "https://raw.githubusercontent.com/1030907690/dayAndNight/master/VersionManager.json";


    private Context mContext;
    private final String TAG = "MainActivity";

    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_ijkPlayer)
    Button btnIjkPlayer;
    @BindView(R.id.btn_exoPlayer)
    Button btnExoPlayer;
    /*https://blog.csdn.net/panghaha12138/article/details/72412902*/
    private UpdataDialog updataDialog;

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

    /**
     * 返回当前程序版本名  build.gradle
     */
    public String getAppVersionName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            String versionName = pi.versionName;
            int versioncode = pi.versionCode;
            Log.d("versionName:---" + versionName, "versioncode:---" + versioncode);
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
            return versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return null;
    }

    /***
     * 对比版本
     * */
    private void comparison() {
        String currentVersion = GlobalConfig.getInstance().getVersionUpdate().getCurrentVersion();
        String url = GlobalConfig.getInstance().getVersionUpdate().getDownloadUrl();
        //String version = sharedPreferences.getString("version", "1.0.0");
        String version = getAppVersionName(MainActivity.this);
        if (0 != version.compareTo(currentVersion)) {
            ToastUtil.showToast(MainActivity.this, "需要更新");
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

    public void checkVersionGet(View view) {
        OkHttpClient client = new OkHttpClient();
        //构造Request对象
        //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
        Request request = new Request.Builder().get().url(INIT_URL).build();
        Call call = client.newCall(request);
        //异步调用并设置回调函数
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                ToastUtil.showToast(MainActivity.this, "Get 失败");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseStr = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // VersionUpdate versionUpdate = JSONObject.parseObject(responseStr, VersionUpdate.class);
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("json", responseStr);
                        msg.setData(data);
                        msg.what = 0;
                        mainActivityHandle.sendMessage(msg);
                        // initTextView.setText("当前版本 :" + currentVersion);
                        //comparison(currentVersion, jsonObject.getString("downloadUrl"));
                    }
                });
            }
        });
    }

    public void checkVersionPost(View view) {
        OkHttpClient client = new OkHttpClient();
        //构建FormBody，传入要提交的参数
        FormBody formBody = new FormBody
                .Builder()
                /*.add("username", "initObject")
                .add("password", "initObject")*/
                .build();
        final Request request = new Request.Builder()
                .url(INIT_URL)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtil.showToast(MainActivity.this, "Post Parameter 失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                ToastUtil.showToast(MainActivity.this, "Code：" + String.valueOf(response.code()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //initTextView.setText(responseStr);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        NetworkChangedReceiver register = NetWorkControl.register(TAG, this);
//        register.setNetWorkChangeListener(new NetWorkControl.NetWorkChangeListener() {
//            @Override
//            public boolean isConnected(boolean wifiConnected, boolean wifiAvailable, boolean mobileConnected, boolean mobileAvailable) {
//
//                return false;
//            }
//        });
        initView();
        checkVersionGet(null);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetWorkControl.unRegister(TAG, this);
    }

    @OnClick({R.id.btn_setting, R.id.btn_ijkPlayer, R.id.btn_exoPlayer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.btn_ijkPlayer:
//                String videoPath = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";
//                String videoPath = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8";
//                String videoPath = "http://hot.vrs.sohu.com/ipad3969651_4718009227337_6170972.m3u8?plat=3&uid=e8192000-5281-4dac-9d6d-f4db0f8c7efa&pt=3&prod=mdk&pg=1&qd=130015&cv=1.5";
//                String videoPath = "http://daai.waaarp.wscdns.com/live-transcode/_definst_/smil:daai/tv01.smil/playlist.m3u8";
//                String videoPath = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";
                //String videoPath = "https://youku.rebo5566.com/20190716/F66s4OVm/index.m3u8";
                String videoPath = "https://meigui.qqqq-kuyun.com/20190627/9918_47cdf731/index.m3u8";
                VideoActivity.intentTo(mContext, videoPath, "测试");
                break;
            case R.id.btn_exoPlayer:
                startActivity(new Intent(mContext, ExoActivity.class));
                break;
        }
    }
}
