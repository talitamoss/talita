package com.core.talita.cloud;

/**
 * CloudProvider - Interface for cloud backup providers
 * 
 * Implement this interface to add support for different
 * cloud storage providers (Google Drive, Dropbox, etc.)
 */
public interface CloudProvider {
    
    /**
     * Get unique provider ID
     */
    String getProviderId();
    
    /**
     * Get display name for UI
     */
    String getDisplayName();
    
    /**
     * Check if provider is authenticated
     */
    boolean isAuthenticated();
    
    /**
     * Authenticate with the provider
     */
    boolean authenticate();
    
    /**
     * Upload data (JSON)
     */
    boolean uploadData(String id, String type, String jsonData);
    
    /**
     * Upload file
     */
    boolean uploadFile(String id, String filePath);
    
    /**
     * Download data
     */
    String downloadData(String id);
    
    /**
     * Download file
     */
    boolean downloadFile(String id, String localPath);
    
    /**
     * Delete data
     */
    boolean deleteData(String id);
    
    /**
     * List all backup IDs
     */
    String[] listBackups();
    
    /**
     * Get storage info
     */
    StorageInfo getStorageInfo();
    
    /**
     * Storage information
     */
    class StorageInfo {
        public final long usedBytes;
        public final long totalBytes;
        public final boolean unlimited;
        
        public StorageInfo(long usedBytes, long totalBytes, boolean unlimited) {
            this.usedBytes = usedBytes;
            this.totalBytes = totalBytes;
            this.unlimited = unlimited;
        }
        
        public double getUsagePercent() {
            if (unlimited || totalBytes == 0) return 0;
            return (double) usedBytes / totalBytes * 100;
        }
    }
}
