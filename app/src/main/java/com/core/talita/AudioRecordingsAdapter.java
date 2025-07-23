package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AudioRecordingsAdapter extends RecyclerView.Adapter<AudioRecordingsAdapter.ViewHolder> {
    
    private final List<PersonalData> recordings;
    private final OnRecordingClickListener clickListener;
    
    public interface OnRecordingClickListener {
        void onRecordingClick(PersonalData recording);
    }
    
    public AudioRecordingsAdapter(List<PersonalData> recordings, OnRecordingClickListener clickListener) {
        this.recordings = recordings;
        this.clickListener = clickListener;
    }
    
    public void updateRecordings(List<PersonalData> newRecordings) {
        this.recordings.clear();
        this.recordings.addAll(newRecordings);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a simple text view for now
        TextView textView = new TextView(parent.getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(16);
        return new ViewHolder(textView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalData recording = recordings.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(recording.getTimestamp()));
        
        String text = dateStr + " - " + recording.getDisplaySummary();
        holder.textView.setText(text);
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecordingClick(recording);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return recordings.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }
}
