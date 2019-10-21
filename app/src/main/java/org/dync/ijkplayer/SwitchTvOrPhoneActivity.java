package org.dync.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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
 * 2019年10月21日18:03:59
 * 切换是电视剧或者是手机的activity
 * */
public class SwitchTvOrPhoneActivity extends AppCompatActivity {


    private Button tvBtn;

    private Button phoneBtn;

    private Activity context = this;

    private final String SWITCH_DEVICE[] = {"TV", "PHONE"};

    //步骤1：创建一个SharedPreferences对象
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_tv_phone_activity);

        initView();
        onListener();

        redirect();

    }



    private void redirect() {
        String device = sharedPreferences.getString("switch", null);
        if (null != device) {
            tvBtn.setVisibility(View.GONE);
            phoneBtn.setVisibility(View.GONE);
            //tv
            if(SWITCH_DEVICE[0].equals(device)){

                Intent intent = new Intent(context, MainTvActivity.class);
                context.startActivity(intent);
                finish();
            }else  if(SWITCH_DEVICE[1].equals(device)){
                //phone
                Intent intent = new Intent(context, BottomNavigationViewActivity.class);
                context.startActivity(intent);
                finish();
            }
        }
    }

    private void initView() {

        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        tvBtn = findViewById(R.id.selected_tv);
        phoneBtn = findViewById(R.id.selected_phone);
    }


    private void onListener() {
        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showToast((Activity) context, "选择tv");
                //步骤2： 实例化SharedPreferences.Editor对象
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //步骤3：将获取过来的值放入文件
                editor.putString("switch", SWITCH_DEVICE[0]);
                //步骤4：提交
                editor.commit();
                Intent intent = new Intent(context, MainTvActivity.class);
                context.startActivity(intent);
                finish();

            }
        });

        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showToast((Activity) context, "选择手机");
                //步骤2： 实例化SharedPreferences.Editor对象
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //步骤3：将获取过来的值放入文件
                editor.putString("switch", SWITCH_DEVICE[1]);
                //步骤4：提交
                editor.commit();
                Intent intent = new Intent(context, BottomNavigationViewActivity.class);
                context.startActivity(intent);
                finish();
            }
        });
    }


}
