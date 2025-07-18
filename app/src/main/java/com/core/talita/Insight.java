package com.core.talita;

public class Insight {
    private final String description;
    private final double correlationStrength;
    private final String category;
    private final long discoveredTimestamp;

    public Insight(String description, double correlationStrength,
                   String category, long discoveredTimestamp) {
        this.description = description;
        this.correlationStrength = correlationStrength;
        this.category = category;
        this.discoveredTimestamp = discoveredTimestamp;
    }

    public String getDescription() {
        return description;
    }

    public double getCorrelationStrength() {
        return correlationStrength;
    }

    public String getCategory() {
        return category;
    }

    public long getDiscoveredTimestamp() {
        return discoveredTimestamp;
    }
}