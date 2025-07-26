package com.core.talita.api;

import java.util.Map;

/**
 * CollectorResult - Result of a data collection operation
 * 
 * Represents the outcome of attempting to collect data,
 * including success/failure status and any collected data.
 */
public class CollectorResult {
    
    public enum Status {
        SUCCESS,    // Data collected successfully
        FAILURE,    // Collection failed
        PENDING,    // Collection in progress (e.g., user input dialog shown)
        CANCELLED   // User cancelled the collection
    }
    
    private final Status status;
    private final String dataType;
    private final Map<String, Object> data;
    private final String errorMessage;
    private final long timestamp;
    
    private CollectorResult(Status status, String dataType, Map<String, Object> data, String errorMessage) {
        this.status = status;
        this.dataType = dataType;
        this.data = data;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a successful result with data
     */
    public static CollectorResult success(String dataType, Map<String, Object> data) {
        return new CollectorResult(Status.SUCCESS, dataType, data, null);
    }
    
    /**
     * Create a failure result with error message
     */
    public static CollectorResult failure(String dataType, String errorMessage) {
        return new CollectorResult(Status.FAILURE, dataType, null, errorMessage);
    }
    
    /**
     * Create a pending result (collection in progress)
     */
    public static CollectorResult pending(String dataType) {
        return new CollectorResult(Status.PENDING, dataType, null, null);
    }
    
    /**
     * Create a cancelled result
     */
    public static CollectorResult cancelled(String dataType) {
        return new CollectorResult(Status.CANCELLED, dataType, null, "Collection cancelled by user");
    }
    
    // Getters
    public Status getStatus() {
        return status;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isFailure() {
        return status == Status.FAILURE;
    }
    
    public boolean isPending() {
        return status == Status.PENDING;
    }
    
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
}
