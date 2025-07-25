package com.core.talita.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.*;

/**
 * PluginManager - Central manager for all data collector plugins
 * Updated to remove references to non-existent plugins
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
        registerPlugin(new com.core.talita.plugins.i.WaterPlugin());
        registerPlugin(new com.core.talita.plugins.i.MoodPlugin());
        registerPlugin(new com.core.talita.plugins.i.ExercisePlugin());
        registerPlugin(new com.core.talita.plugins.i.SleepPlugin());
        registerPlugin(new com.core.talita.plugins.i.NutritionPlugin());
        registerPlugin(new com.core.talita.plugins.i.SubstancePlugin());
        
        // Dynamic Collector Plugin - allows users to create custom collectors
        registerPlugin(new DynamicCollectorPlugin());
        
        // "We" category - Relationship/connection plugins
        registerPlugin(new com.core.talita.plugins.we.FocusPlugin());
        registerPlugin(new com.core.talita.plugins.we.RelationshipsPlugin());
        
        // "All" category - Universal plugins
        // TODO: Add these plugins when implemented:
        // - WeatherPlugin
        // - EnvironmentPlugin
        // - SharedLocationPlugin (might go in "We" category)
        // - SharedMoodPlugin (might go in "We" category)
        
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
        // TODO: Implement loading of external plugins from APKs or other sources
        // For now, this is a placeholder for future expansion
    }
    
    /**
     * Register a plugin
     */
    public void registerPlugin(DataCollectorPlugin plugin) {
        String id = plugin.getPluginId();
        plugins.put(id, plugin);
        
        // Initialize plugin
        plugin.initialize(context);
        
        // Check if enabled (default to true for new plugins)
        boolean isEnabled = prefs.getBoolean("plugin_" + id + "_enabled", true);
        if (isEnabled) {
            enabledPluginIds.add(id);
            plugin.onPluginEnabled(context);
        }
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            listener.onPluginRegistered(plugin);
        }
        
        Log.d(TAG, "Registered plugin: " + plugin.getPluginName() + " [" + id + "]");
    }
    
    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.remove(pluginId);
        if (plugin != null) {
            if (enabledPluginIds.contains(pluginId)) {
                plugin.onPluginDisabled(context);
                enabledPluginIds.remove(pluginId);
            }
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginUnregistered(plugin);
            }
            
            Log.d(TAG, "Unregistered plugin: " + pluginId);
        }
    }
    
    /**
     * Enable a plugin
     */
    public void enablePlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin != null && !enabledPluginIds.contains(pluginId)) {
            enabledPluginIds.add(pluginId);
            prefs.edit().putBoolean("plugin_" + pluginId + "_enabled", true).apply();
            plugin.onPluginEnabled(context);
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginEnabled(plugin);
            }
            
            Log.d(TAG, "Enabled plugin: " + pluginId);
        }
    }
    
    /**
     * Disable a plugin
     */
    public void disablePlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin != null && enabledPluginIds.contains(pluginId)) {
            enabledPluginIds.remove(pluginId);
            prefs.edit().putBoolean("plugin_" + pluginId + "_enabled", false).apply();
            plugin.onPluginDisabled(context);
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginDisabled(plugin);
            }
            
            Log.d(TAG, "Disabled plugin: " + pluginId);
        }
    }
    
    /**
     * Check if a plugin is enabled
     */
    public boolean isPluginEnabled(String pluginId) {
        return enabledPluginIds.contains(pluginId);
    }
    
    /**
     * Get a specific plugin
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
        List<DataCollectorPlugin> enabled = new ArrayList<>();
        for (String pluginId : enabledPluginIds) {
            DataCollectorPlugin plugin = plugins.get(pluginId);
            if (plugin != null) {
                enabled.add(plugin);
            }
        }
        return enabled;
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
        
        for (String id : dynamicIds) {
            unregisterPlugin(id);
        }
        
        // Load fresh
        loadDynamicCollectors();
    }
    
    /**
     * Get plugin statistics
     */
    public PluginStats getStats() {
        int total = plugins.size();
        int enabled = enabledPluginIds.size();
        
        Map<String, Integer> byCategory = new HashMap<>();
        for (DataCollectorPlugin plugin : plugins.values()) {
            String category = plugin.getCategory();
            byCategory.put(category, byCategory.getOrDefault(category, 0) + 1);
        }
        
        return new PluginStats(total, enabled, byCategory);
    }
    
    /**
     * Plugin statistics
     */
    public static class PluginStats {
        public final int totalPlugins;
        public final int enabledPlugins;
        public final Map<String, Integer> pluginsByCategory;
        
        PluginStats(int total, int enabled, Map<String, Integer> byCategory) {
            this.totalPlugins = total;
            this.enabledPlugins = enabled;
            this.pluginsByCategory = new HashMap<>(byCategory);
        }
    }
}
