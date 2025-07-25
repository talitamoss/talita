package com.core.talita.api;

/**
 * Result of a data collection operation
 * 
 * File path: app/src/main/java/com/core/talita/api/CollectorResult.java
 */
public class CollectorResult {
    
    public enum Status {
        SUCCESS,
        FAILURE,
        PENDING,
        CANCELLED
    }
    
    private final Status status;
    private final String type;
    private final String message;
    private final Object data;
    
    private CollectorResult(Status status, String type, String message, Object data) {
        this.status = status;
        this.type = type;
        this.message = message;
        this.data = data;
    }
    
    // Static factory methods
    
    public static CollectorResult success() {
        return new CollectorResult(Status.SUCCESS, null, null, null);
    }
    
    public static CollectorResult success(String type, Object data) {
        return new CollectorResult(Status.SUCCESS, type, null, data);
    }
    
    public static CollectorResult failure(String type, String message) {
        return new CollectorResult(Status.FAILURE, type, message, null);
    }
    
    public static CollectorResult pending(String type) {
        return new CollectorResult(Status.PENDING, type, "Operation pending", null);
    }
    
    public static CollectorResult pending(String type, String message) {
        return new CollectorResult(Status.PENDING, type, message, null);
    }
    
    public static CollectorResult cancelled(String type) {
        return new CollectorResult(Status.CANCELLED, type, "Operation cancelled", null);
    }
    
    // Getters
    
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
    
    public Status getStatus() {
        return status;
    }
    
    public String getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorMessage() {
        return message; // For backward compatibility
    }
    
    public Object getData() {
        return data;
    }
}
