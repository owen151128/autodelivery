package com.hpcnt.autodelivery.ui.dialog;

import android.content.Context;
import android.support.annotation.StringRes;

import com.hpcnt.autodelivery.LifeCycleProvider;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;

public interface BuildEditContract {
    /**
     * FLAG 정리
     * EDIT - 일반적인 QA 빌드 를 나타내는 FLAG (BuildEditDialog 에서 가장 일반적으로 사용 되는 FLAG)
     * APK - MainPresenter.selectMyAbiBuild 메소드 호출시 abi에 맞는 apk가 없을때 (오래 된 빌드) 사용 되는 FLAG
     * PR - PR 빌드 를 다운로드 하기 위한 FLAG
     * SELECTOR - BuildEditDialog 를 show 했을때 EDIT, MASTER, PR 을 선택할때 사용되는 FLAG
     * MASTER - Master Branch 빌드를 다운로드 하기 위한 FLAG
     * MASTER_APK - Master Branch 에서 빌드 버젼 선택후 APK(qatest, release) 를 선택할때 사용되는 FLAG
     */
    enum FLAG {
        EDIT, APK, PR, SELECTOR, MASTER, MASTER_APK
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
