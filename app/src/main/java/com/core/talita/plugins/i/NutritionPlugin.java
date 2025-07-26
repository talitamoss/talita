package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Nutrition Plugin - "I" category
 * Tracks food intake and nutrition
 */
public class NutritionPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "i.nutrition";
    }
    
    @Override
    public String getPluginName() {
        return "Nutrition";
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
        return 70;
    }
    
    @Override
    public String getEmoji() {
        return "🥗";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#8BC34A");
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
        return new SimpleDataCollector.Builder("nutrition", "Nutrition")
                .description("Track meals and nutrition")
                .emoji("🥗")
                .category(PluginCategories.I)
                .inputHint("What did you eat?")
                .inputType(SimpleDataCollector.InputType.TEXT)
                .build();
    }
    
    @Override
    public boolean hasSettings() {
        return false;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Settings screen for meal reminders, calorie goals, etc.
    }
}
