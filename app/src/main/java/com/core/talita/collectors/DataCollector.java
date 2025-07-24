package com.core.talita.collectors;

import android.content.Context;
import java.util.List;

/**
 * DataCollector Interface
 * 
 * Base interface for all data collection implementations.
 * Used by plugins to implement their collection logic.
 */
public interface DataCollector {
    
    /**
     * Perform data collection
     * This method is called when the user initiates data collection
     */
    void collect();
    
    /**
     * Start automated/background collection
     * Used for collectors that support continuous tracking
     */
    void startCollection(Context context);
    
    /**
     * Stop automated/background collection
     */
    void stopCollection(Context context);
    
    /**
     * Check if collector is currently active
     */
    boolean isCollecting();
    
    /**
     * Get the collector type identifier
     */
    String getType();
    
    /**
     * Get human-readable name
     */
    String getName();
    
    /**
     * Get emoji/icon representation
     */
    String getEmoji();
    
    /**
     * Validate collected data before storage
     */
    boolean validateData(Object data);
    
    /**
     * Get default configuration for this collector
     */
    CollectorSettings getDefaultSettings();
    
    /**
     * Get required permissions for this collector
     */
    List<String> getRequiredPermissions();
    
    /**
     * Get current settings
     */
    CollectorSettings getSettings();
}
