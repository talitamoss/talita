package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.*;

/**
 * Data Collector Manager - manages data collectors from plugins
 * No more hard-coded collectors - everything comes from plugins
 */
public class DataCollectorManager {
    private static final String TAG = "DataCollectorManager";

    private final Context context;
    private final UniversalDataService dataService;
    private final PluginManager pluginManager;
    private final Map<String, DataCollector> activeCollectors;

    public DataCollectorManager(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
        this.pluginManager = PluginManager.getInstance(context);
        this.activeCollectors = new HashMap<>();

        Log.d(TAG, "📊 Data Collector Manager initialized");
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
     * Quick logging through plugin collectors
     */
    public void quickLog(String pluginId, Map<String, Object> data) {
        DataCollector collector = activeCollectors.get(pluginId);
        
        if (collector == null) {
            // Try to create collector if not active
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                collector = plugin.createCollector(context);
                collector.initialize(context);
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
    public CollectionStats getStats() {
        List<DataCollectorPlugin> allPlugins = pluginManager.getAllPlugins();
        List<DataCollectorPlugin> enabledPlugins = pluginManager.getEnabledPlugins();
        
        Map<String, List<DataCollector>> collectorsByCategory = new HashMap<>();
        int automatedCount = 0;
        
        // Group by category and count automated
        for (Map.Entry<String, DataCollector> entry : activeCollectors.entrySet()) {
            DataCollector collector = entry.getValue();
            String category = collector.getCategory();
            
            if (!collectorsByCategory.containsKey(category)) {
                collectorsByCategory.put(category, new ArrayList<>());
            }
            collectorsByCategory.get(category).add(collector);
            
            if (collector.isCollectingAutomatically()) {
                automatedCount++;
            }
        }
        
        return new CollectionStats(
            allPlugins.size(),
            enabledPlugins.size(),
            activeCollectors.size(),
            automatedCount,
            collectorsByCategory
        );
    }

    /**
     * Statistics about collection system
     */
    public static class CollectionStats {
        public final int totalPlugins;
        public final int enabledPlugins;
        public final int activeCollectors;
        public final int automatedCollectors;
        public final Map<String, List<DataCollector>> collectorsByCategory;

        public CollectionStats(int totalPlugins, int enabledPlugins,
                               int activeCollectors, int automatedCollectors,
                               Map<String, List<DataCollector>> collectorsByCategory) {
            this.totalPlugins = totalPlugins;
            this.enabledPlugins = enabledPlugins;
            this.activeCollectors = activeCollectors;
            this.automatedCollectors = automatedCollectors;
            this.collectorsByCategory = collectorsByCategory;
        }

        public String getSummary() {
            return activeCollectors + " active (" + automatedCollectors + " automated)";
        }
    }
}
