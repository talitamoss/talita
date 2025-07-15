package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings configuration for collectors
 */
public class CollectorSettings {
    private final Map<String, Object> settings;

    public CollectorSettings() {
        this.settings = new HashMap<>();
    }

    public CollectorSettings setFrequency(long intervalMs) {
        settings.put("frequency", intervalMs);
        return this;
    }

    public CollectorSettings setThreshold(String key, Object value) {
        settings.put("threshold_" + key, value);
        return this;
    }

    public CollectorSettings setBatteryOptimized(boolean optimized) {
        settings.put("battery_optimized", optimized);
        return this;
    }

    public Object get(String key) {
        return settings.get(key);
    }

    public long getFrequency() {
        return (Long) settings.getOrDefault("frequency", 60000L);
    }
}