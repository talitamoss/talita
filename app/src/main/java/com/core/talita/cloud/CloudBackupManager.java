package com.core.talita.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.UniversalDataType;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cloud Backup Manager - Handles queuing and syncing data to cloud providers
 */
public class CloudBackupManager {
    private static final String TAG = "CloudBackupManager";
    private static final String PREFS_NAME = "cloud_backup_prefs";
    private static CloudBackupManager instance;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Queue<BackupItem> backupQueue;
    private final ScheduledExecutorService scheduler;
    
    private CloudProvider cloudProvider;
    private boolean isBackupEnabled;
    private boolean isSyncing = false;
    
    private CloudBackupManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.backupQueue = new ConcurrentLinkedQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        loadSettings();
        startBackupScheduler();
    }
    
    public static synchronized CloudBackupManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudBackupManager(context);
        }
        return instance;
    }
    
    private void loadSettings() {
        isBackupEnabled = prefs.getBoolean("cloud_backup_enabled", false);
        String providerName = prefs.getString("cloud_provider", null);
        
        if (providerName != null) {
            initializeProvider(providerName);
        }
    }
    
    private void initializeProvider(String providerName) {
        switch (providerName) {
            case "GoogleDrive":
                // cloudProvider = new GoogleDriveProvider(context);
                break;
            case "Dropbox":
                // cloudProvider = new DropboxProvider(context);
                break;
            case "SolidPod":
                // cloudProvider = new SolidPodProvider(context);
                break;
        }
    }
    
    public void enableBackup(String providerName) {
        prefs.edit()
            .putBoolean("cloud_backup_enabled", true)
            .putString("cloud_provider", providerName)
            .apply();
        
        isBackupEnabled = true;
        initializeProvider(providerName);
        
        // Start initial sync
        syncNow();
    }
    
    public void disableBackup() {
        prefs.edit()
            .putBoolean("cloud_backup_enabled", false)
            .apply();
        
        isBackupEnabled = false;
        cloudProvider = null;
    }
    
    public boolean isBackupEnabled() {
        return isBackupEnabled;
    }
    
    public String getCurrentProvider() {
        return prefs.getString("cloud_provider", "None");
    }
    
    /**
     * Queue data for backup
     */
    public void queueForBackup(long localId, UniversalDataType data) {
        if (!isBackupEnabled || cloudProvider == null) {
            return;
        }
        
        BackupItem item = new BackupItem(localId, data);
        backupQueue.offer(item);
        
        Log.d(TAG, "Queued for backup: " + data.getType() + " [" + data.getId() + "]");
    }
    
    /**
     * Start the backup scheduler
     */
    private void startBackupScheduler() {
        // Run backup every 5 minutes
        scheduler.scheduleWithFixedDelay(this::processBackupQueue, 
            1, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Process the backup queue
     */
    private void processBackupQueue() {
        if (!isBackupEnabled || cloudProvider == null || isSyncing) {
            return;
        }
        
        isSyncing = true;
        
        try {
            BackupItem item;
            while ((item = backupQueue.poll()) != null) {
                backupItem(item);
            }
        } finally {
            isSyncing = false;
        }
    }
    
    /**
     * Backup a single item
     */
    private void backupItem(BackupItem item) {
        try {
            UniversalDataType data = item.data;
            
            // Create metadata JSON
            JSONObject metadata = new JSONObject();
            metadata.put("type", data.getType());
            metadata.put("id", data.getId());
            metadata.put("timestamp", data.getTimestamp());
            metadata.put("localId", item.localId);
            
            String path = "Talita/" + data.getType() + "/" + data.getId() + ".json";
            
            // Upload metadata with callback
            cloudProvider.uploadMetadata(
                path,
                data.toJson().getBytes(),
                new CloudProvider.UploadCallback() {
                    @Override
                    public void onSuccess(String uploadedPath) {
                        Log.d(TAG, "✅ Backed up: " + uploadedPath);
                        markAsBackedUp(item.localId);
                    }
                    
                    @Override
                    public void onProgress(int percentage) {
                        // Could update UI here
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Backup failed: " + error);
                        // Re-queue for retry
                        backupQueue.offer(item);
                    }
                }
            );
            
            // Upload associated file if present
            if (data.getFilePath() != null) {
                String filePath = "Talita/" + data.getType() + "/" + data.getId() + "_file";
                
                cloudProvider.uploadFile(
                    filePath,
                    data.getFilePath(),
                    new CloudProvider.UploadCallback() {
                        @Override
                        public void onSuccess(String uploadedPath) {
                            Log.d(TAG, "✅ File backed up: " + uploadedPath);
                        }
                        
                        @Override
                        public void onProgress(int percentage) {
                            // Could update UI here
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ File backup failed: " + error);
                        }
                    }
                );
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Backup error", e);
        }
    }
    
    /**
     * Mark item as backed up in local database
     */
    private void markAsBackedUp(long localId) {
        // Update local database to mark as synced
        prefs.edit()
            .putLong("last_backup_" + localId, System.currentTimeMillis())
            .apply();
    }
    
    /**
     * Force sync now
     */
    public void syncNow() {
        scheduler.execute(this::processBackupQueue);
    }
    
    /**
     * Restore from cloud
     */
    public void restoreFromCloud(RestoreCallback callback) {
        if (cloudProvider == null) {
            callback.onError("No cloud provider configured");
            return;
        }
        
        scheduler.execute(() -> {
            try {
                restoreData(callback);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    private void restoreData(RestoreCallback callback) {
        cloudProvider.listFiles("Talita/", new CloudProvider.ListFilesCallback() {
            @Override
            public void onSuccess(List<CloudProvider.CloudFile> files) {
                processRestoredFiles(files, callback);
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to list files: " + error);
            }
        });
    }
    
    private void processRestoredFiles(List<CloudProvider.CloudFile> files, RestoreCallback callback) {
        List<UniversalDataType> restoredData = new ArrayList<>();
        int totalFiles = files.size();
        int[] processedFiles = {0};
        
        for (CloudProvider.CloudFile file : files) {
            if (file.name.endsWith(".json") && !file.name.contains("_file")) {
                cloudProvider.downloadMetadata(file.name, new CloudProvider.DownloadCallback() {
                    @Override
                    public void onSuccess(String jsonData) {
                        try {
                            // Parse and restore the data
                            JSONObject json = new JSONObject(jsonData);
                            // Create appropriate data type from JSON
                            // This would need a factory method to recreate the correct type
                            
                            processedFiles[0]++;
                            
                            if (processedFiles[0] == totalFiles) {
                                callback.onComplete(restoredData);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse restored data", e);
                        }
                    }
                    
                    @Override
                    public void onProgress(int percentage) {
                        callback.onProgress(percentage);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Failed to download: " + error);
                        processedFiles[0]++;
                        
                        if (processedFiles[0] == totalFiles) {
                            callback.onComplete(restoredData);
                        }
                    }
                });
            } else {
                processedFiles[0]++;
                if (processedFiles[0] == totalFiles) {
                    callback.onComplete(restoredData);
                }
            }
        }
    }
    
    /**
     * Get backup statistics
     */
    public BackupStats getBackupStats() {
        long lastBackup = prefs.getLong("last_backup_time", 0);
        int queueSize = backupQueue.size();
        boolean isConnected = cloudProvider != null;
        
        return new BackupStats(lastBackup, queueSize, isConnected, isBackupEnabled);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        scheduler.shutdown();
    }
    
    // Inner classes
    
    private static class BackupItem {
        final long localId;
        final UniversalDataType data;
        final long queuedTime;
        
        BackupItem(long localId, UniversalDataType data) {
            this.localId = localId;
            this.data = data;
            this.queuedTime = System.currentTimeMillis();
        }
    }
    
    public static class BackupStats {
        public final long lastBackupTime;
        public final int queueSize;
        public final boolean isConnected;
        public final boolean isEnabled;
        
        BackupStats(long lastBackupTime, int queueSize, boolean isConnected, boolean isEnabled) {
            this.lastBackupTime = lastBackupTime;
            this.queueSize = queueSize;
            this.isConnected = isConnected;
            this.isEnabled = isEnabled;
        }
    }
    
    public interface RestoreCallback {
        void onProgress(int percentage);
        void onComplete(List<UniversalDataType> restoredData);
        void onError(String error);
    }
}
