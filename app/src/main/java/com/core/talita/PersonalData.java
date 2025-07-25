package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * PersonalData - Core data model for all personal information
 * 
 * Represents a single data point collected by the app.
 * Immutable once created to ensure data integrity.
 */
public class PersonalData implements PersonalDataInterface {
    private final String type;
    private final Map<String, Object> data;
    private final Map<String, Object> metadata;
    private final long timestamp;
    
    /**
     * Create new PersonalData with current timestamp
     */
    public PersonalData(String type, Map<String, Object> data, Map<String, Object> metadata) {
        this.type = type;
        this.data = new HashMap<>(data);
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        
        // Add default metadata
        if (!this.metadata.containsKey("version")) {
            this.metadata.put("version", "1.0");
        }
    }
    
    /**
     * Create PersonalData with specific timestamp (for imports/exports)
     */
    public PersonalData(String type, Map<String, Object> data, Map<String, Object> metadata, long timestamp) {
        this.type = type;
        this.data = new HashMap<>(data);
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = timestamp;
    }
    
    /**
     * Factory method to create PersonalData
     */
    public static PersonalData create(String type) {
        return new PersonalData(type, new HashMap<>(), new HashMap<>());
    }
    
    // Implement PersonalDataInterface methods
    
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
        return new HashMap<>(metadata);
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get data type - alias for getType() for backward compatibility
     */
    public String getDataType() {
        return type;
    }
    
    /**
     * Get a specific data value
     */
    public Object getValue(String key) {
        return data.get(key);
    }
    
    /**
     * Get a specific metadata value
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Check if data contains a key
     */
    public boolean hasValue(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("timestamp", timestamp);
            
            // Convert data map to JSON
            JSONObject dataJson = new JSONObject();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                dataJson.put(entry.getKey(), entry.getValue());
            }
            json.put("data", dataJson);
            
            // Convert metadata map to JSON
            JSONObject metadataJson = new JSONObject();
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                metadataJson.put(entry.getKey(), entry.getValue());
            }
            json.put("metadata", metadataJson);
            
            return json.toString();
        } catch (JSONException e) {
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
            
            String type = json.getString("type");
            long timestamp = json.getLong("timestamp");
            
            // Parse data
            Map<String, Object> data = new HashMap<>();
            JSONObject dataJson = json.getJSONObject("data");
            Iterator<String> dataKeys = dataJson.keys();
            while (dataKeys.hasNext()) {
                String key = dataKeys.next();
                data.put(key, dataJson.get(key));
            }
            
            // Parse metadata
            Map<String, Object> metadata = new HashMap<>();
            if (json.has("metadata")) {
                JSONObject metadataJson = json.getJSONObject("metadata");
                Iterator<String> metaKeys = metadataJson.keys();
                while (metaKeys.hasNext()) {
                    String key = metaKeys.next();
                    metadata.put(key, metadataJson.get(key));
                }
            }
            
            return new PersonalData(type, data, metadata, timestamp);
            
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Builder pattern for creating PersonalData
     */
    public static class Builder {
        private String type;
        private Map<String, Object> data = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Long timestamp = null;
        
        public Builder(String type) {
            this.type = type;
        }
        
        public Builder withData(String key, Object value) {
            data.put(key, value);
            return this;
        }
        
        public Builder withAllData(Map<String, Object> data) {
            this.data.putAll(data);
            return this;
        }
        
        public Builder withMetadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }
        
        public Builder withAllMetadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public PersonalData build() {
            if (timestamp != null) {
                return new PersonalData(type, data, metadata, timestamp);
            } else {
                return new PersonalData(type, data, metadata);
            }
        }
    }
    
    @Override
    public String toString() {
        return "PersonalData{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", dataSize=" + data.size() +
                ", metadataSize=" + metadata.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PersonalData that = (PersonalData) o;
        
        if (timestamp != that.timestamp) return false;
        if (!type.equals(that.type)) return false;
        if (!data.equals(that.data)) return false;
        return metadata.equals(that.metadata);
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + metadata.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
