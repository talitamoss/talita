package com.core.talita.api;

/**
 * Result of a data collection operation
 */
public class CollectorResult {
    private final boolean success;
    private final String type;
    private final String message;
    private final Object data;
    
    private CollectorResult(boolean success, String type, String message, Object data) {
        this.success = success;
        this.type = type;
        this.message = message;
        this.data = data;
    }
    
    public static CollectorResult success() {
        return new CollectorResult(true, null, null, null);
    }
    
    public static CollectorResult success(String type, Object data) {
        return new CollectorResult(true, type, null, data);
    }
    
    public static CollectorResult failure(String type, String message) {
        return new CollectorResult(false, type, message, null);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getType() {
        return type;
    }
    
    public String getErrorMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
}
