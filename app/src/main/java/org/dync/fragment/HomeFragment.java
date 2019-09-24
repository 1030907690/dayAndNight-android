package org.dync.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.dync.ijkplayer.BottomNavigationViewActivity;
import org.dync.ijkplayer.ExoActivity;
import org.dync.ijkplayer.R;
import org.dync.ijkplayer.SettingActivity;
import org.dync.ijkplayer.VideoActivity;
import org.dync.ijkplayerlib.widget.receiver.NetWorkControl;
import org.dync.ijkplayerlib.widget.receiver.NetworkChangedReceiver;
import org.dync.ijkplayerlib.widget.util.Settings;


import butterknife.ButterKnife;


/***
 * 首页
 * */
public class HomeFragment extends Fragment {


    private Button btnSetting;
    private final String TAG = "HomeFragment";

    Button btnExoPlayer;
    private Button btnPlayer;
    private TextView tv;

    public static TestFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString("name", name);
        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.bind(getActivity());
        NetworkChangedReceiver register = NetWorkControl.register(TAG, getActivity());
        //设置默认的播放器
        final Settings settings = new Settings(getActivity());
        settings.setPlayer(Settings.PV_PLAYER__IjkExoMediaPlayer);
        return view;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //tv = (TextView) view.findViewById(R.id.fragment_test_tv);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
            //tv.setText(name);
        }
        btnExoPlayer = view.findViewById(R.id.btn_exoPlayer);

        btnSetting = (Button) view.findViewById(R.id.btn_setting);

        // 隐藏
        btnExoPlayer.setVisibility(View.GONE);
        btnPlayer.setVisibility(View.GONE);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });




        btnPlayer = (Button) view.findViewById(R.id.btn_ijkPlayer);
        btnPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoPath = "https://meigui.qqqq-kuyun.com/20190627/9918_47cdf731/index.m3u8";
                VideoActivity.intentTo(getActivity(), videoPath, "测试","");
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetWorkControl.unRegister(TAG, getActivity());
    }
}