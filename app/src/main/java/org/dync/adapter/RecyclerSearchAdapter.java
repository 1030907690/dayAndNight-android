package org.dync.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dync.bean.VideoSearch;
import org.dync.ijkplayer.R;
import org.dync.utils.GlobalConfig;
import org.dync.utils.ImageLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*https://blog.csdn.net/lmj623565791/article/details/45059587*/
public class RecyclerSearchAdapter extends RecyclerView.Adapter<RecyclerSearchAdapter.MyViewHolder> {

    private Context context;
    private List<VideoSearch> mDatas;
    /**
     * 事件
     **/
    private OnItemClickListener mOnItemClickListener;

    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍
    public RecyclerSearchAdapter(Context context, List<VideoSearch> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.video_search_list_item, parent, false));
        return holder;
    }


    public void showImageByAsyncTask(String url) {
        GlobalConfig.getInstance().executorService().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapFromUrl(url);
                Message msg = handler.obtainMessage();
                msg.obj = bitmap;
                msg.what = 0;
                handler.sendMessage(msg);
            }
        });
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


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    holder.iv.setImageBitmap(bitmap);
                    break;
                default:
                    break;
            }
        }
    };
    private MyViewHolder holder;

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv.setText(mDatas.get(position).getName());
        imageLoader.DisplayImage(mDatas.get(position).getPhoto() , holder.iv);
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

        TextView tv;
        ImageView iv;

        public MyViewHolder(View view) {
            super(view);
            tv = view.findViewById(R.id.video_list_item);
            iv = view.findViewById(R.id.video_list_image_item);
        }
    }
}
