package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataCollectorManager - Manages data collectors from plugins
 * 
 * This is the central manager for all data collection in the app.
 * It works with the plugin system to create and manage collectors.
 */
public class DataCollectorManager {
    private static final String TAG = "DataCollectorManager";
    private static DataCollectorManager instance;

    private final Context context;
    private final UniversalDataService dataService;
    private final PluginManager pluginManager;
    private final Map<String, DataCollector> activeCollectors;

    /**
     * Get singleton instance
     */
    public static DataCollectorManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataCollectorManager(context.getApplicationContext());
        }
        return instance;
    }

    public DataCollectorManager(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
        this.pluginManager = PluginManager.getInstance(context);
        this.activeCollectors = new HashMap<>();

        // Register core plugins if not already done
        registerCorePlugins();
        
        Log.d(TAG, "📊 Data Collector Manager initialized");
    }

    /**
     * Register built-in core plugins
     */
    private void registerCorePlugins() {
        // Check if core plugins are already registered
        if (pluginManager.getPlugin("core.water") == null) {
            Log.d(TAG, "Registering core plugins...");
            
            // Register core plugins
            pluginManager.registerPlugin(new com.core.talita.plugins.core.WaterPlugin());
            // TODO: Add other core plugins as they're created
            // pluginManager.registerPlugin(new com.core.talita.plugins.core.LocationPlugin());
            // pluginManager.registerPlugin(new com.core.talita.plugins.core.AudioPlugin());
            // pluginManager.registerPlugin(new com.core.talita.plugins.core.ExercisePlugin());
            // pluginManager.registerPlugin(new com.core.talita.plugins.core.MoodPlugin());
        }
    }

    /**
     * Start collecting for all enabled plugins
     */
    public void startEnabledCollectors() {
        int startedCount = 0;

        // Get collectors from all enabled plugins
        List<DataCollectorPlugin> enabledPlugins = pluginManager.getEnabledPlugins();
        
        for (DataCollectorPlugin plugin : enabledPlugins) {
            try {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    
                    // Only start automated collection if configured
                    if (collector.getSettings().isAutomatedCollection()) {
                        collector.startAutomatedCollection();
                    }
                    
                    activeCollectors.put(plugin.getPluginId(), collector);
                    startedCount++;
                    Log.d(TAG, "▶️ Started: " + collector.getDisplayName() + " from " + plugin.getPluginId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to start collector from plugin: " + plugin.getPluginId(), e);
            }
        }

        Log.d(TAG, "✅ Started " + startedCount + " collectors from plugins");
    }

    /**
     * Stop all active collectors
     */
    public void stopAllCollectors() {
        for (Map.Entry<String, DataCollector> entry : activeCollectors.entrySet()) {
            try {
                DataCollector collector = entry.getValue();
                if (collector.isCollectingAutomatically()) {
                    collector.stopAutomatedCollection();
                }
                collector.onDestroy();
                Log.d(TAG, "⏹️ Stopped: " + collector.getDisplayName());
            } catch (Exception e) {
                Log.e(TAG, "Error stopping collector", e);
            }
        }
        activeCollectors.clear();
    }

    /**
     * Get active collector by plugin ID
     */
    public DataCollector getActiveCollector(String pluginId) {
        return activeCollectors.get(pluginId);
    }

    /**
     * Get all available collectors from plugins
     */
    public List<DataCollector> getAllAvailableCollectors() {
        List<DataCollector> collectors = new ArrayList<>();
        
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            try {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    collectors.add(collector);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to create collector from plugin: " + plugin.getPluginId(), e);
            }
        }
        
        return collectors;
    }

    /**
     * Get collectors organized by category
     */
    public Map<String, List<DataCollector>> getCollectorsByCategory() {
        Map<String, List<DataCollector>> byCategory = new HashMap<>();
        
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            try {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    
                    String category = collector.getCategory();
                    byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(collector);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to create collector", e);
            }
        }
        
        return byCategory;
    }

    /**
     * Quick logging through plugin collectors
     */
    public void quickLog(String pluginId, Map<String, Object> data) {
        // Get or create collector
        DataCollector collector = activeCollectors.get(pluginId);
        
        if (collector == null) {
            // Try to create collector if not active
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                collector = plugin.createCollector(context);
                collector.initialize(context);
                activeCollectors.put(pluginId, collector);
            }
        }
        
        if (collector != null) {
            CollectorResult result = collector.collectQuick(data);
            
            if (result.isSuccess()) {
                Log.d(TAG, "✅ Quick logged data for: " + pluginId);
            } else {
                Log.e(TAG, "❌ Quick log failed for " + pluginId + ": " + result.getErrorMessage());
            }
        } else {
            Log.e(TAG, "No collector found for plugin: " + pluginId);
        }
    }

    /**
     * Trigger collection UI for a specific plugin
     */
    public void triggerCollection(String pluginId) {
        DataCollector collector = activeCollectors.get(pluginId);
        
        if (collector == null) {
            // Try to create collector
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                collector = plugin.createCollector(context);
                collector.initialize(context);
                activeCollectors.put(pluginId, collector);
            }
        }
        
        if (collector != null) {
            CollectorResult result = collector.collect();
            
            if (result.isSuccess()) {
                Log.d(TAG, "✅ Collection successful for: " + pluginId);
            } else {
                Log.e(TAG, "❌ Collection failed for " + pluginId + ": " + result.getErrorMessage());
            }
        } else {
            Log.e(TAG, "No active collector for plugin: " + pluginId);
        }
    }

    /**
     * Update collector settings
     */
    public void updateCollectorSettings(String pluginId, CollectorSettings newSettings) {
        DataCollector collector = activeCollectors.get(pluginId);
        
        if (collector != null) {
            collector.updateSettings(newSettings);
            
            // Handle automated collection changes
            if (newSettings.isAutomatedCollection() && !collector.isCollectingAutomatically()) {
                collector.startAutomatedCollection();
            } else if (!newSettings.isAutomatedCollection() && collector.isCollectingAutomatically()) {
                collector.stopAutomatedCollection();
            }
            
            Log.d(TAG, "Updated settings for: " + pluginId);
        }
    }

    /**
     * Get statistics about collection system
     */
    public CollectionStats getCollectionStats() {
        List<DataCollectorPlugin> allPlugins = pluginManager.getAllPlugins();
        int totalPlugins = allPlugins.size();
        int enabledPlugins = 0;
        int activeCollectors = this.activeCollectors.size();
        
        for (DataCollectorPlugin plugin : allPlugins) {
            if (plugin.isEnabled()) {
                enabledPlugins++;
            }
        }
        
        return new CollectionStats(totalPlugins, enabledPlugins, activeCollectors);
    }

    /**
     * Collection statistics
     */
    public static class CollectionStats {
        public final int totalPlugins;
        public final int enabledPlugins;
        public final int activeCollectors;
        
        public CollectionStats(int totalPlugins, int enabledPlugins, int activeCollectors) {
            this.totalPlugins = totalPlugins;
            this.enabledPlugins = enabledPlugins;
            this.activeCollectors = activeCollectors;
        }
        
        public String getSummary() {
            return String.format("Plugins: %d total, %d enabled, %d active", 
                totalPlugins, enabledPlugins, activeCollectors);
        }
    }

    // ========== BACKWARD COMPATIBILITY METHODS ==========
    // These methods help during migration from old collector system

    /**
     * Quick log water (backward compatibility)
     * @deprecated Use quickLog("core.water", data) instead
     */
    @Deprecated
    public void quickLogWater(int amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", amount);
        quickLog("core.water", data);
    }

    /**
     * Check if a collector is available by type
     * @deprecated Use plugin system instead
     */
    @Deprecated
    public boolean isCollectorAvailable(String dataType) {
        // Map old data types to plugin IDs
        String pluginId = mapDataTypeToPluginId(dataType);
        DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
        
        if (plugin != null) {
            DataCollector collector = plugin.createCollector(context);
            if (collector != null) {
                collector.initialize(context);
                return collector.isAvailable();
            }
        }
        
        return false;
    }

    /**
     * Map old data type names to plugin IDs
     */
    private String mapDataTypeToPluginId(String dataType) {
        switch (dataType.toLowerCase()) {
            case "water":
                return "core.water";
            case "location":
                return "core.location";
            case "audio":
                return "core.audio";
            case "exercise":
                return "core.exercise";
            case "mood":
                return "core.mood";
            default:
                return dataType;
        }
    }
}
