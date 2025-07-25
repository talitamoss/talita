package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * UniversalPersonalData - Universal format for all personal data
 * 
 * This is the standardized format that all data types convert to
 * before encryption and storage.
 */
public class UniversalPersonalData implements PersonalDataInterface {
    private final String type;
    private final Map<String, Object> data;
    private final long timestamp;
    
    /**
     * Create new universal data
     */
    public UniversalPersonalData(String type, Map<String, Object> data) {
        this.type = type;
        this.data = new HashMap<>(data);
        this.timestamp = data.containsKey("timestamp") ? 
            ((Number) data.get("timestamp")).longValue() : 
            System.currentTimeMillis();
    }
    
    /**
     * Create with explicit timestamp
     */
    public UniversalPersonalData(String type, Map<String, Object> data, long timestamp) {
        this.type = type;
        this.data = new HashMap<>(data);
        this.timestamp = timestamp;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        // Extract metadata from data if present
        if (data.containsKey("metadata") && data.get("metadata") instanceof Map) {
            return new HashMap<>((Map<String, Object>) data.get("metadata"));
        }
        return new HashMap<>();
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get data type - alias for getType()
     */
    public String getDataType() {
        return type;
    }
    
    /**
     * Convert to JSON
     */
    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("timestamp", timestamp);
        
        // Put all data
        JSONObject dataJson = new JSONObject(data);
        json.put("data", dataJson);
        
        return json.toString();
    }
    
    /**
     * Create from JSON
     */
    public static UniversalPersonalData fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        
        String type = json.getString("type");
        long timestamp = json.getLong("timestamp");
        
        // Parse data
        Map<String, Object> data = new HashMap<>();
        JSONObject dataJson = json.getJSONObject("data");
        Iterator<String> keys = dataJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            data.put(key, dataJson.get(key));
        }
        
        return new UniversalPersonalData(type, data, timestamp);
    }
    
    /**
     * Get a specific value from data
     */
    public Object getValue(String key) {
        return data.get(key);
    }
    
    /**
     * Check if data contains a key
     */
    public boolean hasValue(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Create a summary string
     */
    public String createSummary() {
        // Type-specific summaries
        switch (type) {
            case "water":
                Object amount = getValue("amount");
                return amount != null ? amount + "ml" : "Water logged";
                
            case "mood":
                Object mood = getValue("mood");
                Object score = getValue("score");
                if (mood != null && score != null) {
                    return mood + " (" + score + "/5)";
                }
                return "Mood logged";
                
            case "exercise":
                Object activity = getValue("activity");
                Object duration = getValue("duration");
                if (activity != null) {
                    return activity + (duration != null ? " - " + duration + " min" : "");
                }
                return "Exercise logged";
                
            default:
                // Try to create a generic summary
                if (data.containsKey("summary")) {
                    return String.valueOf(data.get("summary"));
                }
                if (data.containsKey("display_name")) {
                    return String.valueOf(data.get("display_name"));
                }
                return type + " recorded";
        }
    }
    
    @Override
    public String toString() {
        return "UniversalPersonalData{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", dataSize=" + data.size() +
                '}';
    }
}
