package com.core.talita.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.core.talita.UniversalDataService;
import com.core.talita.plugins.PluginManager;
import java.util.HashMap;
import java.util.Map;

/**
 * PluginContext - Provides plugins with controlled access to app functionality
 * 
 * This context object is passed to plugins to give them access to:
 * - Android context (for UI, resources, etc.)
 * - Data service (for storing collected data)
 * - Plugin settings (for configuration)
 * - Inter-plugin communication
 * 
 * Location: app/src/main/java/com/core/talita/api/PluginContext.java
 */
public class PluginContext {
    private static final String TAG = "PluginContext";
    private static final String PREFS_PREFIX = "plugin_settings_";
    
    private final Context androidContext;
    private final String pluginId;
    private final UniversalDataService dataService;
    private final PluginManager pluginManager;
    private final SharedPreferences pluginPrefs;
    private final Map<String, Object> sharedData;
    
    public PluginContext(Context androidContext, String pluginId) {
        this.androidContext = androidContext;
        this.pluginId = pluginId;
        this.dataService = UniversalDataService.getInstance(androidContext);
        this.pluginManager = PluginManager.getInstance(androidContext);
        this.pluginPrefs = androidContext.getSharedPreferences(
            PREFS_PREFIX + pluginId, Context.MODE_PRIVATE);
        this.sharedData = new HashMap<>();
    }
    
    /**
     * Get the Android context for UI operations
     */
    public Context getAndroidContext() {
        return androidContext;
    }
    
    /**
     * Get the Android context (alias for getAndroidContext)
     */
    public Context getContext() {
        return androidContext;
    }
    
    /**
     * Get the plugin's unique ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * Get the data service for storing collected data
     */
    public UniversalDataService getDataService() {
        return dataService;
    }
    
    /**
     * Get plugin-specific SharedPreferences
     */
    public SharedPreferences getPluginPreferences() {
        return pluginPrefs;
    }
    
    /**
     * Get a plugin setting
     */
    public String getStringSetting(String key, String defaultValue) {
        return pluginPrefs.getString(key, defaultValue);
    }
    
    /**
     * Get a boolean plugin setting
     */
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return pluginPrefs.getBoolean(key, defaultValue);
    }
    
    /**
     * Get an integer plugin setting
     */
    public int getIntSetting(String key, int defaultValue) {
        return pluginPrefs.getInt(key, defaultValue);
    }
    
    /**
     * Save a plugin setting
     */
    public void putSetting(String key, String value) {
        pluginPrefs.edit().putString(key, value).apply();
    }
    
    /**
     * Save a boolean plugin setting
     */
    public void putBooleanSetting(String key, boolean value) {
        pluginPrefs.edit().putBoolean(key, value).apply();
    }
    
    /**
     * Save an integer plugin setting
     */
    public void putIntSetting(String key, int value) {
        pluginPrefs.edit().putInt(key, value).apply();
    }
    
    /**
     * Log a message (plugin-specific)
     */
    public void log(String message) {
        Log.d(TAG + ":" + pluginId, message);
    }
    
    /**
     * Log an error
     */
    public void logError(String message, Throwable error) {
        Log.e(TAG + ":" + pluginId, message, error);
    }
    
    /**
     * Show a toast message
     */
    public void showToast(String message) {
        Toast.makeText(androidContext, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Send data to another plugin
     */
    public void sendToPlugin(String targetPluginId, String dataType, Map<String, Object> data) {
        // Implementation would use PluginManager to route data
        log("Sending data to plugin: " + targetPluginId);
    }
    
    /**
     * Get shared data (for inter-plugin communication)
     */
    public Object getSharedData(String key) {
        return sharedData.get(key);
    }
    
    /**
     * Set shared data
     */
    public void setSharedData(String key, Object value) {
        sharedData.put(key, value);
    }
    
    /**
     * Check if another plugin is available
     */
    public boolean isPluginAvailable(String pluginId) {
        return pluginManager.getPlugin(pluginId) != null;
    }
    
    /**
     * Get resource string
     */
    public String getString(int resId) {
        return androidContext.getString(resId);
    }
    
    /**
     * Get resource string with formatting
     */
    public String getString(int resId, Object... formatArgs) {
        return androidContext.getString(resId, formatArgs);
    }
}
