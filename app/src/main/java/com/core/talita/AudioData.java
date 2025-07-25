package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * AudioData - Represents audio recording data
 * 
 * Stores metadata about audio recordings (the actual audio is encrypted separately)
 */
public class AudioData implements PersonalDataInterface {
    private final String filename;
    private final long duration; // in seconds
    private final long timestamp;
    private final String transcription; // optional
    
    public AudioData(String filename, long duration) {
        this.filename = filename;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.transcription = null;
    }
    
    public AudioData(String filename, long duration, String transcription) {
        this.filename = filename;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.transcription = transcription;
    }
    
    @Override
    public String getType() {
        return "audio";
    }
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("filename", filename);
        data.put("duration", duration);
        if (transcription != null) {
            data.put("transcription", transcription);
        }
        return data;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("format", "encrypted_audio");
        metadata.put("version", "1.0");
        return metadata;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    // Getters
    public String getFilename() {
        return filename;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public String getTranscription() {
        return transcription;
    }
    
    /**
     * Convert to PersonalData for storage
     */
    public PersonalData toPersonalData() {
        return new PersonalData(getType(), getData(), getMetadata(), timestamp);
    }
}
