package com.hpcnt.autodelivery.ui.dialog;

import com.hpcnt.autodelivery.model.BuildList;

interface BuildEditContract {

    interface View {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);

        void showVersionTitle(String versionTitle);

        void showOnDismiss(BuildList buildList, String versionName);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList();

        void onItemClick(android.view.View v);
    }

    interface OnDismissListener {
        void onDismiss(BuildList buildList, String versionName);
    }
}
