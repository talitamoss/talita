package com.core.talita;

import android.location.Location;
import java.util.HashMap;
import java.util.Map;

/**
 * LocationData - Represents location data points
 * 
 * Wraps Android Location objects for storage in the data pipeline
 */
public class LocationData implements PersonalDataInterface {
    private final double latitude;
    private final double longitude;
    private final float accuracy;
    private final float speed;
    private final float bearing;
    private final double altitude;
    private final String provider;
    private final long timestamp;
    
    public LocationData(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();
        this.altitude = location.getAltitude();
        this.provider = location.getProvider();
        this.timestamp = location.getTime();
    }
    
    public LocationData(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = 0;
        this.speed = 0;
        this.bearing = 0;
        this.altitude = 0;
        this.provider = "manual";
        this.timestamp = timestamp;
    }
    
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
        data.put("speed", speed);
        data.put("bearing", bearing);
        data.put("altitude", altitude);
        data.put("provider", provider);
        return data;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "gps");
        metadata.put("accuracy_meters", accuracy);
        metadata.put("provider", provider);
        return metadata;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    // Getters
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public float getAccuracy() {
        return accuracy;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public float getBearing() {
        return bearing;
    }
    
    public double getAltitude() {
        return altitude;
    }
    
    public String getProvider() {
        return provider;
    }
    
    /**
     * Convert to PersonalData for storage
     */
    public PersonalData toPersonalData() {
        return new PersonalData(getType(), getData(), getMetadata(), timestamp);
    }
    
    /**
     * Calculate distance to another location in meters
     */
    public float distanceTo(LocationData other) {
        float[] results = new float[1];
        Location.distanceBetween(
            this.latitude, this.longitude,
            other.latitude, other.longitude,
            results
        );
        return results[0];
    }
}
