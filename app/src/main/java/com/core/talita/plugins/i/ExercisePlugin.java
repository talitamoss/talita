package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.ExerciseCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Exercise Plugin - "I" category
 * Tracks personal physical activity
 */
public class ExercisePlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "i.exercise";
    }
    
    @Override
    public String getPluginName() {
        return "Exercise";
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
        return 95;
    }
    
    @Override
    public String getEmoji() {
        return "💪";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#FF5722");
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
        return new ExerciseCollector();
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
            "Exercise",
            "Log workout",
            QuickAddStyle.DURATION_TIMER,
            true
        );
    }
}
