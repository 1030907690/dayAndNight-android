package org.dync.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 异步加载图片
 */
class DownLoadTask extends AsyncTask<String ,Void, BitmapDrawable> {
    private ImageView mImageView;
    String url;
    private Context context;
    public DownLoadTask(ImageView imageView, Context context){
        mImageView = imageView;
        this.context = context;
    }
    @Override
    protected BitmapDrawable doInBackground(String... params) {
        url = params[0];
        Bitmap bitmap = downLoadBitmap(url);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(),bitmap);
        return  drawable;
    }

    private Bitmap downLoadBitmap(String url) {
        Bitmap bitmap = null;
        OkHttpClient client = new OkHttpClient();
        Log.d("recyclerAdapter",url);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            bitmap = BitmapFactory.decodeStream(response.body().byteStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(BitmapDrawable drawable) {
        super.onPostExecute(drawable);

        if ( mImageView != null && drawable != null){
            mImageView.setImageDrawable(drawable);
        }
    }
}
