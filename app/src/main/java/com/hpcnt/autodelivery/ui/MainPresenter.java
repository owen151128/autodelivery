package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.util.StringUtil;

import java.io.File;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class MainPresenter implements MainContract.Presenter {

    private MainContract.View mView;
    private MainContract.State mState;
    @NonNull
    private Build mBuild = new Build();

    MainPresenter(MainContract.View view) {
        mView = view;
    }

    @Override
    public void loadLatestBuild(BuildFetcher fetcher) {
        setState(MainContract.State.LOADING);
        executeBuildFetch(fetcher, "", s -> new LatestBuildFetchListener().onStringFetched(s, fetcher));
    }

    @Override
    public void downloadApk() {
        if (mState != MainContract.State.DOWNLOAD) return;
        if (mBuild.getApkName().equals("")) {
            editCurrentBuild(mBuild.getVersionName(), BuildEditContract.FLAG.APK);
            return;
        }
        setState(MainContract.State.DOWNLOADING);
        Uri apkUri = Uri.parse(mBuild.getApkUrl());
        List<String> pathSegments = apkUri.getPathSegments();
        DownloadManager.Request request = new DownloadManager.Request(apkUri);
        request.setTitle(mBuild.getVersionName())
                .setDescription(mBuild.getDate())
                .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        mBuild.getVersionName() + pathSegments.get(pathSegments.size() - 1));
        mView.addDownloadRequest(request);
    }

    @Override
    public void installApk() {
        if (mState != MainContract.State.INSTALL) return;
        mView.showApkInstall(mBuild.getApkDownloadedPath());
    }

    @Override
    public void onClickButton() {
        if (mState == MainContract.State.DOWNLOAD) {
            downloadApk();
        } else if (mState == MainContract.State.INSTALL) {
            installApk();
        }
    }

    @Override
    public void editCurrentBuild(String versionPath, BuildEditContract.FLAG flag) {
        mView.showEditDialog(versionPath, flag);
    }

    @Override
    public void setApkName(String apkName) {
        mBuild.setApkName(apkName);
        downloadApk();
    }

    @Override
    public void selectMyAbiBuild(BuildList buildList, String versionName) {
        if (!StringUtil.isDirectory(versionName))
            versionName += "/";
        setupMyAbiBuild(buildList, versionName);
    }

    @Override
    public void stateSetting() {
        hasLastestFile().subscribe(hasFile -> {
            if (hasFile) {
                setState(MainContract.State.INSTALL);
            } else {
                setState(MainContract.State.DOWNLOAD);
            }
        });
    }

    @NonNull
    Build getMyAbiBuild(ABIWrapper abiWrapper, BuildList buildList, String versionName) {
        String myAbi = abiWrapper.getABI() + "-qatest";
        String apkName = "";
        String date = "";
        boolean hasCorrectApk = false;
        for (int i = 0; i < buildList.size(); i++) {
            apkName = buildList.get(i).getVersionName();
            date = buildList.get(i).getDate();
            if (apkName.contains(myAbi)) {
                hasCorrectApk = true;
                break;
            }
        }

        if (!hasCorrectApk)
            apkName = "";

        return new Build(versionName, date, apkName);
    }

    private class LatestBuildFetchListener {

        private StringBuilder mFullVersionName = new StringBuilder();

        void onStringFetched(String response, BuildFetcher fetcher) {
            BuildList buildList = BuildList.fromHtml(response);
            setBuild(buildList.getLatestBuild());

            String versionName = mBuild.getVersionName();

            if (StringUtil.isDirectory(versionName)) {
                mFullVersionName.append(versionName);
                executeBuildFetch(fetcher, mFullVersionName.toString(), s -> this.onStringFetched(s, fetcher));
            } else {
                setupMyAbiBuild(buildList, mFullVersionName.toString());
            }
        }
    }

    private void executeBuildFetch(BuildFetcher fetcher, String path, Consumer<String> consumer) {
        fetcher.fetchBuildList(path)
                .subscribe(consumer,
                        throwable -> {
                            setState(MainContract.State.FAIL);
                            setBuild(Build.EMPTY);
                        });
    }

    private void setupMyAbiBuild(BuildList buildList, String versionName) {
        setBuild(getMyAbiBuild(new ABIWrapper(), buildList, versionName));
        mView.showLastestBuild(mBuild);
        stateSetting();
    }

    private Single<Boolean> hasLastestFile() {
        return Single.<Boolean>create(e -> {
            File buildFile = new File(mBuild.getApkDownloadedPath());
            e.onSuccess(buildFile.exists());
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void setState(MainContract.State state) {
        mState = state;
        mView.showButton(state);
    }

    MainContract.State getState() {
        return mState;
    }

    Build getBuild() {
        return mBuild;
    }

    void setBuild(@NonNull Build build) {
        mBuild = build;
    }

    static class ABIWrapper {

        String getABI() {
            String myAbi;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                myAbi = android.os.Build.SUPPORTED_ABIS[0];
            } else {
                //noinspection deprecation
                myAbi = android.os.Build.CPU_ABI;
            }
            return myAbi;
        }
    }
}
