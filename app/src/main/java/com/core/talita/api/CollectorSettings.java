package com.core.talita.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * CollectorSettings - Configuration for data collectors
 * 
 * Immutable settings object that plugins use to configure collectors.
 * Use the Builder pattern for creating instances.
 */
public class CollectorSettings implements Serializable {
    private final boolean enabled;
    private final boolean automatedCollection;
    private final int collectionFrequencyMinutes;
    private final boolean batteryOptimized;
    private final boolean requiresNetwork;
    private final boolean requiresLocation;
    private final Map<String, Object> customSettings;

    private CollectorSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.automatedCollection = builder.automatedCollection;
        this.collectionFrequencyMinutes = builder.collectionFrequencyMinutes;
        this.batteryOptimized = builder.batteryOptimized;
        this.requiresNetwork = builder.requiresNetwork;
        this.requiresLocation = builder.requiresLocation;
        this.customSettings = new HashMap<>(builder.customSettings);
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public boolean isAutomatedCollection() { return automatedCollection; }
    public int getCollectionFrequencyMinutes() { return collectionFrequencyMinutes; }
    public boolean isBatteryOptimized() { return batteryOptimized; }
    public boolean requiresNetwork() { return requiresNetwork; }
    public boolean requiresLocation() { return requiresLocation; }
    public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }
    public Object getCustomSetting(String key) { return customSettings.get(key); }

    /**
     * Builder for creating CollectorSettings
     */
    public static class Builder {
        private boolean enabled = true;
        private boolean automatedCollection = false;
        private int collectionFrequencyMinutes = 0;
        private boolean batteryOptimized = true;
        private boolean requiresNetwork = false;
        private boolean requiresLocation = false;
        private Map<String, Object> customSettings = new HashMap<>();

        public Builder() {}

        public Builder(CollectorSettings settings) {
            this.enabled = settings.enabled;
            this.automatedCollection = settings.automatedCollection;
            this.collectionFrequencyMinutes = settings.collectionFrequencyMinutes;
            this.batteryOptimized = settings.batteryOptimized;
            this.requiresNetwork = settings.requiresNetwork;
            this.requiresLocation = settings.requiresLocation;
            this.customSettings = new HashMap<>(settings.customSettings);
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setAutomatedCollection(boolean automated) {
            this.automatedCollection = automated;
            return this;
        }

        public Builder setCollectionFrequency(int minutes) {
            this.collectionFrequencyMinutes = minutes;
            return this;
        }

        public Builder setBatteryOptimized(boolean optimized) {
            this.batteryOptimized = optimized;
            return this;
        }

        public Builder setRequiresNetwork(boolean requires) {
            this.requiresNetwork = requires;
            return this;
        }

        public Builder setRequiresLocation(boolean requires) {
            this.requiresLocation = requires;
            return this;
        }

        public Builder setCustomSetting(String key, Object value) {
            this.customSettings.put(key, value);
            return this;
        }

        public Builder setCustomSettings(Map<String, Object> settings) {
            this.customSettings.putAll(settings);
            return this;
        }

        public CollectorSettings build() {
            return new CollectorSettings(this);
        }
    }

    /**
     * Create a default settings instance
     */
    public static CollectorSettings getDefault() {
        return new Builder().build();
    }
}
