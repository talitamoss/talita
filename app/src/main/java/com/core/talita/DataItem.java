package com.core.talita;

/**
 * Simple data item class for database operations
 */
public class DataItem {
    private String id;
    private String type;
    private long createdAt;
    private String dataJson;
    private String filePath;
    private Object value;
    private long timestamp;
    
    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public long getCreatedAt() { return createdAt; }
    public String getDataJson() { return dataJson; }
    public String getFilePath() { return filePath; }
    public Object getValue() { return value; }
    public long getTimestamp() { return timestamp != 0 ? timestamp : createdAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setValue(Object value) { this.value = value; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
