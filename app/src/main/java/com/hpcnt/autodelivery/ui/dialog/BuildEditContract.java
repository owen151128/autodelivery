package com.hpcnt.autodelivery.ui.dialog;

import android.support.annotation.StringRes;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

public interface BuildEditContract {

    enum FLAG {
        EDIT, APK, PR
    }

    String KEY_VERSION_PATH = "KEY_VERSION_PATH";
    String KEY_FLAG = "KEY_FLAG";

    interface View extends LifeCycleProvider {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);

        void showToast(int resID);

        void showVersionTitle(String versionTitle);

        void showOnDismiss(BuildList buildList, String versionName);

        void showOnDismiss(String apkName);

        void showOnDismiss(Build build);

        void hideDialog();

        void showHintText(@StringRes int hint);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList(BuildFetcher fetcher, String versionPath);

        void onItemClick(BuildFetcher fetcher, String currentTitle, String currentVersion);

        void onSearchClick(BuildFetcher fetcher, String keyword);
    }

    interface OnDismissListener {

        void onDismiss(BuildList buildList, String versionName);
    }

    interface OnDismissApkListener {

        void onDismiss(String apkName);
    }

    interface OnDismissBuildListener {

        void onDismiss(Build build);
    }
}
