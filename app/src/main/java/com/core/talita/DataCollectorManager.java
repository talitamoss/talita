package com.core.talita;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.api.DataCollector;
import com.core.talita.api.CollectorResult;
import com.core.talita.api.CollectorSettings;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.PluginCategories;
import java.util.*;

/**
 * DataCollectorManager - Manages all data collectors
 * 
 * Fixed to include all missing methods that activities expect.
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
                        Log.d(TAG, "✅ Started collector: " + plugin.getPluginName());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to start collector: " + plugin.getPluginId(), e);
            }
        }
        
        Log.d(TAG, "Started " + startedCount + " collectors");
    }

    /**
     * Stop all collectors
     */
    public void stopAllCollectors() {
        Log.d(TAG, "Stopping all collectors...");
        
        for (DataCollector collector : activeCollectors.values()) {
            try {
                if (collector.isCollectingAutomatically()) {
                    collector.stopAutomatedCollection();
                }
                collector.onDestroy();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping collector", e);
            }
        }
        
        activeCollectors.clear();
    }

    /**
     * Get a specific collector by type
     */
    public DataCollector getCollector(String dataType) {
        // First check active collectors
        for (DataCollector collector : activeCollectors.values()) {
            if (collector.getDataType().equals(dataType)) {
                return collector;
            }
        }
        
        // Try to create from plugins
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            DataCollector collector = plugin.createCollector(context);
            if (collector != null && collector.getDataType().equals(dataType)) {
                collector.initialize(context);
                return collector;
            }
        }
        
        return null;
    }

    /**
     * Get all available collectors
     */
    public List<DataCollector> getAllCollectors() {
        List<DataCollector> collectors = new ArrayList<>();
        
        // Get from all plugins
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            DataCollector collector = plugin.createCollector(context);
            if (collector != null) {
                if (!activeCollectors.containsKey(plugin.getPluginId())) {
                    collector.initialize(context);
                }
                collectors.add(collector);
            }
        }
        
        return collectors;
    }

    /**
     * Get collectors organized by category
     */
    public Map<String, List<DataCollector>> getCollectorsByCategory() {
        Map<String, List<DataCollector>> categoryMap = new HashMap<>();
        
        // Initialize categories
        categoryMap.put(PluginCategories.I_DISPLAY, new ArrayList<>());
        categoryMap.put(PluginCategories.WE_DISPLAY, new ArrayList<>());
        categoryMap.put(PluginCategories.ALL_DISPLAY, new ArrayList<>());
        
        // Sort collectors into categories
        for (DataCollector collector : getAllCollectors()) {
            String category = collector.getCategory();
            String displayCategory = PluginCategories.getDisplayName(category);
            
            List<DataCollector> categoryList = categoryMap.get(displayCategory);
            if (categoryList != null) {
                categoryList.add(collector);
            }
        }
        
        return categoryMap;
    }

    /**
     * Enable or disable a collector
     */
    public void setCollectorEnabled(String dataType, boolean enabled) {
        prefs.edit().putBoolean(dataType + "_enabled", enabled).apply();
        
        // Update active collector if exists
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
     * Get collection statistics
     */
    public CollectionStats getCollectionStats() {
        int totalCollectors = 0;
        int enabledCollectors = 0;
        int activeCollectors = 0;
        
        for (DataCollector collector : getAllCollectors()) {
            totalCollectors++;
            if (collector.isEnabled()) {
                enabledCollectors++;
                if (collector.isCollectingAutomatically()) {
                    activeCollectors++;
                }
            }
        }
        
        return new CollectionStats(totalCollectors, enabledCollectors, activeCollectors);
    }

    /**
     * Quick log water intake
     * Convenience method for water logging
     */
    public void quickLogWater(int amountMl) {
        DataCollector waterCollector = getCollector("water");
        
        if (waterCollector != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("amount", amountMl);
            data.put("unit", "ml");
            
            CollectorResult result = waterCollector.collectQuick(data);
            
            if (result.isSuccess()) {
                Log.d(TAG, "Water logged: " + amountMl + "ml");
            } else {
                Log.e(TAG, "Failed to log water: " + result.getMessage());
            }
        } else {
            Log.e(TAG, "Water collector not found");
        }
    }

    /**
     * Collection statistics inner class
     */
    public static class CollectionStats {
        public final int totalCollectors;
        public final int enabledCollectors;
        public final int activeCollectors;
        
        public CollectionStats(int total, int enabled, int active) {
            this.totalCollectors = total;
            this.enabledCollectors = enabled;
            this.activeCollectors = active;
        }
        
        public String getSummary() {
            return String.format(Locale.getDefault(),
                "%d enabled / %d total (%d active)",
                enabledCollectors, totalCollectors, activeCollectors);
        }
    }
}
