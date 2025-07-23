package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.MoodCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Mood Tracking Plugin - "I" category
 * Tracks personal emotional state
 */
public class MoodPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "i.mood";
    }
    
    @Override
    public String getPluginName() {
        return "Mood";
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
        return 90;
    }
    
    @Override
    public String getEmoji() {
        return "😊";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#FFD54F");
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
        return new MoodCollector(context);
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
            "Mood",
            "How are you feeling?",
            QuickAddStyle.CHOICE_PICKER,
            true
        );
    }
}
