package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.plugins.base.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;

/**
 * Water tracking plugin - uses SimpleDataCollector for easy implementation
 */
public class WaterPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "core.water";
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
        return "Core Team";
    }
    
    @Override
    public String getCategory() {
        return "i"; // Personal category
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority
    }
    
    @Override
    public String getEmoji() {
        return "💧";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#3B82F6"); // Blue
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
        return true; // For reminders
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector.Builder("water", "Water Intake")
            .description("Track your daily water consumption")
            .emoji("💧")
            .category("i")
            .inputHint("Amount in ml")
            .inputType(SimpleDataCollector.InputType.NUMBER)
            .build();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open water settings (daily goal, reminders, etc.)
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Water",
            "Track hydration",
            QuickAddConfig.QuickAddStyle.GRID,
            true
        );
    }
}
