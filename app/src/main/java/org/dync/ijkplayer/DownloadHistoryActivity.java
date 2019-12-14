package org.dync.ijkplayer;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.dync.adapter.VideoListAdapter;
import org.dync.crash.MyCrashHandler;
import org.dync.db.SQLiteOperationHelper;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.dync.utils.StorageUtils;
import org.dync.utils.VideoType;

import java.io.File;
import java.net.URL;
import java.util.List;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;
import jaygoo.library.m3u8downloader.OnM3U8DownloadListener;
import jaygoo.library.m3u8downloader.bean.M3U8Task;
import jaygoo.library.m3u8downloader.utils.AES128Utils;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import jaygoo.library.m3u8downloader.utils.MUtils;

public class DownloadHistoryActivity extends AppCompatActivity {
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    M3U8Task[] taskList = new M3U8Task[5];
    private VideoListAdapter adapter;
    private String dirPath;
    private String encryptKey = "63F06F99D823D33AAB89A0A93DECFEE0"; //get the key by AES128Utils.getAESKey()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCrashHandler.instance().init(getApplicationContext());
        setContentView(R.layout.download_history_activity);
        requestAppPermissions();
        try {
            M3U8Log.d("AES BASE64 Random Key:" + AES128Utils.getAESKey());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        dirPath = StorageUtils.getCacheDirectory(this).getPath() + "/m3u8Downloader";
        //common config !
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setDebugMode(true)
        ;

        // add listener
        M3U8Downloader.getInstance().setOnM3U8DownloadListener(onM3U8DownloadListener);
        M3U8Downloader.getInstance().setEncryptKey(encryptKey);
        initData();
        adapter = new VideoListAdapter(this, R.layout.download_history_activity_list_item, taskList);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                M3U8Task m3U8Task = taskList[position];
                String url = m3U8Task.getUrl();
                if (M3U8Downloader.getInstance().checkM3U8IsExist(url)) {
                    Toast.makeText(getApplicationContext(), "本地文件已下载，正在播放中！！！", Toast.LENGTH_SHORT).show();

                    String device = GlobalConfig.getInstance().getSharedPreferences().getString(Constant.SWITCH, null);
                    if(SwitchTvOrPhoneActivity.SWITCH_DEVICE[0].equals(device)){
                        VideoTvActivity.intentTo(DownloadHistoryActivity.this, M3U8Downloader.getInstance().getM3U8Path(url), null, null, m3U8Task.getName(), VideoType.DOWNLOAD.getCode());
                    }else  if(SwitchTvOrPhoneActivity.SWITCH_DEVICE[1].equals(device)){
                        VideoActivity.intentTo(DownloadHistoryActivity.this, M3U8Downloader.getInstance().getM3U8Path(url), null, null, m3U8Task.getName(), VideoType.DOWNLOAD.getCode());
                    }



                } else {
                    M3U8Downloader.getInstance().download(m3U8Task.getName(),url);
                }
            }
        });

        findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MUtils.clearDir(new File(dirPath));
                deleteAllData();
                taskList = new M3U8Task[0];
                adapter = new VideoListAdapter(DownloadHistoryActivity.this, R.layout.download_history_activity_list_item, taskList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }


    public void deleteAllData() {
        SQLiteOperationHelper sqLiteOperationHelper = new SQLiteOperationHelper(this);
        SQLiteDatabase db = sqLiteOperationHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(SQLiteOperationHelper.DOWNLOAD_TABLE_NAME, "id > ?", new String[]{String.valueOf(0)});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        sqLiteOperationHelper.close();

    }

    public static void main(String[] args) {
        try {
            URL url = new URL("https://pp.ziyuan605.com/20190716/5urEiagx/index.m3u8");
            System.out.println(url.getHost() + "-- " + url.getProtocol());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void initData() {
        /*String testM3U8 = "https://pp.ziyuan605.com/20190716/5urEiagx/index.m3u8";
        M3U8Task bean0 = new M3U8Task(testM3U8);
        M3U8Task bean1 = new M3U8Task("http://pl-ali.youku.com/playlist/m3u8?ts=1524205957&keyframe=0&m3u8Md5=a85842b9ca4e77db4aa57c314c8e61c7&t1=200&pid=1133275aa6ac0891&vid=XMzU1MDY0NjEyMA==&type=flv&oip=1779113856&sid=0524205957937209643a0&token=2124&did=ae8263a35f7eaca76f68bb61436e6dac&ev=1&ctype=20&ep=YlUi3d%2BWQ%2F5shnijRhmbvlc%2FYJ8QmCsaCWAJ1RRpNbA%3D&ymovie=1");
        M3U8Task bean2 = new M3U8Task("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8");
        M3U8Task bean3 = new M3U8Task("https://www3.laqddc.com/hls/2018/04/07/BQ2cqpyZ/playlist.m3u8");
        M3U8Task bean4 = new M3U8Task("http://hcjs2ra2rytd8v8np1q.exp.bcevod.com/mda-hegtjx8n5e8jt9zv/mda-hegtjx8n5e8jt9zv.m3u8");
        taskList[0] = bean0;
        taskList[1] = bean1;
        taskList[2] = bean2;
        taskList[3] = bean3;
        taskList[4] = bean4;*/

        SQLiteOperationHelper sqLiteOperationHelper = new SQLiteOperationHelper(DownloadHistoryActivity.this);
        SQLiteDatabase db = sqLiteOperationHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteOperationHelper.DOWNLOAD_TABLE_NAME,
                SQLiteOperationHelper.TABLE_COLUMNS,
                "id > ?",
                new String[]{"0"},
                null, null, null);

        taskList = new M3U8Task[cursor.getCount()];
        int lineCount = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                M3U8Task m3U8Task = new M3U8Task(cursor.getString(1), cursor.getString(2));
                taskList[lineCount] = m3U8Task;
                lineCount++;
            }
        }
        db.close();
        sqLiteOperationHelper.close();
    }

    private OnM3U8DownloadListener onM3U8DownloadListener = new OnM3U8DownloadListener() {

        @Override
        public void onDownloadItem(M3U8Task task, long itemFileSize, int totalTs, int curTs) {
            super.onDownloadItem(task, itemFileSize, totalTs, curTs);
        }

        @Override
        public void onDownloadSuccess(M3U8Task task) {
            super.onDownloadSuccess(task);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onDownloadPending(M3U8Task task) {
            super.onDownloadPending(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadPause(M3U8Task task) {
            super.onDownloadPause(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadProgress(final M3U8Task task) {
            super.onDownloadProgress(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadPrepare(final M3U8Task task) {
            super.onDownloadPrepare(task);
            notifyChanged(task);

        }

        @Override
        public void onDownloadError(final M3U8Task task, Throwable errorMsg) {
            super.onDownloadError(task, errorMsg);
            notifyChanged(task);
        }

    };

    private void notifyChanged(final M3U8Task task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyChanged(taskList, task);
            }
        });

    }

    private void requestAppPermissions() {
        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            initView();
                            Toast.makeText(getApplicationContext(), "权限获取成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "权限获取失败", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                })
                .check();
    }
}
