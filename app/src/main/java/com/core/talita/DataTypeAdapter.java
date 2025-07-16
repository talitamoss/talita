package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DataTypeAdapter extends RecyclerView.Adapter<DataTypeAdapter.DataTypeViewHolder> {

    public interface OnDataTypeClickListener {
        void onDataTypeClick(DataViewActivity.DataTypeOverview dataType);
    }

    private final List<DataViewActivity.DataTypeOverview> dataTypes;
    private final OnDataTypeClickListener listener;

    public DataTypeAdapter(List<DataViewActivity.DataTypeOverview> dataTypes, OnDataTypeClickListener listener) {
        this.dataTypes = dataTypes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DataTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DataTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataTypeViewHolder holder, int position) {
        DataViewActivity.DataTypeOverview dataType = dataTypes.get(position);
        holder.bind(dataType, listener);
    }

    @Override
    public int getItemCount() {
        return dataTypes.size();
    }

    static class DataTypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView primaryText;
        private final TextView secondaryText;

        DataTypeViewHolder(View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(android.R.id.text1);
            secondaryText = itemView.findViewById(android.R.id.text2);
        }

        void bind(DataViewActivity.DataTypeOverview dataType, OnDataTypeClickListener listener) {
            primaryText.setText(dataType.icon + " " + dataType.name);
            secondaryText.setText(dataType.count + " items");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDataTypeClick(dataType);
                }
            });
        }
    }
}