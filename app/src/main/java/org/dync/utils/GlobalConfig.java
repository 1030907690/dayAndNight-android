package org.dync.utils;


import org.dync.bean.VersionUpdate;

/***
 * 全局配置
 * */
public class GlobalConfig {

    private VersionUpdate versionUpdate;

    private static volatile GlobalConfig globalConfig;

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

    }

    public VersionUpdate getVersionUpdate() {
        return versionUpdate;
    }

    public void setVersionUpdate(VersionUpdate versionUpdate) {
        this.versionUpdate = versionUpdate;
    }
}
