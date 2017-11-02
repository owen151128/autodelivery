package com.hpcnt.autodelivery.ui.dialog;

import android.content.Context;
import android.support.annotation.StringRes;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

public interface BuildEditContract {

    enum FLAG {
        EDIT, APK, PR, SELECTOR, MASTER
    }

    String KEY_VERSION_PATH = "KEY_VERSION_PATH";
    String KEY_FLAG = "KEY_FLAG";

    interface View extends LifeCycleProvider {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);

        void showToast(int resID);

        void showVersionTitle(String versionTitle);

        void setOnBackDismissListenerClear();

        void showOnDismiss(BuildList buildList, String versionName);

        void showOnSelectorDismiss(String result);

        void showOnDismiss(String apkName);

        void showOnDismiss(Build build);

        void showOnBackDismiss();

        void hideDialog();

        void showHintText(@StringRes int hint);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList(BuildFetcher fetcher, String versionPath);

        void initSelector(Context context);

        void onItemClick(BuildFetcher fetcher, String currentTitle, String currentVersion);

        void onSearchClick(BuildFetcher fetcher, String keyword);
    }

    interface OnDismissListener {

        void onDismiss(BuildList buildList, String versionName);
    }

    interface OnDismissSelectorListener {

        void onDismiss(String result);
    }

    interface OnDismissApkListener {

        void onDismiss(String apkName);
    }

    interface OnDismissBuildListener {

        void onDismiss(Build build);
    }

    interface OnDismissBackListener {
        void onDismiss();
    }

}
