package com.core.talita.api;

import android.content.Context;
import android.content.SharedPreferences;
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
 */
public class PluginContext {
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
     * Store a setting for this plugin
     */
    public void putSetting(String key, String value) {
        pluginPrefs.edit().putString(key, value).apply();
    }
    
    /**
     * Store a boolean setting for this plugin
     */
    public void putSetting(String key, boolean value) {
        pluginPrefs.edit().putBoolean(key, value).apply();
    }
    
    /**
     * Store an integer setting for this plugin
     */
    public void putSetting(String key, int value) {
        pluginPrefs.edit().putInt(key, value).apply();
    }
    
    /**
     * Get a string setting
     */
    public String getSetting(String key, String defaultValue) {
        return pluginPrefs.getString(key, defaultValue);
    }
    
    /**
     * Get a boolean setting
     */
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return pluginPrefs.getBoolean(key, defaultValue);
    }
    
    /**
     * Get an integer setting
     */
    public int getIntSetting(String key, int defaultValue) {
        return pluginPrefs.getInt(key, defaultValue);
    }
    
    /**
     * Check if another plugin is available
     */
    public boolean isPluginAvailable(String pluginId) {
        return pluginManager.getPlugin(pluginId) != null;
    }
    
    /**
     * Check if another plugin is enabled
     */
    public boolean isPluginEnabled(String pluginId) {
        return pluginManager.isPluginEnabled(pluginId);
    }
    
    /**
     * Send data to another plugin
     */
    public void sendToPlugin(String targetPluginId, String key, Object data) {
        // This could trigger a callback in the target plugin
        // For now, we'll use a simple shared data approach
        String dataKey = pluginId + "->" + targetPluginId + ":" + key;
        sharedData.put(dataKey, data);
    }
    
    /**
     * Receive data from another plugin
     */
    public Object receiveFromPlugin(String sourcePluginId, String key) {
        String dataKey = sourcePluginId + "->" + pluginId + ":" + key;
        return sharedData.get(dataKey);
    }
    
    /**
     * Log a message (for debugging)
     */
    public void log(String message) {
        android.util.Log.d("Plugin[" + pluginId + "]", message);
    }
    
    /**
     * Log an error
     */
    public void logError(String message, Throwable error) {
        android.util.Log.e("Plugin[" + pluginId + "]", message, error);
    }
    
    /**
     * Show a toast message to the user
     */
    public void showToast(String message) {
        android.widget.Toast.makeText(androidContext, message, 
            android.widget.Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Get a resource string
     */
    public String getString(int resId) {
        return androidContext.getString(resId);
    }
    
    /**
     * Get a resource string with formatting
     */
    public String getString(int resId, Object... formatArgs) {
        return androidContext.getString(resId, formatArgs);
    }
}
