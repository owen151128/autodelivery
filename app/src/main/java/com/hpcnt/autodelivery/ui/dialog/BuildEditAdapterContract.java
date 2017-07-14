package com.hpcnt.autodelivery.ui.dialog;

import com.hpcnt.autodelivery.model.BuildList;

public interface BuildEditAdapterContract {

    interface View {

        void refresh();
    }

    interface Model {

        void setList(BuildList buildList);
    }
}
