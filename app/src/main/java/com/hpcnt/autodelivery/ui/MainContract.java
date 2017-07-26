package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

public interface MainContract {

    enum STATE {
        DOWNLOAD, LOADING, DOWNLOADING, INSTALL
    }

    int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    interface View extends LifeCycleProvider{

        void showLastestBuild(Build lastestBuild);

        void showToast(String response);

        void showToast(int resoureceId);

        void showButton(STATE state);

        void addDownloadRequest(DownloadManager.Request request);

        void showApkInstall(String apkPath);

        void showEditDialog();
    }

    interface Presenter {

        void loadLatestBuild();

        void downloadApk();

        void stateSetting();

        void installApk();

        void onClickButton();

        void setEditBuild();

        void setEditedBuild(BuildList buildList, String versionName);
    }
}
