package com.hpcnt.autodelivery.ui.dialog;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.DialogBuildEditBinding;

public class BuildEditDialog extends DialogFragment implements BuildEditContract.View {
    private static final String TAG = BuildEditDialog.class.getSimpleName();

    private DialogBuildEditBinding binding;
    private BuildEditContract.Presenter mPresenter;

    public BuildEditDialog() {
    }

    public static BuildEditDialog newInstance() {
        BuildEditDialog fragment = new BuildEditDialog();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_build_edit, container, false);
        binding = DataBindingUtil.bind(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new BuildEditPresenter(this);

        BuildEditAdapter adapter = new BuildEditAdapter();
        mPresenter.setList(adapter);

        mPresenter.loadBuildList();
    }

    @Override
    public void setList(BuildEditAdapter adapter) {
        binding.editDialogList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.editDialogList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        binding.editDialogList.setAdapter(adapter);
    }

    @Override
    public void showToast(String response) {
        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
    }
}
