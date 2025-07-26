package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * PersonalData - Core data model for all personal data types
 * 
 * This class represents any piece of personal data collected by the app.
 * It implements PersonalDataInterface for compatibility with the data pipeline.
 */
public class PersonalData implements PersonalDataInterface {
    
    private String type;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private long timestamp;
    
    /**
     * Default constructor
     */
    public PersonalData() {
        this.data = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor with type
     */
    public PersonalData(String type) {
        this();
        this.type = type;
    }
    
    /**
     * Full constructor
     */
    public PersonalData(String type, Map<String, Object> data, Map<String, Object> metadata, long timestamp) {
        this.type = type;
        this.data = data != null ? data : new HashMap<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.timestamp = timestamp;
    }
    
    // PersonalDataInterface implementation
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Map<String, Object> getData() {
        return data;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
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
    
    // Convenience methods
    
    public void addData(String key, Object value) {
        data.put(key, value);
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getDataValue(String key) {
        return data.get(key);
    }
    
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Get data type (alias for getType)
     */
    public String getDataType() {
        return type;
    }
    public String getDisplaySummary() {
        // Generate summary based on type
        switch (type) {
            case "water":
                Integer ml = (Integer) data.get("volume_ml");
                return ml != null ? ml + "ml" : "Water";
                
            case "exercise":
                String exerciseType = (String) data.get("exercise_type");
                String duration = (String) data.get("duration");
                if (exerciseType != null && duration != null) {
                    return exerciseType + " - " + duration;
                }
                return exerciseType != null ? exerciseType : "Exercise";
                
            case "mood":
                Integer rating = (Integer) data.get("mood_rating");
                return rating != null ? "Mood: " + rating + "/5" : "Mood";
                
            case "sleep":
                Double hours = (Double) data.get("hours_slept");
                return hours != null ? String.format("%.1fh sleep", hours) : "Sleep";
                
            case "location":
                Double lat = (Double) data.get("latitude");
                Double lon = (Double) data.get("longitude");
                if (lat != null && lon != null) {
                    return String.format("📍 %.4f, %.4f", lat, lon);
                }
                return "Location";
                
            case "audio":
                Long audioDuration = (Long) data.get("duration_ms");
                if (audioDuration != null) {
                    int seconds = (int) (audioDuration / 1000);
                    return String.format("🎙️ %d:%02d", seconds / 60, seconds % 60);
                }
                return "Audio recording";
                
            case "steps":
                Integer steps = (Integer) data.get("count");
                return steps != null ? steps + " steps" : "Steps";
                
            default:
                // For custom types, try to create a meaningful summary
                if (!data.isEmpty()) {
                    Object firstValue = data.values().iterator().next();
                    return type + ": " + firstValue;
                }
                return type;
        }
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("timestamp", timestamp);
            json.put("data", new JSONObject(data));
            json.put("metadata", new JSONObject(metadata));
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Create from JSON string
     */
    public static PersonalData fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            PersonalData pd = new PersonalData();
            
            pd.setType(json.optString("type"));
            pd.setTimestamp(json.optLong("timestamp", System.currentTimeMillis()));
            
            // Parse data
            JSONObject dataJson = json.optJSONObject("data");
            if (dataJson != null) {
                Map<String, Object> dataMap = new HashMap<>();
                Iterator<String> keys = dataJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataMap.put(key, dataJson.get(key));
                }
                pd.setData(dataMap);
            }
            
            // Parse metadata
            JSONObject metadataJson = json.optJSONObject("metadata");
            if (metadataJson != null) {
                Map<String, Object> metadataMap = new HashMap<>();
                Iterator<String> keys = metadataJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    metadataMap.put(key, metadataJson.get(key));
                }
                pd.setMetadata(metadataMap);
            }
            
            return pd;
            
        } catch (Exception e) {
            return null;
        }
    }
}
