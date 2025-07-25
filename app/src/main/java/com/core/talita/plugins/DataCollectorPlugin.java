package com.core.talita.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.core.talita.api.DataCollector;
import com.core.talita.api.PluginContext;
import com.core.talita.api.PluginResult;
import com.core.talita.api.QuickAddConfig;
import java.util.Map;

/**
 * Base class for all data collector plugins
 * 
 * Plugins are modular extensions that can collect specific types of data.
 * They define WHAT data to collect and provide the collector that knows HOW.
 */
public abstract class DataCollectorPlugin {
    
    protected PluginContext pluginContext;
    
    // ===== Plugin Identity =====
    
    /**
     * Get unique plugin identifier
     * Example: "core.water", "community.meditation"
     */
    public abstract String getPluginId();
    
    /**
     * Get human-readable plugin name
     */
    public abstract String getPluginName();
    
    /**
     * Get plugin description
     */
    public abstract String getDescription();
    
    /**
     * Get plugin author
     */
    public abstract String getAuthor();
    
    /**
     * Get plugin version
     */
    public abstract String getVersion();
    
    /**
     * Get category: "i" (personal), "we" (social), "all" (universal)
     */
    public abstract String getCategory();
    
    /**
     * Get emoji icon for this plugin
     */
    public abstract String getEmoji();
    
    // ===== Plugin Lifecycle =====
    
    /**
     * Initialize the plugin
     */
    public final void initialize(Context context) {
        this.pluginContext = new PluginContext(context, getPluginId());
        onInitialize(pluginContext);
    }
    
    /**
     * Called when plugin is initialized
     * Override to perform setup
     */
    protected void onInitialize(PluginContext context) {
        // Default implementation - override if needed
    }
    
    /**
     * Called when plugin is enabled
     */
    public void onPluginEnabled(Context context) {
        // Override to handle plugin being enabled
        pluginContext.log("Plugin enabled: " + getPluginName());
    }
    
    /**
     * Called when plugin is disabled
     */
    public void onPluginDisabled(Context context) {
        // Override to handle plugin being disabled
        pluginContext.log("Plugin disabled: " + getPluginName());
    }
    
    // ===== Configuration =====
    
    /**
     * Get plugin priority (higher = more important)
     * Used for sorting in UI
     */
    public int getPriority() {
        return 50; // Default medium priority
    }
    
    /**
     * Check if this plugin supports quick add
     */
    public boolean supportsQuickAdd() {
        return true; // Most plugins support quick add
    }
    
    /**
     * Get quick add configuration
     */
    public QuickAddConfig getQuickAddConfig() {
        // Default configuration
        return new QuickAddConfig.Builder()
            .setTitle(getPluginName())
            .setDescription("Add " + getPluginName().toLowerCase())
            .setStyle("TILE")
            .build();
    }
    
    /**
     * Check if plugin has settings UI
     */
    public boolean hasSettingsUI() {
        return false; // Override if plugin has settings
    }
    
    /**
     * Get settings activity intent
     */
    public Intent getSettingsIntent(Context context) {
        return null; // Override to provide settings activity
    }
    
    // ===== Data Collection =====
    
    /**
     * Create the data collector for this plugin
     * This is the main purpose - to provide a collector
     */
    public abstract DataCollector createCollector(Context context);
    
    /**
     * Handle quick add tap
     * Default implementation uses the collector
     */
    public void onQuickAddTapped(Context context) {
        DataCollector collector = createCollector(context);
        if (collector != null) {
            collector.initialize(context);
            collector.collect();
        }
    }
    
    // ===== Advanced Features =====
    
    /**
     * Handle custom actions
     */
    public PluginResult handleAction(String action, Bundle params) {
        return PluginResult.failure("Unknown action: " + action);
    }
    
    /**
     * Get custom UI view for embedding
     */
    public View getCustomView(Context context, String viewType) {
        return null; // Override to provide custom views
    }
    
    /**
     * Export plugin data
     */
    public PluginResult exportData(String format) {
        return PluginResult.failure("Export not supported");
    }
    
    /**
     * Import plugin data
     */
    public PluginResult importData(String format, String data) {
        return PluginResult.failure("Import not supported");
    }
    
    // ===== Inter-plugin Communication =====
    
    /**
     * Receive data from another plugin
     */
    public void onDataReceived(String fromPluginId, String dataType, Bundle data) {
        // Override to handle data from other plugins
    }
    
    /**
     * Check dependencies
     */
    public boolean checkDependencies() {
        return true; // Override if plugin has dependencies
    }
    
    // ===== Helpers =====
    
    /**
     * Get the plugin context
     */
    protected PluginContext getPluginContext() {
        return pluginContext;
    }
    
    /**
     * Log a message
     */
    protected void log(String message) {
        if (pluginContext != null) {
            pluginContext.log(message);
        }
    }
    
    /**
     * Log an error
     */
    protected void logError(String message, Throwable error) {
        if (pluginContext != null) {
            pluginContext.logError(message, error);
        }
    }
    
    @Override
    public String toString() {
        return "Plugin{" +
                "id='" + getPluginId() + '\'' +
                ", name='" + getPluginName() + '\'' +
                ", version='" + getVersion() + '\'' +
                '}';
    }
}
