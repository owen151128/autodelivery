package com.hpcnt.autodelivery.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class GlobalConfig {
    private static GlobalConfig instance;

    private GlobalConfig() {
    }

    public static synchronized GlobalConfig getInstance() {
        if (instance == null)
            instance = new GlobalConfig();
        return instance;
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("autodelivery", Activity.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public void putInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).commit();
    }

    public int getInteger(Context context, String key) {
        return getSharedPreferences(context).getInt(key, 0);
    }

    public void putBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public boolean getBoolean(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }

    public void putString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();
    }

    public String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }
}