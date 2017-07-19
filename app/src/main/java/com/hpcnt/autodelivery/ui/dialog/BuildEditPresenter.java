package com.hpcnt.autodelivery.ui.dialog;

import android.os.Bundle;

import com.hpcnt.autodelivery.StringFetchListener;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class BuildEditPresenter implements BuildEditContract.Presenter {
    private static final String TAG = BuildEditPresenter.class.getSimpleName();

    private BuildEditContract.View mView;
    private BuildEditAdapterContract.View mAdapterView;
    private BuildEditAdapterContract.Model mAdapterModel;

    private BuildList mBuildList;
    private BuildEditContract.FLAG mFlag;

    BuildEditPresenter(BuildEditContract.View view, Bundle arguments) {
        mView = view;
        mFlag = (BuildEditContract.FLAG) arguments.getSerializable(BuildEditContract.KEY_FLAG);
    }

    @Override
    public void setList(BuildEditAdapter adapter) {
        mAdapterView = adapter;
        mAdapterModel = adapter;
        mView.setList(adapter);
    }

    @Override
    public void loadBuildList(String versionPath) {
        BuildFetcher fetcher = new BuildFetcher();
        mAdapterModel.setSelectedVersion(versionPath);
        fetcher.fetchBuildList(nextBuildListFetchListener, versionPath);
    }

    @Override
    public void onItemClick(String currentVersion) {
        Build build = new Build();
        build.setVersionName(currentVersion);
        int separateSize = build.getSeparateName().size();

        List<String> separateName = new ArrayList<>();
        separateName.addAll(build.getSeparateName());
        setVersionData(new StringBuilder(currentVersion), separateName, separateSize);
    }

    private void setVersionData(StringBuilder versionTitle, List<String> separateName, int index) {
        Set<String> versionSet = mBuildList.getVersionSet(separateName, index);
        if (versionSet.size() == 1) {
            for (String version : versionSet) {
                if (versionTitle.length() != 0)
                    versionTitle.append(".");
                versionTitle.append(version);
                separateName.add(version);
            }

            setVersionData(versionTitle, separateName, index + 1);
        } else if (versionSet.size() > 1) {
            List<String> versionList = new ArrayList<>();
            for (String version : versionSet)
                versionList.add(versionTitle + "." + version);

            mAdapterModel.setList(versionList);
            mAdapterModel.setSelectedVersion(versionTitle.toString());
            mAdapterView.refresh();
            mView.showVersionTitle(versionTitle.toString());
        } else {
            if (mFlag == BuildEditContract.FLAG.APK) {
                mView.showOnDismiss(versionTitle.toString());
                return;
            }
            String version = mBuildList.get(versionTitle.toString()).getVersionName();
            mAdapterModel.setSelectedVersion(version);
            BuildFetcher fetcher = new BuildFetcher();
            fetcher.fetchBuildList(nextBuildListFetchListener, version);
        }
    }

    private StringFetchListener nextBuildListFetchListener = new StringFetchListener() {

        @Override
        public void onStringFetched(String response) {
            String selectedVersion = mAdapterModel.getSelectedVersion();
            if (selectedVersion.equals("")) {
                mBuildList = BuildList.fromHtml(response);
                setVersionData(new StringBuilder(), new ArrayList<>(), 0);
                return;
            }

            BuildList buildList = BuildList.fromHtml(response);
            if (buildList.size() == 0) {
                mView.showToast("APK가 없습니다");
                mView.hideDialog();
                return;
            }
            buildList.reverse();
            String buildVersionName = buildList.get(0).getVersionName();
            // 마지막 문자가 '/'라면 즉, 버전 이름이 디렉토리를 나타낸다면
            if (buildVersionName.charAt(buildVersionName.length() - 1) != '/' && mFlag == BuildEditContract.FLAG.EDIT) {
                mView.showOnDismiss(buildList, selectedVersion);
                return;
            }

            if (mFlag == BuildEditContract.FLAG.EDIT) {
                Build build = mBuildList.get(selectedVersion);
                selectedVersion = build.getVersionName();
                mBuildList.remove(build);
            } else if (mFlag == BuildEditContract.FLAG.APK) {
                mBuildList = buildList;
            }

            // mBuildList 갱신 및, adapterList 갱신
            List<String> adapterList = new ArrayList<>();
            for (Build nextBuild : buildList.getList()) {
                String version = nextBuild.getVersionName();
                if (mFlag == BuildEditContract.FLAG.APK) {
                    nextBuild.setVersionName(version);
                } else if (mFlag == BuildEditContract.FLAG.EDIT) {
                    nextBuild.setVersionName(selectedVersion + version);
                    mBuildList.add(nextBuild);
                }
                adapterList.add(nextBuild.getVersionName());
            }

            mAdapterModel.setList(adapterList);
            mAdapterView.refresh();
            mView.showVersionTitle(selectedVersion);
        }

        @Override
        public void onStringError(String response) {
            mView.showToast(response);
        }
    };
}