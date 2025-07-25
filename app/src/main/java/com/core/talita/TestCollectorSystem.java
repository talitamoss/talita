package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.core.WaterPlugin;
import java.util.*;

/**
 * Test class to verify the plugin-based collector system works
 * Updated to use the new plugin architecture
 */
public class TestCollectorSystem {
    private static final String TAG = "TestCollectorSystem";

    /**
     * Test the water collection system using plugins
     */
    public static void testWaterCollection(Context context) {
        Log.d(TAG, "🧪 Testing water collection system with plugins...");

        try {
            // 1. Get plugin manager
            PluginManager pluginManager = PluginManager.getInstance(context);
            Log.d(TAG, "✅ PluginManager initialized");

            // 2. Check if water plugin is registered
            DataCollectorPlugin waterPlugin = pluginManager.getPlugin("core.water");
            if (waterPlugin == null) {
                Log.d(TAG, "Water plugin not found, registering...");
                pluginManager.registerPlugin(new WaterPlugin());
                waterPlugin = pluginManager.getPlugin("core.water");
            }
            Log.d(TAG, "✅ Water plugin registered: " + waterPlugin.getPluginName());

            // 3. Enable water plugin
            pluginManager.setPluginEnabled("core.water", true);
            Log.d(TAG, "✅ Water plugin enabled");

            // 4. Create data collector manager
            DataCollectorManager manager = new DataCollectorManager(context);
            Log.d(TAG, "✅ DataCollectorManager created");

            // 5. Get collection stats
            DataCollectorManager.CollectionStats stats = manager.getCollectionStats();
            Log.d(TAG, "📊 Stats: " + stats.getSummary());

            // 6. Test manual water logging through plugin system
            Map<String, Object> waterData = new HashMap<>();
            waterData.put("value", 250);
            manager.quickLog("core.water", waterData);
            Log.d(TAG, "✅ Manual water log test completed (250ml)");

            // 7. Check today's total using helper
            int todayTotal = WaterPlugin.WaterHelper.getTodayTotal(context);
            Log.d(TAG, "💧 Today's water total: " + todayTotal + "ml");

            // 8. Test starting enabled collectors
            manager.startEnabledCollectors();
            Log.d(TAG, "✅ Started enabled collectors");

            // 9. Test backward compatibility method
            manager.quickLogWater(100);
            Log.d(TAG, "✅ Backward compatibility test (100ml)");

            // 10. Check updated total
            todayTotal = WaterPlugin.WaterHelper.getTodayTotal(context);
            Log.d(TAG, "💧 Updated water total: " + todayTotal + "ml");

            Log.d(TAG, "🎉 Water collection system test PASSED!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Water collection system test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test getting all collectors for UI
     */
    public static void testCollectorCategories(Context context) {
        Log.d(TAG, "🧪 Testing collector categories with plugin system...");

        try {
            DataCollectorManager manager = new DataCollectorManager(context);

            // Get collectors by category
            Map<String, List<DataCollector>> categories = manager.getCollectorsByCategory();

            for (String category : categories.keySet()) {
                Log.d(TAG, "📂 Category: " + getCategoryName(category));

                for (DataCollector collector : categories.get(category)) {
                    String status = collector.isAvailable() ?
                            (collector.isEnabled() ? "ENABLED" : "available") :
                            "unavailable";

                    Log.d(TAG, "  " + collector.getEmoji() + " " +
                            collector.getDisplayName() + " - " + status);
                }
            }

            Log.d(TAG, "🎉 Collector categories test PASSED!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Collector categories test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test plugin system functionality
     */
    public static void testPluginSystem(Context context) {
        Log.d(TAG, "🧪 Testing plugin system...");

        try {
            PluginManager pluginManager = PluginManager.getInstance(context);

            // List all plugins
            List<DataCollectorPlugin> allPlugins = pluginManager.getAllPlugins();
            Log.d(TAG, "📦 Total plugins: " + allPlugins.size());

            for (DataCollectorPlugin plugin : allPlugins) {
                Log.d(TAG, "  Plugin: " + plugin.getEmoji() + " " + 
                           plugin.getPluginName() + " v" + plugin.getPluginVersion() +
                           " [" + plugin.getPluginId() + "]");
            }

            // Test plugin categories
            String[] categories = {"i", "we", "all"};
            for (String category : categories) {
                List<DataCollectorPlugin> categoryPlugins = pluginManager.getPluginsByCategory(category);
                Log.d(TAG, "📂 Category '" + getCategoryName(category) + "' has " + 
                           categoryPlugins.size() + " plugins");
            }

            // Test enabling/disabling
            String testPluginId = "core.water";
            boolean originalState = pluginManager.getPlugin(testPluginId).isEnabled();
            
            pluginManager.setPluginEnabled(testPluginId, false);
            Log.d(TAG, "🔄 Disabled " + testPluginId);
            
            pluginManager.setPluginEnabled(testPluginId, true);
            Log.d(TAG, "🔄 Re-enabled " + testPluginId);
            
            pluginManager.setPluginEnabled(testPluginId, originalState);

            Log.d(TAG, "🎉 Plugin system test PASSED!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Plugin system test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run all tests
     */
    public static void runAllTests(Context context) {
        Log.d(TAG, "🚀 Starting plugin-based collector system tests...");
        Log.d(TAG, "================================================");

        testPluginSystem(context);
        Log.d(TAG, "------------------------------------------------");
        
        testWaterCollection(context);
        Log.d(TAG, "------------------------------------------------");
        
        testCollectorCategories(context);
        Log.d(TAG, "------------------------------------------------");

        Log.d(TAG, "✅ All collector system tests completed!");
        Log.d(TAG, "================================================");
    }

    /**
     * Get friendly category name
     */
    private static String getCategoryName(String category) {
        switch (category.toLowerCase()) {
            case "i":
                return "I (Personal)";
            case "we":
                return "We (Social)";
            case "all":
                return "All (Universal)";
            default:
                return category;
        }
    }

    /**
     * Clear test data
     */
    public static void clearTestData(Context context) {
        Log.d(TAG, "🧹 Clearing test data...");
        
        // Clear water data
        WaterPlugin.WaterHelper.clearTodayData(context);
        
        // Clear other test data as needed
        
        Log.d(TAG, "✅ Test data cleared");
    }
}
