package com.core.talita;

public class PersonalDataAdapter implements UniversalDataType {
    private final PersonalData personalData;

    public PersonalDataAdapter(PersonalData personalData) {
        this.personalData = personalData;
    }

    @Override
    public String getType() {
        return personalData.getType();
    }

    @Override
    public String getId() {
        return personalData.getId();
    }

    @Override
    public String toJson() {
        return personalData.toJson();
    }

    @Override
    public String getFilePath() {
        return null; // Most manual collectors don't have files
    }

    @Override
    public long getTimestamp() {
        return personalData.getTimestamp();
    }

    @Override
    public double getLatitude() {
        // Extract from metadata if available
        try {
            Object lat = personalData.getMetadata().get("latitude");
            return lat instanceof Number ? ((Number) lat).doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getLongitude() {
        // Extract from metadata if available
        try {
            Object lng = personalData.getMetadata().get("longitude");
            return lng instanceof Number ? ((Number) lng).doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public String getDisplayName() {
        return personalData.getDisplayName();
    }

    @Override
    public String getDisplaySummary() {
        return personalData.getDisplaySummary();
    }
}
