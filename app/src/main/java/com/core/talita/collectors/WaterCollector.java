// ============================================================================
// FILE: app/src/main/java/com/core/talita/collectors/WaterCollector.java
// ============================================================================
package com.core.talita.collectors;

import android.content.Context;
import android.util.Log;
import com.core.talita.*;
import java.util.*;

/**
 * Water intake collector - tracks daily hydration
 * Brand-agnostic: works with any app name
 */
public class WaterCollector implements DataCollector {
    private static final String TAG = "WaterCollector";
    private static final String PREFS_KEY = "water_intake_ml";
    private static final String PREFS_NAME = "personal_data_collectors"; // Generic name
    private static final String DATA_PREFS = "water_data"; // Generic name

    @Override
    public String getDataType() { return "water"; }

    @Override
    public String getDisplayName() { return "Water Intake"; }

    @Override
    public String getIcon() { return "💧"; }

    @Override
    public boolean isAvailable(Context context) {
        return true; // Always available (manual input)
    }

    @Override
    public boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("water_enabled", false);
    }

    @Override
    public void startCollection(Context context, DataCollectionCallback callback) {
        Log.d(TAG, "💧 Starting water intake collection");

        // Get current daily total
        int dailyTotal = context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE)
                .getInt(getTodayKey(), 0);

        // Simulate logging 250ml of water
        int newIntake = 250; // Standard glass
        int newTotal = dailyTotal + newIntake;

        // Create water data
        Map<String, Object> waterData = new HashMap<>();
        waterData.put("volume_ml", newIntake);
        waterData.put("daily_total_ml", newTotal);
        waterData.put("method", "manual"); // manual, reminder, automatic
        waterData.put("display_name", "Water Logged");
        waterData.put("summary", newIntake + "ml water (daily: " + newTotal + "ml)");

        // Save daily total
        context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(getTodayKey(), newTotal)
                .apply();

        // Create universal data object
        UniversalPersonalData data = new UniversalPersonalData("water", waterData);

        // Report success
        callback.onDataCollected(data);

        Log.d(TAG, "💧 Water logged: " + newIntake + "ml (daily total: " + newTotal + "ml)");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "💧 Stopping water collection");
        // Nothing to stop for manual logging
    }

    @Override
    public List<String> getRequiredPermissions() {
        return new ArrayList<>(); // No permissions needed for manual water logging
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(2 * 60 * 60 * 1000) // Remind every 2 hours
                .setThreshold("daily_goal", 2000); // 2L daily goal
    }

    /**
     * Get today's key for SharedPreferences (resets daily)
     */
    private String getTodayKey() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return PREFS_KEY + "_" + sdf.format(new Date());
    }

    /**
     * Public method to manually log water (for UI integration)
     * Brand-agnostic: works with any app name
     */
    public static void logWater(Context context, int volumeMl) {
        WaterCollector collector = new WaterCollector();

        if (collector.isEnabled(context)) {
            collector.startCollection(context, new DataCollectionCallback() {
                @Override
                public void onDataCollected(PersonalData data) {
                    Log.d(TAG, "💧 Water logged via manual entry: " + data.getDisplaySummary());
                }

                @Override
                public void onCollectionError(String error) {
                    Log.e(TAG, "💧 Water logging error: " + error);
                }
            });
        }
    }

    /**
     * Enable/disable water collection
     * Brand-agnostic helper method
     */
    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("water_enabled", enabled)
                .apply();

        Log.d(TAG, "💧 Water collection " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Get today's water total
     */
    public static int getTodayTotal(Context context) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayKey = PREFS_KEY + "_" + sdf.format(new Date());

        return context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE)
                .getInt(todayKey, 0);
    }
}