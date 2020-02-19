package org.dync.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;


public class Utils {

    public static void applyPermission(Activity context) {

        /**
         * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int REQUEST_EXTERNAL_STORAGE = 1;
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                //验证是否许可权限
                ActivityCompat.requestPermissions(
                        context,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }

    public static Integer multiply(int v1, int v2) {
        BigDecimal b1 = new BigDecimal(Integer.toString(v1));
        BigDecimal b2 = new BigDecimal(Integer.toString(v2));
        return b1.multiply(b2).intValue();
    }


    public static int round(int v1, int v2) {
        if (v2 < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Integer.toString(v1));
        BigDecimal two = new BigDecimal(Integer.toString(v2));
        return one.divide(two, 2, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
                is.close();
                os.close();
            }
        } catch (Exception ex) {
        }
    }
}
