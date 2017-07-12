package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;

import com.hpcnt.autodelivery.model.Build;

public interface MainContract {

    int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    interface View {

        void showLastestBuild(Build lastestBuild);

        void showToast(String response);

        void showDownload();

        void showLoading();

        void showDownloading();

        void addDownloadRequest(DownloadManager.Request request);

        void showInstall();
    }

    interface Presenter {

        void loadLastestBuild();

        void downloadApk();

        void downloadComplete();
    }
}
