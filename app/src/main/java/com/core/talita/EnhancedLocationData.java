package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import android.location.Location;
import java.util.UUID;

/**
 * Enhanced Location Data with activity context and step correlation
 */
class EnhancedLocationData implements TalitaDataType {

    private final String id;
    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final String provider;
    private final long timestamp;
    private final float speed;
    private final float bearing;
    private final String activity;
    private final int stepCount;

    public EnhancedLocationData(double latitude, double longitude, double accuracy,
                                String provider, float speed, float bearing,
                                String activity, int stepCount) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.provider = provider != null ? provider : "unknown";
        this.timestamp = System.currentTimeMillis();
        this.speed = speed;
        this.bearing = bearing;
        this.activity = activity != null ? activity : "unknown";
        this.stepCount = stepCount;
    }

    @Override
    public String getType() { return "enhanced_location"; }

    @Override
    public String getId() { return id; }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("accuracy", accuracy);
            json.put("provider", provider);
            json.put("speed", speed);
            json.put("bearing", bearing);
            json.put("activity", activity);
            json.put("step_count", stepCount);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String getFilePath() { return null; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public double getLatitude() { return latitude; }

    @Override
    public double getLongitude() { return longitude; }

    @Override
    public String getDisplayName() { return "Enhanced Location"; }

    @Override
    public String getDisplaySummary() {
        return String.format("%.6f, %.6f • %s • %.1fm/s • %d steps",
                latitude, longitude, activity, speed, stepCount);
    }
}

/**
 * Step Counter Data
 */
class StepData implements TalitaDataType {

    private final String id;
    private final int dailySteps;
    private final long timestamp;
    private final double latitude;
    private final double longitude;

    public StepData(int dailySteps, Location location) {
        this.id = UUID.randomUUID().toString();
        this.dailySteps = dailySteps;
        this.timestamp = System.currentTimeMillis();

        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        } else {
            this.latitude = 0.0;
            this.longitude = 0.0;
        }
    }

    @Override
    public String getType() { return "steps"; }

    @Override
    public String getId() { return id; }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("daily_steps", dailySteps);
            json.put("location_lat", latitude);
            json.put("location_lon", longitude);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String getFilePath() { return null; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public double getLatitude() { return latitude; }

    @Override
    public double getLongitude() { return longitude; }

    @Override
    public String getDisplayName() { return "Daily Steps"; }

    @Override
    public String getDisplaySummary() {
        return dailySteps + " steps today";
    }
}

/**
 * Activity Recognition Data
 */
class ActivityData implements TalitaDataType {

    private final String id;
    private final String activity;
    private final int confidence;
    private final long timestamp;
    private final double latitude;
    private final double longitude;

    public ActivityData(String activity, int confidence, Location location) {
        this.id = UUID.randomUUID().toString();
        this.activity = activity != null ? activity : "unknown";
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();

        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        } else {
            this.latitude = 0.0;
            this.longitude = 0.0;
        }
    }

    @Override
    public String getType() { return "activity"; }

    @Override
    public String getId() { return id; }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("activity", activity);
            json.put("confidence", confidence);
            json.put("location_lat", latitude);
            json.put("location_lon", longitude);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String getFilePath() { return null; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public double getLatitude() { return latitude; }

    @Override
    public double getLongitude() { return longitude; }

    @Override
    public String getDisplayName() { return "Activity Recognition"; }

    @Override
    public String getDisplaySummary() {
        return activity + " (" + confidence + "% confidence)";
    }
}