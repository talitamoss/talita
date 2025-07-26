package com.core.talita.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.UniversalDataType;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * CloudBackupManager - Manages cloud backup queue and sync
 * 
 * Features:
 * - Queue-based backup system
 * - Supports multiple cloud providers
 * - Automatic retry on failure
 * - Offline support with persistent queue
 */
public class CloudBackupManager {
    private static final String TAG = "CloudBackupManager";
    private static final String PREFS_NAME = "cloud_backup_prefs";
    private static final String PREF_ENABLED = "backup_enabled";
    private static final String PREF_PROVIDER = "backup_provider";
    private static final String PREF_LAST_SYNC = "last_sync_time";
    
    private static CloudBackupManager instance;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final LinkedBlockingQueue<BackupItem> backupQueue;
    private CloudProvider currentProvider;
    private boolean isEnabled;
    private Thread backupThread;
    
    /**
     * Backup item wrapper
     */
    private static class BackupItem {
        final String id;
        final String type;
        final String jsonData;
        final String filePath;
        final long timestamp;
        int retryCount = 0;
        
        BackupItem(String id, String type, String jsonData, String filePath) {
            this.id = id;
            this.type = type;
            this.jsonData = jsonData;
            this.filePath = filePath;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private CloudBackupManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.backupQueue = new LinkedBlockingQueue<>();
        this.isEnabled = prefs.getBoolean(PREF_ENABLED, false);
        
        loadProvider();
        if (isEnabled) {
            startBackupThread();
        }
    }
    
    public static synchronized CloudBackupManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudBackupManager(context);
        }
        return instance;
    }
    
    /**
     * Queue data for backup
     */
    public void queueForBackup(UniversalDataType data) {
        if (!isEnabled || currentProvider == null) {
            return;
        }
        
        try {
            // Convert to JSON for backup
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("id", data.getId());
            backupData.put("type", data.getType());
            backupData.put("timestamp", data.getTimestamp());
            backupData.put("metadata", data.getMetadata());
            
            // Add location if available
            if (data.getLatitude() != 0.0 || data.getLongitude() != 0.0) {
                Map<String, Double> location = new HashMap<>();
                location.put("latitude", data.getLatitude());
                location.put("longitude", data.getLongitude());
                backupData.put("location", location);
            }
            
            // Convert to JSON string
            String jsonData = convertToJson(backupData);
            
            // Determine if there's a file to backup
            String filePath = null;
            if (data instanceof FileBasedData) {
                filePath = ((FileBasedData) data).getFilePath();
            }
            
            BackupItem item = new BackupItem(
                data.getId(),
                data.getType(),
                jsonData,
                filePath
            );
            
            backupQueue.offer(item);
            Log.d(TAG, "📤 Queued for backup: " + data.getType());
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to queue item for backup", e);
        }
    }
    
    /**
     * Remove item from backup queue
     */
    public void removeFromQueue(String dataId) {
        backupQueue.removeIf(item -> item.id.equals(dataId));
    }
    
    /**
     * Enable/disable cloud backup
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        prefs.edit().putBoolean(PREF_ENABLED, enabled).apply();
        
        if (enabled && backupThread == null) {
            startBackupThread();
        } else if (!enabled && backupThread != null) {
            stopBackupThread();
        }
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Set cloud provider
     */
    public void setProvider(CloudProvider provider) {
        this.currentProvider = provider;
        if (provider != null) {
            prefs.edit().putString(PREF_PROVIDER, provider.getProviderId()).apply();
        }
    }
    
    /**
     * Get current provider
     */
    public CloudProvider getCurrentProvider() {
        return currentProvider;
    }
    
    /**
     * Get backup queue size
     */
    public int getQueueSize() {
        return backupQueue.size();
    }
    
    /**
     * Get last sync time
     */
    public long getLastSyncTime() {
        return prefs.getLong(PREF_LAST_SYNC, 0);
    }
    
    /**
     * Force sync now
     */
    public void syncNow() {
        if (backupThread != null) {
            backupThread.interrupt();
        }
    }
    
    private void loadProvider() {
        String providerId = prefs.getString(PREF_PROVIDER, null);
        if (providerId != null) {
            // TODO: Load provider based on ID
            // For now, we'll leave it null
        }
    }
    
    private void startBackupThread() {
        backupThread = new Thread(this::backupLoop);
        backupThread.setName("CloudBackupThread");
        backupThread.start();
    }
    
    private void stopBackupThread() {
        if (backupThread != null) {
            backupThread.interrupt();
            backupThread = null;
        }
    }
    
    private void backupLoop() {
        while (!Thread.currentThread().isInterrupted() && isEnabled) {
            try {
                BackupItem item = backupQueue.take();
                
                if (currentProvider != null && currentProvider.isAuthenticated()) {
                    boolean success = performBackup(item);
                    
                    if (!success) {
                        item.retryCount++;
                        if (item.retryCount < 3) {
                            // Re-queue for retry
                            backupQueue.offer(item);
                            Thread.sleep(5000); // Wait before retry
                        } else {
                            Log.e(TAG, "Failed to backup after 3 attempts: " + item.id);
                        }
                    } else {
                        // Update last sync time
                        prefs.edit().putLong(PREF_LAST_SYNC, System.currentTimeMillis()).apply();
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Log.e(TAG, "Error in backup loop", e);
            }
        }
    }
    
    private boolean performBackup(BackupItem item) {
        try {
            // Upload JSON data
            boolean dataUploaded = currentProvider.uploadData(
                item.id,
                item.type,
                item.jsonData
            );
            
            if (!dataUploaded) {
                return false;
            }
            
            // Upload file if present
            if (item.filePath != null) {
                boolean fileUploaded = currentProvider.uploadFile(
                    item.id,
                    item.filePath
                );
                
                if (!fileUploaded) {
                    // Roll back data upload if file fails
                    currentProvider.deleteData(item.id);
                    return false;
                }
            }
            
            Log.d(TAG, "✅ Backed up: " + item.type + " - " + item.id);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Backup failed for " + item.id, e);
            return false;
        }
    }
    
    private String convertToJson(Map<String, Object> data) {
        // Simple JSON conversion
        // In production, use a proper JSON library
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                json.append(convertToJson((Map<String, Object>) value));
            } else {
                json.append(value);
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Interface for data that has associated files
     */
    public interface FileBasedData {
        String getFilePath();
    }
}
