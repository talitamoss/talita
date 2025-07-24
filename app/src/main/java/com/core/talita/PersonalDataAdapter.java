package com.core.talita;

import java.util.Map;
import java.util.HashMap;

/**
 * Adapter to convert PersonalData objects to UniversalDataType
 * No longer tries to implement PersonalData since it's a class
 */
public class PersonalDataAdapter implements UniversalDataType {
    private final PersonalData personalData;
    
    public PersonalDataAdapter(PersonalData data) {
        this.personalData = data;
    }
    
    // Static factory method for compatibility
    public PersonalDataAdapter(UniversalPersonalData data) {
        // Convert UniversalPersonalData to PersonalData
        this.personalData = PersonalData.create(data.getType());
        this.personalData.setData(data.getAllData());
        if (data.getFilePath() != null) {
            this.personalData.setFilePath(data.getFilePath());
        }
    }
    
    // UniversalDataType methods
    @Override
    public String getId() {
        return personalData.getId();
    }
    
    @Override
    public String getType() {
        return personalData.getType();
    }
    
    @Override
    public String getFilePath() {
        return personalData.getFilePath();
    }
    
    @Override
    public String toJson() {
        return personalData.toJson();
    }
    
    @Override
    public String getDisplayName() {
        return personalData.getDisplayName();
    }
    
    @Override
    public String getDisplaySummary() {
        return personalData.getDisplaySummary();
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return personalData.getMetadata();
    }
    
    @Override
    public double getLatitude() {
        return personalData.getLatitude();
    }
    
    @Override
    public double getLongitude() {
        return personalData.getLongitude();
    }
    
    @Override
    public long getTimestamp() {
        return personalData.getTimestamp();
    }
    
    // Expose the wrapped PersonalData for compatibility
    public PersonalData getPersonalData() {
        return personalData;
    }
}
