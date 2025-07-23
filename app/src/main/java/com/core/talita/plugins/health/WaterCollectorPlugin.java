package com.core.talita.plugins.health;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import com.core.talita.R;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.WaterCollector;
import com.core.talita.plugins.DataCollectorPlugin;

/**
 * Water Intake Tracking Plugin
 * Tracks daily water consumption with quick add support
 */
public class WaterCollectorPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "health.water";
    }
    
    @Override
    public String getPluginName() {
        return "Water Intake";
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
        return "Health & Wellness";
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority - shown first
    }
    
    @Override
    public String getEmoji() {
        return "💧";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#4FC3F7"); // Light blue
    }
    
    @Override
    public int getIconResource() {
        return 0; // Use emoji instead
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return new String[0]; // No special permissions needed
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
        return true; // Can set reminders to drink water
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new WaterCollector(context);
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open water settings activity
        Intent intent = new Intent(context, WaterSettingsActivity.class);
        context.startActivity(intent);
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Water",
            "Track hydration",
            QuickAddStyle.SIMPLE_TAP,
            true // Show in main grid
        );
    }
    
    @Override
    public void onPluginEnabled(Context context) {
        // Set default daily goal if not set
        WaterCollector.setDailyGoal(context, 2000); // 2L default
    }
}
