package com.core.talita.plugins.core;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.DataCollector;
import com.core.talita.api.QuickAddConfig;
import com.core.talita.api.CollectorResult;
import com.core.talita.api.CollectorSettings;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.base.BaseDataCollector;
import com.core.talita.PersonalData;
import com.core.talita.UniversalDataService;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;

/**
 * WaterPlugin - Core water intake tracking plugin
 * 
 * This is our reference implementation showing the complete vertical slice
 * of how plugins should work with the new architecture.
 */
public class WaterPlugin extends DataCollectorPlugin {
    private static final String TAG = "WaterPlugin";
    
    // ===== Plugin Identity (Required by DataCollectorPlugin) =====
    
    @Override
    public String getPluginId() {
        return "core.water";
    }
    
    @Override
    public String getPluginName() {
        return "Water Intake";
    }
    
    @Override
    public String getDescription() {
        return "Track your daily water consumption to stay hydrated";
    }
    
    @Override
    public String getAuthor() {
        return "Core Team";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getCategory() {
        return "i"; // Personal category
    }
    
    @Override
    public String getEmoji() {
        return "💧";
    }
    
    // ===== Plugin Configuration =====
    
    @Override
    public int getPriority() {
        return 100; // High priority - water is essential
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true;
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        // Using the proper Builder pattern with correct constructor
        return new QuickAddConfig.Builder()
            .setTitle("Water")
            .setDescription("Log water intake")
            .setStyle(QuickAddConfig.QuickAddStyle.NUMERIC_INPUT)
            .defaultValue(250) // Default 250ml
            .range(50, 1000)   // Range from 50ml to 1L
            .build();
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        WaterDataCollector collector = new WaterDataCollector();
        collector.initialize(context);
        return collector;
    }
    
    @Override
    public void onQuickAddTapped(Context context) {
        // For water, we'll use the default numeric input behavior
        DataCollector collector = createCollector(context);
        if (collector != null) {
            Map<String, Object> quickData = new HashMap<>();
            quickData.put("amount", 250); // Default amount
            quickData.put("unit", "ml");
            collector.collectQuick(quickData);
        }
    }
    
    // ===== The Actual Water Data Collector =====
    
    /**
     * Inner class that implements the actual data collection logic
     */
    private static class WaterDataCollector extends BaseDataCollector {
        private static final String TAG = "WaterDataCollector";
        
        @Override
        public String getDataType() {
            return "water";
        }
        
        @Override
        public String getDisplayName() {
            return "Water Intake";
        }
        
        @Override
        public String getDescription() {
            return "Track daily water consumption";
        }
        
        @Override
        public String getEmoji() {
            return "💧";
        }
        
        @Override
        public String getCategory() {
            return "i";
        }
        
        @Override
        public boolean isAvailable() {
            // Water tracking is always available - no special sensors needed
            return true;
        }
        
        @Override
        public List<String> getRequiredPermissions() {
            // No special permissions needed for manual water tracking
            return new ArrayList<>();
        }
        
        @Override
        public CollectorResult collect() {
            // For manual collection, we'd show a dialog
            // For now, return pending as the UI will handle it
            Log.d(TAG, "Manual water collection requested");
            return CollectorResult.pending(getDataType());
        }
        
        @Override
        public CollectorResult collectQuick(Map<String, Object> data) {
            if (!validateData(data)) {
                return CollectorResult.failure(getDataType(), "Invalid water data");
            }
            
            try {
                // Extract amount and unit
                int amount = getIntValue(data, "amount", 250);
                String unit = getStringValue(data, "unit", "ml");
                
                // Normalize to ml if needed
                int amountMl = convertToMl(amount, unit);
                
                // Create the data map for storage
                Map<String, Object> waterData = new HashMap<>();
                waterData.put("amount", amountMl);
                waterData.put("unit", "ml");
                waterData.put("originalAmount", amount);
                waterData.put("originalUnit", unit);
                waterData.put("timestamp", System.currentTimeMillis());
                
                // Save using the base class method
                saveData(waterData);
                
                Log.d(TAG, "Water intake recorded: " + amountMl + "ml");
                return CollectorResult.success(getDataType(), waterData);
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to collect water data", e);
                return CollectorResult.failure(getDataType(), e.getMessage());
            }
        }
        
        @Override
        protected CollectorSettings getDefaultSettings() {
            return new CollectorSettings.Builder()
                .setAutomatedCollection(false) // Water is manually logged
                .setNotificationsEnabled(true)  // But we can remind users
                .setNotificationInterval(2 * 60 * 60 * 1000) // Every 2 hours
                .build();
        }
        
        @Override
        public boolean validateData(Map<String, Object> data) {
            if (data == null || data.isEmpty()) {
                return false;
            }
            
            // Must have amount
            if (!data.containsKey("amount")) {
                return false;
            }
            
            // Amount must be positive
            try {
                int amount = getIntValue(data, "amount", 0);
                return amount > 0;
            } catch (Exception e) {
                return false;
            }
        }
        
        // ===== Helper Methods =====
        
        private int getIntValue(Map<String, Object> data, String key, int defaultValue) {
            Object value = data.get(key);
            if (value == null) return defaultValue;
            
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            return defaultValue;
        }
        
        private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
            Object value = data.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        
        private int convertToMl(int amount, String unit) {
            switch (unit.toLowerCase()) {
                case "ml":
                    return amount;
                case "l":
                case "liter":
                case "litre":
                    return amount * 1000;
                case "oz":
                case "fl oz":
                    return (int) (amount * 29.5735);
                case "cup":
                    return amount * 237;
                case "glass":
                    return amount * 250; // Assume standard glass
                default:
                    return amount; // Assume ml if unknown
            }
        }
    }
    
    // ===== Optional Features =====
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Create WaterSettingsActivity
        Log.d(TAG, "Water settings requested");
    }
    
    @Override
    public boolean supportsScheduling() {
        return true; // Support reminder notifications
    }
    
    @Override
    public boolean requiresBackgroundTracking() {
        return false; // Water is manually tracked
    }
}
