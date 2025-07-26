package com.core.talita;

import android.location.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * LocationData - Represents a location data point with activity context
 * Implements both PersonalDataInterface and UniversalDataType
 */
public class LocationData implements PersonalDataInterface, UniversalDataType {
    
    private final String id;
    private final double latitude;
    private final double longitude;
    private final long timestamp;
    private double accuracy = 0;
    private String provider = "unknown";
    private float speed = 0;
    private float bearing = 0;
    private String activity = "unknown";
    private int activityConfidence = 0;
    private int steps = 0;
    
    /**
     * Constructor from Android Location
     */
    public LocationData(Location location) {
        this.id = UUID.randomUUID().toString();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.timestamp = location.getTime();
        this.accuracy = location.getAccuracy();
        this.provider = location.getProvider();
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();
    }
    
    /**
     * Constructor with basic coordinates
     */
    public LocationData(double latitude, double longitude, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    
    /**
     * Private constructor for Builder
     */
    private LocationData(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.timestamp = builder.timestamp;
        this.accuracy = builder.accuracy;
        this.provider = builder.provider;
        this.speed = builder.speed;
        this.bearing = builder.bearing;
        this.activity = builder.activity;
        this.activityConfidence = builder.activityConfidence;
        this.steps = builder.steps;
    }
    
    // PersonalDataInterface implementation
    
    @Override
    public String getType() {
        return "location";
    }
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("accuracy", accuracy);
        data.put("provider", provider);
        data.put("speed", speed);
        data.put("bearing", bearing);
        return data;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("activity", activity);
        metadata.put("activity_confidence", activityConfidence);
        metadata.put("steps", steps);
        return metadata;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    // UniversalDataType implementation
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getDisplayName() {
        return "Location";
    }
    
    @Override
    public String getDisplaySummary() {
        return String.format("📍 %.4f, %.4f", latitude, longitude);
    }
    
    @Override
    public double getLatitude() {
        return latitude;
    }
    
    @Override
    public double getLongitude() {
        return longitude;
    }
    
    // Additional getters
    
    public double getAccuracy() {
        return accuracy;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public float getBearing() {
        return bearing;
    }
    
    public String getActivity() {
        return activity;
    }
    
    public int getActivityConfidence() {
        return activityConfidence;
    }
    
    public int getSteps() {
        return steps;
    }
    
    /**
     * Builder for LocationData
     */
    public static class Builder {
        private final double latitude;
        private final double longitude;
        private long timestamp = System.currentTimeMillis();
        private double accuracy = 0;
        private String provider = "unknown";
        private float speed = 0;
        private float bearing = 0;
        private String activity = "unknown";
        private int activityConfidence = 0;
        private int steps = 0;
        
        public Builder(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
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
        
        public Builder activity(String activity, int confidence) {
            this.activity = activity;
            this.activityConfidence = confidence;
            return this;
        }
        
        public Builder steps(int steps) {
            this.steps = steps;
            return this;
        }
        
        public LocationData build() {
            return new LocationData(this);
        }
    }
}
