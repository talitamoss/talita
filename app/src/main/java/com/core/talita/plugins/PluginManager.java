package com.core.talita.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central manager for all data collector plugins
 * Organized by I • We • All categories
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static final String PREFS_NAME = "plugin_preferences";
    private static PluginManager instance;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, DataCollectorPlugin> plugins;
    private final List<PluginListener> listeners;
    
    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.plugins = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        loadBuiltInPlugins();
    }
    
    public static synchronized PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }
    
    /**
     * Load all built-in plugins organized by I • We • All
     */
    private void loadBuiltInPlugins() {
        // "I" category - Personal/self plugins
        registerPlugin(new com.core.talita.plugins.i.WaterPlugin());
        registerPlugin(new com.core.talita.plugins.i.MoodPlugin());
        registerPlugin(new com.core.talita.plugins.i.ExercisePlugin());
        registerPlugin(new com.core.talita.plugins.i.SleepPlugin());
        
        // "We" category - Relationship/connection plugins
        registerPlugin(new com.core.talita.plugins.we.SharedLocationPlugin());
        registerPlugin(new com.core.talita.plugins.we.SharedMoodPlugin());
        
        // "All" category - Universal plugins
        registerPlugin(new com.core.talita.plugins.all.WeatherPlugin());
        registerPlugin(new com.core.talita.plugins.all.EnvironmentPlugin());
        
        Log.d(TAG, "Loaded " + plugins.size() + " built-in plugins");
    }
    
    /**
     * Register a plugin
     */
    public void registerPlugin(DataCollectorPlugin plugin) {
        String id = plugin.getId();
        plugins.put(id, plugin);
        
        // Initialize plugin
        plugin.initialize(context);
        
        // Check if enabled
        boolean isEnabled = prefs.getBoolean("plugin_" + id + "_enabled", true);
        if (isEnabled) {
            plugin.enable();
        }
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            listener.onPluginRegistered(plugin);
        }
        
        Log.d(TAG, "Registered plugin: " + plugin.getName());
    }
    
    /**
     * Get all plugins
     */
    public List<DataCollectorPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get plugins by category
     */
    public List<DataCollectorPlugin> getPluginsByCategory(String category) {
        return plugins.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Get enabled plugins
     */
    public List<DataCollectorPlugin> getEnabledPlugins() {
        return plugins.values().stream()
                .filter(DataCollectorPlugin::isEnabled)
                .collect(Collectors.toList());
    }
    
    /**
     * Get plugin by ID
     */
    public DataCollectorPlugin getPlugin(String id) {
        return plugins.get(id);
    }
    
    /**
     * Enable/disable plugin
     */
    public void setPluginEnabled(String id, boolean enabled) {
        DataCollectorPlugin plugin = plugins.get(id);
        if (plugin != null) {
            if (enabled) {
                plugin.enable();
            } else {
                plugin.disable();
            }
            
            // Save preference
            prefs.edit().putBoolean("plugin_" + id + "_enabled", enabled).apply();
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginStateChanged(plugin, enabled);
            }
        }
    }
    
    /**
     * Add plugin listener
     */
    public void addListener(PluginListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove plugin listener
     */
    public void removeListener(PluginListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Plugin categories
     */
    public static class Categories {
        public static final String I = "I";     // Personal/self
        public static final String WE = "We";   // Relationships
        public static final String ALL = "All"; // Universal
    }
    
    /**
     * Plugin listener interface
     */
    public interface PluginListener {
        void onPluginRegistered(DataCollectorPlugin plugin);
        void onPluginStateChanged(DataCollectorPlugin plugin, boolean enabled);
    }
}
