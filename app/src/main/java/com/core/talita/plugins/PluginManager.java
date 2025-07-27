package com.core.talita.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.plugins.core.WaterPlugin;
import java.util.*;

/**
 * PluginManager - Manages all data collector plugins
 * 
 * SIMPLIFIED VERSION: Starting with just WaterPlugin for MVP.
 * Other plugins can be added once water tracking is working perfectly.
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static final String PREFS_NAME = "plugin_manager";
    private static final String PREF_ENABLED_PLUGINS = "enabled_plugins";
    
    private static PluginManager instance;
    
    private final Context context;
    private final Map<String, DataCollectorPlugin> plugins = new HashMap<>();
    private final Set<String> enabledPluginIds = new HashSet<>();
    private final List<PluginListener> listeners = new ArrayList<>();
    
    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        initializePlugins();
        loadEnabledState();
    }
    
    public static synchronized PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize built-in plugins
     * For MVP, we're only including WaterPlugin
     */
    private void initializePlugins() {
        Log.d(TAG, "Initializing plugins...");
        
        // Register only WaterPlugin for now
        registerPlugin(new WaterPlugin());
        
        Log.d(TAG, "Initialized " + plugins.size() + " plugins");
    }
    
    /**
     * Load enabled state from preferences
     */
    private void loadEnabledState() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> enabled = prefs.getStringSet(PREF_ENABLED_PLUGINS, new HashSet<>());
        
        if (enabled.isEmpty()) {
            // First run - enable water by default
            enabledPluginIds.add("core.water");
            saveEnabledState();
        } else {
            enabledPluginIds.addAll(enabled);
        }
    }
    
    /**
     * Save enabled state to preferences
     */
    private void saveEnabledState() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(PREF_ENABLED_PLUGINS, enabledPluginIds).apply();
    }
    
    /**
     * Register a plugin
     */
    public void registerPlugin(DataCollectorPlugin plugin) {
        if (plugin == null) {
            Log.w(TAG, "Cannot register null plugin");
            return;
        }
        
        String pluginId = plugin.getPluginId();
        if (plugins.containsKey(pluginId)) {
            Log.w(TAG, "Plugin already registered: " + pluginId);
            return;
        }
        
        // Initialize plugin
        plugin.initialize(createPluginContext(plugin));
        
        // Store plugin
        plugins.put(pluginId, plugin);
        
        // Check if enabled
        if (enabledPluginIds.contains(pluginId)) {
            plugin.setEnabled(true);
        }
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            listener.onPluginRegistered(plugin);
            if (plugin.isEnabled()) {
                listener.onPluginEnabled(plugin);
            }
        }
        
        Log.d(TAG, "Registered plugin: " + plugin.getPluginName());
    }
    
    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            return;
        }
        
        // Disable first
        if (plugin.isEnabled()) {
            disablePlugin(pluginId);
        }
        
        // Remove from registry
        plugins.remove(pluginId);
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            listener.onPluginUnregistered(plugin);
        }
        
        Log.d(TAG, "Unregistered plugin: " + pluginId);
    }
    
    /**
     * Enable a plugin
     */
    public void enablePlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            Log.w(TAG, "Plugin not found: " + pluginId);
            return;
        }
        
        if (!enabledPluginIds.contains(pluginId)) {
            enabledPluginIds.add(pluginId);
            plugin.setEnabled(true);
            saveEnabledState();
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginEnabled(plugin);
            }
            
            Log.d(TAG, "Enabled plugin: " + plugin.getPluginName());
        }
    }
    
    /**
     * Disable a plugin
     */
    public void disablePlugin(String pluginId) {
        DataCollectorPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            return;
        }
        
        if (enabledPluginIds.contains(pluginId)) {
            enabledPluginIds.remove(pluginId);
            plugin.setEnabled(false);
            saveEnabledState();
            
            // Notify listeners
            for (PluginListener listener : listeners) {
                listener.onPluginDisabled(plugin);
            }
            
            Log.d(TAG, "Disabled plugin: " + plugin.getPluginName());
        }
    }
    
    /**
     * Get a specific plugin
     */
    public DataCollectorPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * Get all registered plugins
     */
    public List<DataCollectorPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get enabled plugins
     */
    public List<DataCollectorPlugin> getEnabledPlugins() {
        List<DataCollectorPlugin> enabled = new ArrayList<>();
        for (DataCollectorPlugin plugin : plugins.values()) {
            if (plugin.isEnabled()) {
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
     * Reload all plugins
     */
    public void reloadPlugins() {
        Log.d(TAG, "Reloading all plugins...");
        
        // Clear existing plugins
        plugins.clear();
        enabledPluginIds.clear();
        
        // Re-initialize
        initializePlugins();
        loadEnabledState();
        
        // Notify listeners
        for (PluginListener listener : listeners) {
            for (DataCollectorPlugin plugin : plugins.values()) {
                listener.onPluginRegistered(plugin);
                if (enabledPluginIds.contains(plugin.getPluginId())) {
                    listener.onPluginEnabled(plugin);
                }
            }
        }
        
        Log.d(TAG, "Reloaded " + plugins.size() + " plugins");
    }
    
    /**
     * Create plugin context
     */
    private PluginContext createPluginContext(DataCollectorPlugin plugin) {
        return new PluginContextImpl(context, plugin);
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
}
