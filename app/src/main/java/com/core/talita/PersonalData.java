package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PersonalData - Core data container for all personal information
 * Implements UniversalDataType for automatic handling by the system
 */
public class PersonalData implements UniversalDataType {
    private final String type;
    private final String id;
    private final long timestamp;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private String filePath;
    private double latitude = 0.0;
    private double longitude = 0.0;

    // Private constructor - use factory method
    private PersonalData(String type) {
        this.type = type;
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    // Factory method
    public static PersonalData create(String type) {
        return new PersonalData(type);
    }

    // Setters
    public void setData(Map<String, Object> data) {
        this.data = new HashMap<>(data);
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    // Getters
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    // UniversalDataType implementation
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
            json.put("type", type);
            json.put("id", id);
            json.put("timestamp", timestamp);
            json.put("data", new JSONObject(data));
            json.put("metadata", new JSONObject(metadata));
            
            if (latitude != 0.0 || longitude != 0.0) {
                json.put("latitude", latitude);
                json.put("longitude", longitude);
            }
            
            if (filePath != null) {
                json.put("filePath", filePath);
            }
            
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
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    @Override
    public String getDisplayName() {
        // Try to get a meaningful name from data
        if (data.containsKey("name")) {
            return data.get("name").toString();
        }
        return type.replace("_", " ").substring(0, 1).toUpperCase() + 
               type.replace("_", " ").substring(1);
    }

    @Override
    public String getDisplaySummary() {
        // Generate a summary based on data content
        if (data.containsKey("value")) {
            return data.get("value").toString();
        } else if (data.containsKey("amount")) {
            return data.get("amount").toString() + " " + data.getOrDefault("unit", "");
        } else if (!data.isEmpty()) {
            return data.size() + " items";
        }
        return "No data";
    }
}
