package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;

import com.hpcnt.autodelivery.model.Build;

public interface MainContract {

    enum STATE {
        DOWNLOAD, LOADING, DOWNLOADING, INSTALL
    }

    int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    interface View {

        void showLastestBuild(Build lastestBuild);

        void showToast(String response);

        void showButton(STATE state);

        void addDownloadRequest(DownloadManager.Request request);

        void showApkInstall(String apkPath);
    }

    interface Presenter {

        void loadLastestBuild();

        void downloadApk();

        void downloadComplete();

        void installApk();

        void onClickButton();
    }
}
