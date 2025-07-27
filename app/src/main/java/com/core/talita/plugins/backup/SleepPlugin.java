package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.plugins.base.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;

/**
 * Sleep tracking plugin
 */
public class SleepPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "core.sleep";
    }
    
    @Override
    public String getPluginName() {
        return "Sleep Tracker";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Core Team";
    }
    
    @Override
    public String getCategory() {
        return "i"; // Personal category
    }
    
    @Override
    public int getPriority() {
        return 80;
    }
    
    @Override
    public String getEmoji() {
        return "😴";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#6366F1"); // Indigo
    }
    
    @Override
    public int getIconResource() {
        return 0; // Use emoji
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
        return true; // For sleep reminders
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector.Builder("sleep", "Sleep")
            .description("Track your sleep patterns and quality")
            .emoji("😴")
            .category("i")
            .inputHint("Hours of sleep")
            .inputType(SimpleDataCollector.InputType.DECIMAL)
            .build();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open sleep settings
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Sleep",
            "Log sleep",
            QuickAddConfig.QuickAddStyle.GRID,
            true
        );
    }
}
