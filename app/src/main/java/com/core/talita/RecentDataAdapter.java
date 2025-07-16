package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentDataAdapter extends RecyclerView.Adapter<RecentDataAdapter.RecentDataViewHolder> {

    private final List<DataViewActivity.RecentDataItem> recentItems;

    public RecentDataAdapter(List<DataViewActivity.RecentDataItem> recentItems) {
        this.recentItems = recentItems;
    }

    @NonNull
    @Override
    public RecentDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new RecentDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentDataViewHolder holder, int position) {
        DataViewActivity.RecentDataItem item = recentItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return recentItems.size();
    }

    static class RecentDataViewHolder extends RecyclerView.ViewHolder {
        private final TextView primaryText;
        private final TextView secondaryText;

        RecentDataViewHolder(View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(android.R.id.text1);
            secondaryText = itemView.findViewById(android.R.id.text2);
        }

        void bind(DataViewActivity.RecentDataItem item) {
            primaryText.setText(item.icon + " " + item.summary);
            secondaryText.setText(item.timeAgo);
        }
    }
}