package com.core.talita.api;

/**
 * PluginResult - Result of plugin operations
 * 
 * Simple success/failure result for plugin operations.
 */
public class PluginResult {
    private final boolean success;
    private final String message;
    private final Object data;
    
    private PluginResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    /**
     * Create a success result
     */
    public static PluginResult success() {
        return new PluginResult(true, null, null);
    }
    
    /**
     * Create a success result with data
     */
    public static PluginResult success(Object data) {
        return new PluginResult(true, null, data);
    }
    
    /**
     * Create a success result with message
     */
    public static PluginResult success(String message) {
        return new PluginResult(true, message, null);
    }
    
    /**
     * Create a failure result
     */
    public static PluginResult failure(String message) {
        return new PluginResult(false, message, null);
    }
    
    // Getters
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
}
