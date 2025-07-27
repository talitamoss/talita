package com.core.talita;

/**
 * EncryptedData - Represents encrypted data stored in the database
 * 
 * This class holds encrypted content along with metadata needed
 * for storage and retrieval.
 */
public class EncryptedData {
    private final String id;
    private final String type;
    private final String encryptedContent;
    private final String filePath;
    private final long timestamp;
    
    public EncryptedData(String id, String type, String encryptedContent, 
                        String filePath, long timestamp) {
        this.id = id;
        this.type = type;
        this.encryptedContent = encryptedContent;
        this.filePath = filePath;
        this.timestamp = timestamp;
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public String getEncryptedContent() {
        return encryptedContent;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "EncryptedData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", hasContent=" + (encryptedContent != null) +
                ", hasFile=" + (filePath != null) +
                ", timestamp=" + timestamp +
                '}';
    }
}
