package com.hpcnt.autodelivery.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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

    public boolean isAvailableNetwork() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSSID = wifiInfo.getSSID();
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