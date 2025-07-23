package com.core.talita;

import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Enhanced location data with activity context
 */
class EnhancedLocationData implements UniversalDataType {
    private final String id;
    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final String provider;
    private final long timestamp;
    private final float speed;
    private final float bearing;
    private final String activityType;
    private final int confidence;

    public EnhancedLocationData(double latitude, double longitude, double accuracy,
                                String provider, float speed, float bearing,
                                String activityType, int confidence) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.provider = provider != null ? provider : "unknown";
        this.speed = speed;
        this.bearing = bearing;
        this.activityType = activityType != null ? activityType : "unknown";
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", provider);
        metadata.put("accuracy", accuracy);
        metadata.put("speed", speed);
        metadata.put("bearing", bearing);
        metadata.put("activityType", activityType);
        metadata.put("confidence", confidence);
        return metadata;
    }

    @Override
    public String getType() {
        return "enhanced_location";
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
            json.put("type", "enhanced_location");
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("accuracy", accuracy);
            json.put("provider", provider);
            json.put("speed", speed);
            json.put("bearing", bearing);
            json.put("activityType", activityType);
            json.put("confidence", confidence);
            json.put("timestamp", timestamp);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return null;
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
        return String.format("Location (%s)", activityType);
    }

    @Override
    public String getDisplaySummary() {
        return getDisplayName();
    }
}

/**
 * Step count data
 */
class StepData implements UniversalDataType {
    private final String id;
    private final int steps;
    private final long timestamp;
    
    public StepData(int steps) {
        this.id = UUID.randomUUID().toString();
        this.steps = steps;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("steps", steps);
        return metadata;
    }

    @Override
    public String getType() {
        return "steps";
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
            json.put("type", "steps");
            json.put("steps", steps);
            json.put("timestamp", timestamp);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getLatitude() {
        return 0.0;
    }

    @Override
    public double getLongitude() {
        return 0.0;
    }

    @Override
    public String getDisplayName() {
        return steps + " steps";
    }

    @Override
    public String getDisplaySummary() {
        return getDisplayName();
    }
}

/**
 * Activity recognition data
 */
class ActivityData implements UniversalDataType {
    private final String id;
    private final String activity;
    private final int confidence;
    private final long timestamp;
    
    public ActivityData(String activity, int confidence) {
        this.id = UUID.randomUUID().toString();
        this.activity = activity;
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("activity", activity);
        metadata.put("confidence", confidence);
        return metadata;
    }

    @Override
    public String getType() {
        return "activity";
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
            json.put("type", "activity");
            json.put("activity", activity);
            json.put("confidence", confidence);
            json.put("timestamp", timestamp);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getLatitude() {
        return 0.0;
    }

    @Override
    public double getLongitude() {
        return 0.0;
    }

    @Override
    public String getDisplayName() {
        return activity + " (" + confidence + "%)";
    }

    @Override
    public String getDisplaySummary() {
        return getDisplayName();
    }
}
