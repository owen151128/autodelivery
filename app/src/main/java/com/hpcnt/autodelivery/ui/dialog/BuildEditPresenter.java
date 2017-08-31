package com.hpcnt.autodelivery.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.util.ABIWrapper;
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
                .subscribe(s -> {
                            if (TextUtils.isEmpty(versionPath)) {
                                firstBuildListfetched(fetcher, s);
                            } else {
                                nextBuildListFetch(versionPath, s);
                            }
                        },
                        throwable -> mView.showToast(throwable.toString()));
    }

    @Override
    public void onItemClick(BuildFetcher fetcher, String currentTitle, String currentVersion) {
        Build build;
        switch (mFlag) {
            case EDIT:
                build = new Build();
                build.setVersionName(currentVersion);
                int separateSize = build.getSeparateName().size();
                List<String> separateName = new ArrayList<>();
                separateName.addAll(build.getSeparateName());
                setVersionData(fetcher, new StringBuilder(currentVersion), separateName, separateSize);
                break;
            case APK:
                mView.showOnDismiss(currentVersion);
                break;
            case PR:
                if (StringUtil.isApkFile(currentVersion)) {
                    build = mBuildList.get(currentTitle);
                    build.setApkName(currentVersion);
                    build.setVersionName("pr/" + build.getVersionName());
                    mView.showOnDismiss(build);
                } else {
                    fetcher.fetchBuildList("pr/" + currentVersion)
                            .subscribe((response) -> nextBuildListFetch(currentVersion, response),
                                    throwable -> mView.showToast(throwable.toString()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setSearchData(String keyword) {
        List<String> returnList = new ArrayList<>();
        for (Build build : mBuildList.getList()) {
            if (build.getVersionName().startsWith(keyword)) {
                returnList.add(build.getVersionName());
            }
        }
        setList(returnList);
    }

    private void nextBuildListFetch(String selectedVersion, String response) {
        switch (mFlag) {
            case EDIT:
                executeEditFetched(selectedVersion, response);
                break;
            case APK:
                executeApkFetched(selectedVersion, response);
                break;
            case PR:
                executePrFetched(selectedVersion, response);
                break;
            default:
                break;
        }
    }

    private void executePrFetched(String selectedVersion, String response) {
        Log.d("BuildEditPresenter", "selectedVersion : " + selectedVersion);
        BuildList buildList = getReverseBuildList(response);
        if (buildList == null) return;
        if ("pr/".equals(selectedVersion))
            mBuildList = buildList;
        List<String> adapterList = new ArrayList<>();
        String myAbi = new ABIWrapper().getABI() + "-";

        for (Build nextBuild : buildList.getList()) {
            String version = nextBuild.getVersionName();
            if (StringUtil.isApkFile(version)) {
                if (version.contains(myAbi)) {
                    adapterList.add(nextBuild.getVersionName());
                }
            } else {
                adapterList.add(nextBuild.getVersionName());
            }
        }

        if (adapterList.size() == 0 && buildList.size() != 0) {
            for (Build nextBuild : buildList.getList()) {
                adapterList.add(nextBuild.getVersionName());
            }
        }

        showVersionList(adapterList, selectedVersion);
    }

    private void executeApkFetched(String selectedVersion, String response) {
        BuildList buildList = getReverseBuildList(response);
        if (buildList == null) return;

        List<String> adapterList = new ArrayList<>();
        for (Build nextBuild : buildList.getList()) {
            adapterList.add(nextBuild.getVersionName());
        }
        showVersionList(adapterList, selectedVersion);
    }

    private void executeEditFetched(String selectedVersion, String response) {
        BuildList buildList = getReverseBuildList(response);
        if (buildList == null) return;

        String buildVersionName = buildList.get(0).getVersionName();
        if (!StringUtil.isDirectory(buildVersionName)) {
            mView.showOnDismiss(buildList, selectedVersion);
            return;
        }

        Build build = mBuildList.get(selectedVersion);
        selectedVersion = build.getVersionName();
        mBuildList.remove(build);

        // mBuildList 갱신 및, adapterList 갱신
        List<String> adapterList = new ArrayList<>();
        for (Build nextBuild : buildList.getList()) {
            nextBuild.setVersionName(selectedVersion + nextBuild.getVersionName());
            mBuildList.add(nextBuild);
            adapterList.add(nextBuild.getVersionName());
        }

        showVersionList(adapterList, selectedVersion);
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

            showVersionList(versionList, versionTitle.toString());
        } else {
            String version = mBuildList.get(versionTitle.toString()).getVersionName();

            fetcher.fetchBuildList(version)
                    .subscribe((response) -> nextBuildListFetch(version, response),
                            throwable -> mView.showToast(throwable.toString()));
        }
    }

    private void firstBuildListfetched(BuildFetcher fetcher, String response) {
        mBuildList = BuildList.fromHtml(response);
        setVersionData(fetcher, new StringBuilder(), new ArrayList<>(), 0);
    }

    private void showVersionList(List<String> versionList, String versionTitle2) {
        setList(versionList);
        mView.showVersionTitle(versionTitle2);
    }

    private void setList(List<String> versionList) {
        mAdapterModel.setList(versionList);
        mAdapterView.refresh();
    }

    @Nullable
    private BuildList getReverseBuildList(String response) {
        BuildList buildList = BuildList.fromHtml(response);
        if (buildList.size() == 0) {
            mView.showToast(R.string.message_no_apk);
            mView.hideDialog();
            return null;
        }
        buildList.reverse();
        return buildList;
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