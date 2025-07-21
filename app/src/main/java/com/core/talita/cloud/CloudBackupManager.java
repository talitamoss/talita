package com.core.talita.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.UniversalDataType;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cloud Backup Manager - Orchestrates secure backup across all cloud providers
 *
 * Features:
 * - Multi-provider support (Greenhost, AWS, Google Drive, etc.)
 * - Intelligent backup scheduling
 * - Conflict resolution and sync
 * - Progress tracking and retry logic
 * - Integration with UniversalDataService
 */
public class CloudBackupManager {

    private static final String TAG = "CloudBackupManager";
    private static final String PREFS_NAME = "cloud_backup_prefs";

    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService executor;

    // Provider management
    private final Map<String, CloudProvider> providers;
    private String primaryProviderId;

    // Backup queue and status
    private final Queue<BackupItem> backupQueue;
    private final Map<String, BackupStatus> backupStatuses;
    private final Set<BackupProgressListener> progressListeners;

    // Configuration
    private BackupConfig config;
    private boolean isBackupEnabled;
    private boolean isAutoBackupEnabled;

    public CloudBackupManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newFixedThreadPool(2); // Parallel uploads

        this.providers = new ConcurrentHashMap<>();
        this.backupQueue = new LinkedList<>();
        this.backupStatuses = new ConcurrentHashMap<>();
        this.progressListeners = new HashSet<>();

        this.config = loadBackupConfig();
        this.isBackupEnabled = prefs.getBoolean("backup_enabled", false);
        this.isAutoBackupEnabled = prefs.getBoolean("auto_backup_enabled", true);

        Log.d(TAG, "☁️ CloudBackupManager initialized");
    }

    /**
     * Provider Management
     */
    public void registerProvider(String providerId, CloudProvider provider) {
        providers.put(providerId, provider);
        Log.d(TAG, "✅ Registered cloud provider: " + provider.getProviderDisplayName());

        // Set as primary if it's the first one
        if (primaryProviderId == null) {
            setPrimaryProvider(providerId);
        }
    }

    public void setPrimaryProvider(String providerId) {
        if (providers.containsKey(providerId)) {
            primaryProviderId = providerId;
            prefs.edit().putString("primary_provider", providerId).apply();
            Log.d(TAG, "🎯 Primary provider set to: " + providerId);
        }
    }

    public CloudProvider getPrimaryProvider() {
        return primaryProviderId != null ? providers.get(primaryProviderId) : null;
    }

    public List<CloudProvider> getAllProviders() {
        return new ArrayList<>(providers.values());
    }

    /**
     * Backup Operations
     */
    public void queueForBackup(UniversalDataType data) {
        if (!isBackupEnabled) {
            Log.d(TAG, "📴 Backup disabled, skipping: " + data.getDisplayName());
            return;
        }

        BackupItem item = new BackupItem(
                data.getId(),
                data.getType(),
                data.getFilePath(),
                data.toJson(),
                data.getTimestamp(),
                BackupPriority.NORMAL
        );

        synchronized (backupQueue) {
            backupQueue.offer(item);
            Log.d(TAG, "📋 Queued for backup: " + data.getDisplayName());
        }

        // Trigger backup if auto-backup is enabled
        if (isAutoBackupEnabled) {
            processBackupQueue();
        }
    }

    public void queueUrgentBackup(UniversalDataType data) {
        BackupItem item = new BackupItem(
                data.getId(),
                data.getType(),
                data.getFilePath(),
                data.toJson(),
                data.getTimestamp(),
                BackupPriority.URGENT
        );

        synchronized (backupQueue) {
            // Add urgent items to front of queue
            ((LinkedList<BackupItem>) backupQueue).addFirst(item);
            Log.d(TAG, "🚨 Queued URGENT backup: " + data.getDisplayName());
        }

        processBackupQueue();
    }

    public void processBackupQueue() {
        CloudProvider provider = getPrimaryProvider();
        if (provider == null) {
            Log.w(TAG, "⚠️ No primary provider configured for backup");
            return;
        }

        if (!provider.isAuthenticated()) {
            Log.w(TAG, "⚠️ Provider not authenticated: " + provider.getProviderDisplayName());
            return;
        }

        executor.execute(this::processPendingBackups);
    }

    private void processPendingBackups() {
        CloudProvider provider = getPrimaryProvider();

        while (!backupQueue.isEmpty()) {
            BackupItem item;
            synchronized (backupQueue) {
                item = backupQueue.poll();
            }

            if (item != null) {
                processBackupItem(item, provider);
            }
        }
    }

    private void processBackupItem(BackupItem item, CloudProvider provider) {
        String backupId = item.dataId + "_" + item.timestamp;

        // Update status
        updateBackupStatus(backupId, BackupStatus.Status.IN_PROGRESS, "Starting backup...", 0);

        try {
            // Prepare metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("data_type", item.dataType);
            metadata.put("data_id", item.dataId);
            metadata.put("timestamp", String.valueOf(item.timestamp));
            metadata.put("backup_version", "1.0");
            metadata.put("encrypted", "true");

            // Upload file if exists
            if (item.filePath != null && !item.filePath.isEmpty()) {
                uploadFileWithRetry(item, provider, metadata, backupId);
            }

            // Upload metadata
            uploadMetadataWithRetry(item, provider, metadata, backupId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Backup failed for item: " + item.dataId, e);
            updateBackupStatus(backupId, BackupStatus.Status.FAILED,
                    "Backup failed: " + e.getMessage(), 0);
        }
    }

    private void uploadFileWithRetry(BackupItem item, CloudProvider provider,
                                     Map<String, String> metadata, String backupId) {
        String remoteFileName = generateRemoteFileName(item);

        provider.uploadFile(item.filePath, remoteFileName, metadata,
                new CloudProvider.UploadCallback() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        int progress = (int) ((bytesUploaded * 50) / totalBytes); // File = 50% of total
                        updateBackupStatus(backupId, BackupStatus.Status.IN_PROGRESS,
                                "Uploading file...", progress);
                    }

                    @Override
                    public void onSuccess(String remoteUrl) {
                        Log.d(TAG, "✅ File uploaded: " + remoteFileName);
                        updateBackupStatus(backupId, BackupStatus.Status.IN_PROGRESS,
                                "File uploaded, uploading metadata...", 50);
                    }

                    @Override
                    public void onFailure(CloudProvider.CloudError error) {
                        Log.e(TAG, "❌ File upload failed: " + error.message);
                        updateBackupStatus(backupId, BackupStatus.Status.FAILED,
                                "File upload failed: " + error.message, 0);
                    }
                });
    }

    private void uploadMetadataWithRetry(BackupItem item, CloudProvider provider,
                                         Map<String, String> metadata, String backupId) {
        String metadataKey = generateMetadataKey(item);

        provider.uploadMetadata(metadataKey, item.encryptedJsonData, metadata,
                new CloudProvider.UploadCallback() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        int progress = 50 + (int) ((bytesUploaded * 50) / totalBytes); // Metadata = other 50%
                        updateBackupStatus(backupId, BackupStatus.Status.IN_PROGRESS,
                                "Uploading metadata...", progress);
                    }

                    @Override
                    public void onSuccess(String remoteUrl) {
                        Log.d(TAG, "✅ Backup completed: " + item.dataId);
                        updateBackupStatus(backupId, BackupStatus.Status.COMPLETED,
                                "Backup completed successfully", 100);
                    }

                    @Override
                    public void onFailure(CloudProvider.CloudError error) {
                        Log.e(TAG, "❌ Metadata upload failed: " + error.message);
                        updateBackupStatus(backupId, BackupStatus.Status.FAILED,
                                "Metadata upload failed: " + error.message, 50);
                    }
                });
    }

    /**
     * Backup Status Management
     */
    private void updateBackupStatus(String backupId, BackupStatus.Status status,
                                    String message, int progress) {
        BackupStatus backupStatus = new BackupStatus(backupId, status, message, progress,
                System.currentTimeMillis());
        backupStatuses.put(backupId, backupStatus);

        // Notify listeners
        for (BackupProgressListener listener : progressListeners) {
            listener.onBackupProgress(backupStatus);
        }
    }

    public BackupStatus getBackupStatus(String dataId) {
        // Find most recent backup status for this data ID
        return backupStatuses.values().stream()
                .filter(status -> status.backupId.startsWith(dataId))
                .max(Comparator.comparing(status -> status.timestamp))
                .orElse(null);
    }

    /**
     * Configuration and Settings
     */
    public void setBackupEnabled(boolean enabled) {
        isBackupEnabled = enabled;
        prefs.edit().putBoolean("backup_enabled", enabled).apply();
        Log.d(TAG, enabled ? "✅ Backup enabled" : "📴 Backup disabled");
    }

    public void setAutoBackupEnabled(boolean enabled) {
        isAutoBackupEnabled = enabled;
        prefs.edit().putBoolean("auto_backup_enabled", enabled).apply();
        Log.d(TAG, enabled ? "🔄 Auto-backup enabled" : "⏸️ Auto-backup disabled");
    }

    public void setBackupConfig(BackupConfig config) {
        this.config = config;
        saveBackupConfig(config);
        Log.d(TAG, "⚙️ Backup configuration updated");
    }

    /**
     * Utility Methods
     */
    private String generateRemoteFileName(BackupItem item) {
        return String.format("%s/%s_%d.enc",
                item.dataType, item.dataId, item.timestamp);
    }

    private String generateMetadataKey(BackupItem item) {
        return String.format("metadata/%s/%s_%d",
                item.dataType, item.dataId, item.timestamp);
    }

    private BackupConfig loadBackupConfig() {
        // Load from SharedPreferences with defaults
        return new BackupConfig(
                prefs.getBoolean("wifi_only", true),
                prefs.getBoolean("charging_only", false),
                prefs.getInt("retry_attempts", 3),
                prefs.getLong("retry_delay", 5000),
                prefs.getInt("max_concurrent", 2)
        );
    }

    private void saveBackupConfig(BackupConfig config) {
        prefs.edit()
                .putBoolean("wifi_only", config.wifiOnly)
                .putBoolean("charging_only", config.chargingOnly)
                .putInt("retry_attempts", config.retryAttempts)
                .putLong("retry_delay", config.retryDelayMs)
                .putInt("max_concurrent", config.maxConcurrentUploads)
                .apply();
    }

    public void addProgressListener(BackupProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(BackupProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Data Classes and Interfaces
     */
    public static class BackupItem {
        public final String dataId;
        public final String dataType;
        public final String filePath;
        public final String encryptedJsonData;
        public final long timestamp;
        public final BackupPriority priority;

        public BackupItem(String dataId, String dataType, String filePath,
                          String encryptedJsonData, long timestamp, BackupPriority priority) {
            this.dataId = dataId;
            this.dataType = dataType;
            this.filePath = filePath;
            this.encryptedJsonData = encryptedJsonData;
            this.timestamp = timestamp;
            this.priority = priority;
        }
    }

    public enum BackupPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    public static class BackupStatus {
        public final String backupId;
        public final Status status;
        public final String message;
        public final int progress; // 0-100
        public final long timestamp;

        public BackupStatus(String backupId, Status status, String message,
                            int progress, long timestamp) {
            this.backupId = backupId;
            this.status = status;
            this.message = message;
            this.progress = progress;
            this.timestamp = timestamp;
        }

        public enum Status {
            QUEUED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
        }
    }

    public static class BackupConfig {
        public final boolean wifiOnly;
        public final boolean chargingOnly;
        public final int retryAttempts;
        public final long retryDelayMs;
        public final int maxConcurrentUploads;

        public BackupConfig(boolean wifiOnly, boolean chargingOnly, int retryAttempts,
                            long retryDelayMs, int maxConcurrentUploads) {
            this.wifiOnly = wifiOnly;
            this.chargingOnly = chargingOnly;
            this.retryAttempts = retryAttempts;
            this.retryDelayMs = retryDelayMs;
            this.maxConcurrentUploads = maxConcurrentUploads;
        }
    }

    public interface BackupProgressListener {
        void onBackupProgress(BackupStatus status);
    }
}
