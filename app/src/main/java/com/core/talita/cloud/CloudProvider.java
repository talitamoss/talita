package com.core.talita.cloud;

import java.util.List;
import java.util.Map;

/**
 * CloudProvider Interface
 * Defines the contract for cloud storage providers
 */
public interface CloudProvider {
    
    // Provider info
    String getProviderName();
    boolean isAuthenticated();
    void authenticate(AuthCallback callback);
    
    // File operations with callbacks
    void uploadFile(String path, String content, UploadCallback callback);
    void uploadMetadata(String path, byte[] data, UploadCallback callback);
    void downloadFile(String path, String localPath, DownloadCallback callback);
    void downloadMetadata(String path, DownloadCallback callback);
    void listFiles(String path, ListFilesCallback callback);
    void deleteFile(String path, DeleteCallback callback);
    
    // Cloud item types
    enum CloudItemType {
        FILE,
        METADATA,
        DIRECTORY
    }
    
    // Data structures
    class CloudItem {
        public final String path;
        public final String content;
        public CloudItemType type = CloudItemType.METADATA;
        public Map<String, String> metadata;
        
        public CloudItem(String path, String content) {
            this.path = path;
            this.content = content;
        }
        
        public CloudItem(String path, byte[] data) {
            this.path = path;
            this.content = new String(data);
        }
    }
    
    class CloudFile {
        public final String name;
        public final String path;
        public final long size;
        public final long lastModified;
        public final Map<String, String> metadata;
        
        public CloudFile(String name, String path, long size, long lastModified) {
            this(name, path, size, lastModified, null);
        }
        
        public CloudFile(String name, String path, long size, long lastModified, Map<String, String> metadata) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.lastModified = lastModified;
            this.metadata = metadata;
        }
    }
    
    // Callbacks
    interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
    
    interface UploadCallback {
        void onSuccess(String path);
        void onProgress(int percentage);
        void onError(String error);
    }
    
    interface DownloadCallback {
        void onSuccess(String data);
        void onProgress(int percentage);
        void onError(String error);
    }
    
    interface ListFilesCallback {
        void onSuccess(List<CloudFile> files);
        void onError(String error);
    }
    
    interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}
