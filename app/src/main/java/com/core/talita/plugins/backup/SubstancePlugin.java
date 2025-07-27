package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Substance Plugin - "I" category
 * Tracks substance intake (caffeine, medication, supplements, etc.)
 */
public class SubstancePlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "i.substance";
    }
    
    @Override
    public String getPluginName() {
        return "Substances";
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
        return 50;
    }
    
    @Override
    public String getEmoji() {
        return "💊";
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
        return new SimpleDataCollector.Builder("substance", "Substance")
                .description("Track medication, supplements, caffeine, etc.")
                .emoji("💊")
                .category(PluginCategories.I)
                .inputHint("What substance? (e.g., Coffee, Vitamin D)")
                .inputType(SimpleDataCollector.InputType.TEXT)
                .build();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Settings for reminders, dosage tracking, etc.
    }
}
