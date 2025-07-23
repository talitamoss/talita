package com.core.talita.plugins.bridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import com.core.talita.PersonalData;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/**
 * PluginPermissionManager - Manages permissions between plugins
 * 
 * Features:
 * - Permission grants between plugins
 * - User consent management
 * - Permission rules and policies
 * - Audit logging
 */
public class PluginPermissionManager {
    private static final String TAG = "PluginPermissionManager";
    private static final String PREFS_NAME = "plugin_permissions";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, PermissionGrant> grants;
    private final List<PermissionRequest> pendingRequests;
    
    public PluginPermissionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.grants = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        
        loadPermissions();
    }
    
    /**
     * Check if plugin can receive events from another plugin
     */
    public boolean canReceiveEvent(String receiverPluginId, String senderPluginId, String eventType) {
        // Check if there's an explicit grant
        String key = createKey(senderPluginId, receiverPluginId, "event:" + eventType);
        PermissionGrant grant = grants.get(key);
        
        if (grant != null) {
            return grant.allowed && !grant.isExpired();
        }
        
        // Check wildcard permissions
        String wildcardKey = createKey(senderPluginId, receiverPluginId, "event:*");
        PermissionGrant wildcardGrant = grants.get(wildcardKey);
        
        if (wildcardGrant != null) {
            return wildcardGrant.allowed && !wildcardGrant.isExpired();
        }
        
        // Default policy: Allow non-sensitive events
        return isPublicEvent(eventType);
    }
    
    /**
     * Check if plugin can access data from another plugin
     */
    public boolean canAccessData(String requestingPluginId, String providerPluginId, String dataType) {
        // Self-access always allowed
        if (requestingPluginId.equals(providerPluginId)) {
            return true;
        }
        
        // Check explicit grant
        String key = createKey(requestingPluginId, providerPluginId, "data:" + dataType);
        PermissionGrant grant = grants.get(key);
        
        if (grant != null) {
            logAccess(requestingPluginId, providerPluginId, dataType, grant.allowed);
            return grant.allowed && !grant.isExpired();
        }
        
        // Check if data type is public
        return isPublicDataType(dataType);
    }
    
    /**
     * Check if plugin can send messages to another plugin
     */
    public boolean canSendMessage(String senderPluginId, String receiverPluginId, String messageType) {
        // Self-messaging always allowed
        if (senderPluginId.equals(receiverPluginId)) {
            return true;
        }
        
        // Check explicit grant
        String key = createKey(senderPluginId, receiverPluginId, "message:" + messageType);
        PermissionGrant grant = grants.get(key);
        
        if (grant != null) {
            return grant.allowed && !grant.isExpired();
        }
        
        // Default: Allow basic messages
        return isBasicMessageType(messageType);
    }
    
    /**
     * Check if plugin can receive broadcasts
     */
    public boolean canReceiveBroadcast(String receiverPluginId, String senderPluginId, String messageType) {
        // Check if receiver has opted out of broadcasts from sender
        String key = createKey(senderPluginId, receiverPluginId, "broadcast:*");
        PermissionGrant grant = grants.get(key);
        
        if (grant != null && !grant.allowed) {
            return false;
        }
        
        // Default: Allow broadcasts
        return true;
    }
    
    /**
     * Request user consent for data sharing
     */
    public void requestDataSharingConsent(String fromPluginId, String toPluginId, 
                                        List<PersonalData> data, ConsentCallback callback) {
        // Get plugin information
        PluginManager pm = PluginManager.getInstance(context);
        DataCollectorPlugin fromPlugin = pm.getPlugin(fromPluginId);
        DataCollectorPlugin toPlugin = pm.getPlugin(toPluginId);
        
        if (fromPlugin == null || toPlugin == null) {
            callback.onResult(false);
            return;
        }
        
        // Check if already granted
        String key = createKey(fromPluginId, toPluginId, "share:data");
        PermissionGrant existingGrant = grants.get(key);
        
        if (existingGrant != null && existingGrant.allowed && !existingGrant.isExpired()) {
            callback.onResult(true);
            return;
        }
        
        // Show consent dialog
        showConsentDialog(fromPlugin, toPlugin, data, granted -> {
            if (granted) {
                // Grant permission
                grantPermission(fromPluginId, toPluginId, "share:data", 
                              PermissionDuration.SESSION);
            }
            callback.onResult(granted);
        });
    }
    
    /**
     * Grant permission between plugins
     */
    public void grantPermission(String fromPluginId, String toPluginId, 
                              String permissionType, PermissionDuration duration) {
        String key = createKey(fromPluginId, toPluginId, permissionType);
        
        long expiryTime = calculateExpiryTime(duration);
        PermissionGrant grant = new PermissionGrant(
            fromPluginId, toPluginId, permissionType, true, 
            System.currentTimeMillis(), expiryTime
        );
        
        grants.put(key, grant);
        savePermissions();
        
        Log.d(TAG, "Granted permission: " + key);
    }
    
    /**
     * Revoke permission between plugins
     */
    public void revokePermission(String fromPluginId, String toPluginId, String permissionType) {
        String key = createKey(fromPluginId, toPluginId, permissionType);
        
        PermissionGrant grant = grants.get(key);
        if (grant != null) {
            grant.allowed = false;
            savePermissions();
            
            Log.d(TAG, "Revoked permission: " + key);
        }
    }
    
    /**
     * Get all permissions for a plugin
     */
    public List<PermissionGrant> getPluginPermissions(String pluginId) {
        List<PermissionGrant> pluginGrants = new ArrayList<>();
        
        for (PermissionGrant grant : grants.values()) {
            if (grant.fromPluginId.equals(pluginId) || grant.toPluginId.equals(pluginId)) {
                pluginGrants.add(grant);
            }
        }
        
        return pluginGrants;
    }
    
    /**
     * Clear all permissions for a plugin (when uninstalled)
     */
    public void clearPluginPermissions(String pluginId) {
        Iterator<Map.Entry<String, PermissionGrant>> iterator = grants.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, PermissionGrant> entry = iterator.next();
            PermissionGrant grant = entry.getValue();
            
            if (grant.fromPluginId.equals(pluginId) || grant.toPluginId.equals(pluginId)) {
                iterator.remove();
            }
        }
        
        savePermissions();
        Log.d(TAG, "Cleared all permissions for plugin: " + pluginId);
    }
    
    // Private helper methods
    
    private void loadPermissions() {
        try {
            String json = prefs.getString("grants", "[]");
            JSONArray array = new JSONArray(json);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                PermissionGrant grant = PermissionGrant.fromJson(obj);
                
                if (grant != null && !grant.isExpired()) {
                    String key = createKey(grant.fromPluginId, grant.toPluginId, grant.permissionType);
                    grants.put(key, grant);
                }
            }
            
            Log.d(TAG, "Loaded " + grants.size() + " permission grants");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading permissions", e);
        }
    }
    
    private void savePermissions() {
        try {
            JSONArray array = new JSONArray();
            
            for (PermissionGrant grant : grants.values()) {
                if (!grant.isExpired()) {
                    array.put(grant.toJson());
                }
            }
            
            prefs.edit().putString("grants", array.toString()).apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving permissions", e);
        }
    }
    
    private String createKey(String fromPluginId, String toPluginId, String permissionType) {
        return fromPluginId + "->" + toPluginId + ":" + permissionType;
    }
    
    private boolean isPublicEvent(String eventType) {
        // Define which events are public by default
        return eventType.equals("plugin_enabled") || 
               eventType.equals("plugin_disabled") ||
               eventType.equals("data_collected");
    }
    
    private boolean isPublicDataType(String dataType) {
        // Define which data types are public by default
        return dataType.equals("statistics") || 
               dataType.equals("configuration");
    }
    
    private boolean isBasicMessageType(String messageType) {
        // Define which message types are allowed by default
        return messageType.equals("ping") || 
               messageType.equals("status") ||
               messageType.equals("info");
    }
    
    private long calculateExpiryTime(PermissionDuration duration) {
        switch (duration) {
            case ONCE:
                return System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
            case SESSION:
                return System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours
            case WEEK:
                return System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days
            case PERMANENT:
                return Long.MAX_VALUE;
            default:
                return System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour default
        }
    }
    
    private void showConsentDialog(DataCollectorPlugin fromPlugin, DataCollectorPlugin toPlugin,
                                  List<PersonalData> data, ConsentCallback callback) {
        // In a real implementation, this would show a proper UI dialog
        // For now, we'll use a simple AlertDialog
        
        String message = String.format(
            "%s wants to share %d data items with %s.\n\nAllow this?",
            fromPlugin.getPluginName(),
            data.size(),
            toPlugin.getPluginName()
        );
        
        new AlertDialog.Builder(context)
            .setTitle("Data Sharing Request")
            .setMessage(message)
            .setPositiveButton("Allow", (dialog, which) -> callback.onResult(true))
            .setNegativeButton("Deny", (dialog, which) -> callback.onResult(false))
            .setCancelable(false)
            .show();
    }
    
    private void logAccess(String requestingPluginId, String providerPluginId, 
                         String dataType, boolean allowed) {
        // Log data access for audit purposes
        Log.d(TAG, String.format("Data access: %s -> %s [%s] = %s",
            requestingPluginId, providerPluginId, dataType, allowed ? "ALLOWED" : "DENIED"));
    }
    
    // Data classes
    
    public static class PermissionGrant {
        public final String fromPluginId;
        public final String toPluginId;
        public final String permissionType;
        public boolean allowed;
        public final long grantedTime;
        public final long expiryTime;
        
        PermissionGrant(String fromPluginId, String toPluginId, String permissionType,
                       boolean allowed, long grantedTime, long expiryTime) {
            this.fromPluginId = fromPluginId;
            this.toPluginId = toPluginId;
            this.permissionType = permissionType;
            this.allowed = allowed;
            this.grantedTime = grantedTime;
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
        
        JSONObject toJson() throws Exception {
            JSONObject obj = new JSONObject();
            obj.put("from", fromPluginId);
            obj.put("to", toPluginId);
            obj.put("type", permissionType);
            obj.put("allowed", allowed);
            obj.put("granted", grantedTime);
            obj.put("expiry", expiryTime);
            return obj;
        }
        
        static PermissionGrant fromJson(JSONObject obj) throws Exception {
            return new PermissionGrant(
                obj.getString("from"),
                obj.getString("to"),
                obj.getString("type"),
                obj.getBoolean("allowed"),
                obj.getLong("granted"),
                obj.getLong("expiry")
            );
        }
    }
    
    public static class PermissionRequest {
        public final String requestId;
        public final String fromPluginId;
        public final String toPluginId;
        public final String permissionType;
        public final String reason;
        public final long requestTime;
        
        PermissionRequest(String fromPluginId, String toPluginId, 
                         String permissionType, String reason) {
            this.requestId = UUID.randomUUID().toString();
            this.fromPluginId = fromPluginId;
            this.toPluginId = toPluginId;
            this.permissionType = permissionType;
            this.reason = reason;
            this.requestTime = System.currentTimeMillis();
        }
    }
    
    public enum PermissionDuration {
        ONCE,       // One-time use
        SESSION,    // Until app restart
        WEEK,       // 7 days
        PERMANENT   // No expiry
    }
    
    public interface ConsentCallback {
        void onResult(boolean granted);
    }
}
