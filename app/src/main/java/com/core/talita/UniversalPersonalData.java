package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * UniversalPersonalData - Internal representation for data processing
 * Used by UniversalDataService for data transformation
 */
public class UniversalPersonalData {
    
    private String type;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private long timestamp;
    private String filePath;
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    /**
     * Default constructor
     */
    public UniversalPersonalData() {
        this.data = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with type and data
     */
    public UniversalPersonalData(String type, Map<String, Object> data) {
        this();
        this.type = type;
        this.data = data != null ? data : new HashMap<>();
    }
    
    /**
     * Constructor with type, data, and timestamp
     */
    public UniversalPersonalData(String type, Map<String, Object> data, long timestamp) {
        this(type, data);
        this.timestamp = timestamp;
    }
    
    // Getters
    
    public String getType() {
        return type;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    // Setters
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() {
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("type", type);
            json.put("timestamp", timestamp);
            json.put("data", data);
            json.put("metadata", metadata);
            
            if (filePath != null) {
                json.put("filePath", filePath);
            }
            
            if (latitude != 0.0 || longitude != 0.0) {
                Map<String, Double> location = new HashMap<>();
                location.put("latitude", latitude);
                location.put("longitude", longitude);
                json.put("location", location);
            }
            
            // Simple JSON serialization
            return serializeMap(json);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String serializeMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                sb.append(serializeMap((Map<String, Object>) value));
            } else {
                sb.append(value);
            }
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
}
