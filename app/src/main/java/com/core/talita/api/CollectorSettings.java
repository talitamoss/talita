package com.core.talita.api;

import java.util.HashMap;
import java.util.Map;

/**
 * CollectorSettings - Configuration for a data collector
 * 
 * Stores all settings for how a collector should behave
 */
public class CollectorSettings {
    
    // Core settings
    private final boolean automatedCollection;
    private final int collectionFrequency; // in minutes
    private final boolean notificationsEnabled;
    
    // Advanced settings
    private final boolean backgroundCollection;
    private final boolean wifiOnlySync;
    private final int dataRetentionDays;
    private final Map<String, Object> customSettings;
    
    private CollectorSettings(Builder builder) {
        this.automatedCollection = builder.automatedCollection;
        this.collectionFrequency = builder.collectionFrequency;
        this.notificationsEnabled = builder.notificationsEnabled;
        this.backgroundCollection = builder.backgroundCollection;
        this.wifiOnlySync = builder.wifiOnlySync;
        this.dataRetentionDays = builder.dataRetentionDays;
        this.customSettings = new HashMap<>(builder.customSettings);
    }
    
    // Getters
    
    public boolean isAutomatedCollection() {
        return automatedCollection;
    }
    
    public int getCollectionFrequency() {
        return collectionFrequency;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public boolean isBackgroundCollection() {
        return backgroundCollection;
    }
    
    public boolean isWifiOnlySync() {
        return wifiOnlySync;
    }
    
    public int getDataRetentionDays() {
        return dataRetentionDays;
    }
    
    public Map<String, Object> getCustomSettings() {
        return new HashMap<>(customSettings);
    }
    
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    public boolean hasCustomSetting(String key) {
        return customSettings.containsKey(key);
    }
    
    /**
     * Builder for CollectorSettings
     */
    public static class Builder {
        private boolean automatedCollection = false;
        private int collectionFrequency = 60; // Default 60 minutes
        private boolean notificationsEnabled = false;
        private boolean backgroundCollection = false;
        private boolean wifiOnlySync = true;
        private int dataRetentionDays = 0; // 0 = keep forever
        private Map<String, Object> customSettings = new HashMap<>();
        
        public Builder() {
        }
        
        public Builder(CollectorSettings existing) {
            this.automatedCollection = existing.automatedCollection;
            this.collectionFrequency = existing.collectionFrequency;
            this.notificationsEnabled = existing.notificationsEnabled;
            this.backgroundCollection = existing.backgroundCollection;
            this.wifiOnlySync = existing.wifiOnlySync;
            this.dataRetentionDays = existing.dataRetentionDays;
            this.customSettings = new HashMap<>(existing.customSettings);
        }
        
        public Builder setAutomatedCollection(boolean automatedCollection) {
            this.automatedCollection = automatedCollection;
            return this;
        }
        
        public Builder setCollectionFrequency(int minutes) {
            this.collectionFrequency = minutes;
            return this;
        }
        
        public Builder setNotificationsEnabled(boolean enabled) {
            this.notificationsEnabled = enabled;
            return this;
        }
        
        public Builder setBackgroundCollection(boolean enabled) {
            this.backgroundCollection = enabled;
            return this;
        }
        
        public Builder setWifiOnlySync(boolean wifiOnly) {
            this.wifiOnlySync = wifiOnly;
            return this;
        }
        
        public Builder setDataRetentionDays(int days) {
            this.dataRetentionDays = days;
            return this;
        }
        
        public Builder putCustomSetting(String key, Object value) {
            this.customSettings.put(key, value);
            return this;
        }
        
        public Builder putAllCustomSettings(Map<String, Object> settings) {
            this.customSettings.putAll(settings);
            return this;
        }
        
        public CollectorSettings build() {
            return new CollectorSettings(this);
        }
    }
    
    @Override
    public String toString() {
        return "CollectorSettings{" +
                "automated=" + automatedCollection +
                ", frequency=" + collectionFrequency + "min" +
                ", notifications=" + notificationsEnabled +
                ", background=" + backgroundCollection +
                ", wifiOnly=" + wifiOnlySync +
                ", retention=" + dataRetentionDays + "days" +
                ", customCount=" + customSettings.size() +
                '}';
    }
}
