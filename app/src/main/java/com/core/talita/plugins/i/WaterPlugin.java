package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.SimpleDataCollector;
import com.core.talita.api.QuickAddConfig;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Water Plugin - "I" category
 * Tracks water intake throughout the day
 * 
 * Location: app/src/main/java/com/core/talita/plugins/i/WaterPlugin.java
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
    public String getDescription() {
        return "Track your daily water consumption";
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
        return PluginCategories.I;
    }
    
    @Override
    public int getPriority() {
        return 90; // High priority - commonly used
    }
    
    @Override
    public String getEmoji() {
        return "💧";
    }
    
    public int getAccentColor() {
        return Color.parseColor("#2196F3");
    }
    
    public int getIconResource() {
        return 0; // Return actual resource ID if available
    }
    
    public String[] getRequiredPermissions() {
        return new String[0]; // No special permissions needed
    }
    
    public boolean requiresBackgroundTracking() {
        return false;
    }
    
    public boolean supportsQuickAdd() {
        return true;
    }
    
    public boolean supportsScheduling() {
        return true; // Can set reminders
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector.Builder("water", "Water Intake")
                .description("Track water consumption in ml")
                .emoji("💧")
                .category(PluginCategories.I)
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
        // TODO: Open water settings activity
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig.Builder()
                .setTitle("Water")
                .setDescription("Log water intake")
                .setStyle("NUMERIC_INPUT")
                .defaultValue(250)
                .range(50, 1000)
                .build();
    }
}
