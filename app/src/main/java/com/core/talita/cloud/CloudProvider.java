package com.core.talita.cloud;

import java.util.List;
import java.util.Map;

/**
 * Cloud Provider Interface
 * 
 * Base interface that all cloud storage providers must implement.
 * This enables pluggable cloud backends (Google Drive, Dropbox, Solid Pods, etc.)
 */
public interface CloudProvider {
    
    /**
     * Provider identification
     */
    String getProviderId();
    String getProviderName();
    String getProviderDescription();
    int getProviderIcon();
    
    /**
     * Authentication
     */
    boolean isAuthenticated();
    void authenticate(AuthenticationCallback callback);
    void deauthenticate();
    String getAccountEmail();
    
    /**
     * File Operations
     */
    void uploadFile(String localPath, String remotePath, UploadCallback callback);
    void downloadFile(String remotePath, String localPath, DownloadCallback callback);
    void deleteFile(String remotePath, OperationCallback callback);
    void listFiles(String remotePath, ListFilesCallback callback);
    
    /**
     * Metadata Operations (for smaller data like settings, records)
     */
    void uploadMetadata(String key, byte[] data, UploadCallback callback);
    void downloadMetadata(String key, DownloadCallback callback);
    void deleteMetadata(String key, OperationCallback callback);
    void listMetadata(ListMetadataCallback callback);
    
    /**
     * Batch Operations
     */
    void uploadBatch(List<CloudItem> items, BatchUploadCallback callback);
    void downloadBatch(List<String> remoteNames, String localFolder, BatchDownloadCallback callback);
    
    /**
     * Storage Info
     */
    void getStorageInfo(StorageInfoCallback callback);
    
    /**
     * Provider-specific configuration
     */
    void configure(Map<String, Object> config);
    Map<String, Object> getConfiguration();

    // Callback Interfaces

    interface AuthenticationCallback {
        void onSuccess();
        void onFailure(CloudError error);
    }

    interface UploadCallback {
        void onProgress(long bytesUploaded, long totalBytes);
        void onSuccess(String remoteUrl);
        void onFailure(CloudError error);
    }

    interface DownloadCallback {
        void onProgress(long bytesDownloaded, long totalBytes);
        void onSuccess(String localPath, byte[] data); // data for metadata, null for files
        void onFailure(CloudError error);
    }

    interface OperationCallback {
        void onSuccess();
        void onFailure(CloudError error);
    }

    interface ListFilesCallback {
        void onSuccess(List<CloudFile> files);
        void onFailure(CloudError error);
    }

    interface ListMetadataCallback {
        void onSuccess(List<CloudMetadata> metadata);
        void onFailure(CloudError error);
    }

    interface BatchUploadCallback {
        void onProgress(int completed, int total);
        void onItemComplete(CloudItem item, boolean success, CloudError error);
        void onAllComplete(int successful, int failed);
    }

    interface BatchDownloadCallback {
        void onProgress(int completed, int total);
        void onItemComplete(String remoteName, boolean success, CloudError error);
        void onAllComplete(int successful, int failed);
    }

    interface StorageInfoCallback {
        void onSuccess(CloudStorageInfo storageInfo);
        void onFailure(CloudError error);
    }

    // Data Classes

    class CloudError {
        public final int code;
        public final String message;
        public final Exception exception;
        
        public CloudError(int code, String message, Exception exception) {
            this.code = code;
            this.message = message;
            this.exception = exception;
        }
    }

    class CloudFile {
        public final String name;
        public final String path;
        public final long size;
        public final long modifiedTime;
        public final boolean isDirectory;
        
        public CloudFile(String name, String path, long size, long modifiedTime, boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.modifiedTime = modifiedTime;
            this.isDirectory = isDirectory;
        }
    }

    class CloudMetadata {
        public final String key;
        public final long size;
        public final long timestamp;
        
        public CloudMetadata(String key, long size, long timestamp) {
            this.key = key;
            this.size = size;
            this.timestamp = timestamp;
        }
    }

    class CloudItem {
        public final String localPath;
        public final String remotePath;
        public final boolean isMetadata;
        public final byte[] data; // For metadata
        
        public CloudItem(String localPath, String remotePath) {
            this.localPath = localPath;
            this.remotePath = remotePath;
            this.isMetadata = false;
            this.data = null;
        }
        
        public CloudItem(String key, byte[] data) {
            this.localPath = key;
            this.remotePath = key;
            this.isMetadata = true;
            this.data = data;
        }
    }

    class CloudStorageInfo {
        public final long totalSpace;
        public final long usedSpace;
        public final long freeSpace;
        public final String accountType;
        
        public CloudStorageInfo(long totalSpace, long usedSpace, long freeSpace, String accountType) {
            this.totalSpace = totalSpace;
            this.usedSpace = usedSpace;
            this.freeSpace = freeSpace;
            this.accountType = accountType;
        }
    }
}
