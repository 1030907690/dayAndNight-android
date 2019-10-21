package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;

import org.dync.bean.VersionUpdate;
import org.dync.dialog.UpdataDialog;
import org.dync.utils.BaseUtils;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.io.IOException;

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

    private Button searchBtn;

    private Activity content = this;

    private UpdataDialog updataDialog;

    private final Activity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_main_activity);

        initView();
        onListener();
        checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
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
                if( GlobalConfig.reCount <= GlobalConfig.getInstance().getRemoteServer().length){
                    ToastUtil.showToast( context, "获取服务信息失败!正在重试第"+GlobalConfig.reCount+"次");
                    checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                }else{
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
        String version = BaseUtils.getAppVersionName(context);
        if (0 != version.compareTo(currentVersion)) {
            ToastUtil.showToast( context, "需要更新");
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
    }


    private void onListener() {
        searchEdit.setText("水中火");
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editable = searchEdit.getText();
                if (null != editable && null != editable.toString() && !"".equals(editable.toString())) {
                    if (editable.toString().startsWith("http://") || editable.toString().startsWith("https://")) {

                    } else {

                    }
                } else {
                    ToastUtil.showToast(content, "请输入要搜索的内容!");
                }
            }
        });

    }
}
