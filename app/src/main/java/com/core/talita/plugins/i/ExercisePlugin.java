package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.plugins.base.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;

/**
 * Exercise tracking plugin
 */
public class ExercisePlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "core.exercise";
    }
    
    @Override
    public String getPluginName() {
        return "Exercise Tracker";
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
        return 85;
    }
    
    @Override
    public String getEmoji() {
        return "💪";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#10B981"); // Green
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
        return false;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new SimpleDataCollector.Builder("exercise", "Exercise")
            .description("Log your workouts and physical activities")
            .emoji("💪")
            .category("i")
            .inputHint("What exercise did you do?")
            .inputType(SimpleDataCollector.InputType.TEXT)
            .build();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open exercise settings
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Exercise",
            "Log workout",
            QuickAddConfig.QuickAddStyle.GRID,
            true
        );
    }
}
