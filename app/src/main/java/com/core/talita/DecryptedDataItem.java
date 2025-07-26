package com.core.talita;

/**
 * DecryptedDataItem - Represents an encrypted data item from the database
 * 
 * Note: The data is still encrypted in this object. 
 * It needs to be decrypted using EncryptionService when accessed.
 */
public class DecryptedDataItem {
    private String id;
    private String type;
    private String encryptedData;
    private String filePath;
    private long timestamp;
    private long createdAt;
    private boolean synced;
    
    // Default constructor
    public DecryptedDataItem() {}
    
    // Getters
    public String getId() { 
        return id; 
    }
    
    public String getType() { 
        return type; 
    }
    
    public String getEncryptedData() { 
        return encryptedData; 
    }
    
    public String getFilePath() { 
        return filePath; 
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public long getCreatedAt() { 
        return createdAt; 
    }
    
    public boolean isSynced() { 
        return synced; 
    }
    
    // Setters
    public void setId(String id) { 
        this.id = id; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }
    
    public void setEncryptedData(String encryptedData) { 
        this.encryptedData = encryptedData; 
    }
    
    public void setFilePath(String filePath) { 
        this.filePath = filePath; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
    
    public void setCreatedAt(long createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public void setSynced(boolean synced) { 
        this.synced = synced; 
    }
}
