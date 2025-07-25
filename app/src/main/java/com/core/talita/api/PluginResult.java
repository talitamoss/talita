package com.core.talita.api;

import java.util.HashMap;
import java.util.Map;

/**
 * PluginResult - Represents the result of a plugin operation
 * 
 * Used to return status and data from plugin methods in a consistent way.
 * Similar to CollectorResult but more general purpose.
 */
public class PluginResult {
    
    public enum Status {
        SUCCESS,
        FAILURE,
        PENDING,
        CANCELLED,
        REQUIRES_PERMISSION,
        REQUIRES_USER_INPUT
    }
    
    private final Status status;
    private final String message;
    private final Map<String, Object> data;
    private final Throwable error;
    
    private PluginResult(Status status, String message, Map<String, Object> data, Throwable error) {
        this.status = status;
        this.message = message;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.error = error;
    }
    
    /**
     * Create a success result
     */
    public static PluginResult success() {
        return new PluginResult(Status.SUCCESS, null, null, null);
    }
    
    /**
     * Create a success result with message
     */
    public static PluginResult success(String message) {
        return new PluginResult(Status.SUCCESS, message, null, null);
    }
    
    /**
     * Create a success result with data
     */
    public static PluginResult success(Map<String, Object> data) {
        return new PluginResult(Status.SUCCESS, null, data, null);
    }
    
    /**
     * Create a success result with message and data
     */
    public static PluginResult success(String message, Map<String, Object> data) {
        return new PluginResult(Status.SUCCESS, message, data, null);
    }
    
    /**
     * Create a failure result
     */
    public static PluginResult failure(String message) {
        return new PluginResult(Status.FAILURE, message, null, null);
    }
    
    /**
     * Create a failure result with error
     */
    public static PluginResult failure(String message, Throwable error) {
        return new PluginResult(Status.FAILURE, message, null, error);
    }
    
    /**
     * Create a pending result
     */
    public static PluginResult pending() {
        return new PluginResult(Status.PENDING, "Operation in progress", null, null);
    }
    
    /**
     * Create a pending result with message
     */
    public static PluginResult pending(String message) {
        return new PluginResult(Status.PENDING, message, null, null);
    }
    
    /**
     * Create a cancelled result
     */
    public static PluginResult cancelled() {
        return new PluginResult(Status.CANCELLED, "Operation cancelled", null, null);
    }
    
    /**
     * Create a cancelled result with message
     */
    public static PluginResult cancelled(String message) {
        return new PluginResult(Status.CANCELLED, message, null, null);
    }
    
    /**
     * Create a requires permission result
     */
    public static PluginResult requiresPermission(String permission) {
        Map<String, Object> data = new HashMap<>();
        data.put("permission", permission);
        return new PluginResult(Status.REQUIRES_PERMISSION, 
            "Permission required: " + permission, data, null);
    }
    
    /**
     * Create a requires user input result
     */
    public static PluginResult requiresUserInput(String inputType) {
        Map<String, Object> data = new HashMap<>();
        data.put("inputType", inputType);
        return new PluginResult(Status.REQUIRES_USER_INPUT, 
            "User input required: " + inputType, data, null);
    }
    
    // Builder pattern for complex results
    public static class Builder {
        private Status status = Status.SUCCESS;
        private String message;
        private Map<String, Object> data = new HashMap<>();
        private Throwable error;
        
        public Builder status(Status status) {
            this.status = status;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder putData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }
        
        public Builder putAllData(Map<String, Object> data) {
            this.data.putAll(data);
            return this;
        }
        
        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }
        
        public PluginResult build() {
            return new PluginResult(status, message, data, error);
        }
    }
    
    // Getters
    
    public Status getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public Throwable getError() {
        return error;
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
    
    public boolean requiresPermission() {
        return status == Status.REQUIRES_PERMISSION;
    }
    
    public boolean requiresUserInput() {
        return status == Status.REQUIRES_USER_INPUT;
    }
    
    @Override
    public String toString() {
        return "PluginResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", dataSize=" + data.size() +
                ", hasError=" + (error != null) +
                '}';
    }
}
