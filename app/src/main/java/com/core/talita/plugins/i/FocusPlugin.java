package com.core.talita.plugins.we;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Focus Plugin - "We" category
 * Tracks shared focus sessions (work, study, collaboration)
 */
public class FocusPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "we.focus";
    }
    
    @Override
    public String getPluginName() {
        return "Focus Sessions";
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
        return 60;
    }
    
    @Override
    public String getEmoji() {
        return "🎯";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#00BCD4");
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
        return true;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector("focus", "Focus Session", "🎯");
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
            "Focus",
            "Start session",
            QuickAddStyle.DURATION_TIMER,
            true
        );
    }
}
