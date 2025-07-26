package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * UniversalDataType - Interface for all data types in the system
 * Provides a common contract for data handling
 */
public interface UniversalDataType {
    
    /**
     * Get the data type identifier
     */
    String getType();
    
    /**
     * Get the unique ID
     */
    String getId();
    
    /**
     * Get the timestamp
     */
    long getTimestamp();
    
    /**
     * Get metadata
     */
    Map<String, Object> getMetadata();
    
    /**
     * Get display name for UI
     */
    String getDisplayName();
    
    /**
     * Get display summary for UI
     */
    String getDisplaySummary();
    
    /**
     * Get latitude (0.0 if not location data)
     */
    double getLatitude();
    
    /**
     * Get longitude (0.0 if not location data)
     */
    double getLongitude();
}
