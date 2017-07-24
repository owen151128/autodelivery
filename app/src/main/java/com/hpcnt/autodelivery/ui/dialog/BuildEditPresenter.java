package com.hpcnt.autodelivery.ui.dialog;

import android.view.View;
import android.widget.TextView;

import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class BuildEditPresenter implements BuildEditContract.Presenter {
    private static final String TAG = BuildEditPresenter.class.getSimpleName();

    private BuildEditContract.View mView;
    private BuildEditAdapterContract.View mAdapterView;
    private BuildEditAdapterContract.Model mAdapterModel;

    private BuildList mBuildList;

    BuildEditPresenter(BuildEditContract.View view) {
        mView = view;
    }

    @Override
    public void setList(BuildEditAdapter adapter) {
        mAdapterView = adapter;
        mAdapterModel = adapter;
        mView.setList(adapter);
    }

    @Override
    public void loadBuildList() {
        BuildFetcher fetcher = new BuildFetcher();
        fetcher.fetchBuildList("")
                .subscribe(this::nextBuildListFetch,
                        throwable -> mView.showToast(throwable.toString()));
    }

    @Override
    public void onItemClick(View v) {
        String currentVersion = ((TextView) v).getText().toString();
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
            mAdapterModel.setSelectedVersion(versionTitle.toString());
            BuildFetcher fetcher = new BuildFetcher();
            fetcher.fetchBuildList(mBuildList.get(separateName).getVersionName())
                    .subscribe(this::nextBuildListFetch,
                            throwable -> mView.showToast(throwable.toString()));
        }
    }

    private void nextBuildListFetch(String response) {
        String selectedVersion = mAdapterModel.getSelectedVersion();
        if (selectedVersion.equals("")) {
            mBuildList = BuildList.fromHtml(response);
            setVersionData(new StringBuilder(), new ArrayList<>(), 0);
            return;
        }

        BuildList buildList = BuildList.fromHtml(response);
        buildList.reverse();
        String buildVersionName = buildList.get(0).getVersionName();
        if (!StringUtil.isDirectory(buildVersionName)) {
            mView.showOnDismiss(buildList, selectedVersion);
            return;
        }

        Build build = mBuildList.get(selectedVersion);
        mBuildList.remove(build);

        // mBuildList 갱신 및, adapterList 갱신
        List<String> adapterList = new ArrayList<>();
        for (Build nextBuild : buildList.getList()) {
            String version = nextBuild.getVersionName();
            nextBuild.setVersionName(build.getVersionName() + version);
            mBuildList.add(nextBuild);
            adapterList.add(nextBuild.getVersionName());
        }

        mAdapterModel.setList(adapterList);
        mAdapterView.refresh();
        mView.showVersionTitle(build.getVersionName());

    }
}