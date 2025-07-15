package com.core.talita.cloud;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Universal Cloud Provider Interface
 *
 * Any cloud service (Greenhost, AWS, Google Drive, etc.) implements this interface
 * to provide secure backup capabilities for Talita's encrypted data.
 *
 * Key Design Principles:
 * - Provider-agnostic (works with any cloud service)
 * - Encryption-first (all data already encrypted before reaching provider)
 * - Async operations with progress callbacks
 * - Comprehensive error handling
 * - Metadata preservation
 */
public interface CloudProvider {

    /**
     * Provider identification and capabilities
     */
    String getProviderName();
    String getProviderDisplayName();
    boolean supportsFileStorage();
    boolean supportsMetadataStorage();
    boolean supportsProgressCallbacks();
    long getMaxFileSize(); // in bytes, -1 for unlimited

    /**
     * Authentication and connection
     */
    boolean isAuthenticated();
    void authenticate(AuthenticationCallback callback);
    void disconnect();
    CloudConnectionStatus getConnectionStatus();

    /**
     * File operations (for encrypted audio files, photos, etc.)
     */
    void uploadFile(
            String localFilePath,
            String remoteFileName,
            Map<String, String> metadata,
            UploadCallback callback
    );

    void downloadFile(
            String remoteFileName,
            String localFilePath,
            DownloadCallback callback
    );

    void deleteFile(String remoteFileName, OperationCallback callback);

    void listFiles(String folder, ListFilesCallback callback);

    /**
     * Metadata operations (for encrypted JSON data)
     */
    void uploadMetadata(
            String key,
            String encryptedJsonData,
            Map<String, String> metadata,
            UploadCallback callback
    );

    void downloadMetadata(String key, DownloadCallback callback);

    void deleteMetadata(String key, OperationCallback callback);

    void listMetadata(String prefix, ListMetadataCallback callback);

    /**
     * Batch operations for efficiency
     */
    void uploadBatch(List<CloudItem> items, BatchUploadCallback callback);
    void downloadBatch(List<String> remoteNames, String localFolder, BatchDownloadCallback callback);

    /**
     * Storage info and quota
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

    enum CloudConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        AUTHENTICATED,
        ERROR
    }

    class CloudItem {
        public final String localPath;
        public final String remoteName;
        public final CloudItemType type;
        public final Map<String, String> metadata;
        public final String encryptedData; // for metadata items

        public CloudItem(String localPath, String remoteName, CloudItemType type,
                         Map<String, String> metadata) {
            this.localPath = localPath;
            this.remoteName = remoteName;
            this.type = type;
            this.metadata = metadata;
            this.encryptedData = null;
        }

        public CloudItem(String encryptedData, String remoteName,
                         Map<String, String> metadata) {
            this.localPath = null;
            this.remoteName = remoteName;
            this.type = CloudItemType.METADATA;
            this.metadata = metadata;
            this.encryptedData = encryptedData;
        }
    }

    enum CloudItemType {
        FILE,
        METADATA
    }

    class CloudFile {
        public final String name;
        public final long size;
        public final long lastModified;
        public final String url;
        public final Map<String, String> metadata;

        public CloudFile(String name, long size, long lastModified, String url,
                         Map<String, String> metadata) {
            this.name = name;
            this.size = size;
            this.lastModified = lastModified;
            this.url = url;
            this.metadata = metadata;
        }
    }

    class CloudMetadata {
        public final String key;
        public final String encryptedData;
        public final long lastModified;
        public final Map<String, String> metadata;

        public CloudMetadata(String key, String encryptedData, long lastModified,
                             Map<String, String> metadata) {
            this.key = key;
            this.encryptedData = encryptedData;
            this.lastModified = lastModified;
            this.metadata = metadata;
        }
    }

    class CloudStorageInfo {
        public final long totalSpace;
        public final long usedSpace;
        public final long availableSpace;
        public final int fileCount;
        public final String planName;

        public CloudStorageInfo(long totalSpace, long usedSpace, long availableSpace,
                                int fileCount, String planName) {
            this.totalSpace = totalSpace;
            this.usedSpace = usedSpace;
            this.availableSpace = availableSpace;
            this.fileCount = fileCount;
            this.planName = planName;
        }

        public double getUsagePercentage() {
            if (totalSpace <= 0) return 0.0;
            return (double) usedSpace / totalSpace * 100.0;
        }
    }

    class CloudError {
        public final CloudErrorType type;
        public final String message;
        public final String details;
        public final Throwable cause;

        public CloudError(CloudErrorType type, String message, String details, Throwable cause) {
            this.type = type;
            this.message = message;
            this.details = details;
            this.cause = cause;
        }

        public CloudError(CloudErrorType type, String message) {
            this(type, message, null, null);
        }
    }

    enum CloudErrorType {
        AUTHENTICATION_FAILED,
        NETWORK_ERROR,
        STORAGE_FULL,
        FILE_NOT_FOUND,
        PERMISSION_DENIED,
        QUOTA_EXCEEDED,
        INVALID_CREDENTIALS,
        SERVER_ERROR,
        TIMEOUT,
        UNKNOWN
    }
}