package com.hpcnt.autodelivery.ui.dialog;

interface BuildEditContract {

    interface View {

        void setList(BuildEditAdapter adapter);

        void showToast(String response);
    }

    interface Presenter {

        void setList(BuildEditAdapter adapter);

        void loadBuildList();
    }
}
