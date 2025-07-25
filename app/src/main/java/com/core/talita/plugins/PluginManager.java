package com.core.talita.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.*;

/**
 * PluginManager - Central manager for all data collector plugins
 * Fixed to handle missing plugin classes gracefully
 * 
 * File path: app/src/main/java/com/core/talita/plugins/PluginManager.java
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static final String PREFS_NAME = "plugin_settings";
    private static PluginManager instance;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, DataCollectorPlugin> plugins;
    private final Set<String> enabledPluginIds;
    private final List<PluginListener> listeners;
    
    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.plugins = new LinkedHashMap<>();
        this.enabledPluginIds = new HashSet<>();
        this.listeners = new ArrayList<>();
        
        loadEnabledState();
        loadBuiltInPlugins();
        loadDynamicCollectors();
        loadExternalPlugins();
    }
    
    public static synchronized PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }
    
    /**
     * Load enabled state from preferences
     */
    private void loadEnabledState() {
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().startsWith("plugin_") && entry.getKey().endsWith("_enabled")) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    String pluginId = entry.getKey()
                        .replace("plugin_", "")
                        .replace("_enabled", "");
                    enabledPluginIds.add(pluginId);
                }
            }
        }
        Log.d(TAG, "Loaded " + enabledPluginIds.size() + " enabled plugins from preferences");
    }
    
    /**
     * Load all built-in plugins
     */
    private void loadBuiltInPlugins() {
        // "I" category - Personal/self plugins
        try {
            registerPlugin(new com.core.talita.plugins.i.WaterPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load WaterPlugin: " + e.getMessage());
        }
        
        try {
            registerPlugin(new com.core.talita.plugins.i.MoodPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load MoodPlugin: " + e.getMessage());
        }
        
        try {
            registerPlugin(new com.core.talita.plugins.i.ExercisePlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load ExercisePlugin: " + e.getMessage());
        }
        
        try {
            registerPlugin(new com.core.talita.plugins.i.SleepPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load SleepPlugin: " + e.getMessage());
        }
        
        // Note: NutritionPlugin and SubstancePlugin are referenced but don't exist yet
        // They should be created or removed from references
        
        // Dynamic Collector Plugin - allows users to create custom collectors
        try {
            registerPlugin(new DynamicCollectorPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load DynamicCollectorPlugin: " + e.getMessage());
        }
        
        // "We" category - Relationship/connection plugins
        try {
            registerPlugin(new com.core.talita.plugins.we.FocusPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load FocusPlugin: " + e.getMessage());
        }
        
        try {
            registerPlugin(new com.core.talita.plugins.we.RelationshipsPlugin());
        } catch (Exception e) {
            Log.w(TAG, "Failed to load RelationshipsPlugin: " + e.getMessage());
        }
        
        Log.d(TAG, "Loaded " + plugins.size() + " built-in plugins");
    }
    
    /**
     * Load user-created dynamic collectors as plugins
     */
    private void loadDynamicCollectors() {
        try {
            List<DataCollectorPlugin> dynamicPlugins = DynamicCollectorPlugin.getDynamicCollectorPlugins(context);
            for (DataCollectorPlugin plugin : dynamicPlugins) {
                registerPlugin(plugin);
            }
            Log.d(TAG, "Loaded " + dynamicPlugins.size() + " dynamic collectors");
        } catch (Exception e) {
            Log.e(TAG, "Error loading dynamic collectors", e);
        }
    }
    
    /**
     * Load external plugins (future feature)
     */
    private void loadExternalPlugins() {
        // TODO: Implement plugin discovery from external sources
    }
    
    /**
     * Register a plugin
     */
    public void registerPlugin(DataCollectorPlugin plugin) {
        if (plugin == null) {
            Log.w(TAG, "Attempted to register null plugin");
            return;
        }
        
        String pluginId = plugin.getPluginId();
        if (pluginId == null || pluginId.isEmpty()) {
            Log.w(TAG, "Plugin has invalid ID: " + plugin.getClass().getName());
            return;
        }
        
        plugins.put(pluginId, plugin);
        
        // Check if enabled by default
        if (!prefs.contains("plugin_" + pluginId + "_enabled")) {
            // Enable by default for certain categories
            boolean enableByDefault = "i".equals(plugin.getCategory());
            setPluginEnabled(pluginId, enableByDefault);
        }
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            listener.onPluginRegistered(plugin);
        }
        
        Log.d(TAG, "Registered plugin: " + pluginId);
    }
    
    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.remove(pluginId);
        if (plugin != null) {
            enabledPluginIds.remove(pluginId);
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginUnregistered(plugin);
            }
        }
    }
    
    /**
     * Get plugin by ID
     */
    public DataCollectorPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * Get all plugins
     */
    public List<DataCollectorPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get enabled plugins
     */
    public List<DataCollectorPlugin> getEnabledPlugins() {
        List<DataCollectorPlugin> result = new ArrayList<>();
        for (String pluginId : enabledPluginIds) {
            DataCollectorPlugin plugin = plugins.get(pluginId);
            if (plugin != null) {
                result.add(plugin);
            }
        }
        return result;
    }
    
    /**
     * Enable/disable a plugin
     */
    public void setPluginEnabled(String pluginId, boolean enabled) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            return;
        }
        
        if (enabled) {
            enabledPluginIds.add(pluginId);
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginEnabled(plugin);
            }
        } else {
            enabledPluginIds.remove(pluginId);
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginDisabled(plugin);
            }
        }
        
        // Save to preferences
        prefs.edit()
            .putBoolean("plugin_" + pluginId + "_enabled", enabled)
            .apply();
    }
    
    /**
     * Check if a plugin is enabled
     */
    public boolean isPluginEnabled(String pluginId) {
        return enabledPluginIds.contains(pluginId);
    }
    
    /**
     * Get plugins by category
     */
    public List<DataCollectorPlugin> getPluginsByCategory(String category) {
        List<DataCollectorPlugin> result = new ArrayList<>();
        for (DataCollectorPlugin plugin : plugins.values()) {
            if (category.equals(plugin.getCategory())) {
                result.add(plugin);
            }
        }
        return result;
    }
    
    /**
     * Get plugins that support quick add
     */
    public List<DataCollectorPlugin> getQuickAddPlugins() {
        List<DataCollectorPlugin> result = new ArrayList<>();
        for (DataCollectorPlugin plugin : plugins.values()) {
            if (plugin.supportsQuickAdd() && enabledPluginIds.contains(plugin.getPluginId())) {
                result.add(plugin);
            }
        }
        
        // Sort by priority
        result.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        return result;
    }
    
    /**
     * Add a plugin listener
     */
    public void addPluginListener(PluginListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a plugin listener
     */
    public void removePluginListener(PluginListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Plugin event listener interface
     */
    public interface PluginListener {
        void onPluginRegistered(DataCollectorPlugin plugin);
        void onPluginUnregistered(DataCollectorPlugin plugin);
        void onPluginEnabled(DataCollectorPlugin plugin);
        void onPluginDisabled(DataCollectorPlugin plugin);
    }
    
    /**
     * Reload dynamic collectors (call this when a new schema is created)
     */
    public void reloadDynamicCollectors() {
        // Remove existing dynamic collectors
        List<String> dynamicIds = new ArrayList<>();
        for (String pluginId : plugins.keySet()) {
            if (pluginId.startsWith("dynamic.")) {
                dynamicIds.add(pluginId);
            }
        }
        
        for (String pluginId : dynamicIds) {
            unregisterPlugin(pluginId);
        }
        
        // Reload
        loadDynamicCollectors();
    }
}
