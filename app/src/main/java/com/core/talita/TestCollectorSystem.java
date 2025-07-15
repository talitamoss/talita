package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.collectors.WaterCollector;

/**
 * Simple test class to verify the new collector system works
 * Brand-agnostic: works with any app name
 *
 * Call this from your MainActivity to test water collection!
 */
public class TestCollectorSystem {
    private static final String TAG = "TestCollectorSystem";

    /**
     * Test the water collector system
     */
    public static void testWaterCollection(Context context) {
        Log.d(TAG, "🧪 Testing water collection system...");

        try {
            // 1. Enable water collection
            WaterCollector.setEnabled(context, true);
            Log.d(TAG, "✅ Water collection enabled");

            // 2. Create data collector manager
            DataCollectorManager manager = new DataCollectorManager(context);
            Log.d(TAG, "✅ DataCollectorManager created");

            // 3. Get collection stats
            DataCollectorManager.CollectionStats stats = manager.getCollectionStats();
            Log.d(TAG, "📊 Stats: " + stats.getSummary());

            // 4. Test manual water logging
            WaterCollector.logWater(context, 250);
            Log.d(TAG, "✅ Manual water log test completed");

            // 5. Check today's total
            int todayTotal = WaterCollector.getTodayTotal(context);
            Log.d(TAG, "💧 Today's water total: " + todayTotal + "ml");

            // 6. Test starting enabled collectors
            manager.startEnabledCollectors();
            Log.d(TAG, "✅ Started enabled collectors");

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
        Log.d(TAG, "🧪 Testing collector categories...");

        try {
            DataCollectorManager manager = new DataCollectorManager(context);

            // Get collectors by category
            var categories = manager.getCollectorsByCategory();

            for (String category : categories.keySet()) {
                Log.d(TAG, "📂 Category: " + category);

                for (DataCollector collector : categories.get(category)) {
                    String status = collector.isAvailable(context) ?
                            (collector.isEnabled(context) ? "ENABLED" : "available") :
                            "unavailable";

                    Log.d(TAG, "  " + collector.getIcon() + " " +
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
     * Run all tests
     */
    public static void runAllTests(Context context) {
        Log.d(TAG, "🚀 Starting collector system tests...");

        testWaterCollection(context);
        testCollectorCategories(context);

        Log.d(TAG, "✅ All collector system tests completed!");
    }
}