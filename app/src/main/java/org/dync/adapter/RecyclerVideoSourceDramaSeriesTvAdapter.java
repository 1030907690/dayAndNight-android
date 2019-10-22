package org.dync.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.dync.bean.Video;
import org.dync.ijkplayer.R;

import java.util.List;

/*https://blog.csdn.net/lmj623565791/article/details/45059587*/
public class RecyclerVideoSourceDramaSeriesTvAdapter extends RecyclerView.Adapter<RecyclerVideoSourceDramaSeriesTvAdapter.MyViewHolder> {

    private Context context;
    private List<Video> mDatas;
    /**
     * 事件
     **/
    private OnItemClickListener mOnItemClickListener;


    public RecyclerVideoSourceDramaSeriesTvAdapter(Context context, List<Video> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.video_source_drama_series_list_item_tv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Video video = mDatas.get(position);
        holder.btn.setText(video.getName());
        holder.btn.setTag(video.getUrl());
        holder.tv.setText(video.getUrl());
        holder.tv.setVisibility(View.GONE);


        // item click
        if (mOnItemClickListener != null) {

            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.btn, position);
                }
            });

            // item long click
            holder.btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemClickListener.onItemLongClick(holder.btn, position);
                    return true;
                }
            });



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
        TextView tv;

        public MyViewHolder(View view) {
            super(view);
            btn = view.findViewById(R.id.video_source_drama_series_list_item_but_tv);
            tv = view.findViewById(R.id.video_source_drama_series_video_tv);
        }
    }
}
