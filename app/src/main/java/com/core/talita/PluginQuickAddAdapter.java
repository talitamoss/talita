package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;
import java.util.List;

/**
 * Adapter for displaying plugins in the quick add grid
 */
public class PluginQuickAddAdapter extends RecyclerView.Adapter<PluginQuickAddAdapter.ViewHolder> {
    
    private final List<DataCollectorPlugin> plugins;
    private final OnPluginSelectedListener listener;
    
    public interface OnPluginSelectedListener {
        void onPluginSelected(DataCollectorPlugin plugin);
    }
    
    public PluginQuickAddAdapter(List<DataCollectorPlugin> plugins, OnPluginSelectedListener listener) {
        this.plugins = plugins;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_plugin_quick_add, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataCollectorPlugin plugin = plugins.get(position);
        DataCollectorPlugin.QuickAddConfig config = plugin.getQuickAddConfig();
        
        // Set emoji
        holder.emojiText.setText(plugin.getEmoji());
        
        // Set title
        holder.titleText.setText(config.quickAddTitle);
        
        // Set description
        holder.descriptionText.setText(config.quickAddDescription);
        
        // Set category color
        int categoryColor = PluginCategories.getCategoryColor(plugin.getCategory());
        holder.card.setCardBackgroundColor(0x33000000 | (categoryColor & 0x00FFFFFF));
        
        // Click listener
        holder.card.setOnClickListener(v -> listener.onPluginSelected(plugin));
    }
    
    @Override
    public int getItemCount() {
        return plugins.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView emojiText;
        TextView titleText;
        TextView descriptionText;
        
        ViewHolder(View view) {
            super(view);
            card = (CardView) view;
            emojiText = view.findViewById(R.id.plugin_emoji);
            titleText = view.findViewById(R.id.plugin_title);
            descriptionText = view.findViewById(R.id.plugin_description);
        }
    }
}
