package com.hpcnt.autodelivery.ui.dialog;


import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.trello.rxlifecycle2.components.support.RxDialogFragment;


/**
 * @author Stark
 *         시나리오상 버튼을 사용해야하는 AlertDialog가 아니라
 *         Custom Dialog 구현이 필요해서 fagment 생명주기를 보장해주는 DialogFragment를 사용했다
 */

public class BuildEditDialog extends RxDialogFragment implements BuildEditContract.View {

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
        adapter.setOnClickListener(
                v -> mPresenter.onItemClick(((TextView) v).getText().toString()));

        mPresenter.loadBuildList(getArguments().getString(BuildEditContract.KEY_VERSION_PATH));
    }

    @Override
    public void setList(BuildEditAdapter adapter) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setAutoMeasureEnabled(false);
        binding.editDialogList.setLayoutManager(linearLayoutManager);
        binding.editDialogList.addItemDecoration(new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL));
        binding.editDialogList.setAdapter(adapter);
    }

    @Override
    public void showToast(String response) {
        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(int resID) {
        Toast.makeText(getContext(), resID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showVersionTitle(String versionTitle) {
        binding.editDialogCurrentTitle.setText(versionTitle);
    }

    @Override
    public void showOnDismiss(BuildList buildList, String versionName) {
        if (mOnDismissListener == null) return;
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

    public void setOnDismissApkListener(
            BuildEditContract.OnDismissApkListener onDismissApkListener) {
        mOnDismissApkListener = onDismissApkListener;
    }
}
