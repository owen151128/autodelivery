package com.hpcnt.autodelivery.network;

import com.android.volley.RequestQueue;
import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.ui.MainContract;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BuildFetcher {
    private static final String TAG = BuildFetcher.class.getSimpleName();
    private RequestQueue mQueue = BaseApplication.getRequestQueue();
    private LifeCycleProvider mLifeCycleProvider;

    public BuildFetcher(LifeCycleProvider lifeCycleProvider) {
        mLifeCycleProvider = lifeCycleProvider;
    }

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
                .compose(mLifeCycleProvider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
