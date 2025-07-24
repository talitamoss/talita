package com.core.talita.collectors;

/**
 * DataCollector Interface
 * 
 * Base interface for all data collection implementations.
 * This defines the contract that all collectors must follow.
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
    void startCollection();
    
    /**
     * Stop automated/background collection
     */
    void stopCollection();
    
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
}
