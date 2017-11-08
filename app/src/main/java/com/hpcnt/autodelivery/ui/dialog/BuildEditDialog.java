package com.hpcnt.autodelivery.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.DialogBuildEditBinding;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
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
    private BuildEditContract.OnDismissSelectorListener mOnDismissSelectorListener;
    private BuildEditContract.OnDismissBackListener mOnDismissBackListener;
    private BuildEditContract.OnDismissApkListener mOnDismissApkListener;
    private BuildEditContract.OnDismissBuildListener mOnDismissBuildListener;

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
        binding.setFlag((BuildEditContract.FLAG) getArguments().getSerializable(BuildEditContract.KEY_FLAG));
        binding.setDialog(this);
        mPresenter = new BuildEditPresenter(this, getArguments());
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showOnBackDismiss();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        super.onDismiss(dialog);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BuildEditAdapter adapter = new BuildEditAdapter();
        mPresenter.setList(adapter);
        adapter.setOnClickListener(
                v -> mPresenter.onItemClick(new BuildFetcher(this),
                        binding.editDialogCurrentTitle.getText().toString(), ((TextView) v).getText().toString()));

        BuildEditContract.FLAG flag
                = (BuildEditContract.FLAG) getArguments().getSerializable(BuildEditContract.KEY_FLAG);

        if (flag == BuildEditContract.FLAG.SELECTOR) {
            mPresenter.initSelector(getContext());
        } else if (flag == BuildEditContract.FLAG.PR) {
            binding.selecotrText.setVisibility(View.GONE);
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (flag == BuildEditContract.FLAG.EDIT) {
            binding.selecotrText.setText(getString(R.string.selector_qa));
            binding.selecotrText.setVisibility(View.VISIBLE);
        } else if (flag == BuildEditContract.FLAG.MASTER) {
            binding.selecotrText.setText(getString(R.string.selector_master));
            binding.selecotrText.setVisibility(View.VISIBLE);
        }

        if (flag == BuildEditContract.FLAG.EDIT || flag == BuildEditContract.FLAG.MASTER) {
            mPresenter.loadBuildList(new BuildFetcher(this),
                    getArguments().getString(BuildEditContract.KEY_VERSION_PATH));
        }
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

    @SuppressWarnings("UnusedParameters")
    public void onSearchClick(View view) {
        mPresenter.onSearchClick(new BuildFetcher(this), binding.editDialogSearch.getText().toString());
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
    public void showOnSelectorDismiss(String result) {
        if (mOnDismissSelectorListener == null) return;
        mOnDismissSelectorListener.onDismiss(result);
        dismiss();
    }

    @Override
    public void showOnDismiss(String apkName) {
        mOnDismissApkListener.onDismiss(apkName);
        dismiss();
    }

    @Override
    public void showOnDismiss(Build build) {
        mOnDismissBuildListener.onDismiss(build);
        dismiss();
    }

    @Override
    public void setOnBackDismissListenerClear() {
        mOnDismissBackListener = null;
    }

    @Override
    public void showOnBackDismiss() {
        if (mOnDismissBackListener == null) return;
        mOnDismissBackListener.onDismiss();
        dismiss();
    }

    @Override
    public void hideDialog() {
        dismiss();
    }

    @Override
    public void showHintText(@StringRes int hint) {
        binding.editDialogSearch.setEnabled(true);
        binding.editDialogSearch.setHint(hint);
    }

    public void setOnDismissListener(BuildEditContract.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setOnDismissSelectorListener(BuildEditContract.OnDismissSelectorListener onDismissSelectorListener) {
        mOnDismissSelectorListener = onDismissSelectorListener;
    }

    public void setmOnDismissBackListener(BuildEditContract.OnDismissBackListener onDismissBackListener) {
        mOnDismissBackListener = onDismissBackListener;
    }

    public void setOnDismissApkListener(
            BuildEditContract.OnDismissApkListener onDismissApkListener) {
        mOnDismissApkListener = onDismissApkListener;
    }

    public void setOnDismissBuildListener(
            BuildEditContract.OnDismissBuildListener onDismissBuildListener) {
        mOnDismissBuildListener = onDismissBuildListener;
    }

    DialogBuildEditBinding getBinding() {
        return binding;
    }

    BuildEditContract.Presenter getPresenter() {
        return mPresenter;
    }
}
