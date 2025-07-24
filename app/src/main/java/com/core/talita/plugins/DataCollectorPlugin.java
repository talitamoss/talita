package com.core.talita.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.core.talita.api.DataCollector;
import com.core.talita.api.PluginContext;
import com.core.talita.api.PluginResult;
import java.util.Map;

/**
 * Base class for all data collector plugins
 * 
 * Plugins are modular extensions that can collect specific types of data.
 * They are organized into three categories: I, We, All
 */
public abstract class DataCollectorPlugin {
    
    /**
     * Plugin metadata - must be implemented by all plugins
     */
    public abstract String getPluginId();
    public abstract String getPluginName();
    public abstract String getPluginVersion();
    public abstract String getAuthor();
    public abstract String getCategory(); // "I", "We", or "All"
    public abstract int getPriority(); // 0-100, higher = more important
    
    /**
     * Visual identity
     */
    public abstract String getEmoji(); // Emoji icon for the plugin
    public abstract int getAccentColor(); // Color for UI elements
    public abstract int getIconResource(); // Optional drawable resource
    
    /**
     * Plugin capabilities
     */
    public abstract String[] getRequiredPermissions();
    public abstract boolean requiresBackgroundTracking();
    public abstract boolean supportsQuickAdd();
    public abstract boolean supportsScheduling();
    public abstract DataCollector createCollector(Context context);
    
    /**
     * Plugin lifecycle
     */
    public void initialize(Context context) {
        // Override if needed
    }
    
    public void enable() {
        // Override if needed
    }
    
    public void disable() {
        // Override if needed
    }
    
    public boolean isEnabled() {
        return true; // Override to check actual state
    }
    
    /**
     * Get simple ID without package prefix
     */
    public String getId() {
        return getPluginId();
    }
    
    /**
     * Get display name
     */
    public String getName() {
        return getPluginName();
    }
    
    /**
     * Configuration UI
     */
    public boolean hasSettings() {
        return false; // Override if plugin has settings
    }
    
    public void openSettings(Context context) {
        // Override to open settings activity
    }
    
    /**
     * Quick Add support
     */
    public QuickAddConfig getQuickAddConfig() {
        return null; // Override if supports quick add
    }
    
    /**
     * Quick Add configuration
     */
    public static class QuickAddConfig {
        public final String title;
        public final String description;
        public final String style;
        public final boolean showInMainGrid;
        
        public enum QuickAddStyle {
            GRID,
            LIST,
            CARD
        }
        
        public QuickAddConfig(String title, String description, String style, boolean showInMainGrid) {
            this.title = title;
            this.description = description;
            this.style = style;
            this.showInMainGrid = showInMainGrid;
        }
    }
}
