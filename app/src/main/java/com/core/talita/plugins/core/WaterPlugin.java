package com.core.talita.plugins.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.Map;
import java.util.HashMap;

/**
 * WaterPlugin - Core plugin for water intake tracking
 * 
 * This replaces the old WaterCollector class with a plugin-based approach.
 * Uses SimpleDataCollector for easy implementation.
 */
public class WaterPlugin extends DataCollectorPlugin {
    
    private static final String PREFS_NAME = "water_tracking";
    
    @Override
    public String getPluginId() {
        return "core.water";
    }
    
    @Override
    public String getPluginName() {
        return "Water Intake";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Core Team";
    }
    
    @Override
    public String getCategory() {
        return "i"; // Personal category
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority - essential tracker
    }
    
    @Override
    public String getEmoji() {
        return "💧";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#3B82F6"); // Blue
    }
    
    @Override
    public int getIconResource() {
        return 0; // Use emoji instead
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return new String[0]; // No permissions needed
    }
    
    @Override
    public boolean requiresBackgroundTracking() {
        return false;
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true;
    }
    
    @Override
    public boolean supportsScheduling() {
        return true; // For reminders
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        // Create a custom water collector that extends SimpleDataCollector
        return new WaterDataCollector(context);
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open water settings activity
        // For now, just show a toast
        android.widget.Toast.makeText(context, 
            "Water settings: Daily goal, reminders, etc.", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Water",
            "Track hydration",
            "GRID",
            true // Show in main grid
        );
    }
    
    /**
     * Custom water collector with additional functionality
     */
    private static class WaterDataCollector extends SimpleDataCollector {
        private final Context context;
        
        public WaterDataCollector(Context context) {
            super(new SimpleDataCollector.Builder("water", "Water Intake")
                .description("Track your daily water consumption")
                .emoji("💧")
                .category("i")
                .inputHint("Amount in ml")
                .inputType(SimpleDataCollector.InputType.NUMBER)
                .unit("ml")
                .quickOptions("100", "250", "500", "750")
                .build());
            
            this.context = context;
        }
        
        @Override
        public void initialize(Context context) {
            super.initialize(context);
            // Additional initialization if needed
        }
        
        @Override
        protected CollectorResult performQuickCollection(Map<String, Object> data) {
            // First do the normal collection
            CollectorResult result = super.performQuickCollection(data);
            
            if (result.isSuccess()) {
                // Update daily total
                updateDailyTotal(data);
            }
            
            return result;
        }
        
        /**
         * Update the daily total (replaces WaterCollector.logWater)
         */
        private void updateDailyTotal(Map<String, Object> data) {
            try {
                Object value = data.get("value");
                int amount = 0;
                
                if (value instanceof Number) {
                    amount = ((Number) value).intValue();
                } else if (value instanceof String) {
                    amount = Integer.parseInt(value.toString());
                }
                
                if (amount > 0) {
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String todayKey = "water_" + getTodayDateString();
                    int currentTotal = prefs.getInt(todayKey, 0);
                    int newTotal = currentTotal + amount;
                    
                    prefs.edit().putInt(todayKey, newTotal).apply();
                    
                    // Log for debugging
                    Log.d("WaterPlugin", 
                        "Updated daily total: " + currentTotal + " + " + amount + " = " + newTotal + "ml");
                }
            } catch (Exception e) {
                Log.e("WaterPlugin", "Failed to update daily total", e);
            }
        }
        
        /**
         * Get today's date as string (replaces date logic from WaterCollector)
         */
        private String getTodayDateString() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            return sdf.format(new java.util.Date());
        }
    }
    
    /**
     * Static helper methods for backward compatibility during migration
     * These replace the old WaterCollector static methods
     */
    public static class WaterHelper {
        
        /**
         * Get today's water total (replaces WaterCollector.getTodayTotal)
         */
        public static int getTodayTotal(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String todayKey = "water_" + getTodayDateString();
            return prefs.getInt(todayKey, 0);
        }
        
        /**
         * Log water amount (replaces WaterCollector.logWater)
         * @deprecated Use DataCollectorManager.quickLog instead
         */
        @Deprecated
        public static void logWater(Context context, int amount) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String todayKey = "water_" + getTodayDateString();
            int currentTotal = prefs.getInt(todayKey, 0);
            prefs.edit().putInt(todayKey, currentTotal + amount).apply();
        }
        
        /**
         * Clear today's data (for testing)
         */
        public static void clearTodayData(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String todayKey = "water_" + getTodayDateString();
            prefs.edit().remove(todayKey).apply();
        }
        
        /**
         * Check if water tracking is enabled
         */
        public static boolean isEnabled(Context context) {
            SharedPreferences prefs = context.getSharedPreferences("collector_settings", Context.MODE_PRIVATE);
            return prefs.getBoolean("water_enabled", true);
        }
        
        /**
         * Enable/disable water tracking
         */
        public static void setEnabled(Context context, boolean enabled) {
            SharedPreferences prefs = context.getSharedPreferences("collector_settings", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("water_enabled", enabled).apply();
        }
        
        private static String getTodayDateString() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            return sdf.format(new java.util.Date());
        }
    }
}
