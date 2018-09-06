package com.hpcnt.autodelivery;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {
    private static String buildServerUrl = "http://server.owens.kr/apk/azar/";
    private static RequestQueue mQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static String getBuildServerUrl() {
        return buildServerUrl;
    }

    public static boolean setNormalMode() {
        buildServerUrl = "http://server.owens.kr:8081/apk/azar/";
        return true;
    }

    public static boolean setMasterBranchMode() {
        buildServerUrl = "http://server.owens.kr:8081/apk/azar/master/";
        return true;
    }

    public static RequestQueue getRequestQueue() {
        return mQueue;
    }
}
