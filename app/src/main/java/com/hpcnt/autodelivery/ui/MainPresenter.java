package com.hpcnt.autodelivery.ui;

import com.hpcnt.autodelivery.StringFetchListener;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

public class MainPresenter implements MainContract.Presenter {
    private MainContract.View mView;

    public MainPresenter(MainContract.View view) {
        mView = view;
    }

    @Override
    public void loadLastestBuild() {
        BuildFetcher buildFetcher = new BuildFetcher();
        buildFetcher.fetchBuildList(lastestBuildFetchListener);
    }

    private StringFetchListener lastestBuildFetchListener = new StringFetchListener() {
        @Override
        public void onStringFetched(String response) {
            BuildList buildList = BuildList.fromHtml(response);
            Build lastestBuild = buildList.getLastestBuild();
            if (lastestBuild == null) return;
            mView.showLastestBuild(lastestBuild);
        }

        @Override
        public void onStringError(String response) {
            mView.showToast(response);
        }
    };
}
