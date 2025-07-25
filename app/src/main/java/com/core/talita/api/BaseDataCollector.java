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
        
        // Stop collection if disabling
        if (!enabled && isCollecting) {
            stopAutomatedCollection();
        }
        
        Log.d(TAG, getDataType() + " enabled: " + enabled);
    }
    
    /**
     * Get current settings
     */
    @Override
    public CollectorSettings getSettings() {
        return settings;
    }
    
    /**
     * Update settings
     */
    @Override
    public void updateSettings(CollectorSettings newSettings) {
        this.settings = newSettings;
        saveSettings();
        
        // Apply changes
        if (isCollecting) {
            stopAutomatedCollection();
            if (settings.isAutomatedCollection()) {
                startAutomatedCollection();
            }
        }
    }
    
    /**
     * Start automated collection
     */
    @Override
    public void startAutomatedCollection() {
        if (!isAvailable() || !isEnabled()) {
            Log.w(TAG, "Cannot start collection - collector not available or enabled");
            return;
        }
        
        if (!settings.isAutomatedCollection()) {
            Log.w(TAG, "Automated collection not enabled in settings");
            return;
        }
        
        if (isCollecting) {
            Log.d(TAG, "Already collecting");
            return;
        }
        
        isCollecting = true;
        onStartCollection();
        Log.d(TAG, "Started automated collection: " + getDataType());
    }
    
    /**
     * Stop automated collection
     */
    @Override
    public void stopAutomatedCollection() {
        if (!isCollecting) {
            return;
        }
        
        isCollecting = false;
        onStopCollection();
        Log.d(TAG, "Stopped automated collection: " + getDataType());
    }
    
    /**
     * Check if currently collecting
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
    
    /**
     * Handle data collected
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
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = getDataType() + "_settings";
        
        // Load saved settings or return defaults
        return CollectorSettings.getDefault(); // Override to load custom settings
    }
    
    /**
     * Save settings to preferences
     */
    protected void saveSettings() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = getDataType() + "_settings";
        
        // Save settings
        // Override to implement custom saving
    }
    
    /**
     * Check missing permissions
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
    
    /**
     * Cleanup resources
     */
    @Override
    public void onDestroy() {
        if (isCollecting) {
            stopAutomatedCollection();
        }
        activeCallback = null;
    }
    
    // Abstract methods to implement
    
    /**
     * Check if device has required capabilities
     */
    protected abstract boolean checkDeviceCapabilities();
    
    /**
     * Called when automated collection starts
     */
    protected abstract void onStartCollection();
    
    /**
     * Called when automated collection stops
     */
    protected abstract void onStopCollection();
    
    /**
     * Perform manual collection
     */
    protected abstract CollectorResult performCollection();
    
    /**
     * Perform quick collection with provided data
     */
    protected abstract CollectorResult performQuickCollection(Map<String, Object> data);
}
