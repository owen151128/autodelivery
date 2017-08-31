package com.hpcnt.autodelivery.util;

public class ABIWrapper {

    public String getABI() {
        String myAbi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            myAbi = android.os.Build.SUPPORTED_ABIS[0];
        } else {
            //noinspection deprecation
            myAbi = android.os.Build.CPU_ABI;
        }
        return myAbi;
    }
}