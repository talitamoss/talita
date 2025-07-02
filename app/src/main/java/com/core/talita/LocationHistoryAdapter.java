package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class LocationHistoryAdapter extends RecyclerView.Adapter<LocationHistoryAdapter.LocationViewHolder> {

    private final List<LocationActivity.LocationRecord> locationHistory;

    public LocationHistoryAdapter(List<LocationActivity.LocationRecord> locationHistory) {
        this.locationHistory = locationHistory;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationActivity.LocationRecord record = locationHistory.get(position);

        // Format coordinates
        String coordinates = String.format(Locale.getDefault(),
                "%.6f, %.6f", record.latitude, record.longitude);

        holder.primaryText.setText(coordinates);
        holder.secondaryText.setText(record.formattedTime);

        // Style the text for dark theme
        holder.primaryText.setTextColor(0xFFFFFFFF); // White
        holder.secondaryText.setTextColor(0xFFCCCCCC); // Light gray
    }

    @Override
    public int getItemCount() {
        return locationHistory.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(android.R.id.text1);
            secondaryText = itemView.findViewById(android.R.id.text2);

            // Set background for dark theme
            itemView.setBackgroundColor(0xFF2A2A2A);
        }
    }
}