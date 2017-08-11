package com.hpcnt.autodelivery.network;

import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.LifeCycleProvider;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BuildFetcher {
    private LifeCycleProvider mLifeCycleProvider;

    public BuildFetcher(LifeCycleProvider lifeCycleProvider) {
        mLifeCycleProvider = lifeCycleProvider;
    }

    public Single<String> fetchBuildList(String path) {
        return Single.<String>create(e -> {
            URL url = new URL(BaseApplication.BUILD_SERVER_URL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            String response = IOUtils.toString(inputStream, "UTF-8");
            e.onSuccess(response);
        })
                .subscribeOn(Schedulers.io())
                .compose(mLifeCycleProvider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
