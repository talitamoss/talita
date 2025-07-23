package com.example.plugins;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import com.core.talita.PersonalData;
import com.core.talita.TalitaDataType;
import com.core.talita.UniversalDataService;
import com.core.talita.collectors.DataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;
import com.core.talita.plugins.bridge.PluginBridge;
import java.util.HashMap;
import java.util.Map;

/**
 * ExamplePlugin - Demonstrates how to create a plugin for Talita
 * 
 * This example tracks daily gratitude entries and demonstrates:
 * - Data collection
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
        return false; // Manual entry only
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true; // Show in quick add screen
    }
    
    @Override
    public boolean supportsScheduling() {
        return true; // Can be scheduled for reminders
    }
    
    // Create the data collector
    @Override
    public DataCollector createCollector(Context context) {
        return new GratitudeCollector(context);
    }
    
    // Settings
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // Launch settings activity
        // In a real plugin, this would open a custom settings screen
        Log.d(TAG, "Opening gratitude settings");
    }
    
    // Quick add configuration
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Gratitude",
            "What are you grateful for?",
            QuickAddStyle.TEXT_NOTE,
            true // Show in main grid
        );
    }
    
    // Plugin lifecycle
    @Override
    public void onPluginEnabled(Context context) {
        super.onPluginEnabled(context);
        Log.d(TAG, "Gratitude plugin enabled");
        
        // Register with plugin bridge
        PluginBridge bridge = PluginBridge.getInstance(context);
        
        // Listen for mood data from mood tracker
        bridge.addEventListener(getPluginId(), "mood_logged", 
            (sourcePluginId, eventType, data) -> {
                // React to mood changes
                String mood = data.getString("mood", "");
                if (mood.equals("sad") || mood.equals("anxious")) {
                    // Suggest gratitude practice
                    Log.d(TAG, "Mood is " + mood + ", suggesting gratitude practice");
                }
            }
        );
        
        // Register data provider
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
    private static class GratitudeCollector implements DataCollector, TalitaDataType {
        private final Context context;
        private final UniversalDataService dataService;
        
        GratitudeCollector(Context context) {
            this.context = context;
            this.dataService = new UniversalDataService(context);
        }
        
        @Override
        public void collect() {
            // In a real implementation, this would show a dialog
            // For now, we'll create a sample entry
            logGratitude("I'm grateful for the beautiful weather today!");
        }
        
        public void logGratitude(String entry) {
            // Create personal data entry
            PersonalData data = new PersonalData(getDataTypeName());
            
            Map<String, Object> gratitudeData = new HashMap<>();
            gratitudeData.put("entry", entry);
            gratitudeData.put("timestamp", System.currentTimeMillis());
            gratitudeData.put("wordCount", entry.split("\\s+").length);
            
            data.setData(gratitudeData);
            
            // Save using Universal Data Service
            dataService.saveData(data);
            
            // Emit event for other plugins
            PluginBridge bridge = PluginBridge.getInstance(context);
            Bundle eventData = new Bundle();
            eventData.putString("entry", entry);
            eventData.putLong("timestamp", System.currentTimeMillis());
            
            bridge.emitEvent("com.example.gratitude", "gratitude_logged", eventData);
            
            Log.d(TAG, "Logged gratitude: " + entry);
        }
        
        @Override
        public void startCollection(Context context) {
            // Not needed for manual collection
        }
        
        @Override
        public void stopCollection(Context context) {
            // Not needed for manual collection
        }
        
        @Override
        public List<String> getRequiredPermissions() {
            return new ArrayList<>(); // No special permissions
        }
        
        @Override
        public CollectorSettings getSettings() {
            return new CollectorSettings()
                .setFrequency(0) // Manual only
                .setBatteryOptimized(true);
        }
        
        // TalitaDataType implementation
        @Override
        public String getDataTypeName() {
            return "gratitude";
        }
        
        @Override
        public String getDisplayName() {
            return "Daily Gratitude";
        }
        
        @Override
        public String getDescription() {
            return "Track what you're grateful for each day";
        }
        
        @Override
        public Map<String, Object> serializeData(Object data) {
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
            return new HashMap<>();
        }
        
        @Override
        public Object deserializeData(Map<String, Object> data) {
            return data;
        }
        
        @Override
        public String getDataCategory() {
            return "wellness";
        }
        
        @Override
        public boolean requiresEncryption() {
            return true; // Personal thoughts should be encrypted
        }
        
        @Override
        public boolean canExport() {
            return true;
        }
        
        @Override
        public String exportFormat() {
            return "json";
        }
    }
}
