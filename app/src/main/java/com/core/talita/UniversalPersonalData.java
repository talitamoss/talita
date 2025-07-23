package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Universal personal data class that can represent ANY data type
 * Brand-agnostic: works with any app name
 */
public class UniversalPersonalData implements UniversalDataType, PersonalData {
    private final String id;
    private final String type;
    private final long timestamp;
    private final Map<String, Object> data;
    private final Map<String, Object> metadata;
    private String filePath;
    
    public UniversalPersonalData(String type, Map<String, Object> data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.data = data != null ? data : new HashMap<>();
        this.metadata = new HashMap<>();
        
        // Ensure timestamp is in data
        if (!this.data.containsKey("timestamp")) {
            this.data.put("timestamp", timestamp);
        }
    }
    
    // UniversalDataType methods
    @Override
    public String getId() { return id; }
    
    @Override
    public String getType() { return type; }
    
    @Override
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String getFilePath() { return filePath; }
    
    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("type", type);
            json.put("timestamp", timestamp);
            json.put("data", new JSONObject(data));
            json.put("metadata", new JSONObject(metadata));
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    
    @Override
    public String getDisplayName() {
        return data.getOrDefault("display_name", type).toString();
    }
    
    @Override
    public Map<String, Object> getMetadata() { return metadata; }
    
    @Override
    public double getLatitude() {
        Object lat = data.get("latitude");
        if (lat instanceof Number) {
            return ((Number) lat).doubleValue();
        }
        return 0.0;
    }
    
    @Override
    public double getLongitude() {
        Object lon = data.get("longitude");
        if (lon instanceof Number) {
            return ((Number) lon).doubleValue();
        }
        return 0.0;
    }
    
    // PersonalData methods
    @Override
    public String getDataType() { return type; }
    
    @Override
    public String getDisplaySummary() {
        return data.getOrDefault("summary", "Data collected").toString();
    }
    
    @Override
    public Object getValue() {
        return data.get("value");
    }
    
    // Additional helper methods
    public Map<String, Object> getAllData() { return data; }
    
    public Object getValue(String key) { return data.get(key); }
    
    public void setValue(String key, Object value) { data.put(key, value); }
    
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
}
