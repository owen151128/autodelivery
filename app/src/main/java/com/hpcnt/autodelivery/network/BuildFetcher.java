package com.hpcnt.autodelivery.network;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.hpcnt.autodelivery.BaseApplication;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class BuildFetcher {
    private static final String TAG = BuildFetcher.class.getSimpleName();
    private RequestQueue mQueue = BaseApplication.getRequestQueue();

    public Observable<String> fetchBuildList(String path) {
        return Observable.create((ObservableOnSubscribe<String>) e -> {
            URL url = new URL(BaseApplication.BUILD_SERVER_URL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            String response = IOUtils.toString(inputStream, "UTF-8");
            e.onNext(response);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
