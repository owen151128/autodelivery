package com.hpcnt.autodelivery.util;

import android.util.Pair;

import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;

public class CurrentBuildWrapper {
    public enum FLAG {
        NONE,   //  값이 아무것도 셋팅 되어 있지 않을 때
        ABI,    //  ABI 빌드인 경우 로 buildList 와 versionName 이 필요할때 사용한다.
        APK,    //  APK 인 경우 로 apkName 이 필요할때 사용한다.
        BUILD   //  BUILD 인 경우 로 BUILD 모델을 통째로 받아온다.
    }

    private static CurrentBuildWrapper instance;
    private FLAG flag;
    private BuildList buildList;
    private String versionName;
    private String apkName;
    private Build build;

    private CurrentBuildWrapper() {
        flag = FLAG.NONE;
    }

    public static synchronized CurrentBuildWrapper getInstance() {
        if (instance == null)
            instance = new CurrentBuildWrapper();
        return instance;
    }

    public FLAG getFlag() {
        return this.flag;
    }

    public Pair<BuildList, String> getAbiBuild() {
        return new Pair<>(this.buildList, this.versionName);
    }

    public String getApkName() {
        return this.apkName;
    }

    public Build getBuild() {
        return build;
    }

    public void setAbiBuild(FLAG flag, BuildList buildList, String versionName) {
        this.flag = flag;
        this.buildList = buildList;
        this.versionName = versionName;
    }

    public void setApkName(FLAG flag, String apkName) {
        this.flag = flag;
        this.apkName = apkName;
    }

    public void setBuild(FLAG flag, Build build) {
        this.flag = flag;
        this.build = build;
    }
}