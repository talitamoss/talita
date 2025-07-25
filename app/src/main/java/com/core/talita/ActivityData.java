package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * ActivityData - Represents detected physical activity data
 * 
 * Used to store activity recognition results like walking, running, cycling, etc.
 * with confidence levels from Google's Activity Recognition API
 */
public class ActivityData {
    private final String activityType;
    private final int confidence;
    private final long timestamp;
    
    /**
     * Create new activity data
     * @param activityType The type of activity (WALKING, RUNNING, etc.)
     * @param confidence Confidence level (0-100)
     */
    public ActivityData(String activityType, int confidence) {
        this.activityType = activityType;
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create activity data with specific timestamp
     */
    public ActivityData(String activityType, int confidence, long timestamp) {
        this.activityType = activityType;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }
    
    /**
     * Convert to PersonalData for storage
     */
    public PersonalData toPersonalData() {
        Map<String, Object> data = new HashMap<>();
        data.put("activity", activityType);
        data.put("confidence", confidence);
        data.put("type", "activity_recognition");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "ActivityRecognition");
        metadata.put("confidence_level", getConfidenceLevel());
        
        return new PersonalData("activity", data, metadata, timestamp);
    }
    
    /**
     * Get human-readable confidence level
     */
    public String getConfidenceLevel() {
        if (confidence >= 75) {
            return "High";
        } else if (confidence >= 50) {
            return "Medium";
        } else {
            return "Low";
        }
    }
    
    /**
     * Check if this is a significant activity (high confidence)
     */
    public boolean isSignificant() {
        return confidence >= 75;
    }
    
    /**
     * Get a user-friendly activity name
     */
    public String getFriendlyActivityName() {
        switch (activityType.toUpperCase()) {
            case "IN_VEHICLE":
                return "Driving";
            case "ON_BICYCLE":
                return "Cycling";
            case "ON_FOOT":
                return "On Foot";
            case "RUNNING":
                return "Running";
            case "STILL":
                return "Still";
            case "TILTING":
                return "Phone Tilting";
            case "WALKING":
                return "Walking";
            case "UNKNOWN":
                return "Unknown";
            default:
                return activityType;
        }
    }
    
    // Getters
    public String getActivityType() {
        return activityType;
    }
    
    public int getConfidence() {
        return confidence;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "ActivityData{" +
                "activity='" + getFriendlyActivityName() + '\'' +
                ", confidence=" + confidence + "%" +
                ", level=" + getConfidenceLevel() +
                '}';
    }
}
