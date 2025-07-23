package com.core.talita;

import java.util.Map;
import java.util.HashMap;

/**
 * Adapter to bridge between PersonalData and UniversalDataType
 */
public class PersonalDataAdapter implements PersonalData, UniversalDataType {
    private final UniversalDataType universalData;
    private final PersonalData personalData;
    
    // Single constructor that handles both types
    public PersonalDataAdapter(Object data) {
        if (data instanceof UniversalDataType) {
            this.universalData = (UniversalDataType) data;
            if (data instanceof PersonalData) {
                this.personalData = (PersonalData) data;
            } else {
                this.personalData = new PersonalDataWrapper((UniversalDataType) data);
            }
        } else if (data instanceof PersonalData) {
            this.personalData = (PersonalData) data;
            this.universalData = new UniversalDataWrapper((PersonalData) data);
        } else {
            throw new IllegalArgumentException("Data must be UniversalDataType or PersonalData");
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
    
    // UniversalDataType methods
    @Override
    public String getId() {
        return universalData.getId();
    }
    
    @Override
    public String getType() {
        return universalData.getType();
    }
    
    @Override
    public String getFilePath() {
        return universalData.getFilePath();
    }
    
    @Override
    public String toJson() {
        return universalData.toJson();
    }
    
    @Override
    public String getDisplayName() {
        return universalData.getDisplayName();
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return universalData.getMetadata();
    }
    
    @Override
    public double getLatitude() {
        return universalData.getLatitude();
    }
    
    @Override
    public double getLongitude() {
        return universalData.getLongitude();
    }
    
    // Inner wrapper classes
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
    
    private static class UniversalDataWrapper implements UniversalDataType {
        private final PersonalData data;
        
        UniversalDataWrapper(PersonalData data) {
            this.data = data;
        }
        
        @Override
        public String getType() {
            return data.getDataType();
        }
        
        @Override
        public String getId() {
            return String.valueOf(data.hashCode());
        }
        
        @Override
        public String toJson() {
            return "{}";
        }
        
        @Override
        public String getFilePath() {
            return null;
        }
        
        @Override
        public long getTimestamp() {
            return data.getTimestamp();
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
        public Map<String, Object> getMetadata() {
            return new HashMap<>();
        }
        
        @Override
        public String getDisplayName() {
            return data.getDisplaySummary();
        }
        
        @Override
        public String getDisplaySummary() {
            return data.getDisplaySummary();
        }
    }
}
