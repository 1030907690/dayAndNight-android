package org.dync.fragment;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.dync.bean.VersionUpdate;
import org.dync.dialog.UpdataDialog;
import org.dync.ijkplayer.BottomNavigationViewActivity;
import org.dync.ijkplayer.BuildConfig;
import org.dync.ijkplayer.R;
import org.dync.utils.BaseUtils;
import org.dync.utils.GlobalConfig;
import org.dync.utils.StorageUtils;
import org.dync.utils.ToastUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
 * zhouzhongqing
 * 2019年9月24日17:40:56
 * 关于 页面
 * */
public class AboutFragment extends Fragment {


    private final String TAG = "AboutFragment";

    private static Context contexst;

    public static AboutFragment newInstance(String name, Context context) {
        contexst = context;
        Bundle args = new Bundle();
        args.putString("name", name);
        AboutFragment fragment = new AboutFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_activity, container, false);
        return view;
    }


    private TextView checkUpdate;

    private UpdataDialog updataDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
        }
        initView();
        checkUpdate = view.findViewById(R.id.check_update);
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //重置次数
                GlobalConfig.reCount = 0;
                checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
            }
        });

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
                    ToastUtil.showToast(getActivity(), "获取服务信息失败!正在重试第" + GlobalConfig.reCount + "次");
                    checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                } else {
                    ToastUtil.showToast(getActivity(), "获取服务信息失败!");
                }

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseStr = response.body().string();
                GlobalConfig.getInstance().executorService().execute(new Runnable() {
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
                                ToastUtil.showToast(getActivity(), "获取服务信息失败!正在重试第" + GlobalConfig.reCount + "次");
                                checkVersionGet(GlobalConfig.getInstance().getRemoteServer()[GlobalConfig.reCount]);
                            } else {
                                ToastUtil.showToast(getActivity(), "获取服务信息失败!");
                            }
                        }
                        // initTextView.setText("当前版本 :" + currentVersion);
                        //comparison(currentVersion, jsonObject.getString("downloadUrl"));
                    }
                });
            }
        });
    }


    private String downloadFullPath = null;
    private DownloadManager downloadManager;
    private long mTaskId;

    /***
     * 去下载
     * */
    private void toDownLoad(String url) {


       /* Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);*/


        //  https://www.jianshu.com/p/46fd1c253701
        //url = "http://192.168.0.111/app-release.apk";
        /**
         * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
         */
        if (Build.VERSION.SDK_INT >= 23) {
            final int REQUEST_EXTERNAL_STORAGE = 1;
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                //验证是否许可权限
                ActivityCompat.requestPermissions(
                        getActivity(),
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }

    /*    //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/download/", url.substring(url.lastIndexOf("/") + 1));
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);*/




        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);


        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir("/download/", url.substring(url.lastIndexOf("/") + 1));
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

        //将下载请求加入下载队列

        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);


        //注册广播接收者，监听下载状态
        getActivity().registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }


    //广播接受者，接收下载状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i(TAG, ">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Log.i(TAG, ">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Log.i(TAG, ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i(TAG, ">>>下载完成");
                    //下载完成安装APK
                    ToastUtil.showToast(getActivity(),"下载完成!在/download文件夹中");
                    //installAPK(new File(downloadPath + downloadFullPath));
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i(TAG, ">>>下载失败");
                    break;
            }
        }
    }

    //下载到本地后执行安装
    protected void installAPK(File file) {
        if (!file.exists()) {
            ToastUtil.showToast(getActivity(), "未找到文件 " + downloadFullPath);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
    /*    Uri uri = Uri.parse("file://" + file.toString());
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag,后面解释
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
*/
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        getActivity().startActivity(intent);
    }


    /***
     * 对比版本
     * */
    private void comparison() {
        String currentVersion = GlobalConfig.getInstance().getVersionUpdate().getCurrentVersion();
        String url = GlobalConfig.getInstance().getVersionUpdate().getDownloadUrl();
        //String version = sharedPreferences.getString("version", "1.0.0");
        String version = BaseUtils.getAppVersionName(getActivity());
        if (0 != version.compareTo(currentVersion)) {
            ToastUtil.showToast(getActivity(), "需要更新");
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

        } else {
            ToastUtil.showToast(getActivity(), "已经是最新版本!");
        }
    }

    private Handler mainActivityHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    VersionUpdate versionUpdate = JSONObject.parseObject(msg.getData().getString("json"), VersionUpdate.class);
                    GlobalConfig.getInstance().setVersionUpdate(versionUpdate);
                    //设置数据源
                    GlobalConfig.getInstance().setOptionDataSourceStrategy(0);
                    comparison();
                default:
                    break;
            }
        }
    };


    private void initView() {
        //初始化弹窗 布局 点击事件的id
        updataDialog = new UpdataDialog(getActivity(), R.layout.dialog_version_update,
                new int[]{R.id.dialog_sure});
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
