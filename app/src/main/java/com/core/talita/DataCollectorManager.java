package com.core.talita;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.api.DataCollector;
import com.core.talita.api.CollectorResult;
import com.core.talita.api.CollectorSettings;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import java.util.*;

/**
 * DataCollectorManager - Manages all data collectors
 * Updated to use the API DataCollector interface
 */
public class DataCollectorManager {
    private static final String TAG = "DataCollectorManager";
    private static final String PREFS_NAME = "collector_settings";
    private static DataCollectorManager instance;
    
    private final Context context;
    private final PluginManager pluginManager;
    private final Map<String, DataCollector> activeCollectors = new HashMap<>();
    private final SharedPreferences prefs;

    private DataCollectorManager(Context context) {
        this.context = context.getApplicationContext();
        this.pluginManager = PluginManager.getInstance(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized DataCollectorManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataCollectorManager(context);
        }
        return instance;
    }

    /**
     * Start all enabled collectors from plugins
     */
    public void startEnabledCollectors() {
        Log.d(TAG, "🚀 Starting enabled collectors from plugins...");
        
        int startedCount = 0;
        for (DataCollectorPlugin plugin : pluginManager.getEnabledPlugins()) {
            try {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    // Initialize the collector with context
                    collector.initialize(context);
                    
                    // Check if enabled
                    if (collector.isEnabled()) {
                        // Only start automated collection if configured
                        CollectorSettings settings = collector.getSettings();
                        if (settings != null && settings.isAutomatedCollection()) {
                            collector.startAutomatedCollection();
                        }
                        
                        activeCollectors.put(plugin.getPluginId(), collector);
                        startedCount++;
                        Log.d(TAG, "▶️ Started: " + collector.getDisplayName() + " from " + plugin.getPluginId());
                    }
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
     * Get all collectors (for settings UI)
     */
    public List<DataCollector> getAllCollectors() {
        return getAllAvailableCollectors();
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
                    if (category == null) category = "other";
                    
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
                if (collector != null) {
                    collector.initialize(context);
                    activeCollectors.put(pluginId, collector);
                }
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
            // Try to create if not active
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    activeCollectors.put(pluginId, collector);
                }
            }
        }
        
        if (collector != null) {
            CollectorResult result = collector.collect();
            
            if (result.isSuccess()) {
                Log.d(TAG, "✅ Collection triggered for: " + pluginId);
            } else {
                Log.e(TAG, "❌ Collection failed for " + pluginId + ": " + result.getErrorMessage());
            }
        } else {
            Log.e(TAG, "No collector found for plugin: " + pluginId);
        }
    }

    /**
     * Update collector settings
     */
    public void updateCollectorSettings(String pluginId, CollectorSettings newSettings) {
        DataCollector collector = activeCollectors.get(pluginId);
        
        if (collector == null) {
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    activeCollectors.put(pluginId, collector);
                }
            }
        }
        
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
     * Enable/disable a collector
     */
    public void setCollectorEnabled(String dataType, boolean enabled) {
        prefs.edit().putBoolean(dataType + "_enabled", enabled).apply();
        
        // Find and update the collector
        for (Map.Entry<String, DataCollector> entry : activeCollectors.entrySet()) {
            DataCollector collector = entry.getValue();
            if (collector.getDataType().equals(dataType)) {
                collector.setEnabled(enabled);
                
                // Stop collection if disabled
                if (!enabled && collector.isCollectingAutomatically()) {
                    collector.stopAutomatedCollection();
                }
                break;
            }
        }
    }

    /**
     * Check if a collector is available (has required sensors/permissions)
     */
    public boolean isCollectorAvailable(String pluginId) {
        try {
            DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin != null) {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    return collector.isAvailable();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking collector availability", e);
        }
        return false;
    }

    /**
     * Get collection statistics
     */
    public CollectionStats getCollectionStats() {
        int totalCollectors = 0;
        int enabledCollectors = 0;
        int activeCollectors = 0;
        
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            try {
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    collector.initialize(context);
                    totalCollectors++;
                    
                    if (collector.isEnabled()) {
                        enabledCollectors++;
                    }
                    
                    if (this.activeCollectors.containsKey(plugin.getPluginId()) &&
                        collector.isCollectingAutomatically()) {
                        activeCollectors++;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting stats", e);
            }
        }
        
        return new CollectionStats(totalCollectors, enabledCollectors, activeCollectors);
    }

    /**
     * Collection statistics
     */
    public static class CollectionStats {
        public final int total;
        public final int enabled;
        public final int active;
        
        CollectionStats(int total, int enabled, int active) {
            this.total = total;
            this.enabled = enabled;
            this.active = active;
        }
        
        public String getSummary() {
            return String.format("%d enabled / %d total (%d active)", 
                enabled, total, active);
        }
    }
}
