package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for displaying tracking options with toggle switches
 */
public class TrackingOptionsAdapter extends RecyclerView.Adapter<TrackingOptionsAdapter.ViewHolder> {
    
    private final List<TrackingOptionsActivity.TrackingOption> options;
    private final OnOptionToggleListener listener;
    
    public interface OnOptionToggleListener {
        void onToggle(TrackingOptionsActivity.TrackingOption option, boolean enabled);
    }
    
    public TrackingOptionsAdapter(List<TrackingOptionsActivity.TrackingOption> options, 
                                 OnOptionToggleListener listener) {
        this.options = options;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_tracking_option, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackingOptionsActivity.TrackingOption option = options.get(position);
        holder.bind(option);
    }
    
    @Override
    public int getItemCount() {
        return options.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView iconText;
        private final TextView nameText;
        private final TextView descriptionText;
        private final SwitchCompat toggleSwitch;
        private final View container;
        
        ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.option_container);
            iconText = itemView.findViewById(R.id.option_icon);
            nameText = itemView.findViewById(R.id.option_name);
            descriptionText = itemView.findViewById(R.id.option_description);
            toggleSwitch = itemView.findViewById(R.id.option_toggle);
        }
        
        void bind(TrackingOptionsActivity.TrackingOption option) {
            iconText.setText(option.icon);
            nameText.setText(option.name);
            descriptionText.setText(option.description);
            
            // Set initial state
            toggleSwitch.setChecked(option.isEnabled);
            
            // Core items are always enabled
            if (option.isCore) {
                toggleSwitch.setEnabled(false);
                toggleSwitch.setAlpha(0.5f);
                container.setAlpha(0.8f);
            } else {
                toggleSwitch.setEnabled(true);
                toggleSwitch.setAlpha(1.0f);
                container.setAlpha(1.0f);
                
                // Handle toggle changes
                toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    option.isEnabled = isChecked;
                    listener.onToggle(option, isChecked);
                });
            }
        }
    }
}
