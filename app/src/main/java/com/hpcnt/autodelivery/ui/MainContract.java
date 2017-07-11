package com.hpcnt.autodelivery.ui;

import com.hpcnt.autodelivery.model.Build;

public interface MainContract {

    interface View {

        void showLastestBuild(Build lastestBuild);

        void showToast(String response);
    }

    interface Presenter {

        void loadLastestBuild();
    }
}
