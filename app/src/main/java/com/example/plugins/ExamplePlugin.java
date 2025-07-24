package com.example.plugins;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.core.talita.api.*;
import com.core.talita.plugins.base.BaseDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;
import com.core.talita.plugins.bridge.PluginBridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExamplePlugin - Demonstrates how to create a plugin for Talita
 * 
 * This example tracks daily gratitude entries and demonstrates:
 * - Data collection using the new API
 * - Inter-plugin communication
 * - Event handling
 * - Data sharing
 */
public class ExamplePlugin extends DataCollectorPlugin {
    private static final String TAG = "GratitudePlugin";
    
    // Plugin metadata
    @Override
    public String getPluginId() {
        return "com.example.gratitude";
    }
    
    @Override
    public String getPluginName() {
        return "Daily Gratitude";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Example Developer";
    }
    
    @Override
    public String getCategory() {
        return PluginCategories.I; // Personal/self category
    }
    
    @Override
    public int getPriority() {
        return 75; // Medium-high priority
    }
    
    // Visual customization
    @Override
    public String getEmoji() {
        return "🙏";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#FFC107"); // Amber
    }
    
    @Override
    public int getIconResource() {
        return 0; // No custom icon, use emoji
    }
    
    // Plugin capabilities
    @Override
    public String[] getRequiredPermissions() {
        return new String[0]; // No special permissions needed
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
        return true; // Can schedule daily reminders
    }
    
    // Create the data collector
    @Override
    public DataCollector createCollector(Context context) {
        return new GratitudeCollector(context);
    }
    
    // Plugin settings
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // In a real implementation, this would open a settings activity
        Toast.makeText(context, "Gratitude settings", Toast.LENGTH_SHORT).show();
    }
    
    // Quick add configuration
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Gratitude",
            "What are you grateful for?",
            QuickAddConfig.QuickAddStyle.CARD,
            true
        );
    }
    
    // Plugin lifecycle
    @Override
    public void onPluginEnabled(Context context) {
        super.onPluginEnabled(context);
        Log.d(TAG, "Gratitude plugin enabled");
        
        // Register with plugin bridge for inter-plugin communication
        PluginBridge bridge = PluginBridge.getInstance(context);
        
        // Register as a data provider
        bridge.registerDataProvider(getPluginId(), "gratitude_stats", 
            (requestingPluginId, dataType, params, callback) -> {
                // Provide gratitude statistics to other plugins
                Bundle stats = calculateGratitudeStats(context);
                callback.onSuccess(stats);
            }
        );
        
        // Register message handler
        bridge.registerMessageHandler(getPluginId(), 
            (fromPluginId, messageType, data, callback) -> {
                Log.d(TAG, "Received message from " + fromPluginId + ": " + messageType);
                
                if (messageType.equals("request_entry")) {
                    // Another plugin is requesting a gratitude entry
                    Bundle response = new Bundle();
                    response.putBoolean("accepted", true);
                    callback.onSuccess(response);
                }
            }
        );
    }
    
    @Override
    public void onPluginDisabled(Context context) {
        super.onPluginDisabled(context);
        Log.d(TAG, "Gratitude plugin disabled");
        
        // Unregister from plugin bridge
        PluginBridge.getInstance(context).unregisterPlugin(getPluginId());
    }
    
    // Helper method to calculate statistics
    private Bundle calculateGratitudeStats(Context context) {
        Bundle stats = new Bundle();
        
        // In a real implementation, this would query the database
        stats.putInt("total_entries", 42);
        stats.putInt("current_streak", 7);
        stats.putInt("longest_streak", 14);
        
        return stats;
    }
    
    /**
     * Custom data collector for gratitude entries
     */
    private static class GratitudeCollector extends BaseDataCollector {
        private static final String TYPE = "gratitude";
        
        public GratitudeCollector(Context context) {
            super();
        }
        
        @Override
        public CollectorResult collect() {
            if (context == null) {
                return CollectorResult.failure(TYPE, "Collector not initialized");
            }
            
            // In a real implementation, this would show a dialog
            // For now, we'll simulate a gratitude entry
            Map<String, Object> data = new HashMap<>();
            data.put("entry", "I'm grateful for the beautiful weather today!");
            data.put("mood", "grateful");
            data.put("timestamp", System.currentTimeMillis());
            
            return collectQuick(data);
        }
        
        @Override
        public String getType() {
            return TYPE;
        }
        
        @Override
        public String getDisplayName() {
            return "Gratitude Journal";
        }
        
        @Override
        public String getDescription() {
            return "Record what you're grateful for each day";
        }
        
        @Override
        public String getEmoji() {
            return "🙏";
        }
        
        @Override
        public String getCategory() {
            return "i"; // Personal
        }
        
        @Override
        public List<String> getRequiredPermissions() {
            return new ArrayList<>(); // No special permissions
        }
        
        @Override
        protected CollectorSettings getDefaultSettings() {
            return new CollectorSettings.Builder()
                .setEnabled(true)
                .setAutomatedCollection(false)
                .setCustomSetting("reminderEnabled", true)
                .setCustomSetting("reminderTime", "20:00")
                .build();
        }
        
        @Override
        public boolean validateData(Map<String, Object> data) {
            if (!super.validateData(data)) {
                return false;
            }
            
            // Check for required fields
            Object entry = data.get("entry");
            return entry != null && !entry.toString().trim().isEmpty();
        }
    }
}
