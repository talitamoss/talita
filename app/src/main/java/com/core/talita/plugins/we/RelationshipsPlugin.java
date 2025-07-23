package com.core.talita.plugins.we;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Relationships Plugin - "We" category
 * Tracks connections and interactions with others
 */
public class RelationshipsPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "we.relationships";
    }
    
    @Override
    public String getPluginName() {
        return "Relationships";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Talita Core Team";
    }
    
    @Override
    public String getCategory() {
        return PluginCategories.WE;
    }
    
    @Override
    public int getPriority() {
        return 80;
    }
    
    @Override
    public String getEmoji() {
        return "💞";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#E91E63");
    }
    
    @Override
    public int getIconResource() {
        return 0;
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }
    
    @Override
    public boolean requiresBackgroundTracking() {
        return false;
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true;
    }
    
    @Override
    public boolean supportsScheduling() {
        return false;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector("relationships", "Relationships", "💞");
    }
    
    @Override
    public boolean hasSettings() {
        return false;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Settings screen
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Connection",
            "Log meaningful interaction",
            QuickAddStyle.TEXT_NOTE,
            true
        );
    }
}
