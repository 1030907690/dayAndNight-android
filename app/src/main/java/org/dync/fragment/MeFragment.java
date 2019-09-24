package org.dync.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.dync.ijkplayer.R;


/*
* zhouzhongqing
* 2019年9月24日17:40:56
* Me 页面
* */
public class MeFragment extends Fragment {


    private final String TAG = "MeFragment";

    private EditText searchEditText;

    private Button btnSearch;

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
        View view = inflater.inflate(R.layout.video_search, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.get("name").toString();
        }



    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
