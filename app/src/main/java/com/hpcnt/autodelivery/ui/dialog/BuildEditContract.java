package com.hpcnt.autodelivery.ui.dialog;

interface BuildEditContract {

    interface View {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);

        void showVersionTitle(String versionTitle);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList();

        void onItemClick(android.view.View v);
    }
}
