package com.core.talita.api;

import java.util.Map;

/**
 * CollectorResult - Result of a data collection attempt
 * 
 * Immutable result object that indicates success/failure and contains collected data
 */
public class CollectorResult {
    private final boolean success;
    private final String dataType;
    private final Map<String, Object> data;
    private final String errorMessage;
    private final long timestamp;

    private CollectorResult(boolean success, String dataType, Map<String, Object> data, String errorMessage) {
        this.success = success;
        this.dataType = dataType;
        this.data = data;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }

    // Factory methods
    public static CollectorResult success(String dataType, Map<String, Object> data) {
        return new CollectorResult(true, dataType, data, null);
    }

    public static CollectorResult failure(String dataType, String errorMessage) {
        return new CollectorResult(false, dataType, null, errorMessage);
    }

    public static CollectorResult cancelled(String dataType) {
        return new CollectorResult(false, dataType, null, "Collection cancelled by user");
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getDataType() { return dataType; }
    public Map<String, Object> getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
    public long getTimestamp() { return timestamp; }
}
