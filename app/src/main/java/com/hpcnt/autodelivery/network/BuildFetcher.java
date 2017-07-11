package com.hpcnt.autodelivery.network;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.StringFetchListener;

public class BuildFetcher {
    private static final String TAG = BuildFetcher.class.getSimpleName();
    private RequestQueue mQueue = BaseApplication.getRequestQueue();

    public void fetchBuildList(StringFetchListener buildFetcherListener) {
        StringRequest request = new StringRequest(Method.GET, BaseApplication.BUILD_SERVER_URL, response -> {
            buildFetcherListener.onStringFetched(response);
        }, error -> {
            buildFetcherListener.onStringError(error.toString());
        });

        mQueue.add(request);
    }
}
