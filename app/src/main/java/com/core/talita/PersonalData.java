package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PersonalData - Core data model for all collected information
 * 
 * This represents a single piece of personal data with:
 * - Type (water, location, mood, etc.)
 * - Timestamp
 * - Data payload (key-value pairs)
 * - Unique ID
 */
public class PersonalData {
    private final String id;
    private final String type;
    private final long timestamp;
    private final Map<String, Object> data;
    
    /**
     * Private constructor - use factory methods
     */
    private PersonalData(String id, String type, long timestamp, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }
    
    /**
     * Create new PersonalData with auto-generated ID and current timestamp
     */
    public static PersonalData create(String type) {
        return new PersonalData(
            UUID.randomUUID().toString(),
            type,
            System.currentTimeMillis(),
            new HashMap<>()
        );
    }
    
    /**
     * Create PersonalData with specific timestamp
     */
    public static PersonalData create(String type, long timestamp) {
        return new PersonalData(
            UUID.randomUUID().toString(),
            type,
            timestamp,
            new HashMap<>()
        );
    }
    
    /**
     * Create PersonalData from JSON
     */
    public static PersonalData fromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        
        String id = obj.getString("id");
        String type = obj.getString("type");
        long timestamp = obj.getLong("timestamp");
        
        Map<String, Object> data = new HashMap<>();
        JSONObject dataObj = obj.getJSONObject("data");
        for (String key : dataObj.keys()) {
            data.put(key, dataObj.get(key));
        }
        
        return new PersonalData(id, type, timestamp, data);
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("type", type);
        obj.put("timestamp", timestamp);
        
        JSONObject dataObj = new JSONObject(data);
        obj.put("data", dataObj);
        
        return obj.toString();
    }
    
    /**
     * Get a summary of the data for display
     */
    public String getSummary() {
        // Generate summary based on type
        switch (type) {
            case "water":
                Object amount = data.get("amount");
                if (amount != null) {
                    return amount + "ml";
                }
                break;
            case "location":
                Object lat = data.get("latitude");
                Object lon = data.get("longitude");
                if (lat != null && lon != null) {
                    return String.format("%.4f, %.4f", lat, lon);
                }
                break;
            case "mood":
                Object mood = data.get("mood");
                if (mood != null) {
                    return mood.toString();
                }
                break;
            default:
                // For other types, try to find a reasonable summary
                if (data.containsKey("value")) {
                    return data.get("value").toString();
                } else if (data.containsKey("name")) {
                    return data.get("name").toString();
                } else if (!data.isEmpty()) {
                    // Return first value
                    return data.values().iterator().next().toString();
                }
        }
        
        return type; // Default to just the type
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data); // Return copy for safety
    }
    
    /**
     * Set the data payload
     */
    public void setData(Map<String, Object> newData) {
        data.clear();
        if (newData != null) {
            data.putAll(newData);
        }
    }
    
    /**
     * Add a single data value
     */
    public void putData(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Get a single data value
     */
    public Object getData(String key) {
        return data.get(key);
    }
    
    @Override
    public String toString() {
        return "PersonalData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", dataSize=" + data.size() +
                '}';
    }
}
