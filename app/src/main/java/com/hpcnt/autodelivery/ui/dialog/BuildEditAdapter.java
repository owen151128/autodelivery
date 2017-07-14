package com.hpcnt.autodelivery.ui.dialog;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ItemEditRowBinding;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;

class BuildEditAdapter extends RecyclerView.Adapter<BuildEditAdapter.ViewHolder>
        implements BuildEditAdapterContract.Model, BuildEditAdapterContract.View {
    private static final String TAG = BuildEditAdapter.class.getSimpleName();

    BuildList mBuildList = new BuildList();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_edit_row, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Build build = mBuildList.get(position);
        holder.binding.setBuild(build);

    }

    @Override
    public int getItemCount() {
        return mBuildList.size();
    }

    @Override
    public void setList(BuildList buildList) {
        mBuildList = buildList;
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemEditRowBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
