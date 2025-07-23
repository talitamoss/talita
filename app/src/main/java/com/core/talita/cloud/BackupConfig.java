package com.core.talita.cloud;

/**
 * Configuration for cloud backup settings
 */
public class BackupConfig {
    public final boolean wifiOnly;
    public final boolean includeMedia;
    public final int retryCount;
    
    public BackupConfig(boolean wifiOnly, boolean includeMedia, int retryCount) {
        this.wifiOnly = wifiOnly;
        this.includeMedia = includeMedia;
        this.retryCount = retryCount;
    }
    
    // Default configuration
    public static BackupConfig getDefault() {
        return new BackupConfig(true, true, 3);
    }
}
