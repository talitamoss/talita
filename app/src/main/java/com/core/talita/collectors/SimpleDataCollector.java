package com.core.talita.collectors;

import android.content.Context;
import android.util.Log;
import com.core.talita.PersonalData;
import com.core.talita.TalitaDataType;
import com.core.talita.UniversalDataService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimpleDataCollector - A generic collector that plugins can use
 * Handles the basic data collection flow for simple data types
 */
public class SimpleDataCollector implements DataCollector, TalitaDataType {
    private static final String TAG = "SimpleDataCollector";
    
    private final String dataType;
    private final String displayName;
    private final String emoji;
    private UniversalDataService dataService;
    
    public SimpleDataCollector(String dataType, String displayName, String emoji) {
        this.dataType = dataType;
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    @Override
    public void collect() {
        // This would be called from UI - for now just log
        Log.d(TAG, "Collecting data for: " + displayName);
        
        // In real usage, this would show a dialog or activity
        // For now, create sample data
        logData("Sample " + displayName + " entry");
    }
    
    public void logData(Object value) {
        if (dataService == null) {
            Log.e(TAG, "DataService not initialized!");
            return;
        }
        
        try {
            PersonalData data = new PersonalData(dataType);
            
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("value", value);
            dataMap.put("timestamp", System.currentTimeMillis());
            dataMap.put("emoji", emoji);
            
            data.setData(dataMap);
            dataService.saveData(data);
            
            Log.d(TAG, "✅ Saved " + displayName + " data: " + value);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to save data", e);
        }
    }
    
    @Override
    public void startCollection(Context context) {
        dataService = new UniversalDataService(context);
        Log.d(TAG, "Started collection for: " + displayName);
    }
    
    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "Stopped collection for: " + displayName);
    }
    
    @Override
    public List<String> getRequiredPermissions() {
        return new ArrayList<>(); // No special permissions
    }
    
    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
            .setFrequency(0) // Manual collection
            .setBatteryOptimized(true);
    }
    
    // TalitaDataType implementation
    @Override
    public String getDataTypeName() {
        return dataType;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getDescription() {
        return "Track your " + displayName.toLowerCase();
    }
    
    @Override
    public Map<String, Object> serializeData(Object data) {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("value", data);
        return map;
    }
    
    @Override
    public Object deserializeData(Map<String, Object> data) {
        return data;
    }
    
    @Override
    public String getDataCategory() {
        return "plugin_data";
    }
    
    @Override
    public boolean requiresEncryption() {
        return true;
    }
    
    @Override
    public boolean canExport() {
        return true;
    }
    
    @Override
    public String exportFormat() {
        return "json";
    }
}
