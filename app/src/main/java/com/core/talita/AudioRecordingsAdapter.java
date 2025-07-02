package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class AudioRecordingsAdapter extends RecyclerView.Adapter<AudioRecordingsAdapter.AudioViewHolder> {

    private final List<AudioActivity.AudioRecord> recordings;
    private final OnRecordingClickListener clickListener;

    public interface OnRecordingClickListener {
        void onRecordingClick(AudioActivity.AudioRecord record);
    }

    public AudioRecordingsAdapter(List<AudioActivity.AudioRecord> recordings, OnRecordingClickListener clickListener) {
        this.recordings = recordings;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioActivity.AudioRecord record = recordings.get(position);

        holder.primaryText.setText(record.displayName);

        // Get file size for additional info
        File audioFile = new File(record.filepath);
        long fileSizeKB = audioFile.length() / 1024;
        String secondaryInfo = record.formattedTime + " • " + fileSizeKB + " KB";

        holder.secondaryText.setText(secondaryInfo);

        // Style the text for dark theme
        holder.primaryText.setTextColor(0xFFFFFFFF); // White
        holder.secondaryText.setTextColor(0xFFCCCCCC); // Light gray

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecordingClick(record);
            }
        });

        // Add visual feedback for clickable item
        holder.itemView.setBackgroundColor(0xFF2A2A2A);
        holder.itemView.setClickable(true);
        holder.itemView.setFocusable(true);
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(android.R.id.text1);
            secondaryText = itemView.findViewById(android.R.id.text2);
        }
    }
}