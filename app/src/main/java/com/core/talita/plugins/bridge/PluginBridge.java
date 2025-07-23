package com.core.talita.plugins.bridge;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.core.talita.PersonalData;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PluginBridge - Facilitates communication between plugins
 * 
 * Features:
 * - Event bus for plugin events
 * - Data sharing with permissions
 * - Request/response messaging
 * - Broadcast notifications
 */
public class PluginBridge {
    private static final String TAG = "PluginBridge";
    private static PluginBridge instance;
    
    private final Context context;
    private final Map<String, List<EventListener>> eventListeners;
    private final Map<String, DataProvider> dataProviders;
    private final Map<String, MessageHandler> messageHandlers;
    private final PluginPermissionManager permissionManager;
    
    private PluginBridge(Context context) {
        this.context = context.getApplicationContext();
        this.eventListeners = new ConcurrentHashMap<>();
        this.dataProviders = new ConcurrentHashMap<>();
        this.messageHandlers = new ConcurrentHashMap<>();
        this.permissionManager = new PluginPermissionManager(context);
    }
    
    public static synchronized PluginBridge getInstance(Context context) {
        if (instance == null) {
            instance = new PluginBridge(context);
        }
        return instance;
    }
    
    /**
     * Register an event listener
     */
    public void addEventListener(String pluginId, String eventType, EventListener listener) {
        String key = eventType;
        eventListeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
            .add(new PluginEventListener(pluginId, listener));
        
        Log.d(TAG, "Plugin " + pluginId + " registered for event: " + eventType);
    }
    
    /**
     * Remove an event listener
     */
    public void removeEventListener(String pluginId, String eventType) {
        String key = eventType;
        List<EventListener> listeners = eventListeners.get(key);
        if (listeners != null) {
            listeners.removeIf(l -> ((PluginEventListener) l).pluginId.equals(pluginId));
        }
    }
    
    /**
     * Emit an event to all registered listeners
     */
    public void emitEvent(String pluginId, String eventType, Bundle data) {
        Log.d(TAG, "Plugin " + pluginId + " emitting event: " + eventType);
        
        List<EventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (EventListener listener : listeners) {
                PluginEventListener pel = (PluginEventListener) listener;
                
                // Check if target plugin has permission to receive this event
                if (permissionManager.canReceiveEvent(pel.pluginId, pluginId, eventType)) {
                    try {
                        listener.onEvent(pluginId, eventType, data);
                    } catch (Exception e) {
                        Log.e(TAG, "Error delivering event to " + pel.pluginId, e);
                    }
                } else {
                    Log.w(TAG, "Plugin " + pel.pluginId + " denied event from " + pluginId);
                }
            }
        }
    }
    
    /**
     * Register a data provider
     */
    public void registerDataProvider(String pluginId, String dataType, DataProvider provider) {
        String key = pluginId + ":" + dataType;
        dataProviders.put(key, provider);
        
        Log.d(TAG, "Plugin " + pluginId + " registered data provider for: " + dataType);
    }
    
    /**
     * Request data from another plugin
     */
    public void requestData(String requestingPluginId, String providerPluginId, 
                          String dataType, Bundle params, DataCallback callback) {
        // Check permissions
        if (!permissionManager.canAccessData(requestingPluginId, providerPluginId, dataType)) {
            callback.onError("Permission denied");
            return;
        }
        
        String key = providerPluginId + ":" + dataType;
        DataProvider provider = dataProviders.get(key);
        
        if (provider != null) {
            try {
                provider.provideData(requestingPluginId, dataType, params, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error requesting data", e);
                callback.onError(e.getMessage());
            }
        } else {
            callback.onError("No data provider found");
        }
    }
    
    /**
     * Register a message handler
     */
    public void registerMessageHandler(String pluginId, MessageHandler handler) {
        messageHandlers.put(pluginId, handler);
        Log.d(TAG, "Plugin " + pluginId + " registered message handler");
    }
    
    /**
     * Send a message to another plugin
     */
    public void sendMessage(String fromPluginId, String toPluginId, 
                          String messageType, Bundle data, MessageCallback callback) {
        // Check permissions
        if (!permissionManager.canSendMessage(fromPluginId, toPluginId, messageType)) {
            if (callback != null) {
                callback.onError("Permission denied");
            }
            return;
        }
        
        MessageHandler handler = messageHandlers.get(toPluginId);
        if (handler != null) {
            try {
                handler.handleMessage(fromPluginId, messageType, data, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        } else {
            if (callback != null) {
                callback.onError("Plugin not found or no message handler");
            }
        }
    }
    
    /**
     * Broadcast a message to all plugins
     */
    public void broadcastMessage(String fromPluginId, String messageType, Bundle data) {
        Log.d(TAG, "Plugin " + fromPluginId + " broadcasting: " + messageType);
        
        for (Map.Entry<String, MessageHandler> entry : messageHandlers.entrySet()) {
            String toPluginId = entry.getKey();
            
            // Skip sender
            if (toPluginId.equals(fromPluginId)) {
                continue;
            }
            
            // Check permissions
            if (permissionManager.canReceiveBroadcast(toPluginId, fromPluginId, messageType)) {
                try {
                    entry.getValue().handleMessage(fromPluginId, messageType, data, null);
                } catch (Exception e) {
                    Log.e(TAG, "Error broadcasting to " + toPluginId, e);
                }
            }
        }
    }
    
    /**
     * Share data between plugins (with user consent)
     */
    public void shareData(String fromPluginId, String toPluginId, 
                         List<PersonalData> data, ShareCallback callback) {
        // Request user consent
        permissionManager.requestDataSharingConsent(fromPluginId, toPluginId, data, granted -> {
            if (granted) {
                // Notify receiving plugin
                MessageHandler handler = messageHandlers.get(toPluginId);
                if (handler != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("action", "data_shared");
                    bundle.putInt("count", data.size());
                    
                    handler.handleMessage(fromPluginId, "data_share", bundle, new MessageCallback() {
                        @Override
                        public void onSuccess(Bundle response) {
                            callback.onSuccess();
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                } else {
                    callback.onError("Target plugin not available");
                }
            } else {
                callback.onError("User denied permission");
            }
        });
    }
    
    /**
     * Get list of plugins that provide specific data type
     */
    public List<PluginInfo> getDataProviders(String dataType) {
        List<PluginInfo> providers = new ArrayList<>();
        PluginManager pluginManager = PluginManager.getInstance(context);
        
        for (String key : dataProviders.keySet()) {
            if (key.endsWith(":" + dataType)) {
                String pluginId = key.substring(0, key.indexOf(':'));
                DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
                
                if (plugin != null) {
                    providers.add(new PluginInfo(
                        pluginId,
                        plugin.getPluginName(),
                        plugin.getEmoji()
                    ));
                }
            }
        }
        
        return providers;
    }
    
    /**
     * Get list of plugins listening for specific event
     */
    public List<PluginInfo> getEventListeners(String eventType) {
        List<PluginInfo> listeners = new ArrayList<>();
        PluginManager pluginManager = PluginManager.getInstance(context);
        
        List<EventListener> eventListenerList = eventListeners.get(eventType);
        if (eventListenerList != null) {
            for (EventListener listener : eventListenerList) {
                PluginEventListener pel = (PluginEventListener) listener;
                DataCollectorPlugin plugin = pluginManager.getPlugin(pel.pluginId);
                
                if (plugin != null) {
                    listeners.add(new PluginInfo(
                        pel.pluginId,
                        plugin.getPluginName(),
                        plugin.getEmoji()
                    ));
                }
            }
        }
        
        return listeners;
    }
    
    /**
     * Clear all registrations for a plugin (when uninstalled)
     */
    public void unregisterPlugin(String pluginId) {
        // Remove event listeners
        for (List<EventListener> listeners : eventListeners.values()) {
            listeners.removeIf(l -> ((PluginEventListener) l).pluginId.equals(pluginId));
        }
        
        // Remove data providers
        dataProviders.entrySet().removeIf(e -> e.getKey().startsWith(pluginId + ":"));
        
        // Remove message handler
        messageHandlers.remove(pluginId);
        
        Log.d(TAG, "Unregistered all handlers for plugin: " + pluginId);
    }
    
    // Interfaces
    
    public interface EventListener {
        void onEvent(String sourcePluginId, String eventType, Bundle data);
    }
    
    public interface DataProvider {
        void provideData(String requestingPluginId, String dataType, 
                        Bundle params, DataCallback callback);
    }
    
    public interface MessageHandler {
        void handleMessage(String fromPluginId, String messageType, 
                         Bundle data, MessageCallback callback);
    }
    
    public interface DataCallback {
        void onSuccess(Bundle data);
        void onError(String error);
    }
    
    public interface MessageCallback {
        void onSuccess(Bundle response);
        void onError(String error);
    }
    
    public interface ShareCallback {
        void onSuccess();
        void onError(String error);
    }
    
    // Helper classes
    
    private static class PluginEventListener implements EventListener {
        final String pluginId;
        final EventListener listener;
        
        PluginEventListener(String pluginId, EventListener listener) {
            this.pluginId = pluginId;
            this.listener = listener;
        }
        
        @Override
        public void onEvent(String sourcePluginId, String eventType, Bundle data) {
            listener.onEvent(sourcePluginId, eventType, data);
        }
    }
    
    public static class PluginInfo {
        public final String id;
        public final String name;
        public final String emoji;
        
        PluginInfo(String id, String name, String emoji) {
            this.id = id;
            this.name = name;
            this.emoji = emoji;
        }
    }
    
    /**
     * Standard event types
     */
    public static class Events {
        public static final String DATA_COLLECTED = "data_collected";
        public static final String PLUGIN_ENABLED = "plugin_enabled";
        public static final String PLUGIN_DISABLED = "plugin_disabled";
        public static final String USER_ACTION = "user_action";
        public static final String SYNC_REQUESTED = "sync_requested";
        public static final String SETTINGS_CHANGED = "settings_changed";
    }
    
    /**
     * Standard data types
     */
    public static class DataTypes {
        public static final String RECENT_DATA = "recent_data";
        public static final String STATISTICS = "statistics";
        public static final String CONFIGURATION = "configuration";
        public static final String USER_PREFERENCES = "user_preferences";
    }
}
