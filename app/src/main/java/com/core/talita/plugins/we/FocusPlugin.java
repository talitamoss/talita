package com.core.talita.plugins.we;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.SimpleDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Focus Plugin - "We" category
 * Tracks shared focus sessions (work, study, collaboration)
 */
public class FocusPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "we.focus";
    }
    
    @Override
    public String getPluginName() {
        return "Focus Sessions";
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
        return PluginCategories.WE;
    }
    
    @Override
    public int getPriority() {
        return 60;
    }
    
    @Override
    public String getEmoji() {
        return "🎯";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#00BCD4");
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
        return new SimpleDataCollector.Builder("focus", "Focus Session")
            .description("Track focus and productivity sessions")
            .emoji("🎯")
            .category(PluginCategories.WE)
            .inputHint("What are you focusing on?")
            .inputType(SimpleDataCollector.InputType.TEXT)
            .build();
    }
    
    @Override
    public boolean hasSettings() {
        return false;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Settings screen
    }
}
