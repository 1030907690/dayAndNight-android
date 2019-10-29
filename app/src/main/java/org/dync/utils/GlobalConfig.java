package org.dync.utils;


import android.content.SharedPreferences;

import org.dync.bean.VersionUpdate;
import org.dync.datasourcestrategy.IDataSourceStrategy;

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
    //public final String INIT_URL = "https://github.com/1030907690/dayAndNight-android/raw/master/VersionManager.json";

    /** 备用初始化地址**/
    public final String BACKUP_INIT_URL = "https://github.com/1030907690/dayAndNight-android/raw/master/VersionManager.json";

    /** 默认选择第一个视频源**/
    private int DEFAULT_DATA_SOURCE_INDEX = 0;

    public final String remoteServer [] = {BACKUP_INIT_URL,BACKUP_INIT_URL};

    /** 重试次数**/
    public static volatile int reCount = 0;

    private VersionUpdate versionUpdate;

    private static volatile GlobalConfig globalConfig;

    private IDataSourceStrategy dataSourceStrategy;


    private SharedPreferences sharedPreferences;



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
        setOptionDataSourceStrategy(DEFAULT_DATA_SOURCE_INDEX);
    }


    /***
     * 选择DataSourceStrategy策略  默认为0
     * */
   public void setOptionDataSourceStrategy(int option){
        //设置DataSourceStrategy
        try {
            String implName = versionUpdate.getDataSource().get(option).getKey() + "DataSourceHandle";
            this.dataSourceStrategy = (IDataSourceStrategy) Class.forName("org.dync.datasourcestrategy.strategy." + implName).newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ExecutorService executorService(){
        return executorService;
    }

    public String[] getRemoteServer() {
        return remoteServer;
    }


    public IDataSourceStrategy getDataSourceStrategy() {
        return dataSourceStrategy;
    }

    public void setDataSourceStrategy(IDataSourceStrategy dataSourceStrategy) {
        this.dataSourceStrategy = dataSourceStrategy;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }
}
