package org.dync.bean;

import java.io.Serializable;
import java.util.List;

/***
 * 关于版本的控制信息
 * */
public class VersionUpdate implements Serializable {

    /**当前版本**/
    private String currentVersion;

    /** 下载地址 **/
    private String downloadUrl;

    /** 数据源**/
    private List<DataSource> dataSource;

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public List<DataSource> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<DataSource> dataSource) {
        this.dataSource = dataSource;
    }
}
