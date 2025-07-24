package com.core.talita.api;

/**
 * Settings for a data collector
 */
public class CollectorSettings {
    private boolean enabled;
    private boolean automatedCollection;
    private int collectionFrequency; // in minutes
    private boolean batteryOptimized;
    private boolean requiresLocation;
    
    private CollectorSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.automatedCollection = builder.automatedCollection;
        this.collectionFrequency = builder.collectionFrequency;
        this.batteryOptimized = builder.batteryOptimized;
        this.requiresLocation = builder.requiresLocation;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isAutomatedCollection() {
        return automatedCollection;
    }
    
    public int getCollectionFrequency() {
        return collectionFrequency;
    }
    
    public boolean isBatteryOptimized() {
        return batteryOptimized;
    }
    
    public boolean isRequiresLocation() {
        return requiresLocation;
    }
    
    public static CollectorSettings getDefault() {
        return new Builder()
            .setEnabled(true)
            .setAutomatedCollection(false)
            .setCollectionFrequency(30)
            .setBatteryOptimized(true)
            .setRequiresLocation(false)
            .build();
    }
    
    public static class Builder {
        private boolean enabled = true;
        private boolean automatedCollection = false;
        private int collectionFrequency = 30;
        private boolean batteryOptimized = true;
        private boolean requiresLocation = false;
        
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder setAutomatedCollection(boolean automatedCollection) {
            this.automatedCollection = automatedCollection;
            return this;
        }
        
        public Builder setCollectionFrequency(int minutes) {
            this.collectionFrequency = minutes;
            return this;
        }
        
        public Builder setBatteryOptimized(boolean optimized) {
            this.batteryOptimized = optimized;
            return this;
        }
        
        public Builder setRequiresLocation(boolean requires) {
            this.requiresLocation = requires;
            return this;
        }
        
        public CollectorSettings build() {
            return new CollectorSettings(this);
        }
    }
}
