package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

/**
 * Location data that implements TalitaDataType
 * Gets automatic database storage, cloud backup, and sharing
 */
public class LocationData implements TalitaDataType {

    private final String id;
    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final String provider;
    private final long timestamp;
    private final float speed;
    private final float bearing;

    public LocationData(double latitude, double longitude, double accuracy, String provider) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.provider = provider != null ? provider : "unknown";
        this.timestamp = System.currentTimeMillis();
        this.speed = 0.0f; // Can be enhanced later
        this.bearing = 0.0f; // Can be enhanced later
    }

    // Enhanced constructor with speed and bearing
    public LocationData(double latitude, double longitude, double accuracy, String provider, float speed, float bearing) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.provider = provider != null ? provider : "unknown";
        this.timestamp = System.currentTimeMillis();
        this.speed = speed;
        this.bearing = bearing;
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
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("accuracy", accuracy);
            json.put("provider", provider);
            json.put("speed", speed);
            json.put("bearing", bearing);
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
        return "Location Point";
    }

    @Override
    public String getDisplaySummary() {
        return String.format("%.6f, %.6f (±%.0fm)", latitude, longitude, accuracy);
    }

    // Getters for additional location-specific data
    public double getAccuracy() { return accuracy; }
    public String getProvider() { return provider; }
    public float getSpeed() { return speed; }
    public float getBearing() { return bearing; }
}