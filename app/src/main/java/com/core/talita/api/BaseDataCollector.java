package com.core.talita.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.UniversalDataService;
import com.core.talita.PersonalData;
import java.util.*;

/**
 * BaseDataCollector - Foundation for all data collectors
 * 
 * Provides common functionality for all collectors:
 * - Settings management
 * - Data storage through UniversalDataService
 * - Enable/disable state
 * - Permission handling
 * - Automated collection support
 */
public abstract class BaseDataCollector implements DataCollector {
    private static final String TAG = "BaseDataCollector";
    private static final String PREFS_NAME = "collector_settings";
    
    protected Context context;
    protected UniversalDataService dataService;
    protected CollectorSettings settings;
    protected boolean isCollecting = false;
    
    // Lifecycle callbacks
    protected CollectionCallback activeCallback;
    
    /**
     * Initialize the collector
     */
    @Override
    public void initialize(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
        this.settings = loadSettings();
        
        Log.d(TAG, "Initialized collector: " + getDataType());
    }
    
    /**
     * Clean up resources
     */
    @Override
    public void onDestroy() {
        if (isCollecting) {
            stopAutomatedCollection();
        }
        Log.d(TAG, "Destroyed collector: " + getDataType());
    }
    
    /**
     * Get the type - delegates to getDataType()
     * This provides the missing getType() method
     */
    public String getType() {
        return getDataType();
    }
    
    /**
     * Check if available on this device
     */
    @Override
    public boolean isAvailable() {
        // Check required permissions
        List<String> missingPermissions = getMissingPermissions();
        if (!missingPermissions.isEmpty()) {
            Log.d(TAG, getDataType() + " missing permissions: " + missingPermissions);
            return false;
        }
        
        // Check device capabilities
        return checkDeviceCapabilities();
    }
    
    /**
     * Check if enabled by user
     */
    @Override
    public boolean isEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(getDataType() + "_enabled", true);
    }
    
    /**
     * Enable/disable collector
     */
    @Override
    public void setEnabled(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(getDataType() + "_enabled", enabled).apply();
        
        if (!enabled && isCollecting) {
            stopAutomatedCollection();
        }
    }
    
    /**
     * Get current settings
     */
    @Override
    public CollectorSettings getSettings() {
        if (settings == null) {
            settings = loadSettings();
        }
        return settings;
    }
    
    /**
     * Update settings
     */
    @Override
    public void updateSettings(CollectorSettings newSettings) {
        this.settings = newSettings;
        saveSettings(newSettings);
    }
    
    /**
     * Start automated collection
     */
    @Override
    public void startAutomatedCollection() {
        if (!isAvailable() || !isEnabled()) {
            Log.w(TAG, "Cannot start automated collection - collector not available or enabled");
            return;
        }
        
        if (!isCollecting) {
            isCollecting = true;
            onStartCollection();
            Log.d(TAG, "Started automated collection for: " + getDataType());
        }
    }
    
    /**
     * Stop automated collection
     */
    @Override
    public void stopAutomatedCollection() {
        if (isCollecting) {
            isCollecting = false;
            onStopCollection();
            Log.d(TAG, "Stopped automated collection for: " + getDataType());
        }
    }
    
    /**
     * Check if collecting automatically
     */
    @Override
    public boolean isCollectingAutomatically() {
        return isCollecting;
    }
    
    /**
     * Manual collection trigger
     */
    @Override
    public CollectorResult collect() {
        if (!isAvailable()) {
            return CollectorResult.failure(getDataType(), "Collector not available");
        }
        
        if (!isEnabled()) {
            return CollectorResult.failure(getDataType(), "Collector not enabled");
        }
        
        return performCollection();
    }
    
    /**
     * Quick collection with data
     */
    @Override
    public CollectorResult collectQuick(Map<String, Object> data) {
        if (!isAvailable() || !isEnabled()) {
            return CollectorResult.failure(getDataType(), "Collector not available or enabled");
        }
        
        return performQuickCollection(data);
    }
    
    /**
     * Get required permissions
     */
    @Override
    public List<String> getRequiredPermissions() {
        return new ArrayList<>(); // Override if permissions needed
    }
    
    // Protected methods for subclasses
    
    /**
     * Check device capabilities (override in subclass)
     */
    protected abstract boolean checkDeviceCapabilities();
    
    /**
     * Called when automated collection starts (override in subclass)
     */
    protected abstract void onStartCollection();
    
    /**
     * Called when automated collection stops (override in subclass)
     */
    protected abstract void onStopCollection();
    
    /**
     * Perform manual collection (override in subclass)
     */
    protected abstract CollectorResult performCollection();
    
    /**
     * Perform quick collection with data (override in subclass)
     */
    protected abstract CollectorResult performQuickCollection(Map<String, Object> data);
    
    /**
     * Handle collected data
     */
    protected void handleCollectedData(Map<String, Object> data) {
        if (activeCallback != null) {
            activeCallback.onDataCollected(getDataType(), data);
        }
        
        // Store through UniversalDataService
        storeData(data);
    }
    
    /**
     * Store data using UniversalDataService
     */
    protected void storeData(Map<String, Object> data) {
        try {
            // Create PersonalData object
            PersonalData personalData = new PersonalData(
                getDataType(),
                data,
                new HashMap<>() // metadata
            );
            
            // Process through universal pipeline
            dataService.processData(personalData);
            
            Log.d(TAG, "Stored " + getDataType() + " data");
        } catch (Exception e) {
            Log.e(TAG, "Failed to store data", e);
        }
    }
    
    /**
     * Load settings from preferences
     */
    protected CollectorSettings loadSettings() {
        SharedPreferences prefs = context.getSharedPreferences(
            PREFS_NAME + "_" + getDataType(), Context.MODE_PRIVATE);
        
        return new CollectorSettings.Builder()
            .setAutomatedCollection(prefs.getBoolean("automated", false))
            .setCollectionFrequency(prefs.getInt("frequency", 60))
            .setNotificationsEnabled(prefs.getBoolean("notifications", false))
            .build();
    }
    
    /**
     * Save settings to preferences
     */
    protected void saveSettings(CollectorSettings settings) {
        SharedPreferences prefs = context.getSharedPreferences(
            PREFS_NAME + "_" + getDataType(), Context.MODE_PRIVATE);
        
        prefs.edit()
            .putBoolean("automated", settings.isAutomatedCollection())
            .putInt("frequency", settings.getCollectionFrequency())
            .putBoolean("notifications", settings.isNotificationsEnabled())
            .apply();
    }
    
    /**
     * Get missing permissions
     */
    protected List<String> getMissingPermissions() {
        List<String> missing = new ArrayList<>();
        
        for (String permission : getRequiredPermissions()) {
            if (context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        
        return missing;
    }
}
