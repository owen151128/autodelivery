package com.hpcnt.autodelivery.ui;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;

import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.StringFetchListener;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

import java.io.File;
import java.util.List;

public class MainPresenter implements MainContract.Presenter {
    private MainContract.View mView;
    private Build mLastestBuild;

    public MainPresenter(MainContract.View view) {
        mView = view;
    }

    @Override
    public void loadLastestBuild() {
        mView.showLoading();
        BuildFetcher buildFetcher = new BuildFetcher();
        buildFetcher.fetchBuildList(new LastestBuildFetchListener(), "");
    }

    @Override
    public void downloadApk() {
        Uri apkUri = Uri.parse(mLastestBuild.getApkUrl());
        List<String> pathSegments = apkUri.getPathSegments();
        DownloadManager.Request request = new DownloadManager.Request(apkUri);
        request.setTitle(mLastestBuild.getVersionName());
        request.setDescription(mLastestBuild.getDate());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                mLastestBuild.getVersionName() + pathSegments.get(pathSegments.size() - 1));
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
        mView.showDownloading();
        mView.addDownloadRequest(request);
    }

    @Override
    public void downloadComplete() {
        mView.showInstall();
    }

    private class LastestBuildFetchListener implements StringFetchListener {
        private StringBuilder mFullVersionName = new StringBuilder();

        @Override
        public void onStringFetched(String response) {
            BuildList buildList = BuildList.fromHtml(response);
            mLastestBuild = buildList.getLastestBuild();
            if (mLastestBuild == null) {
                mView.showToast("잘못된 접근");
                return;
            }

            String versionName = mLastestBuild.getVersionName();
            mLastestBuild.setVersionName(mFullVersionName.toString());
            mFullVersionName.append(versionName);

            // 마지막 문자가 '/'라면 즉, 버전 이름이 디렉토리를 나타낸다면
            if (versionName.charAt(versionName.length() - 1) == '/') {
                BuildFetcher buildFetcher = new BuildFetcher();
                buildFetcher.fetchBuildList(this, mFullVersionName.toString());
            } else {
                mLastestBuild.setApkUrl(BaseApplication.BUILD_SERVER_URL + mFullVersionName.toString());
                mView.showLastestBuild(mLastestBuild);

                File buildFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + mLastestBuild.getVersionName());
                if (buildFile.exists()) {
                    mView.showInstall();
                } else {
                    mView.showDownload();
                }
            }
        }

        @Override
        public void onStringError(String response) {
            mView.showToast(response);
        }
    }
}
