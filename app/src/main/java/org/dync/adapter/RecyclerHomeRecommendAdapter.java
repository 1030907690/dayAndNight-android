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
import android.widget.ImageButton;
import android.widget.TextView;

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
public class RecyclerHomeRecommendAdapter extends RecyclerView.Adapter<RecyclerHomeRecommendAdapter.MyViewHolder> {

    private Context context;
    private List<VideoSearch> mDatas;
    /**
     * 事件
     **/
    private OnItemClickListener mOnItemClickListener;

    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public RecyclerHomeRecommendAdapter(Context context, List<VideoSearch> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.home_recommend_item, parent, false));
        return holder;
    }


    public Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL mUrl = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 异步加载图片
     */
    class DownLoadTask extends AsyncTask<String, Void, BitmapDrawable> {
        private ImageButton mImageView;
        String url;

        public DownLoadTask(ImageButton imageView) {
            mImageView = imageView;
        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            url = params[0];
            Bitmap bitmap = downLoadBitmap(url);
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            return drawable;
        }

        private Bitmap downLoadBitmap(String url) {
            Bitmap bitmap = null;
            try {
                OkHttpClient client = new OkHttpClient();
                if(null != url){
                    //Log.d("recyclerAdapter", url);
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            super.onPostExecute(drawable);

            if (mImageView != null && drawable != null) {
                mImageView.setImageDrawable(drawable);
            }
        }
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        VideoSearch videoSearch = mDatas.get(position);
        holder.tv.setText(videoSearch.getName());
        //执行下载操作
        DownLoadTask task = new DownLoadTask(holder.imageButton);
        holder.imageButton.setTag(videoSearch.getUrl());
        task.execute(videoSearch.getPhoto());

        // item click
        if (mOnItemClickListener != null) {


            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.imageButton, position);
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

        TextView tv;
        ImageButton imageButton;

        public MyViewHolder(View view) {
            super(view);
            tv = view.findViewById(R.id.video_name);
            imageButton = view.findViewById(R.id.video_image_btn);
        }

    }


}
