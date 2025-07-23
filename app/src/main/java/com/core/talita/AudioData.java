package com.core.talita;

import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Audio data that implements UniversalDataType
 * Gets automatic database storage, cloud backup, and sharing
 */
public class AudioData implements UniversalDataType {
    
    private final String id;
    private final String filePath;
    private final long duration;
    private final long timestamp;
    private final double latitude;
    private final double longitude;
    
    public AudioData(String filePath, long duration) {
        this.id = UUID.randomUUID().toString();
        this.filePath = filePath;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.latitude = 0.0;
        this.longitude = 0.0;
    }
    
    public AudioData(String filePath, long duration, double latitude, double longitude) {
        this.id = UUID.randomUUID().toString();
        this.filePath = filePath;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Add getter method for duration
    public long getDurationMs() {
        return duration;
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", duration);
        metadata.put("format", "audio/3gpp");
        return metadata;
    }

    @Override
    public String getType() {
        return "audio";
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
            json.put("type", "audio");
            json.put("filePath", filePath);
            json.put("duration", duration);
            json.put("timestamp", timestamp);
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return filePath;
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
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("Audio Recording (%d:%02d)", minutes, seconds);
    }

    @Override
    public String getDisplaySummary() {
        return getDisplayName();
    }
}
