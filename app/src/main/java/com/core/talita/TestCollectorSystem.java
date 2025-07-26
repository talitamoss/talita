package com.core.talita;

/**
 * TestCollectorSystem - TEMPORARILY DISABLED FOR MVP
 * 
 * Test class for verifying the plugin-based collector system.
 * Not needed for production MVP build.
 */
public class TestCollectorSystem {
    // Test class disabled for MVP build
}

/* ORIGINAL CODE - COMMENTED FOR MVP
package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.core.WaterPlugin;
import java.util.*;

public class TestCollectorSystem {
    private static final String TAG = "TestCollectorSystem";

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

    private static String getCategoryName(String categoryId) {
        switch (categoryId) {
            case "movement": return "Movement & Fitness";
            case "wellness": return "Lifestyle & Wellness";
            case "mood": return "Mood & Energy";
            case "i": return "Personal & Social";
            case "it": return "IT & Technology";
            default: return categoryId;
        }
    }

    public static void testAllPlugins(Context context) {
        Log.d(TAG, "🧪 Testing all plugins...");

        PluginManager pluginManager = PluginManager.getInstance(context);
        List<DataCollectorPlugin> allPlugins = pluginManager.getAllPlugins();

        Log.d(TAG, "📋 Found " + allPlugins.size() + " plugins:");

        for (DataCollectorPlugin plugin : allPlugins) {
            Log.d(TAG, "\n--- Testing " + plugin.getPluginName() + " ---");
            Log.d(TAG, "ID: " + plugin.getPluginId());
            Log.d(TAG, "Version: " + plugin.getVersion());
            Log.d(TAG, "Author: " + plugin.getAuthor());
            Log.d(TAG, "Category: " + plugin.getCategory());
            Log.d(TAG, "Enabled: " + plugin.isEnabled());

            try {
                // Test creating collector
                DataCollector collector = plugin.createCollector(context);
                if (collector != null) {
                    Log.d(TAG, "✅ Collector created successfully");
                    
                    // Test quick add if supported
                    if (plugin.supportsQuickAdd()) {
                        QuickAddConfig config = plugin.getQuickAddConfig();
                        Log.d(TAG, "✅ Quick Add supported: " + config.getTitle());
                    }
                } else {
                    Log.d(TAG, "❌ Failed to create collector");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error testing plugin: " + e.getMessage());
            }
        }

        Log.d(TAG, "\n🎉 Plugin test completed!");
    }
}
*/
