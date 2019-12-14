package org.dync.ijkplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.dync.crash.MyCrashHandler;
import org.dync.utils.Constant;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

/***
 * zhouzhongqing
 * 2019年12月14日16:05:18
 * tv菜单的activity
 * */
public class MenuTvActivity extends AppCompatActivity {

    private Button downloadView;

    private Button watchHistoryView;

    private Spinner spinner;

    private TextView customServerTextView;
    private Button customServerText;

    private final Activity content = this;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyCrashHandler.instance().init(getApplicationContext());
        setContentView(R.layout.menu_tv_activity);
        initView();
        listener();
    }

    private void initView(){
        downloadView = findViewById(R.id.my_download);
        watchHistoryView = findViewById(R.id.my_watch_history);

        spinner = findViewById(R.id.data_source_spinner);

        customServerTextView = findViewById(R.id.custom_server_view_text);
        customServerText = findViewById(R.id.custom_server_text);

    }

    private void listener(){
        downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(content, DownloadHistoryActivity.class);
                content.startActivity(intent);

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
                ToastUtil.showToast( content, "功能正在开发中...");
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

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>( content, android.R.layout.simple_spinner_item, seq);
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
                        ToastUtil.showToast( content, "选择私服后,请点击设置私服!");
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
            ToastUtil.showToast(content, "未能连接上服务器不能提供服务!");
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

        final EditText edit = new EditText(content);
        edit.setText(sharedPreferences.getString(Constant.CUSTOM_API_PREFIX, ""));
        AlertDialog.Builder editDialog = new AlertDialog.Builder(content);
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
                                ToastUtil.showToast(content, "地址必须是http https开头!");
                            }
                        } else {
                            ToastUtil.showToast(content, "您没有输入地址!");
                        }

                        dialog.dismiss();
                    }
                });

        editDialog.create().show();
    }


}
