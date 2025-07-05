package com.core.talita;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.UUID;

/**
 * Audio data that implements TalitaDataType
 * Gets automatic database storage, cloud backup, and sharing
 */
public class AudioData implements TalitaDataType {

    private final String id;
    private final String filePath;
    private final long durationMs;
    private final String format;
    private final int sampleRate;
    private final int channels;
    private final long fileSizeBytes;
    private final long timestamp;
    private final double latitude;
    private final double longitude;
    private final String recordingContext; // "voice_memo", "meeting", etc.

    public AudioData(String filePath, long durationMs, double latitude, double longitude) {
        this.id = UUID.randomUUID().toString();
        this.filePath = filePath;
        this.durationMs = durationMs;
        this.format = "aac"; // Default format
        this.sampleRate = 44100; // Default sample rate
        this.channels = 1; // Mono by default
        this.timestamp = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordingContext = "voice_memo";

        // Calculate file size
        this.fileSizeBytes = calculateFileSize(filePath);
    }

    // Enhanced constructor with audio details
    public AudioData(String filePath, long durationMs, String format, int sampleRate, int channels,
                     double latitude, double longitude, String recordingContext) {
        this.id = UUID.randomUUID().toString();
        this.filePath = filePath;
        this.durationMs = durationMs;
        this.format = format;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.timestamp = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordingContext = recordingContext != null ? recordingContext : "voice_memo";
        this.fileSizeBytes = calculateFileSize(filePath);
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
            json.put("duration_ms", durationMs);
            json.put("format", format);
            json.put("sample_rate", sampleRate);
            json.put("channels", channels);
            json.put("file_size_bytes", fileSizeBytes);
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("recording_context", recordingContext);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
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
        return "Audio Recording";
    }

    @Override
    public String getDisplaySummary() {
        return String.format("%s recording (%.1fs, %s)",
                formatRecordingContext(),
                durationMs / 1000.0,
                formatFileSize());
    }

    // Helper methods
    private long calculateFileSize(String filePath) {
        try {
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    return file.length();
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return 0;
    }

    private String formatRecordingContext() {
        switch (recordingContext.toLowerCase()) {
            case "voice_memo": return "Voice memo";
            case "meeting": return "Meeting";
            case "interview": return "Interview";
            case "note": return "Audio note";
            default: return "Audio";
        }
    }

    private String formatFileSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + "B";
        else if (fileSizeBytes < 1024 * 1024) return String.format("%.1fKB", fileSizeBytes / 1024.0);
        else return String.format("%.1fMB", fileSizeBytes / (1024.0 * 1024.0));
    }

    // Getters for audio-specific data
    public long getDurationMs() { return durationMs; }
    public String getFormat() { return format; }
    public int getSampleRate() { return sampleRate; }
    public int getChannels() { return channels; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getRecordingContext() { return recordingContext; }
}