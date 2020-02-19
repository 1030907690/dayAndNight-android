package org.dync.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.dync.ijkplayer.DownloadHistoryActivity;
import org.dync.ijkplayer.MainActivity;
import org.dync.ijkplayer.R;
import org.dync.ijkplayer.WatchHistoryActivity;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

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


    private static final String TAG = "MeFragment";

    private TextView downloadView;

    private TextView watchHistoryView;

    private Spinner spinner;

    private TextView customServerTextView;
    private TextView customServerText;

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

        spinner = view.findViewById(R.id.data_source_spinner);

        customServerTextView = view.findViewById(R.id.custom_server_view_text);
        customServerText = view.findViewById(R.id.custom_server_text);


        downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), DownloadHistoryActivity.class);
                getActivity().startActivity(intent);

                //ToastUtil.showToast(getActivity(), "功能正在开发中...");


                /**
                 * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
                 */
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                }*/
             /*   String url = "https://youku.rebo5566.com/20190716/F66s4OVm/index.m3u8";
                String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                downloadFile(url, savePath, savePath);
*/

            }

        });

        watchHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ToastUtil.showToast(getActivity(), "功能正在开发中...");
                Intent intent = new Intent(getActivity(), WatchHistoryActivity.class);
                getActivity().startActivity(intent);

            }
        });


        if (null != GlobalConfig.getInstance().getVersionUpdate()) {
            //方法二,使用数组
            String[] seq = new String[GlobalConfig.getInstance().getVersionUpdate().getDataSource().size()];
            for (int i = 0; i < GlobalConfig.getInstance().getVersionUpdate().getDataSource().size(); i++) {
                seq[i] = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(i).getKey();
            }
            SharedPreferences sharedPreferences = GlobalConfig.getInstance().getSharedPreferences();
            int dataSourceOption = sharedPreferences.getInt(Constant.DATA_SOURCE_OPTION, 0);

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, seq);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(dataSourceOption);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(Constant.DATA_SOURCE_OPTION, position);
                    editor.commit();

                    //设置数据源
                    int dataSourceOption = sharedPreferences.getInt(Constant.DATA_SOURCE_OPTION, 0);
                    GlobalConfig.getInstance().setOptionDataSourceStrategy(dataSourceOption);


                    if ("Custom".equals(seq[dataSourceOption]) && (null == customServerTextView.getText() || null == customServerTextView.getText().toString())) {
                        ToastUtil.showToast(getActivity(), "选择私服后,请点击设置私服!");
                    }

                    // ToastUtil.showToast(getActivity(), "Spinner1: position=" + position + " id=" + id);
                    //选择后
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    //ToastUtil.showToast(getActivity(), "没有选择");
                    //没有选择
                }
            });

        } else {
            ToastUtil.showToast(getActivity(), "未能连接上服务器不能提供服务!");
        }


        settingCustomServer();
    }


    private void settingCustomServer() {

        SharedPreferences sharedPreferences = GlobalConfig.getInstance().getSharedPreferences();
        customServerTextView.setText(sharedPreferences.getString(Constant.CUSTOM_API_PREFIX, ""));

        customServerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customServerDialog(sharedPreferences);
            }
        });
        customServerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customServerDialog(sharedPreferences);
            }
        });
    }


    private void customServerDialog(SharedPreferences sharedPreferences) {

        final EditText edit = new EditText(getActivity());
        edit.setText(sharedPreferences.getString(Constant.CUSTOM_API_PREFIX, ""));
        AlertDialog.Builder editDialog = new AlertDialog.Builder(getActivity());
        editDialog.setTitle(getString(R.string.dialog_btn_confirm_text));
        editDialog.setIcon(R.mipmap.ic_launcher_round);

        //设置dialog布局
        editDialog.setView(edit);

        //设置按钮
        editDialog.setPositiveButton(getString(R.string.dialog_edit_text)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Toast.makeText(getActivity(),
                                edit.getText().toString().trim(),Toast.LENGTH_SHORT).show();*/

                        Editable editable = edit.getText();
                        if (null != editable && null != editable.toString()) {
                            if (editable.toString().startsWith("http://") || editable.toString().startsWith("https://")) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Constant.CUSTOM_API_PREFIX, editable.toString());
                                editor.commit();

                                customServerTextView.setText(editable.toString());
                            } else {
                                ToastUtil.showToast(getActivity(), "地址必须是http https开头!");
                            }
                        } else {
                            ToastUtil.showToast(getActivity(), "您没有输入地址!");
                        }

                        dialog.dismiss();
                    }
                });

        editDialog.create().show();
    }

    public static void main(String[] args) {
        String url = "https://youku.rebo5566.com/20190716/F66s4OVm/index.m3u8";
        //String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String savePath = "F:/360极速浏览器下载/m3u8";
        downloadFile(url, savePath, savePath);

    }


    private static void downloadFile(String url, String savePath, String basicPath) {

        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {


                // 储存下载文件的目录

                File file = new File(savePath, url.substring(url.lastIndexOf("/") + 1));


                InputStream is = null;
                try {


                    final long startTime = System.currentTimeMillis();
                    System.out.println("DOWNLOAD" + "startTime=" + startTime);
                    //下载函数
                    //String fileName = url.substring(url.lastIndexOf("/") + 1);
                    //获取文件名
                    URL myURL = new URL(url);
                    URLConnection conn = myURL.openConnection();
                    conn.connect();
                    is = conn.getInputStream();
                    int fileSize = conn.getContentLength();//根据响应获取文件大小
//                    if (fileSize <= 0) throw new RuntimeException("无法获知文件大小 ");
                    if (is == null) throw new RuntimeException("stream is null");

                    //把数据存入路径+文件名
                    FileOutputStream fos = new FileOutputStream(file);
                    byte buf[] = new byte[1024];
                    int downLoadFileSize = 0;
                    do {
                        //循环读取
                        int numread = is.read(buf);
                        if (numread == -1) {
                            break;
                        }
                        fos.write(buf, 0, numread);
                        downLoadFileSize += numread;
                        //更新进度条
                    } while (true);

                    System.out.println("DOWNLOAD" + "download success");
                    System.out.println("DOWNLOAD" + "totalTime=" + (System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }


                // 读取文件
                try {

                    StringBuilder sBuiler = new StringBuilder();
                    String encoding = "UTF-8";
                    File readFile = new File(file.getPath());
                    if (null != readFile && readFile.isFile() && readFile.exists()) { // 判断文件是否存在
                        InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null) {

                            //System.out.println(TAG + "lineText " + lineTxt);

                            if (lineTxt.endsWith(".m3u8") || lineTxt.endsWith(".ts")) {

                                if (lineTxt.startsWith("http://") || lineTxt.startsWith("https://")) {
                                    //网络地址
                                } else {
                                    // 相对路径
                                    // 完整URL
                                    String fullUrl = "";
                                    String relativePath = lineTxt.substring(0, lineTxt.lastIndexOf("/"));
                                    if (!relativePath.startsWith("/")) {
                                        relativePath = File.separator + relativePath;
                                    }

                                    File nextFile = new File(basicPath + relativePath);
                                    if (!nextFile.exists()) {
                                        nextFile.mkdirs();
                                    }

                                    String tempLineTxt = lineTxt;
                                    if (tempLineTxt.startsWith("/")) {
                                        tempLineTxt = tempLineTxt.substring(1);
                                    }

                                    // 解析出domain
                                    if (url.startsWith("http://")) {
                                        String tempUrl = url.replace("http://", "");
                                        int index = tempUrl.indexOf("/");
                                        fullUrl = "http://" + tempUrl.substring(0, index + 1) + tempLineTxt;
                                    } else if (url.startsWith("https://")) {
                                        String tempUrl = url.replace("https://", "");
                                        int index = tempUrl.indexOf("/");
                                        fullUrl = "https://" + tempUrl.substring(0, index + 1) + tempLineTxt;
                                    }

                                    sBuiler.append("file:/" + basicPath + "/" + tempLineTxt + "\n");
                                    System.out.println(TAG + "fullUrl " + fullUrl);
                                    downloadFile(fullUrl, nextFile.getPath(), basicPath);
                                }
                            } else {
                                //原样输出
                                sBuiler.append(lineTxt + "\n");
                            }
                        }

                        writeText(file.getPath(), sBuiler.toString());
                        bufferedReader.close();
                        read.close();
                    } else {
                        System.out.println("找不到指定的文件");
                    }

                } catch (Exception e) {
                    System.out.println("读取文件内容出错");
                    e.printStackTrace();
                }
            }


        });
    }

    /**
     * 使用FileWriter类写文本文件
     */
    public static void writeText(String path, String text) {
        try {
            if (new File(path).exists()) {
                new File(path).delete();
            }
            //使用这个构造函数时，如果存在kuka.txt文件，
            //则先把这个文件给删除掉，然后创建新的kuka.txt
            FileWriter writer = new FileWriter(path);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
