package com.hpcnt.autodelivery.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public class NetworkStateUtil {
    private static NetworkStateUtil instance;
    private static final String[] AVAILABLE_SSID = {"\"HyperconnectTest\"", "\"HyperconnectBiz\""};
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;

    private NetworkStateUtil(ConnectivityManager connectivityManager, WifiManager wifiManager) {
        this.connectivityManager = connectivityManager;
        this.wifiManager = wifiManager;
    }

    public static synchronized NetworkStateUtil getInstance(ConnectivityManager connectivityManager, WifiManager wifiManager) {
        if (instance == null) {
            instance = new NetworkStateUtil(connectivityManager, wifiManager);
        }
        return instance;
    }

    public boolean isNetworkConnected() {
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    /**
     * 4.2.2(Jelly_Bean_MR1, API 17) 미만 기기 에서는 wifiInfo.getSSID() 시에 SSID 에 " 가 없이 return 된다.
     * 따라서 android.os.Build.VERSION.SDK_INT를 검사해서 Build.VERSION_CODES.JELLY_BEAN_MR1(API 17)
     * 미만 일 경우에는 currentSSID 시작 과 끝에 " 를 넣어주는 작업이 필요하다.
     */
    public boolean isAvailableNetwork() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSSID = wifiInfo.getSSID();

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            currentSSID = "\"" + currentSSID + "\"";
        }

        for (String s : AVAILABLE_SSID) {
            if (currentSSID.equals(s))
                return true;
        }
        return false;
    }

    public void setWifiEnable() {
        wifiManager.setWifiEnabled(true);
    }
}