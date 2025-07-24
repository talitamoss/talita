package com.core.talita.api;

import android.content.Context;
import java.util.List;
import java.util.Map;

/**
 * DataCollector Interface - The contract all data collectors must implement
 * 
 * This is the public API that plugins use to create data collectors.
 * Keep this interface stable as plugins depend on it.
 */
public interface DataCollector {
    
    /**
     * Initialize the collector with context
     * Called once when the collector is created
     */
    void initialize(Context context);
    
    /**
     * Collect data - shows UI or performs collection
     * @return Result of the collection attempt
     */
    CollectorResult collect();
    
    /**
     * Collect data with parameters (for quick collection)
     * @param data The data to collect
     * @return Result of the collection attempt
     */
    CollectorResult collectQuick(Map<String, Object> data);
    
    /**
     * Start automated/background collection
     * @return true if started successfully
     */
    boolean startAutomatedCollection();
    
    /**
     * Stop automated/background collection
     */
    void stopAutomatedCollection();
    
    /**
     * Check if automated collection is active
     */
    boolean isCollectingAutomatically();
    
    // Metadata methods
    
    /**
     * Get the unique type identifier for this collector
     * Examples: "water", "mood", "location"
     */
    String getType();
    
    /**
     * Get human-readable name
     * Examples: "Water Intake", "Mood Tracker"
     */
    String getDisplayName();
    
    /**
     * Get short description of what this collects
     */
    String getDescription();
    
    /**
     * Get emoji representation
     */
    String getEmoji();
    
    /**
     * Get category (i, we, or all)
     */
    String getCategory();
    
    // Configuration methods
    
    /**
     * Get required Android permissions
     */
    List<String> getRequiredPermissions();
    
    /**
     * Get current settings
     */
    CollectorSettings getSettings();
    
    /**
     * Update settings
     */
    void updateSettings(CollectorSettings settings);
    
    /**
     * Validate data before collection
     * @param data The data to validate
     * @return true if valid
     */
    boolean validateData(Map<String, Object> data);
    
    // Lifecycle methods
    
    /**
     * Called when collector is being destroyed
     */
    void onDestroy();
}
