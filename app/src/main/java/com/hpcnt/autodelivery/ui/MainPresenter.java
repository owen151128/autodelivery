package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.util.StringUtil;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements MainContract.Presenter {
    private MainContract.View mView;
    private Build mBuild;
    private MainContract.STATE mState;
    private BuildFetcher mBuildFetcher;

    public MainPresenter(MainContract.View view) {
        mView = view;
        mBuildFetcher = new BuildFetcher(view);
    }

    @Override
    public void loadLatestBuild() {
        mState = MainContract.STATE.LOADING;
        mView.showButton(mState);
        mBuildFetcher.fetchBuildList("")
                .subscribe(s -> new LatestBuildFetchListener().onStringFetched(s),
                        throwable -> mView.showToast(throwable.toString()));
    }

    @Override
    public void downloadApk() {
        if (mState != MainContract.STATE.DOWNLOAD) return;
        if (mBuild.getApkName().equals("")) {
            setEditBuild(mBuild.getVersionName(), BuildEditContract.FLAG.APK);
            return;
        }
        mState = MainContract.STATE.DOWNLOADING;

        Observable.create((ObservableOnSubscribe<DownloadManager.Request>) e -> {
            Uri apkUri = Uri.parse(mBuild.getApkUrl());
            List<String> pathSegments = apkUri.getPathSegments();
            DownloadManager.Request request = new DownloadManager.Request(apkUri);
            request.setTitle(mBuild.getVersionName());
            request.setDescription(mBuild.getDate());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    mBuild.getVersionName() + pathSegments.get(pathSegments.size() - 1));
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
            e.onNext(request);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(request -> {
                    mView.showButton(mState);
                    mView.addDownloadRequest(request);
                });
    }

    @Override
    public void installApk() {
        if (mState != MainContract.STATE.INSTALL) return;
        String apkPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mBuild.getVersionName() + mBuild.getApkName();
        mView.showApkInstall(apkPath);
    }

    @Override
    public void onClickButton() {
        if (mState == MainContract.STATE.DOWNLOAD) {
            downloadApk();
        } else if (mState == MainContract.STATE.INSTALL) {
            installApk();
        }
    }

    @Override
    public void setEditBuild(String versionPath, BuildEditContract.FLAG flag) {
        mView.showEditDialog(versionPath, flag);
    }

    @Override
    public void setApkName(String apkName) {
        mBuild.setApkName(apkName);
        downloadApk();
    }

    @Override
    public void setEditedBuild(BuildList buildList, String versionName) {
        mBuild.setDate(buildList.get(0).getDate());
        if (!StringUtil.isDirectory(versionName))
            versionName += "/";
        selectBuild(buildList, versionName);
    }

    @Override
    public void stateSetting() {
        hasLastestFile().subscribe(hasFile -> {
            if (hasFile) {
                mState = MainContract.STATE.INSTALL;
                mView.showButton(mState);
            } else {
                mState = MainContract.STATE.DOWNLOAD;
                mView.showButton(mState);
            }
        });
    }

    private void selectBuild(BuildList buildList, String versionName) {
        String myAbi = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            myAbi = android.os.Build.SUPPORTED_ABIS[0] + "-qatest";
        } else {
            //noinspection deprecation
            myAbi = android.os.Build.CPU_ABI + "-qatest";
        }
        String apkName = "";
        boolean hasCorrectApk = false;
        for (int i = 0; i < buildList.size(); i++) {
            apkName = buildList.get(i).getVersionName();
            if (apkName.contains(myAbi)) {
                hasCorrectApk = true;
                break;
            }
        }

        if (hasCorrectApk)
            mBuild.setApkName(apkName);
        else
            mBuild.setApkName("");
        mBuild.setVersionName(versionName);
        mView.showLastestBuild(mBuild);

        stateSetting();
    }


    private class LatestBuildFetchListener {
        private StringBuilder mFullVersionName = new StringBuilder();

        public void onStringFetched(String response) {
            BuildList buildList = BuildList.fromHtml(response);
            mBuild = buildList.getLastestBuild();
            if (mBuild == null) {
                mView.showToast(R.string.message_wrong_access);
                return;
            }

            String versionName = mBuild.getVersionName();

            if (StringUtil.isDirectory(versionName)) {
                mFullVersionName.append(versionName);
                mBuildFetcher.fetchBuildList(mFullVersionName.toString())
                        .subscribe(this::onStringFetched,
                                throwable -> mView.showToast(throwable.toString()));
            } else {
                selectBuild(buildList, mFullVersionName.toString());
            }
        }
    }

    private Single<Boolean> hasLastestFile() {
        return Single.<Boolean>create(e -> {
            File buildFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mBuild.getVersionName());
            e.onSuccess(buildFile.exists());
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
