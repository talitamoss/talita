package com.core.talita;

import java.util.Map;

/**
 * Adapter to bridge between PersonalData and UniversalDataType
 * Allows UniversalDataType objects to be used as PersonalData
 */
public class PersonalDataAdapter implements PersonalData, UniversalDataType {
    private final PersonalData personalData;
    
    public PersonalDataAdapter(PersonalData data) {
        this.personalData = data;
    }
    
    // Constructor for UniversalDataType
    public PersonalDataAdapter(UniversalDataType data) {
        if (data instanceof PersonalData) {
            this.personalData = (PersonalData) data;
        } else {
            // Create a wrapper that implements PersonalData
            this.personalData = new PersonalDataWrapper(data);
        }
    }
    
    // PersonalData methods
    @Override
    public String getDataType() {
        return personalData.getDataType();
    }
    
    @Override
    public long getTimestamp() {
        return personalData.getTimestamp();
    }
    
    @Override
    public String getDisplaySummary() {
        return personalData.getDisplaySummary();
    }
    
    @Override
    public Object getValue() {
        return personalData.getValue();
    }
    
    // UniversalDataType methods (delegate to PersonalData if it implements it)
    @Override
    public String getId() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getId();
        }
        return String.valueOf(personalData.hashCode());
    }
    
    @Override
    public String getType() {
        return personalData.getDataType();
    }
    
    @Override
    public String getFilePath() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getFilePath();
        }
        return null;
    }
    
    @Override
    public String toJson() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).toJson();
        }
        return "{}";
    }
    
    @Override
    public String getDisplayName() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getDisplayName();
        }
        return personalData.getDisplaySummary();
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getMetadata();
        }
        return new java.util.HashMap<>();
    }
    
    @Override
    public double getLatitude() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getLatitude();
        }
        return 0.0;
    }
    
    @Override
    public double getLongitude() {
        if (personalData instanceof UniversalDataType) {
            return ((UniversalDataType) personalData).getLongitude();
        }
        return 0.0;
    }
    
    /**
     * Inner class to wrap UniversalDataType as PersonalData
     */
    private static class PersonalDataWrapper implements PersonalData {
        private final UniversalDataType data;
        
        PersonalDataWrapper(UniversalDataType data) {
            this.data = data;
        }
        
        @Override
        public String getDataType() {
            return data.getType();
        }
        
        @Override
        public long getTimestamp() {
            return data.getTimestamp();
        }
        
        @Override
        public String getDisplaySummary() {
            return data.getDisplayName();
        }
        
        @Override
        public Object getValue() {
            Map<String, Object> metadata = data.getMetadata();
            if (metadata != null && metadata.containsKey("value")) {
                return metadata.get("value");
            }
            return data.getDisplayName();
        }
    }
}
