package com.core.talita;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationHistoryAdapter extends RecyclerView.Adapter<LocationHistoryAdapter.ViewHolder> {
    
    private final List<PersonalData> locationHistory;
    
    public LocationHistoryAdapter(List<PersonalData> locationHistory) {
        this.locationHistory = locationHistory;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(14);
        return new ViewHolder(textView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalData location = locationHistory.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeStr = sdf.format(new Date(location.getTimestamp()));
        
        String text = timeStr + " - " + location.getDisplaySummary();
        holder.textView.setText(text);
    }
    
    @Override
    public int getItemCount() {
        return locationHistory.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }
}
