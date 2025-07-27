package com.core.talita.cloud;

/**
 * BackupConfig - Configuration for cloud backup
 * 
 * Simple data class to hold backup preferences.
 * Currently a placeholder for future cloud backup feature.
 */
public class BackupConfig {
    private final boolean enabled;
    private final boolean wifiOnly;
    private final boolean includeMedia;
    private final String provider;
    private final int retryCount;
    
    // Constructor matching the existing usage
    public BackupConfig(boolean wifiOnly, boolean includeMedia, int retryCount) {
        this(false, wifiOnly, includeMedia, "none", retryCount);
    }
    
    // Full constructor
    public BackupConfig(boolean enabled, boolean wifiOnly, boolean includeMedia, String provider, int retryCount) {
        this.enabled = enabled;
        this.wifiOnly = wifiOnly;
        this.includeMedia = includeMedia;
        this.provider = provider;
        this.retryCount = retryCount;
    }
    
    /**
     * Default configuration - all disabled
     */
    public static BackupConfig getDefault() {
        return new BackupConfig(true, true, 3);
    }
    
    // Getters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isWifiOnly() {
        return wifiOnly;
    }
    
    public boolean includeMedia() {
        return includeMedia;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
}
