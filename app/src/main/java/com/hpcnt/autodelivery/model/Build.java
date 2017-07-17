package com.hpcnt.autodelivery.model;

import com.hpcnt.autodelivery.BaseApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Build {
    private String versionName = "";
    private String date = "";
    private String apkName = "";
    private List<String> separateName = new ArrayList<>();

    public void setVersionName(String versionName) {
        this.versionName = versionName;
        separateName = Arrays.asList(versionName.split("[^\\p{Alnum}]"));
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

    public List<String> getSeparateName() {
        return separateName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Build)) return false;

        Build build = (Build) o;

        if (versionName != null ? !versionName.equals(build.versionName) : build.versionName != null)
            return false;
        if (date != null ? !date.equals(build.date) : build.date != null) return false;
        return apkName != null ? apkName.equals(build.apkName) : build.apkName == null;

    }

    @Override
    public int hashCode() {
        int result = versionName != null ? versionName.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (apkName != null ? apkName.hashCode() : 0);
        return result;
    }
}
