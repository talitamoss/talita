package com.core.talita.api;

import android.content.Context;
import java.util.List;
import java.util.Map;

/**
 * DataCollector - Interface for all data collectors
 * 
 * Defines the contract that all collectors must implement.
 * Collectors handle HOW to collect specific types of data.
 */
public interface DataCollector {
    
    // ===== Core Identity =====
    
    /**
     * Get the data type this collector handles
     * Examples: "water", "location", "mood", "exercise"
     */
    String getDataType();
    
    /**
     * Get display name for UI
     * Examples: "Water Intake", "Location Tracking", "Mood"
     */
    String getDisplayName();
    
    /**
     * Get description of what this collector does
     */
    String getDescription();
    
    /**
     * Get emoji icon for this collector
     * Examples: "💧", "📍", "😊"
     */
    String getEmoji();
    
    /**
     * Get category: "i" (personal), "we" (social), "all" (universal)
     */
    String getCategory();
    
    // ===== Lifecycle =====
    
    /**
     * Initialize the collector with context
     */
    void initialize(Context context);
    
    /**
     * Clean up resources when collector is destroyed
     */
    void onDestroy();
    
    // ===== Availability & State =====
    
    /**
     * Check if this collector can work on this device
     * (has required sensors, permissions available, etc.)
     */
    boolean isAvailable();
    
    /**
     * Check if user has enabled this collector
     */
    boolean isEnabled();
    
    /**
     * Enable or disable this collector
     */
    void setEnabled(boolean enabled);
    
    // ===== Settings =====
    
    /**
     * Get current collector settings
     */
    CollectorSettings getSettings();
    
    /**
     * Update collector settings
     */
    void updateSettings(CollectorSettings settings);
    
    // ===== Permissions =====
    
    /**
     * Get list of required Android permissions
     * Examples: ["android.permission.ACCESS_FINE_LOCATION"]
     */
    List<String> getRequiredPermissions();
    
    // ===== Collection Methods =====
    
    /**
     * Start automated/background collection
     */
    void startAutomatedCollection();
    
    /**
     * Stop automated/background collection
     */
    void stopAutomatedCollection();
    
    /**
     * Check if currently collecting automatically
     */
    boolean isCollectingAutomatically();
    
    /**
     * Trigger manual collection (shows UI if needed)
     */
    CollectorResult collect();
    
    /**
     * Quick collection with provided data (no UI)
     */
    CollectorResult collectQuick(Map<String, Object> data);
    
    // ===== Callbacks =====
    
    /**
     * Callback for data collection events
     */
    interface CollectionCallback {
        void onDataCollected(String dataType, Map<String, Object> data);
        void onCollectionError(String dataType, String error);
    }
}
