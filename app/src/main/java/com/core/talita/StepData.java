package com.core.talita;

import java.util.HashMap;
import java.util.Map;

/**
 * StepData - Represents step count data
 * 
 * Used for tracking daily steps from device sensors
 */
public class StepData implements PersonalDataInterface {
    private final int stepCount;
    private final int stepDelta; // steps since last reading
    private final long timestamp;
    private final String source; // "sensor", "google_fit", etc.
    
    public StepData(int stepCount, int stepDelta) {
        this.stepCount = stepCount;
        this.stepDelta = stepDelta;
        this.timestamp = System.currentTimeMillis();
        this.source = "sensor";
    }
    
    public StepData(int stepCount, int stepDelta, String source) {
        this.stepCount = stepCount;
        this.stepDelta = stepDelta;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }
    
    @Override
    public String getType() {
        return "steps";
    }
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", stepCount);
        data.put("delta", stepDelta);
        data.put("source", source);
        return data;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("type", "cumulative");
        return metadata;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    // Getters
    public int getStepCount() {
        return stepCount;
    }
    
    public int getStepDelta() {
        return stepDelta;
    }
    
    public String getSource() {
        return source;
    }
    
    /**
     * Convert to PersonalData for storage
     */
    public PersonalData toPersonalData() {
        return new PersonalData(getType(), getData(), getMetadata(), timestamp);
    }
}
