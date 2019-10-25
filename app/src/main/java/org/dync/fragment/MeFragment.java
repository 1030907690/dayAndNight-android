package org.dync.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.dync.ijkplayer.MainActivity;
import org.dync.ijkplayer.R;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
 * zhouzhongqing
 * 2019年9月24日17:40:56
 * Me 页面
 * */
public class MeFragment extends Fragment {


    private final String TAG = "MeFragment";

    private TextView downloadView;

    private TextView watchHistoryView;

    public static MeFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString("name", name);
        MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.me_activity, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
        }

        downloadView = view.findViewById(R.id.my_download);
        watchHistoryView = view.findViewById(R.id.my_watch_history);

        downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToastUtil.showToast(getActivity(), "功能正在开发中...");
                downloadFile();
            }
        });

        watchHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showToast(getActivity(), "功能正在开发中...");
            }
        });
    }


    private void downloadFile() {
        String remoteUrl = "https://vs1.baduziyuan.com/20171121/DsHZtCem/index.m3u8";


/**
 * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
 */
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
        /*    for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {*/
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
               /*     return;
               }
            }*/
        }

        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection conn = null;
                RandomAccessFile raf = null;
                try {
                    URL url = new URL(remoteUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(3000);
                    conn.setRequestMethod("GET");
                    int length = -1;
                    if (conn.getResponseCode() == 200) {
                        // 得到下载文件长度
                        length = conn.getContentLength();
                    }

                    if (length <= 0) {
                        return;
                    }

                    File dir = new File("/data");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    // 构建文件对象
                    File file = new File(dir, "test.m3u8");
                    raf = new RandomAccessFile(file, "rwd");
                    raf.setLength(length);


                    //BufferedReader是可以按行读取文件
                    FileInputStream inputStream = new FileInputStream(file);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String str = null;
                    while((str = bufferedReader.readLine()) != null)
                    {
                        Log.d("MeFragment str ",str);
                    }

                    //close
                    inputStream.close();
                    bufferedReader.close();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        conn.disconnect();
                        if(null != raf){
                            raf.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
