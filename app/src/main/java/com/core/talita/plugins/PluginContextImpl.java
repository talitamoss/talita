package com.core.talita.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.core.talita.api.PluginContext;

/**
 * PluginContextImpl - Implementation of PluginContext for plugins
 * 
 * Provides plugins with controlled access to app resources and storage.
 */
public class PluginContextImpl implements PluginContext {
    private static final String TAG = "PluginContextImpl";
    
    private final Context appContext;
    private final DataCollectorPlugin plugin;
    private final SharedPreferences pluginPrefs;
    
    public PluginContextImpl(Context context, DataCollectorPlugin plugin) {
        this.appContext = context.getApplicationContext();
        this.plugin = plugin;
        
        // Create plugin-specific preferences
        String prefsName = "plugin_" + plugin.getPluginId().replace(".", "_");
        this.pluginPrefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
    
    @Override
    public Context getContext() {
        return appContext;
    }
    
    @Override
    public String getPluginId() {
        return plugin.getPluginId();
    }
    
    @Override
    public void log(String message) {
        Log.d(plugin.getPluginName(), message);
    }
    
    @Override
    public void logError(String message, Throwable error) {
        Log.e(plugin.getPluginName(), message, error);
    }
    
    @Override
    public void showToast(String message) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showLongToast(String message) {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public String getSetting(String key, String defaultValue) {
        return pluginPrefs.getString(key, defaultValue);
    }
    
    @Override
    public void putSetting(String key, String value) {
        pluginPrefs.edit().putString(key, value).apply();
    }
    
    @Override
    public int getIntSetting(String key, int defaultValue) {
        return pluginPrefs.getInt(key, defaultValue);
    }
    
    @Override
    public void putIntSetting(String key, int value) {
        pluginPrefs.edit().putInt(key, value).apply();
    }
    
    @Override
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return pluginPrefs.getBoolean(key, defaultValue);
    }
    
    @Override
    public void putBooleanSetting(String key, boolean value) {
        pluginPrefs.edit().putBoolean(key, value).apply();
    }
    
    @Override
    public void clearSettings() {
        pluginPrefs.edit().clear().apply();
    }
    
    @Override
    public void sendDataToPlugin(String targetPluginId, String dataType, 
                                 android.os.Bundle data) {
        // Get target plugin
        DataCollectorPlugin targetPlugin = PluginManager.getInstance(appContext)
            .getPlugin(targetPluginId);
        
        if (targetPlugin != null) {
            targetPlugin.onDataReceived(plugin.getPluginId(), dataType, data);
        } else {
            Log.w(TAG, "Target plugin not found: " + targetPluginId);
        }
    }
}
