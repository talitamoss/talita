package com.core.talita.plugins.i;

import android.content.Context;
import android.graphics.Color;
import com.core.talita.api.*;
import com.core.talita.plugins.base.BaseDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.*;

/**
 * Mood tracking plugin with custom collector
 */
public class MoodPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "core.mood";
    }
    
    @Override
    public String getPluginName() {
        return "Mood Tracker";
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
        return 90; // High priority
    }
    
    @Override
    public String getEmoji() {
        return "😊";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#8B5CF6"); // Purple
    }
    
    @Override
    public int getIconResource() {
        return 0; // Use emoji
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
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
        return true; // For mood check-in reminders
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new MoodCollector();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open mood settings
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Mood",
            "How are you feeling?",
            QuickAddConfig.QuickAddStyle.GRID,
            true
        );
    }
    
    /**
     * Custom mood collector with rating scale
     */
    private static class MoodCollector extends BaseDataCollector {
        private static final String TYPE = "mood";
        private static final String[] MOOD_LABELS = {
            "Terrible", "Bad", "Okay", "Good", "Great"
        };
        private static final String[] MOOD_EMOJIS = {
            "😰", "😕", "😐", "😊", "🤩"
        };
        
        @Override
        public CollectorResult collect() {
            // In a real implementation, this would show a mood selection UI
            // For now, simulate a mood entry
            int rating = 4; // Good mood
            
            Map<String, Object> data = new HashMap<>();
            data.put("rating", rating);
            data.put("mood_label", MOOD_LABELS[rating - 1]);
            data.put("mood_emoji", MOOD_EMOJIS[rating - 1]);
            data.put("timestamp", System.currentTimeMillis());
            
            return collectQuick(data);
        }
        
        @Override
        public String getType() {
            return TYPE;
        }
        
        @Override
        public String getDisplayName() {
            return "Mood Tracker";
        }
        
        @Override
        public String getDescription() {
            return "Track your daily mood and emotions";
        }
        
        @Override
        public String getEmoji() {
            return "😊";
        }
        
        @Override
        public String getCategory() {
            return "i";
        }
        
        @Override
        public List<String> getRequiredPermissions() {
            return new ArrayList<>();
        }
        
        @Override
        protected CollectorSettings getDefaultSettings() {
            return new CollectorSettings.Builder()
                .setEnabled(true)
                .setAutomatedCollection(false)
                .setCustomSetting("reminderEnabled", true)
                .setCustomSetting("reminderTimes", Arrays.asList("09:00", "21:00"))
                .build();
        }
        
        @Override
        public boolean validateData(Map<String, Object> data) {
            if (!super.validateData(data)) {
                return false;
            }
            
            Object rating = data.get("rating");
            if (rating == null) {
                return false;
            }
            
            try {
                int r = Integer.parseInt(rating.toString());
                return r >= 1 && r <= 5;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
