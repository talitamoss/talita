package com.core.talita.plugins.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.PersonalData;
import com.core.talita.UniversalDataService;
import java.util.*;

/**
 * BaseDataCollector - Abstract base class for data collectors
 * 
 * Provides common functionality that most collectors need.
 * Plugins can extend this instead of implementing DataCollector directly.
 * 
 * Fixed to properly implement the DataCollector interface with void return type
 * for startAutomatedCollection().
 */
public abstract class BaseDataCollector implements DataCollector {
    protected static final String TAG = "BaseDataCollector";
    private static final String PREFS_NAME = "collector_settings";
    
    protected Context context;
    protected CollectorSettings settings;
    protected boolean isAutomatedCollectionActive = false;
    private UniversalDataService dataService;
    private SharedPreferences prefs;

    public BaseDataCollector() {
        this.settings = getDefaultSettings();
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Load saved settings
        loadSettings();
        
        Log.d(TAG, "Initialized collector: " + getDisplayName());
    }

    @Override
    public void onDestroy() {
        if (isAutomatedCollectionActive) {
            stopAutomatedCollection();
        }
        Log.d(TAG, "Destroyed collector: " + getDataType());
    }
    
    @Override
    public boolean isAvailable() {
        // By default, collectors are available
        // Override this if your collector needs specific hardware/permissions
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        if (context == null) return false;
        return prefs.getBoolean(getDataType() + "_enabled", true);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        if (context == null) return;
        
        prefs.edit().putBoolean(getDataType() + "_enabled", enabled).apply();
        
        if (!enabled && isAutomatedCollectionActive) {
            stopAutomatedCollection();
        }
    }
    
    @Override
    public CollectorSettings getSettings() {
        return settings;
    }
    
    @Override
    public void updateSettings(CollectorSettings newSettings) {
        this.settings = newSettings;
        saveSettings();
        onSettingsUpdated(newSettings);
    }
    
    @Override
    public List<String> getRequiredPermissions() {
        // By default, no special permissions needed
        return new ArrayList<>();
    }
    
    @Override
    public void startAutomatedCollection() {
        if (!settings.isAutomatedCollection()) {
            Log.w(TAG, "Automated collection not enabled for: " + getDataType());
            return;
        }
        
        if (!isEnabled()) {
            Log.w(TAG, "Collector not enabled: " + getDataType());
            return;
        }
        
        isAutomatedCollectionActive = true;
        Log.d(TAG, "Started automated collection: " + getDataType());
        onAutomatedCollectionStarted();
    }
    
    @Override
    public void stopAutomatedCollection() {
        isAutomatedCollectionActive = false;
        Log.d(TAG, "Stopped automated collection: " + getDataType());
        onAutomatedCollectionStopped();
    }
    
    @Override
    public boolean isCollectingAutomatically() {
        return isAutomatedCollectionActive;
    }
    
    @Override
    public CollectorResult collect() {
        // Default implementation for manual collection
        // Override this for custom UI
        return CollectorResult.pending(getDataType());
    }
    
    @Override
    public CollectorResult collectQuick(Map<String, Object> data) {
        if (!validateData(data)) {
            return CollectorResult.failure(getDataType(), "Invalid data");
        }

        try {
            // Save the data
            saveData(data);
            
            Log.d(TAG, "Quick collected: " + getDataType());
            return CollectorResult.success(getDataType(), data);
            
        } catch (Exception e) {
            Log.e(TAG, "Quick collection failed", e);
            return CollectorResult.failure(getDataType(), e.getMessage());
        }
    }
    
    // Protected helper methods for subclasses
    
    /**
     * Save data using the UniversalDataService
     */
    protected void saveData(Map<String, Object> data) {
        PersonalData personalData = PersonalData.create(getDataType());
        personalData.setData(data);
        dataService.saveData(personalData);
    }
    
    /**
     * Get the application context
     */
    protected Context getContext() {
        return context;
    }
    
    /**
     * Get the data service for advanced operations
     */
    protected UniversalDataService getDataService() {
        return dataService;
    }
    
    /**
     * Load settings from SharedPreferences
     */
    private void loadSettings() {
        String key = getDataType() + "_settings";
        // In a real implementation, you'd deserialize from JSON
        // For now, just use defaults
        this.settings = getDefaultSettings();
    }
    
    /**
     * Save settings to SharedPreferences
     */
    private void saveSettings() {
        String key = getDataType() + "_settings";
        // In a real implementation, you'd serialize to JSON
        // For now, just log
        Log.d(TAG, "Settings saved for: " + getDataType());
    }
    
    // Abstract and overridable methods
    
    /**
     * Get default settings for this collector
     * Subclasses should override this to provide appropriate defaults
     */
    protected abstract CollectorSettings getDefaultSettings();
    
    /**
     * Validate data before saving
     * Override this to add custom validation
     */
    public boolean validateData(Map<String, Object> data) {
        return data != null && !data.isEmpty();
    }
    
    /**
     * Called when automated collection is started
     * Override to start sensors, timers, etc.
     */
    protected void onAutomatedCollectionStarted() {
        // Override if needed
    }
    
    /**
     * Called when automated collection is stopped
     * Override to clean up resources
     */
    protected void onAutomatedCollectionStopped() {
        // Override if needed
    }
    
    /**
     * Called when settings are updated
     * Override to react to setting changes
     */
    protected void onSettingsUpdated(CollectorSettings newSettings) {
        // Override if needed
    }
}
