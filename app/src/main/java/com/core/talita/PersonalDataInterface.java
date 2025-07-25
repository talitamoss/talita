package com.core.talita;

import java.util.Map;

/**
 * PersonalDataInterface - Common interface for all personal data types
 * 
 * This interface allows different data implementations to work with
 * the universal data pipeline.
 */
public interface PersonalDataInterface {
    
    /**
     * Get the data type (e.g., "water", "mood", "location")
     */
    String getType();
    
    /**
     * Get the actual data as a map
     */
    Map<String, Object> getData();
    
    /**
     * Get metadata about this data
     */
    Map<String, Object> getMetadata();
    
    /**
     * Get the timestamp when this data was created
     */
    long getTimestamp();
}
