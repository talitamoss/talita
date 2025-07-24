package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Universal personal data class that can represent ANY data type
 * Implements UniversalDataType only (not PersonalData since it's a class)
 */
public class UniversalPersonalData implements UniversalDataType {
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
        this.filePath = null;
    }

    // UniversalDataType methods
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

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
    public String getFilePath() {
        return filePath;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getLatitude() {
        if (data.containsKey("latitude")) {
            Object lat = data.get("latitude");
            if (lat instanceof Number) {
                return ((Number) lat).doubleValue();
            }
        }
        return 0.0;
    }

    @Override
    public double getLongitude() {
        if (data.containsKey("longitude")) {
            Object lon = data.get("longitude");
            if (lon instanceof Number) {
                return ((Number) lon).doubleValue();
            }
        }
        return 0.0;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String getDisplayName() {
        if (data.containsKey("display_name")) {
            return String.valueOf(data.get("display_name"));
        }
        return type + " data";
    }

    @Override
    public String getDisplaySummary() {
        if (data.containsKey("summary")) {
            return String.valueOf(data.get("summary"));
        }
        return getDisplayName();
    }

    // Methods that were from PersonalData interface
    public String getDataType() {
        return type;
    }

    public Object getValue() {
        return data.get("value");
    }
    
    // Additional helper methods
    public Map<String, Object> getAllData() {
        Map<String, Object> allData = new HashMap<>(data);
        allData.put("id", id);
        allData.put("type", type);
        allData.put("timestamp", timestamp);
        return allData;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
