package com.core.talita;

/**
 * UniversalDataType - Represents encrypted data stored in the database
 * 
 * This is what gets stored in SQLite after encryption
 */
public class UniversalDataType {
    private long id;
    private final String type;
    private final String encryptedData;
    private final long timestamp;
    private final String summary;
    
    /**
     * Constructor for new data (no ID yet)
     */
    public UniversalDataType(String type, String encryptedData, long timestamp, String summary) {
        this.type = type;
        this.encryptedData = encryptedData;
        this.timestamp = timestamp;
        this.summary = summary;
    }
    
    /**
     * Constructor for data loaded from database (has ID)
     */
    public UniversalDataType(long id, String type, String encryptedData, long timestamp, String summary) {
        this.id = id;
        this.type = type;
        this.encryptedData = encryptedData;
        this.timestamp = timestamp;
        this.summary = summary;
    }
    
    // Getters
    public long getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public String getEncryptedData() {
        return encryptedData;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getSummary() {
        return summary;
    }
    
    // Setter for ID (set after database insert)
    public void setId(long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "UniversalDataType{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", summary='" + summary + '\'' +
                ", dataLength=" + (encryptedData != null ? encryptedData.length() : 0) +
                '}';
    }
}
