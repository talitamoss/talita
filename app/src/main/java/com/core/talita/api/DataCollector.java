package com.core.talita.api;

import android.content.Context;
import java.util.List;
import java.util.Map;

/**
 * DataCollector - Base interface for all data collection implementations
 */
public interface DataCollector {
    
    // Initialization
    void initialize(Context context);
    void onDestroy();
    
    // Core collection methods
    CollectorResult collect();
    CollectorResult collectQuick(Map<String, Object> data);
    
    // Automated collection
    void startAutomatedCollection();
    void stopAutomatedCollection();
    boolean isCollectingAutomatically();
    
    // Configuration
    void updateSettings(CollectorSettings settings);
    CollectorSettings getSettings();
    
    // Metadata
    String getType();
    String getDisplayName();
    String getDescription();
    String getEmoji();
    String getCategory();
    List<String> getRequiredPermissions();
    
    // Simplified methods for compatibility
    default String getDataType() {
        return getType();
    }
}
