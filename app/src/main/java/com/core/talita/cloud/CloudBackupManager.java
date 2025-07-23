package com.core.talita.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.UniversalDataType;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * CloudBackupManager - Manages cloud backup queue and operations
 * Ready for cloud provider integration
 */
public class CloudBackupManager {
    private static final String TAG = "CloudBackupManager";
    private static final String PREFS_NAME = "cloud_backup_prefs";
    private static final String KEY_BACKUP_ENABLED = "backup_enabled";
    private static final String KEY_AUTO_BACKUP = "auto_backup";
    private static final String KEY_LAST_BACKUP = "last_backup";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Queue<BackupItem> backupQueue;
    private CloudProvider cloudProvider;
    private boolean isEnabled;
    private boolean autoBackupEnabled;
    
    public CloudBackupManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.backupQueue = new ConcurrentLinkedQueue<>();
        
        loadSettings();
        Log.d(TAG, "☁️ Cloud Backup Manager initialized");
    }
    
    /**
     * Load settings from preferences
     */
    private void loadSettings() {
        isEnabled = prefs.getBoolean(KEY_BACKUP_ENABLED, false);
        autoBackupEnabled = prefs.getBoolean(KEY_AUTO_BACKUP, true);
    }
    
    /**
     * Set cloud provider (Google Drive, Dropbox, etc.)
     */
    public void setCloudProvider(CloudProvider provider) {
        this.cloudProvider = provider;
        Log.d(TAG, "✅ Cloud provider set: " + provider.getProviderName());
        
        if (isEnabled && !backupQueue.isEmpty()) {
            processBackupQueue();
        }
    }
    
    /**
     * Enable/disable cloud backup
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        prefs.edit().putBoolean(KEY_BACKUP_ENABLED, enabled).apply();
        
        if (enabled && cloudProvider != null && !backupQueue.isEmpty()) {
            processBackupQueue();
        }
        
        Log.d(TAG, enabled ? "☁️ Cloud backup enabled" : "⏸️ Cloud backup disabled");
    }
    
    /**
     * Queue data for backup
     */
    public void queueForBackup(UniversalDataType data) {
        if (!isEnabled || !autoBackupEnabled) {
            Log.d(TAG, "⏭️ Skipping backup (disabled): " + data.getType());
            return;
        }
        
        BackupItem item = new BackupItem(
            data.getId(),
            data.getType(),
            data.getTimestamp(),
            data.toJson(),
            data.getFilePath()
        );
        
        backupQueue.offer(item);
        Log.d(TAG, "📥 Queued for backup: " + data.getType() + " - " + data.getId());
        
        // Process immediately if provider is available
        if (cloudProvider != null && cloudProvider.isAuthenticated()) {
            processBackupQueue();
        }
    }

public void setBackupEnabled(boolean enabled) {
    setEnabled(enabled);
}    
    /**
     * Process backup queue
     */
    public void processBackupQueue() {
        if (cloudProvider == null || !cloudProvider.isAuthenticated()) {
            Log.w(TAG, "⚠️ Cannot process backup queue - no authenticated provider");
            return;
        }
        
        new Thread(() -> {
            Log.d(TAG, "🔄 Processing backup queue (" + backupQueue.size() + " items)");
            
            while (!backupQueue.isEmpty()) {
                BackupItem item = backupQueue.poll();
                if (item != null) {
                    try {
                        backupItem(item);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Backup failed for " + item.id, e);
                        // Re-queue failed items
                        backupQueue.offer(item);
                        break;
                    }
                }
            }
            
            // Update last backup time
            prefs.edit().putLong(KEY_LAST_BACKUP, System.currentTimeMillis()).apply();
        }).start();
    }
    
    /**
     * Backup a single item
     */
    private void backupItem(BackupItem item) throws Exception {
        Log.d(TAG, "☁️ Backing up: " + item.type + " - " + item.id);
        
        // Create backup path
        String backupPath = "Talita/" + item.type + "/" + item.id;
        
        // Backup metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", item.type);
        metadata.put("timestamp", String.valueOf(item.timestamp));
        metadata.put("device", android.os.Build.MODEL);
        
        CloudProvider.CloudItem cloudItem = new CloudProvider.CloudItem(
            item.jsonData,
            backupPath + ".json",
            metadata
        );
        
        cloudProvider.uploadMetadata(cloudItem);
        
        // Backup file if exists
        if (item.filePath != null && !item.filePath.isEmpty()) {
            CloudProvider.CloudItem fileItem = new CloudProvider.CloudItem(
                item.filePath,
                backupPath + "_file.enc",
                CloudProvider.CloudItemType.FILE,
                metadata
            );
            
            cloudProvider.uploadFile(fileItem);
        }
        
        Log.d(TAG, "✅ Backup complete: " + item.id);
    }
    
    /**
     * Manual backup trigger
     */
    public void backupNow() {
        if (!isEnabled) {
            Log.w(TAG, "⚠️ Cloud backup is disabled");
            return;
        }
        
        if (cloudProvider == null || !cloudProvider.isAuthenticated()) {
            Log.w(TAG, "⚠️ No authenticated cloud provider");
            return;
        }
        
        processBackupQueue();
    }
    
    /**
     * Restore from cloud
     */
    public void restoreFromCloud(RestoreCallback callback) {
        if (cloudProvider == null || !cloudProvider.isAuthenticated()) {
            callback.onError("No authenticated cloud provider");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "🔄 Starting cloud restore...");
                
                // List all items
                List<CloudProvider.CloudFile> files = cloudProvider.listFiles("Talita/");
                
                Map<String, RestoredItem> restoredItems = new HashMap<>();
                
                for (CloudProvider.CloudFile file : files) {
                    if (file.name.endsWith(".json")) {
                        // Download metadata
                        String jsonData = cloudProvider.downloadMetadata(file.name);
                        
                        String id = extractIdFromPath(file.name);
                        RestoredItem item = restoredItems.computeIfAbsent(id, k -> new RestoredItem());
                        item.jsonData = jsonData;
                        item.metadata = file.metadata;
                    } else if (file.name.endsWith("_file.enc")) {
                        // Download file
                        String localPath = cloudProvider.downloadFile(file.name, 
                            context.getFilesDir() + "/restored/");
                        
                        String id = extractIdFromPath(file.name);
                        RestoredItem item = restoredItems.computeIfAbsent(id, k -> new RestoredItem());
                        item.filePath = localPath;
                    }
                }
                
                callback.onSuccess(new ArrayList<>(restoredItems.values()));
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Restore failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
    
    /**
     * Get backup status for a specific item
     */
    public BackupStatus getBackupStatus(String dataId) {
        // Check if in queue
        for (BackupItem item : backupQueue) {
            if (item.id.equals(dataId)) {
                return BackupStatus.PENDING;
            }
        }
        
        // TODO: Check cloud provider for synced status
        return BackupStatus.NOT_BACKED_UP;
    }
    
    /**
     * Get backup statistics
     */
    public BackupStats getBackupStats() {
        BackupStats stats = new BackupStats();
        stats.queueSize = backupQueue.size();
        stats.lastBackupTime = prefs.getLong(KEY_LAST_BACKUP, 0);
        stats.isEnabled = isEnabled;
        stats.hasProvider = cloudProvider != null;
        stats.isAuthenticated = cloudProvider != null && cloudProvider.isAuthenticated();
        
        return stats;
    }
    
    /**
     * Clear backup queue
     */
    public void clearQueue() {
        backupQueue.clear();
        Log.d(TAG, "🧹 Backup queue cleared");
    }
    
    /**
     * Set auto backup enabled
     */
    public void setAutoBackupEnabled(boolean enabled) {
        this.autoBackupEnabled = enabled;
        prefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply();
        Log.d(TAG, enabled ? "✅ Auto backup enabled" : "⏸️ Auto backup disabled");
    }
    
    /**
     * Extract ID from backup path
     */
    private String extractIdFromPath(String path) {
        // Path format: Talita/type/id.json or Talita/type/id_file.enc
        String[] parts = path.split("/");
        if (parts.length >= 3) {
            String filename = parts[parts.length - 1];
            return filename.replace(".json", "").replace("_file.enc", "");
        }
        return null;
    }
    
    // Inner classes
    
    /**
     * Backup item in queue
     */
    private static class BackupItem {
        final String id;
        final String type;
        final long timestamp;
        final String jsonData;
        final String filePath;
        
        BackupItem(String id, String type, long timestamp, String jsonData, String filePath) {
            this.id = id;
            this.type = type;
            this.timestamp = timestamp;
            this.jsonData = jsonData;
            this.filePath = filePath;
        }
    }
    
    /**
     * Restored item from cloud
     */
    public static class RestoredItem {
        public String jsonData;
        public String filePath;
        public Map<String, String> metadata;
    }
    
    /**
     * Backup status enum
     */
    public enum BackupStatus {
        NOT_BACKED_UP,
        PENDING,
        SYNCED,
        ERROR
    }
    
    /**
     * Backup statistics
     */
    public static class BackupStats {
        public int queueSize;
        public long lastBackupTime;
        public boolean isEnabled;
        public boolean hasProvider;
        public boolean isAuthenticated;
    }
    
    /**
     * Restore callback interface
     */
    public interface RestoreCallback {
        void onSuccess(List<RestoredItem> items);
        void onError(String error);
    }
    
    /**
     * Check if cloud backup is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Get provider name
     */
    public String getProviderName() {
        return cloudProvider != null ? cloudProvider.getProviderName() : "None";
    }
    }
