package com.core.talita.collectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Water intake collector - tracks daily hydration
 * Brand-agnostic: works with any app name
 */
public class WaterCollector implements DataCollector {
    private static final String TAG = "WaterCollector";
    private static final String PREFS_KEY = "water_intake_ml";
    private static final String PREFS_NAME = "personal_data_collectors";
    private static final String DATA_PREFS = "water_data";

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
        // This is for continuous collection - not used for quick logging
        Log.d(TAG, "💧 Water collector in continuous mode (not implemented)");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "💧 Stopping water collection");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return new ArrayList<>(); // No permissions needed
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
    private static String getTodayKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return PREFS_KEY + "_" + sdf.format(new Date());
    }

    /**
     * Get today's total water intake
     */
    public static int getTodayTotal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(getTodayKey(), 0);
    }

    /**
     * FIXED: Public method to manually log water
     * This is the main method for quick logging
     */
    public static void logWater(Context context, int volumeMl) {
        Log.d(TAG, "💧 logWater called with " + volumeMl + "ml");
        
        if (volumeMl <= 0) {
            Log.w(TAG, "Invalid water amount: " + volumeMl);
            return;
        }

        // Get current daily total
        SharedPreferences prefs = context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE);
        int dailyTotal = prefs.getInt(getTodayKey(), 0);
        int newTotal = dailyTotal + volumeMl;

        // Save new total
        prefs.edit().putInt(getTodayKey(), newTotal).apply();
        Log.d(TAG, "💧 Daily total updated: " + dailyTotal + " → " + newTotal);

        // Create water data for storage
        Map<String, Object> waterData = new HashMap<>();
        waterData.put("display_name", "💧 " + volumeMl + "ml");
        waterData.put("summary", volumeMl + "ml water consumed");
        waterData.put("amount_ml", String.valueOf(volumeMl));
        waterData.put("daily_total_ml", String.valueOf(newTotal));
        waterData.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

        // Create universal data object
        UniversalPersonalData data = new UniversalPersonalData("water", waterData);

        try {
            // Save via Universal Data Service
            UniversalDataService dataService = new UniversalDataService(context);
            PersonalDataAdapter adapter = new PersonalDataAdapter(data);
            String dataId = dataService.capture(adapter);
            
            if (dataId != null) {
                Log.d(TAG, "✅ Water data saved with ID: " + dataId);
            } else {
                Log.e(TAG, "❌ Failed to save water data - capture returned null");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception saving water data", e);
        }
    }

    /**
     * Enable/disable water collection
     */
    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("water_enabled", enabled)
                .apply();

        Log.d(TAG, "💧 Water collection " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Clear today's water data (for testing)
     */
    public static void clearTodayData(Context context) {
        context.getSharedPreferences(DATA_PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(getTodayKey())
                .apply();
        Log.d(TAG, "💧 Cleared today's water data");
    }
}
