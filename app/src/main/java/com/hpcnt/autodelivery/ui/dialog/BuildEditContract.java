package com.hpcnt.autodelivery.ui.dialog;

import com.hpcnt.autodelivery.model.BuildList;

public interface BuildEditContract {

    enum FLAG {
        EDIT, APK
    }

    String KEY_VERSION_PATH = "KEY_VERSION_PATH";

    String KEY_FLAG = "KEY_FLAG";

    interface View {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);

        void showVersionTitle(String versionTitle);

        void showOnDismiss(BuildList buildList, String versionName);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList(String versionPath);

        void onItemClick(android.view.View v);
    }

    interface OnDismissListener {
        void onDismiss(BuildList buildList, String versionName);
    }
}
