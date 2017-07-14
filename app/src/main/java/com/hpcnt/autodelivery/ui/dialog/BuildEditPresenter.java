package com.hpcnt.autodelivery.ui.dialog;

import com.hpcnt.autodelivery.StringFetchListener;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

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
        fetcher.fetchBuildList(buildListFetchListener, "");
    }

    private StringFetchListener buildListFetchListener = new StringFetchListener() {

        @Override
        public void onStringFetched(String response) {
            mBuildList = BuildList.fromHtml(response);
            mAdapterModel.setList(mBuildList);
            mAdapterView.refresh();
        }

        @Override
        public void onStringError(String response) {
            mView.showToast(response);
        }
    };
}
