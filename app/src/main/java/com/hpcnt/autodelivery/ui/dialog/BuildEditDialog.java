package com.hpcnt.autodelivery.ui.dialog;


import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.DialogBuildEditBinding;
import com.hpcnt.autodelivery.model.BuildList;

public class BuildEditDialog extends DialogFragment implements BuildEditContract.View {
    private static final String TAG = BuildEditDialog.class.getSimpleName();

    private DialogBuildEditBinding binding;
    private BuildEditContract.Presenter mPresenter;
    private BuildEditContract.OnDismissListener mOnDismissListener;
    private BuildEditContract.OnDismissApkListener mOnDismissApkListener;

    public BuildEditDialog() {
    }

    public static BuildEditDialog newInstance(String versionPath, BuildEditContract.FLAG flag) {
        BuildEditDialog fragment = new BuildEditDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BuildEditContract.KEY_VERSION_PATH, versionPath);
        bundle.putSerializable(BuildEditContract.KEY_FLAG, flag);
        fragment.setArguments(bundle);
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new BuildEditPresenter(this, getArguments());

        BuildEditAdapter adapter = new BuildEditAdapter();
        mPresenter.setList(adapter);
        adapter.setOnClickListener(v -> mPresenter.onItemClick(((TextView) v).getText().toString()));

        mPresenter.loadBuildList(getArguments().getString(BuildEditContract.KEY_VERSION_PATH));
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

    @Override
    public void showVersionTitle(String versionTitle) {
        binding.editDialogCurrentTitle.setText(versionTitle);
    }

    @Override
    public void showOnDismiss(BuildList buildList, String versionName) {
        mOnDismissListener.onDismiss(buildList, versionName);
        dismiss();
    }

    @Override
    public void showOnDismiss(String apkName) {
        mOnDismissApkListener.onDismiss(apkName);
        dismiss();
    }

    @Override
    public void hideDialog() {
        dismiss();
    }

    public void setOnDismissListener(BuildEditContract.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setOnDismissApkListener(BuildEditContract.OnDismissApkListener onDismissApkListener) {
        mOnDismissApkListener = onDismissApkListener;
    }
}
