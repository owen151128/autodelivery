package com.hpcnt.autodelivery.ui.dialog;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ItemEditRowBinding;

import java.util.ArrayList;
import java.util.List;

class BuildEditAdapter extends RecyclerView.Adapter<BuildEditAdapter.ViewHolder>
        implements BuildEditAdapterContract.Model, BuildEditAdapterContract.View {
    private static final String TAG = BuildEditAdapter.class.getSimpleName();

    private List<String> mRecommendVersions = new ArrayList<>();

    private View.OnClickListener mOnClickListener;

    private String mSelectedVersion = "";

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_edit_row, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String recommend = mRecommendVersions.get(position);
        holder.binding.setVersion(recommend);
        holder.binding.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mRecommendVersions.size();
    }

    @Override
    public void setList(List<String> recommendVersions) {
        mRecommendVersions = recommendVersions;
    }

    @Override
    public void setSelectedVersion(String version) {
        mSelectedVersion = version;
    }

    @Override
    public String getSelectedVersion() {
        return mSelectedVersion;
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    // FIXME static / non-static inner class 의 차이를 알아보고, static inner class 로 변경할 것.
    // 참고로 kotlin 에서는 inner class 는 기본적으로 무조건 static inner class 다.
    class ViewHolder extends RecyclerView.ViewHolder {
        ItemEditRowBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
