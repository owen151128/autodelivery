package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;

interface MainContract {

    enum STATE {
        DOWNLOAD, LOADING, DOWNLOADING, INSTALL
    }

    int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    interface View extends LifeCycleProvider {

        void showLastestBuild(Build lastestBuild);

        void showToast(String response);

        void showToast(int resoureceId);

        void showButton(STATE state);

        void addDownloadRequest(DownloadManager.Request request);

        void showApkInstall(String apkPath);

        void showEditDialog(String versionPath, BuildEditContract.FLAG flag);
    }

    interface Presenter {

        void loadLatestBuild();

        void downloadApk();

        void stateSetting();

        void installApk();

        void onClickButton();

        void setEditedBuild(BuildList buildList, String versionName);

        void setEditBuild(String versionPath, BuildEditContract.FLAG flag);

        void setApkName(String apkName);

        void setBuildFetcher(BuildFetcher buildFetcher);
    }
}
