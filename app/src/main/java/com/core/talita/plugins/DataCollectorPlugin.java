package com.core.talita.plugins;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import com.core.talita.collectors.DataCollector;

/**
 * Base class for all data collector plugins
 * This enables community-created collectors without modifying core code
 */
public abstract class DataCollectorPlugin {
    
    // Plugin identification
    public abstract String getPluginId();        // Unique ID like "health.water"
    public abstract String getPluginName();      // Display name like "Water Intake"
    public abstract String getPluginVersion();   // Version like "1.0.0"
    public abstract String getAuthor();          // Plugin author
    
    // Plugin categorization
    public abstract String getCategory();        // Category like "Health", "Fitness", "Finance"
    public abstract int getPriority();           // Display priority (higher = shown first)
    
    // Visual representation
    public abstract String getEmoji();           // Emoji icon like "💧"
    @ColorInt
    public abstract int getAccentColor();        // Plugin's accent color
    @DrawableRes
    public abstract int getIconResource();       // Optional custom icon
    
    // Plugin capabilities
    public abstract String[] getRequiredPermissions();  // Android permissions needed
    public abstract boolean requiresBackgroundTracking(); // If it needs background service
    public abstract boolean supportsQuickAdd();          // If it appears in quick add
    public abstract boolean supportsScheduling();        // If it can be scheduled
    
    // Data collection
    public abstract DataCollector createCollector(Context context);
    
    // Configuration
    public abstract boolean hasSettings();               // If plugin has settings
    public abstract void openSettings(Context context);  // Open plugin settings
    
    // Quick add UI customization
    public abstract QuickAddConfig getQuickAddConfig();
    
    /**
     * Configuration for how this plugin appears in Quick Add
     */
    public static class QuickAddConfig {
        public final String quickAddTitle;        // Title in quick add
        public final String quickAddDescription;  // Subtitle/description
        public final QuickAddStyle style;         // Visual style
        public final boolean showInMainGrid;      // Show in main grid vs overflow
        
        public QuickAddConfig(String title, String description, 
                            QuickAddStyle style, boolean showInMainGrid) {
            this.quickAddTitle = title;
            this.quickAddDescription = description;
            this.style = style;
            this.showInMainGrid = showInMainGrid;
        }
    }
    
    public enum QuickAddStyle {
        SIMPLE_TAP,      // Single tap to add (like water)
        NUMERIC_INPUT,   // Number input (like weight)
        CHOICE_PICKER,   // Multiple choice (like mood)
        DURATION_TIMER,  // Start/stop timer (like exercise)
        TEXT_NOTE,       // Text input (like notes)
        VOICE_RECORD,    // Audio recording
        PHOTO_CAPTURE,   // Camera capture
        LOCATION_MARK,   // Location marking
        CUSTOM           // Custom UI
    }
    
    /**
     * Plugin lifecycle callbacks
     */
    public void onPluginEnabled(Context context) {
        // Called when user enables the plugin
    }
    
    public void onPluginDisabled(Context context) {
        // Called when user disables the plugin
    }
    
    /**
     * Data validation
     */
    public boolean validateData(Object data) {
        return true; // Override for custom validation
    }
    
    /**
     * Export/Import support
     */
    public String exportDataToJson(Object data) {
        return null; // Override to support export
    }
    
    public Object importDataFromJson(String json) {
        return null; // Override to support import
    }
}
