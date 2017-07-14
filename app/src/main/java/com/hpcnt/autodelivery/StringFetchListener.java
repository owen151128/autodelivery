package com.hpcnt.autodelivery;

public interface StringFetchListener {

    void onStringFetched(String response);

    void onStringError(String response);
}
