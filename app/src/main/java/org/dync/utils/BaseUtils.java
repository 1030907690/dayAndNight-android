package org.dync.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by zhouzhongqing on 2017/7/24.
 * 基础工具类
 */

public class BaseUtils {


    /**
     * 返回当前程序版本名  build.gradle
     */
    public  static String getAppVersionName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            String versionName = pi.versionName;
            int versioncode = pi.versionCode;
            Log.d("versionName:---" + versionName, "versioncode:---" + versioncode);
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
            return versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return null;
    }

    /*******
     * 2017年7月24日10:23:10
     * zhouzhongqing
     * 判断网络是否可用  可用 true 不可用false
     * ******/
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /*
     * 使用WIFI时，获取本机IP地址
     * @param mContext
     * @return
     * 2017年7月24日12:28:21
     * zhouzhongqing
     */
    public static String getWIFILocalIpAdress(Context mContext) {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }
    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }
}