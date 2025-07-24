package com.core.talita.collectors;

/**
 * CollectorSettings - Configuration for data collectors
 * Used by plugins to define their collection behavior
 */
public class CollectorSettings {
    private int frequency = 0; // 0 = manual, >0 = minutes between collections
    private boolean batteryOptimized = true;
    private boolean requiresNetwork = false;
    private boolean requiresLocation = false;
    
    public CollectorSettings() {
    }
    
    // Fluent API for easy configuration
    public CollectorSettings setFrequency(int minutes) {
        this.frequency = minutes;
        return this;
    }
    
    public CollectorSettings setBatteryOptimized(boolean optimized) {
        this.batteryOptimized = optimized;
        return this;
    }
    
    public CollectorSettings setRequiresNetwork(boolean requires) {
        this.requiresNetwork = requires;
        return this;
    }
    
    public CollectorSettings setRequiresLocation(boolean requires) {
        this.requiresLocation = requires;
        return this;
    }
    
    // Getters
    public int getFrequency() {
        return frequency;
    }
    
    public boolean isBatteryOptimized() {
        return batteryOptimized;
    }
    
    public boolean requiresNetwork() {
        return requiresNetwork;
    }
    
    public boolean requiresLocation() {
        return requiresLocation;
    }
    
    public boolean isManualCollection() {
        return frequency == 0;
    }
}
