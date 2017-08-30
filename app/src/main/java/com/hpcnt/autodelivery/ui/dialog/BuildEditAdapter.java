package com.hpcnt.autodelivery.ui.dialog;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ItemEditRowBinding;

import java.util.ArrayList;
import java.util.List;

class BuildEditAdapter extends RecyclerView.Adapter<BuildEditAdapter.ViewHolder>
        implements BuildEditAdapterContract.Model, BuildEditAdapterContract.View {

    private List<String> mRecommendVersions = new ArrayList<>();
    private View.OnClickListener mOnClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_row, parent, false);
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
    public int getCount() {
        return mRecommendVersions.size();
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ItemEditRowBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
