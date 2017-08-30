package com.hpcnt.autodelivery.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class BuildEditPresenter implements BuildEditContract.Presenter {

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
    public void loadBuildList(BuildFetcher fetcher, String versionPath) {
        fetcher.fetchBuildList(versionPath)
                .subscribe(s -> nextBuildListFetch(fetcher, versionPath, s),
                        throwable -> mView.showToast(throwable.toString()));
    }

    @Override
    public void onItemClick(BuildFetcher fetcher, String currentVersion) {
        Build build = new Build();
        build.setVersionName(currentVersion);
        int separateSize = build.getSeparateName().size();

        List<String> separateName = new ArrayList<>();
        separateName.addAll(build.getSeparateName());
        setVersionData(fetcher, new StringBuilder(currentVersion), separateName, separateSize);
    }

    private void setVersionData(
            BuildFetcher fetcher, StringBuilder versionTitle, List<String> separateName, int index) {
        Set<String> versionSet = mBuildList.getVersionSet(separateName, index);
        if (versionSet.size() == 1) {
            for (String version : versionSet) {
                if (versionTitle.length() != 0)
                    versionTitle.append(".");
                versionTitle.append(version);
                separateName.add(version);
            }

            setVersionData(fetcher, versionTitle, separateName, index + 1);
        } else if (versionSet.size() > 1) {
            List<String> versionList = new ArrayList<>();
            for (String version : versionSet) {
                versionList.add(versionTitle + "." + version);
            }

            mAdapterModel.setList(versionList);
            mAdapterView.refresh();
            mView.showVersionTitle(versionTitle.toString());
        } else {
            if (mFlag == BuildEditContract.FLAG.APK) {
                mView.showOnDismiss(versionTitle.toString());
                return;
            }
            String version = mBuildList.get(versionTitle.toString()).getVersionName();

            fetcher.fetchBuildList(version)
                    .subscribe((response) -> nextBuildListFetch(fetcher, version, response),
                            throwable -> mView.showToast(throwable.toString()));
        }
    }

    private void nextBuildListFetch(BuildFetcher fetcher, String selectedVersion, String response) {
        if (TextUtils.isEmpty(selectedVersion)) {
            mBuildList = BuildList.fromHtml(response);
            setVersionData(fetcher, new StringBuilder(), new ArrayList<>(), 0);
            return;
        }

        BuildList buildList = BuildList.fromHtml(response);
        if (buildList.size() == 0) {
            mView.showToast(R.string.message_no_apk);
            mView.hideDialog();
            return;
        }
        buildList.reverse();
        String buildVersionName = buildList.get(0).getVersionName();
        if (!StringUtil.isDirectory(buildVersionName) && mFlag == BuildEditContract.FLAG.EDIT) {
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

    BuildEditAdapterContract.Model getAdapterModel() {
        mAdapterModel.getCount();
        return mAdapterModel;
    }

    public BuildEditContract.FLAG getFlag() {
        return mFlag;
    }

    public void setFlag(BuildEditContract.FLAG flag) {
        mFlag = flag;
    }
}