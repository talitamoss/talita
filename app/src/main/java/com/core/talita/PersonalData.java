package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Iterator;

/**
 * PersonalData - The core data model
 * Represents any piece of personal data with type, timestamp, and metadata
 * 
 * Location: app/src/main/java/com/core/talita/PersonalData.java
 */
public class PersonalData implements PersonalDataInterface {
    
    private String id;
    private String type;
    private long timestamp;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    
    /**
     * Default constructor
     */
    public PersonalData() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Constructor with type
     */
    public PersonalData(String type) {
        this();
        this.type = type;
    }
    
    /**
     * Constructor with type, data, and metadata (for BaseDataCollector compatibility)
     */
    public PersonalData(String type, Map<String, Object> data, Map<String, Object> metadata) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>(data);
        this.metadata = new HashMap<>(metadata);
    }
    
    /**
     * Full constructor
     */
    public PersonalData(String type, Map<String, Object> data, Map<String, Object> metadata, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = timestamp;
    }
    
    /**
     * Factory method to create PersonalData
     */
    public static PersonalData create(String type) {
        return new PersonalData(type);
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    /**
     * Get data type (alias for getType)
     */
    public String getDataType() {
        return getType();
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    /**
     * Get display summary
     */
    public String getDisplaySummary() {
        // Create a summary from the data
        if (data.containsKey("summary")) {
            return data.get("summary").toString();
        } else if (data.containsKey("value")) {
            return data.get("value").toString();
        } else if (data.containsKey("message")) {
            return data.get("message").toString();
        } else if (data.containsKey("description")) {
            return data.get("description").toString();
        } else if (!data.isEmpty()) {
            // Return first data value as summary
            return data.values().iterator().next().toString();
        }
        return type + " data";
    }
    
    // Data manipulation methods
    
    /**
     * Add a single data field
     */
    public void addDataField(String key, Object value) {
        this.data.put(key, value);
    }
    
    /**
     * Add a single metadata field
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Get a data field value
     */
    public Object getDataField(String key) {
        return data.get(key);
    }
    
    /**
     * Get a metadata field value
     */
    public Object getMetadataField(String key) {
        return metadata.get(key);
    }
    
    /**
     * Check if data contains a field
     */
    public boolean hasDataField(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Convert to JSON string
     */
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
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create PersonalData from JSON string
     */
    public static PersonalData fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            PersonalData pd = new PersonalData();
            
            // Parse basic fields
            if (json.has("id")) {
                pd.setId(json.getString("id"));
            }
            if (json.has("type")) {
                pd.setType(json.getString("type"));
            }
            if (json.has("timestamp")) {
                pd.setTimestamp(json.getLong("timestamp"));
            }
            
            // Parse data map
            if (json.has("data")) {
                JSONObject dataJson = json.getJSONObject("data");
                Map<String, Object> dataMap = new HashMap<>();
                Iterator<String> keys = dataJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataMap.put(key, dataJson.get(key));
                }
                pd.setData(dataMap);
            }
            
            // Parse metadata map
            if (json.has("metadata")) {
                JSONObject metadataJson = json.getJSONObject("metadata");
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
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "PersonalData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", data=" + data +
                ", metadata=" + metadata +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalData that = (PersonalData) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
