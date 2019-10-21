package org.dync.utils;


import org.dync.bean.VersionUpdate;

import java.lang.reflect.Executable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/***
 * 全局配置
 * */
public class GlobalConfig {

    /** 首先初始化地址 **/
    public final String INIT_URL = "https://gitee.com/apple_1030907690/weiXin/raw/master/VersionManager.json";

    /** 备用初始化地址**/
    public final String BACKUP_INIT_URL = "https://github.com/1030907690/dayAndNight-android/raw/master/VersionManager.json";


    public final String remoteServer [] = {INIT_URL,BACKUP_INIT_URL};

    /** 重试次数**/
    public static volatile int reCount = 0;

    private VersionUpdate versionUpdate;

    private static volatile GlobalConfig globalConfig;

    private ExecutorService executorService;
    public static GlobalConfig getInstance() {
        if (null == globalConfig) {
            synchronized (GlobalConfig.class) {
                if (null == globalConfig) {
                    globalConfig = new GlobalConfig();

                }
            }
        }
        return globalConfig;
    }

    private GlobalConfig() {
        int processors = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolExecutor(processors * 2, processors * 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(processors * 100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public VersionUpdate getVersionUpdate() {
        return versionUpdate;
    }

    public void setVersionUpdate(VersionUpdate versionUpdate) {
        this.versionUpdate = versionUpdate;
    }

    public ExecutorService executorService(){
        return executorService;
    }

    public String[] getRemoteServer() {
        return remoteServer;
    }
}
