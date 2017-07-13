package com.hpcnt.autodelivery.model;

import com.hpcnt.autodelivery.BaseApplication;

public class Build {
    private String versionName = "";
    private String date = "";
    private String apkName = "";

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDate() {
        return date;
    }

    public String getApkName() {
        return apkName;
    }

    public String getApkUrl() {
        return BaseApplication.BUILD_SERVER_URL + versionName + apkName;
    }
}
