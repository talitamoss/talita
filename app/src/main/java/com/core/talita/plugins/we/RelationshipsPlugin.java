package com.core.talita.plugins.we;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.SimpleDataCollector;
import com.core.talita.api.QuickAddConfig;
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
    public String getDescription() {
        return "Track meaningful connections and interactions with others";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Core Team";
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
    
    public int getAccentColor() {
        return Color.parseColor("#E91E63");
    }
    
    public int getIconResource() {
        return 0;
    }
    
    public String[] getRequiredPermissions() {
        return new String[0];
    }
    
    public boolean requiresBackgroundTracking() {
        return false;
    }
    
    public boolean supportsQuickAdd() {
        return true;
    }
    
    public boolean supportsScheduling() {
        return false;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector.Builder("relationships", "Relationships")
                .description("Track meaningful connections and interactions")
                .emoji("💞")
                .category(PluginCategories.WE)
                .inputHint("Who did you connect with?")
                .inputType(SimpleDataCollector.InputType.TEXT)
                .build();
    }
    
    public boolean hasSettings() {
        return false;
    }
    
    public void openSettings(Context context) {
        // TODO: Settings screen
    }
}
