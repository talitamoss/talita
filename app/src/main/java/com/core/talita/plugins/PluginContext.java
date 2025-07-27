package com.core.talita.api;

import android.content.Context;
import android.os.Bundle;

/**
 * PluginContext - Interface provided to plugins for accessing app resources
 * 
 * This provides a controlled API for plugins to interact with the app
 * without giving them direct access to everything.
 */
public interface PluginContext {
    
    /**
     * Get the Android application context
     */
    Context getContext();
    
    /**
     * Get this plugin's ID
     */
    String getPluginId();
    
    /**
     * Log a debug message
     */
    void log(String message);
    
    /**
     * Log an error message
     */
    void logError(String message, Throwable error);
    
    /**
     * Show a toast message to the user
     */
    void showToast(String message);
    
    /**
     * Show a long toast message to the user
     */
    void showLongToast(String message);
    
    /**
     * Get a plugin-specific setting
     */
    String getSetting(String key, String defaultValue);
    
    /**
     * Save a plugin-specific setting
     */
    void putSetting(String key, String value);
    
    /**
     * Get an integer setting
     */
    int getIntSetting(String key, int defaultValue);
    
    /**
     * Save an integer setting
     */
    void putIntSetting(String key, int value);
    
    /**
     * Get a boolean setting
     */
    boolean getBooleanSetting(String key, boolean defaultValue);
    
    /**
     * Save a boolean setting
     */
    void putBooleanSetting(String key, boolean value);
    
    /**
     * Clear all plugin settings
     */
    void clearSettings();
    
    /**
     * Send data to another plugin
     */
    void sendDataToPlugin(String targetPluginId, String dataType, Bundle data);
}
