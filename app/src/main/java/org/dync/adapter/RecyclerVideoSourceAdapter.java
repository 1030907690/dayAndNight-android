package org.dync.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.dync.bean.VideoGroup;
import org.dync.bean.VideoSearch;
import org.dync.ijkplayer.R;
import org.dync.utils.ImageLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*https://blog.csdn.net/lmj623565791/article/details/45059587*/
public class RecyclerVideoSourceAdapter extends RecyclerView.Adapter<RecyclerVideoSourceAdapter.MyViewHolder> {

    private Context context;
    private List<VideoGroup> mDatas;
    /**
     * 事件
     **/
    private OnItemClickListener mOnItemClickListener;


    public RecyclerVideoSourceAdapter(Context context, List<VideoGroup> mDatas) {
        this.context = context;
        this.mDatas = mDatas;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.video_source_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.btn.setText(mDatas.get(position).getGroup());

        // item click
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        // item long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOnItemClickListener.onItemLongClick(holder.itemView, position);
                return true;
            }
        });

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        Button btn;

        public MyViewHolder(View view) {
            super(view);
            btn = view.findViewById(R.id.video_source_list_item_but);
        }
    }
}
