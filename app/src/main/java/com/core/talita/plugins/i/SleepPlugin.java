package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.SleepCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Sleep Plugin - "I" category
 * Tracks personal sleep patterns
 */
public class SleepPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "i.sleep";
    }
    
    @Override
    public String getPluginName() {
        return "Sleep";
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
        return PluginCategories.I;
    }
    
    @Override
    public int getPriority() {
        return 85;
    }
    
    @Override
    public String getEmoji() {
        return "😴";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#9C27B0");
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
        return new SleepCollector();
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
            "Sleep",
            "Log sleep hours",
            QuickAddStyle.NUMERIC_INPUT,
            true
        );
    }
}
