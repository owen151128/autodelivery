package com.hpcnt.autodelivery.model;

import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.BaseApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Build {
    @NonNull
    private String versionName = "";
    @NonNull
    private String date = "";
    @NonNull
    private String apkName = "";
    @NonNull
    private List<String> separateName = new ArrayList<>();

    public void setVersionName(@NonNull String versionName) {
        this.versionName = versionName;
        separateName = Arrays.asList(versionName.split("[^\\p{Alnum}]"));
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public void setApkName(@NonNull String apkName) {
        this.apkName = apkName;
    }

    @NonNull
    public String getVersionName() {
        return versionName;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getApkName() {
        return apkName;
    }

    @NonNull
    public String getApkUrl() {
        return BaseApplication.BUILD_SERVER_URL + versionName + apkName;
    }

    @NonNull
    public List<String> getSeparateName() {
        return separateName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Build build = (Build) o;

        if (!versionName.equals(build.versionName)) return false;
        if (!date.equals(build.date)) return false;
        return apkName.equals(build.apkName);

    }

    @Override
    public int hashCode() {
        int result = versionName.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + apkName.hashCode();
        return result;
    }
}
