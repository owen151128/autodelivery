package com.hpcnt.autodelivery.ui.dialog;

import java.util.List;

interface BuildEditAdapterContract {

    interface View {

        void refresh();
    }

    interface Model {

        void setList(List<String> recommendVersions);

        void setSelectedVersion(String version);

        String getSelectedVersion();
    }
}
