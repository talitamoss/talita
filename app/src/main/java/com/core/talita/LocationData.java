package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Location data that implements UniversalDataType
 * Consolidated version that includes all enhanced features
 */
public class LocationData implements UniversalDataType {
    private final String id;
    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final String provider;
    private final long timestamp;
    private final float speed;
    private final float bearing;
    
    // Enhanced fields (optional)
    private String activityType;
    private int activityConfidence;
    private int stepCount;
    
    // Basic constructor
    public LocationData(double latitude, double longitude, double accuracy, String provider) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.provider = provider != null ? provider : "unknown";
        this.timestamp = System.currentTimeMillis();
        this.speed = 0.0f;
        this.bearing = 0.0f;
        this.activityType = "unknown";
        this.activityConfidence = 0;
        this.stepCount = 0;
    }
    
    // Enhanced constructor with speed and bearing
    public LocationData(double latitude, double longitude, double accuracy, 
                       String provider, float speed, float bearing) {
        this(latitude, longitude, accuracy, provider);
        this.speed = speed;
        this.bearing = bearing;
    }
    
    // Full constructor with activity context
    public LocationData(double latitude, double longitude, double accuracy,
                       String provider, float speed, float bearing,
                       String activityType, int activityConfidence, int stepCount) {
        this(latitude, longitude, accuracy, provider, speed, bearing);
        this.activityType = activityType != null ? activityType : "unknown";
        this.activityConfidence = activityConfidence;
        this.stepCount = stepCount;
    }

    @Override
    public String getType() {
        return "location";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("type", "location");
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("accuracy", accuracy);
            json.put("provider", provider);
            json.put("speed", speed);
            json.put("bearing", bearing);
            json.put("timestamp", timestamp);
            
            // Include enhanced data if available
            if (!activityType.equals("unknown")) {
                json.put("activityType", activityType);
                json.put("activityConfidence", activityConfidence);
            }
            if (stepCount > 0) {
                json.put("stepCount", stepCount);
            }
            
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return null; // Location data has no associated file
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String getDisplayName() {
        if (!activityType.equals("unknown")) {
            return String.format("Location (%s)", activityType);
        }
        return "Location";
    }

    @Override
    public String getDisplaySummary() {
        String summary = String.format("%.6f, %.6f (±%.0fm)", latitude, longitude, accuracy);
        if (!activityType.equals("unknown")) {
            summary += String.format(" • %s (%d%%)", activityType, activityConfidence);
        }
        if (stepCount > 0) {
            summary += String.format(" • %d steps", stepCount);
        }
        return summary;
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", provider);
        metadata.put("accuracy", accuracy);
        metadata.put("speed", speed);
        metadata.put("bearing", bearing);
        
        if (!activityType.equals("unknown")) {
            metadata.put("activityType", activityType);
            metadata.put("activityConfidence", activityConfidence);
        }
        if (stepCount > 0) {
            metadata.put("stepCount", stepCount);
        }
        
        return metadata;
    }

    // Getters for all fields
    public double getAccuracy() { return accuracy; }
    public String getProvider() { return provider; }
    public float getSpeed() { return speed; }
    public float getBearing() { return bearing; }
    public String getActivityType() { return activityType; }
    public int getActivityConfidence() { return activityConfidence; }
    public int getStepCount() { return stepCount; }
    
    // Builder pattern for cleaner construction
    public static class Builder {
        private double latitude;
        private double longitude;
        private double accuracy;
        private String provider = "unknown";
        private float speed = 0.0f;
        private float bearing = 0.0f;
        private String activityType = "unknown";
        private int activityConfidence = 0;
        private int stepCount = 0;
        
        public Builder(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public Builder accuracy(double accuracy) {
            this.accuracy = accuracy;
            return this;
        }
        
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }
        
        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }
        
        public Builder bearing(float bearing) {
            this.bearing = bearing;
            return this;
        }
        
        public Builder activity(String type, int confidence) {
            this.activityType = type;
            this.activityConfidence = confidence;
            return this;
        }
        
        public Builder steps(int steps) {
            this.stepCount = steps;
            return this;
        }
        
        public LocationData build() {
            return new LocationData(latitude, longitude, accuracy, provider, 
                                  speed, bearing, activityType, activityConfidence, stepCount);
        }
    }
}
