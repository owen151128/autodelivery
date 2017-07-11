package com.hpcnt.autodelivery.model;

public class Build {
    private String versionName = "";
    private String date = "";
    private String apkUrl = "";

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDate() {
        return date;
    }

    public String getApkUrl() {
        return apkUrl;
    }
}
