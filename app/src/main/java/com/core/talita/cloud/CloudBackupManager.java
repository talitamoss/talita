package com.core.talita.cloud;

import android.content.Context;
import com.core.talita.EncryptedData;

/**
 * CloudBackupManager - Stub implementation
 * 
 * This is a minimal stub to prevent build errors.
 * Cloud backup functionality is planned for future release.
 */
public class CloudBackupManager {
    private static CloudBackupManager instance;
    
    private CloudBackupManager(Context context) {
        // Stub - no initialization needed
    }
    
    public static synchronized CloudBackupManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudBackupManager(context);
        }
        return instance;
    }
    
    /**
     * Queue data for backup - currently does nothing
     */
    public void queueForBackup(EncryptedData data) {
        // Stub - cloud backup not implemented yet
    }
    
    /**
     * Clear the backup queue - currently does nothing
     */
    public void clearQueue() {
        // Stub - cloud backup not implemented yet
    }
    
    /**
     * Check if backup is enabled - always returns false
     */
    public boolean isBackupEnabled() {
        return false;
    }
    
    /**
     * Stub backup stats class
     */
    public static class BackupStats {
        public final int queueSize = 0;
        public final long lastBackupTime = 0;
        public final boolean enabled = false;
        public final boolean inProgress = false;
        public final String provider = "none";
    }
}
