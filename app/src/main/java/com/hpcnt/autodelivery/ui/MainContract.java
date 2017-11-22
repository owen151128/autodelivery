package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.content.DialogInterface;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;

interface MainContract {

    enum State {
        DOWNLOAD, LOADING, DOWNLOADING, INSTALL, FAIL
    }

    int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    interface View extends LifeCycleProvider {

        void showLastestBuild(Build lastestBuild);

        void makeDialog(String title, String message,
                        boolean isAlert, boolean cancelable, android.view.View view,
                        DialogInterface.OnClickListener onYesListener,
                        DialogInterface.OnClickListener onNoListener);

        void showToast(String response);

        void showToast(int resoureceId);

        void showButton(State state);

        void addDownloadRequest(DownloadManager.Request request);

        void showApkInstall(String apkPath);

        void showEditDialog(String versionPath, BuildEditContract.FLAG flag);
    }

    interface Presenter {

        void loadLatestBuild(BuildFetcher fetcher);

        void downloadApk();

        void setCurrentFlag(BuildEditContract.FLAG flag);

        void stateSetting();

        void installApk();

        void onClickButton(String viewText);

        void selectMyAbiBuild(BuildList buildList, String versionName);

        void editCurrentBuild(String versionPath, BuildEditContract.FLAG flag);

        void setApkName(String apkName);

        void setState(State state);

        void setBuild(Build build);
    }
}
