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
        registerPlugin(new com.core.talita.plugins.we.FocusPlugin());
        registerPlugin(new com.core.talita.plugins.we.RelationshipsPlugin());
        
        // "All" category - World/environment plugins
        registerPlugin(new com.core.talita.plugins.all.LocationPlugin());
        
        Log.d(TAG, "Loaded " + plugins.size() + " built-in plugins");
    }
    
    /**
     * Register a new plugin
     */
    public void registerPlugin(DataCollectorPlugin plugin) {
        String id = plugin.getPluginId();
        plugins.put(id, plugin);
        
        if (isPluginEnabled(id)) {
            plugin.onPluginEnabled(context);
        }
        
        for (PluginListener listener : listeners) {
            listener.onPluginRegistered(plugin);
        }
        
        Log.d(TAG, "Registered plugin: " + id + " [" + plugin.getCategory() + "]");
    }
    
    /**
     * Get all registered plugins
     */
    public List<DataCollectorPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get plugins by category (I, We, or All)
     */
    public List<DataCollectorPlugin> getPluginsByCategory(String category) {
        return plugins.values().stream()
            .filter(p -> p.getCategory().equals(category))
            .sorted((a, b) -> b.getPriority() - a.getPriority())
            .collect(Collectors.toList());
    }
    
    /**
     * Get enabled plugins that support quick add
     */
    public List<DataCollectorPlugin> getQuickAddPlugins() {
        return plugins.values().stream()
            .filter(p -> isPluginEnabled(p.getPluginId()))
            .filter(DataCollectorPlugin::supportsQuickAdd)
            .sorted((a, b) -> b.getPriority() - a.getPriority())
            .collect(Collectors.toList());
    }
    
    /**
     * Get plugins organized by I • We • All categories
     */
    public Map<String, List<DataCollectorPlugin>> getPluginsByCategoryMap() {
        Map<String, List<DataCollectorPlugin>> categoryMap = new LinkedHashMap<>();
        
        // Ensure order: I, We, All
        for (String category : PluginCategories.getAllCategories()) {
            categoryMap.put(category, getPluginsByCategory(category));
        }
        
        return categoryMap;
    }
    
    /**
     * Get a specific plugin
     */
    public DataCollectorPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * Check if a plugin is enabled
     */
    public boolean isPluginEnabled(String pluginId) {
        // Core plugins are enabled by default
        boolean defaultEnabled = isCorePlugin(pluginId);
        return prefs.getBoolean(pluginId + "_enabled", defaultEnabled);
    }
    
    /**
     * Enable or disable a plugin
     */
    public void setPluginEnabled(String pluginId, boolean enabled) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin == null) return;
        
        boolean wasEnabled = isPluginEnabled(pluginId);
        prefs.edit().putBoolean(pluginId + "_enabled", enabled).apply();
        
        if (enabled && !wasEnabled) {
            plugin.onPluginEnabled(context);
            for (PluginListener listener : listeners) {
                listener.onPluginEnabled(plugin);
            }
        } else if (!enabled && wasEnabled) {
            plugin.onPluginDisabled(context);
            for (PluginListener listener : listeners) {
                listener.onPluginDisabled(plugin);
            }
        }
    }
    
    /**
     * Check if this is a core plugin (enabled by default)
     */
    private boolean isCorePlugin(String pluginId) {
        return pluginId.equals("i.water") || 
               pluginId.equals("i.exercise") ||
               pluginId.equals("i.mood");
    }
    
    /**
     * Get plugin statistics by category
     */
    public Map<String, Integer> getPluginStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        
        for (String category : PluginCategories.getAllCategories()) {
            int count = (int) plugins.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .count();
            stats.put(category, count);
        }
        
        return stats;
    }
    
    /**
     * Add a plugin listener
     */
    public void addListener(PluginListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a plugin listener
     */
    public void removeListener(PluginListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Plugin lifecycle listener
     */
    public interface PluginListener {
        void onPluginRegistered(DataCollectorPlugin plugin);
        void onPluginEnabled(DataCollectorPlugin plugin);
        void onPluginDisabled(DataCollectorPlugin plugin);
    }
}
