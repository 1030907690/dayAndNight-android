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
}