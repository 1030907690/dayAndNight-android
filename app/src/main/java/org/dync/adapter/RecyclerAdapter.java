/*
package org.dync.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import org.dync.bean.VideoSearch;
import org.dync.ijkplayer.R;

import java.net.URL;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter {
    private List<VideoSearch> models;

    public RecyclerAdapter(List<VideoSearch> models) {
        this.models = models;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView picture;
        private TextView title, text;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.picture);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        public ImageView getPicture() {
            return picture;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getText() {
            return text;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder vh = (ViewHolder) holder;
        //vh.getPicture().setImageResource(models.get(position).getPhoto());

        try {
            URL picUrl = new URL("http://www.605zy.cc/upload/vod/2019-08/15666429251.jpg");
            Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream());
            vh.getPicture().setImageBitmap(pngBM);
        }catch (Exception e){
            e.printStackTrace();
        }

        vh.getTitle().setText(models.get(position).getName());
        vh.getText().setText(models.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return models.size();
    }



    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }
}

*/
