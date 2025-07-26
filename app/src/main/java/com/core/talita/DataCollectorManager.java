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
                Log.e(TAG, "❌ Failed to start collector from plugin: " + plugin.getPluginId(), e);
            }
        }
        
        Log.d(TAG, "✅ Started " + startedCount + " collectors");
    }

    /**
     * Stop all active collectors
     */
    public void stopAllCollectors() {
        Log.d(TAG, "🛑 Stopping all collectors...");
        
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
     * Get a specific collector by plugin ID
     */
    public DataCollector getCollector(String pluginId) {
        // Check if already active
        if (activeCollectors.containsKey(pluginId)) {
            return activeCollectors.get(pluginId);
        }
        
        // Try to create from plugin
        DataCollectorPlugin plugin = pluginManager.getPlugin(pluginId);
        if (plugin != null) {
            DataCollector collector = plugin.createCollector(context);
            if (collector != null) {
                collector.initialize(context);
                activeCollectors.put(pluginId, collector);
                return collector;
            }
        }
        
        return null;
    }

    /**
     * Get all active collectors
     */
    public Collection<DataCollector> getActiveCollectors() {
        return activeCollectors.values();
    }

    /**
     * Trigger quick collection for a specific plugin
     */
    public void triggerCollection(String pluginId) {
        DataCollector collector = getCollector(pluginId);
        
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
     * Check if a collector is enabled
     */
    public boolean isCollectorEnabled(String dataType) {
        return prefs.getBoolean(dataType + "_enabled", true);
    }

    /**
     * Get enabled collector types
     */
    public Set<String> getEnabledTypes() {
        Set<String> enabled = new HashSet<>();
        Map<String, ?> allPrefs = prefs.getAll();
        
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().endsWith("_enabled") && (Boolean) entry.getValue()) {
                String type = entry.getKey().replace("_enabled", "");
                enabled.add(type);
            }
        }
        
        return enabled;
    }

    /**
     * Quick log water intake
     * Convenience method for water logging
     */
    public void quickLogWater(int amountMl) {
        // Try to find water collector from plugins
        DataCollector waterCollector = null;
        
        // First try to find from active collectors
        for (Map.Entry<String, DataCollector> entry : activeCollectors.entrySet()) {
            if ("water".equals(entry.getValue().getDataType()) || 
                entry.getKey().contains("water")) {
                waterCollector = entry.getValue();
                break;
            }
        }
        
        // If not found, try to create from water plugin
        if (waterCollector == null) {
            DataCollectorPlugin waterPlugin = pluginManager.getPlugin("core.water");
            if (waterPlugin != null) {
                waterCollector = waterPlugin.createCollector(context);
                if (waterCollector != null) {
                    waterCollector.initialize(context);
                    activeCollectors.put("core.water", waterCollector);
                }
            }
        }
        
        // Log the water intake
        if (waterCollector != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("amount_ml", amountMl);
            data.put("unit", "ml");
            
            CollectorResult result = waterCollector.collectQuick(data);
            if (result.isSuccess()) {
                Log.d(TAG, "✅ Quick logged water: " + amountMl + "ml");
            } else {
                Log.e(TAG, "❌ Failed to quick log water: " + result.getErrorMessage());
            }
        } else {
            Log.e(TAG, "❌ No water collector available");
        }
    }

    /**
     * Get summary of all collectors
     */
    public Map<String, CollectorInfo> getCollectorSummary() {
        Map<String, CollectorInfo> summary = new HashMap<>();
        
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            CollectorInfo info = new CollectorInfo();
            info.pluginId = plugin.getPluginId();
            info.name = plugin.getPluginName();
            info.enabled = plugin.isEnabled();
            info.isActive = activeCollectors.containsKey(plugin.getPluginId());
            
            DataCollector collector = activeCollectors.get(plugin.getPluginId());
            if (collector != null) {
                info.isCollecting = collector.isCollectingAutomatically();
                info.dataType = collector.getDataType();
            }
            
            summary.put(plugin.getPluginId(), info);
        }
        
        return summary;
    }

    /**
     * Info class for collector summary
     */
    public static class CollectorInfo {
        public String pluginId;
        public String name;
        public String dataType;
        public boolean enabled;
        public boolean isActive;
        public boolean isCollecting;
    }
}
