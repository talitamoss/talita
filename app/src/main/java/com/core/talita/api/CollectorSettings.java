package com.core.talita.api;

/**
 * CollectorSettings - Configuration for a data collector
 * 
 * Stores user preferences for how a collector should behave,
 * including automation, notifications, and collection frequency.
 */
public class CollectorSettings {
    
    private final boolean enabled;
    private final boolean automatedCollection;
    private final boolean notificationsEnabled;
    private final long collectionFrequency; // milliseconds
    private final long notificationInterval; // milliseconds
    private final int dailyGoal;
    private final String preferredUnit;
    
    private CollectorSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.automatedCollection = builder.automatedCollection;
        this.notificationsEnabled = builder.notificationsEnabled;
        this.collectionFrequency = builder.collectionFrequency;
        this.notificationInterval = builder.notificationInterval;
        this.dailyGoal = builder.dailyGoal;
        this.preferredUnit = builder.preferredUnit;
    }
    
    /**
     * Get default settings for a collector
     */
    public static CollectorSettings getDefault() {
        return new Builder().build();
    }
    
    /**
     * Builder for creating CollectorSettings
     */
    public static class Builder {
        private boolean enabled = true;
        private boolean automatedCollection = false;
        private boolean notificationsEnabled = false;
        private long collectionFrequency = 0; // 0 = manual only
        private long notificationInterval = 0; // 0 = no notifications
        private int dailyGoal = 0; // 0 = no goal
        private String preferredUnit = "";
        
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder setAutomatedCollection(boolean automated) {
            this.automatedCollection = automated;
            return this;
        }
        
        public Builder setNotificationsEnabled(boolean enabled) {
            this.notificationsEnabled = enabled;
            return this;
        }
        
        public Builder setCollectionFrequency(long frequencyMs) {
            this.collectionFrequency = frequencyMs;
            return this;
        }
        
        public Builder setNotificationInterval(long intervalMs) {
            this.notificationInterval = intervalMs;
            return this;
        }
        
        public Builder setDailyGoal(int goal) {
            this.dailyGoal = goal;
            return this;
        }
        
        public Builder setPreferredUnit(String unit) {
            this.preferredUnit = unit;
            return this;
        }
        
        public CollectorSettings build() {
            return new CollectorSettings(this);
        }
    }
    
    // Getters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isAutomatedCollection() {
        return automatedCollection;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public long getCollectionFrequency() {
        return collectionFrequency;
    }
    
    public long getNotificationInterval() {
        return notificationInterval;
    }
    
    public int getDailyGoal() {
        return dailyGoal;
    }
    
    public String getPreferredUnit() {
        return preferredUnit;
    }
    
    /**
     * Create a copy with modifications
     */
    public Builder toBuilder() {
        return new Builder()
            .setEnabled(enabled)
            .setAutomatedCollection(automatedCollection)
            .setNotificationsEnabled(notificationsEnabled)
            .setCollectionFrequency(collectionFrequency)
            .setNotificationInterval(notificationInterval)
            .setDailyGoal(dailyGoal)
            .setPreferredUnit(preferredUnit);
    }
    
    @Override
    public String toString() {
        return "CollectorSettings{" +
                "enabled=" + enabled +
                ", automated=" + automatedCollection +
                ", notifications=" + notificationsEnabled +
                ", frequency=" + collectionFrequency +
                ", goal=" + dailyGoal +
                '}';
    }
}
