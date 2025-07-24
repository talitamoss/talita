package com.core.talita;

/**
 * This interface defines the contract that all data types must implement
 * to work with the universal data processing pipeline.
 */
public interface PersonalDataInterface {
    /**
     * Unique identifier for this data instance
     */
    String getId();
    
    /**
     * Type of data (e.g., "location", "audio", "mood")
     */
    String getType();
    
    /**
     * Unix timestamp when data was collected
     */
    long getTimestamp();
    
    /**
     * The actual data value (JSON string format)
     */
    String getValue();
    
    /**
     * Additional metadata about the data (JSON string format)
     */
    String getMetadata();
    
    /**
     * Human-readable display name
     */
    String getDisplayName();
    
    /**
     * Whether this data is encrypted
     */
    boolean isEncrypted();
    
    /**
     * Whether this data has been backed up to cloud
     */
    boolean isBackedUp();
}
