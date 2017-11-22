package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.util.ABIWrapper;
import com.hpcnt.autodelivery.util.LogWrapper;
import com.hpcnt.autodelivery.util.StringUtil;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class MainPresenter implements MainContract.Presenter {

    private MainContract.View mView;
    private MainContract.State mState;
    private BuildEditContract.FLAG currentFlag;
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

    /**
     * 다운로드 요청 시 setDestinationInExternalPublicDir 메소드 에서 subPath parameter 에
     * Build.java 에서 추가한 getDownloadVersionNamePath를 사용하도록 한다.
     */
    @Override
    public void downloadApk() {
        if (mState != MainContract.State.DOWNLOAD) return;
        if (mBuild.getApkName().equals("")) {
            editCurrentBuild(mBuild.getVersionName(), BuildEditContract.FLAG.APK);
            return;
        }
        setState(MainContract.State.DOWNLOADING);
        Uri apkUri = Uri.parse(mBuild.getApkUrl());
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH.mm.ss", Locale.getDefault());
        List<String> pathSegments = apkUri.getPathSegments();
        DownloadManager.Request request = new DownloadManager.Request(apkUri);
        request.setTitle(mBuild.getVersionName())
                .setDescription(mBuild.getDate())
                .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (currentFlag == BuildEditContract.FLAG.MASTER) {
            LogWrapper.getInstance().saveUrlAndPath(dateFormat.format(timestamp),
                    apkUri.toString(),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + File.separator + mBuild.getMasterDownloadVersionNamePath()
                            + pathSegments.get(pathSegments.size() - 1));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    mBuild.getMasterDownloadVersionNamePath() + pathSegments.get(pathSegments.size() - 1));
        } else {
            LogWrapper.getInstance().saveUrlAndPath(dateFormat.format(timestamp),
                    apkUri.toString(),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + File.separator + mBuild.getDownloadVersionNamePath()
                            + pathSegments.get(pathSegments.size() - 1));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    mBuild.getDownloadVersionNamePath() + pathSegments.get(pathSegments.size() - 1));
        }
        mView.addDownloadRequest(request);
    }

    @Override
    public void setCurrentFlag(BuildEditContract.FLAG flag) {
        this.currentFlag = flag;
    }

    @Override
    public void installApk() {
        if (mState != MainContract.State.INSTALL) return;
        if (currentFlag == BuildEditContract.FLAG.MASTER) {
            mView.showApkInstall(mBuild.getMasterApkDownloadedPath());
        } else {
            mView.showApkInstall(mBuild.getApkDownloadedPath());
        }
    }

    @Override
    public void onClickButton(String viewText) {
        if (mState == MainContract.State.DOWNLOAD) {
            LogWrapper.getInstance().saveTextViewLabel(viewText);
            downloadApk();
        } else if (mState == MainContract.State.INSTALL) {
            LogWrapper.getInstance().saveInstallButtonLog(viewText);
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
        hasLastestFile().subscribe(hasFile -> {
            if (hasFile) {
                setState(MainContract.State.INSTALL);
            } else {
                setState(MainContract.State.DOWNLOAD);
                downloadApk();
            }
        });
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
            File buildFile;
            if (currentFlag == BuildEditContract.FLAG.MASTER) {
                buildFile = new File(mBuild.getMasterApkDownloadedPath());
            } else {
                buildFile = new File(mBuild.getApkDownloadedPath());
            }
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

    @Override
    public void setBuild(@NonNull Build build) {
        mBuild = build;
    }
}
