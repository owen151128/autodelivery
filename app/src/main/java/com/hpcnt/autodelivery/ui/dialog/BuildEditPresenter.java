package com.hpcnt.autodelivery.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
    public void initSelector(Context context) {
        List<String> list = new ArrayList<>();
        list.add(context.getString(R.string.selector_qa));
        list.add(context.getString(R.string.selector_master));
        list.add(context.getString(R.string.selector_pr));
        showVersionList(list, context.getString(R.string.mode_select_title));
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
            case MASTER:
                build = new Build();
                build.setVersionName(currentVersion);
                int masterSeparateSize = build.getSeparateName().size();
                List<String> MasterSeparateName = new ArrayList<>();
                MasterSeparateName.addAll(build.getSeparateName());
                setVersionData(fetcher, new StringBuilder(currentVersion), MasterSeparateName, masterSeparateSize);
                break;
            case SELECTOR:
                mView.showOnSelectorDismiss(currentVersion);
                break;
            case APK:
                mView.showOnDismiss(currentVersion);
                break;
            case PR:
                if (!StringUtil.isDirectory(currentTitle))
                    currentTitle += "/";
                mView.setOnBackDismissListenerClear();
                mView.showOnDismiss(new Build(currentTitle, "", currentVersion));
                break;
            default:
                break;
        }
    }

    @Override
    public void onSearchClick(BuildFetcher fetcher, String keyword) {
        String version = "pr/" + keyword;
        fetcher.fetchBuildList(version)
                .subscribe((response) -> nextBuildListFetch(version, response),
                        throwable -> mView.showToast("없는 PR입니다"));
    }

    private void nextBuildListFetch(String selectedVersion, String response) {
        Log.d("BuildEditPresenter", "selectedVersion : " + selectedVersion);
        getReverseBuildList(response).subscribe(buildList -> {
            Log.d("BuildEditPresenter", "HELELELELELELO");
            if (buildList.size() == 0) {
                mView.showToast(R.string.message_no_apk);
                mView.hideDialog();
                return;
            }
            switch (mFlag) {
                case EDIT:
                    executeEditFetched(buildList, selectedVersion);
                    break;
                case MASTER:
                    executeEditFetched(buildList, selectedVersion);
                    break;
                case APK:
                    executeApkFetched(buildList, selectedVersion);
                    break;
                case PR:
                    executePrFetched(buildList, selectedVersion);
                    break;
                default:
                    break;
            }
        });
    }

    private void executePrFetched(BuildList buildList, String selectedVersion) {
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

        mView.showHintText(R.string.message_search_hint);
        showVersionList(adapterList, selectedVersion);
    }

    private void executeApkFetched(BuildList buildList, String selectedVersion) {
        List<String> adapterList = new ArrayList<>();
        for (Build nextBuild : buildList.getList()) {
            adapterList.add(nextBuild.getVersionName());
        }
        showVersionList(adapterList, selectedVersion);
    }

    private void executeEditFetched(BuildList buildList, String selectedVersion) {
        String buildVersionName = buildList.get(0).getVersionName();
        if (!StringUtil.isDirectory(buildVersionName)) {
            mView.setOnBackDismissListenerClear();
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
        if (separateName.size() > 0) {
            Log.d("BuildEditPresenter", "versionTitle : " + versionTitle);
            Log.d("BuildEditPresenter", "Last separateName : " + separateName.get(separateName.size() - 1));
        }
        if (separateName.size() == 3 && "0".equals(separateName.get(separateName.size() - 1))) {
            if (StringUtil.isDirectory(versionTitle.toString())) {
                fetcher.fetchBuildList(versionTitle.toString())
                        .subscribe((response) -> nextBuildListFetch(versionTitle.toString(), response),
                                throwable -> mView.showToast(throwable.toString()));
            } else {
                List<String> versionList = mBuildList.getVersionList(separateName, index);
                showVersionList(versionList, versionTitle.toString());
            }
        } else {
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

    @NonNull
    private Observable<BuildList> getReverseBuildList(String response) {
        return Observable.<BuildList>create(e -> {
            BuildList buildList = BuildList.fromHtml(response);
            buildList.reverse();
            e.onNext(buildList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.computation())
                .compose(mView.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread());
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