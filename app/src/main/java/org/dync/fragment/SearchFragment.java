package org.dync.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.dync.bean.Video;
import org.dync.bean.VideoSearch;
import org.dync.datasourcestrategy.IDataSourceStrategy;
import org.dync.ijkplayer.R;
import org.dync.ijkplayer.SettingActivity;
import org.dync.ijkplayer.VideoActivity;
import org.dync.ijkplayer.VideoSearchListActivity;
import org.dync.ijkplayer.utils.StatusBarUtil;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ToastUtil;

import java.util.List;

public class SearchFragment extends Fragment {


    private final String TAG = "SearchActivity";

    private EditText searchEditText;

    private Button btnSearch;

    public static SearchFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString("name", name);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_search, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchEditText = view.findViewById(R.id.search_editText);
        btnSearch = view.findViewById(R.id.btn_search);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
        }


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editable = searchEditText.getText();
                if (null != editable && null != editable.toString() && !"".equals(editable.toString())) {
                    GlobalConfig.getInstance().executorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String implName = GlobalConfig.getInstance().getVersionUpdate().getDataSource().get(0).getKey() + "DataSourceHandle";
                                IDataSourceStrategy dataSourceStrategy = (IDataSourceStrategy) Class.forName("org.dync.datasourcestrategy.strategy." + implName).newInstance();
                                List<VideoSearch> videoSearchList = dataSourceStrategy.search(editable.toString());
                                // Log.d(TAG,"videoSearchList size " + videoSearchList.size() );
                                Message msg = new Message();
                                msg.what = 0;
                                Bundle data = new Bundle();
                                data.putString("json", JSONObject.toJSONString(videoSearchList));
                                msg.setData(data);
                                searchVideoHandler.sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    ToastUtil.showToast(getActivity(), "请输入要搜索的内容!");
                }
            }
        });

    }


    private Handler searchVideoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    VideoSearchListActivity.intentTo(getActivity(), msg.getData().getString("json"));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
