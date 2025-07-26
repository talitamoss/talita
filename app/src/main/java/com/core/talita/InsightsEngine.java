package com.core.talita;

import android.content.Context;
import android.util.Log;
import java.util.*;

/**
 * InsightsEngine - Generates insights from personal data
 * Analyzes patterns and provides meaningful feedback to users
 */
public class InsightsEngine {
    private static final String TAG = "InsightsEngine";
    
    private final Context context;
    private final UniversalDataService dataService;
    
    public InsightsEngine(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
    }
    
    /**
     * Alternative constructor with LocalDataManager (for compatibility)
     */
    public InsightsEngine(LocalDataManager localDataManager) {
        this.context = null;
        this.dataService = null;
        // This constructor is deprecated, use Context constructor
    }
    
    /**
     * Generate insights for a time range
     */
    public List<Insight> generateInsights(long startTime, long endTime) {
        List<Insight> insights = new ArrayList<>();
        
        try {
            // Get data for the time range
            List<PersonalData> data = dataService.getDataForTimeRange(startTime, endTime);
            
            // Analyze different data types
            Map<String, List<PersonalData>> byType = groupByType(data);
            
            // Generate insights for each type
            for (Map.Entry<String, List<PersonalData>> entry : byType.entrySet()) {
                String type = entry.getKey();
                List<PersonalData> typeData = entry.getValue();
                
                // Basic frequency insight
                if (typeData.size() > 0) {
                    insights.add(new Insight(
                        getEmojiForType(type),
                        "You tracked " + type + " " + typeData.size() + " times",
                        "Keep up the consistent tracking!",
                        Insight.Type.FREQUENCY
                    ));
                }
                
                // Type-specific insights
                switch (type) {
                    case "water":
                        insights.addAll(generateWaterInsights(typeData));
                        break;
                    case "exercise":
                        insights.addAll(generateExerciseInsights(typeData));
                        break;
                    case "mood":
                        insights.addAll(generateMoodInsights(typeData));
                        break;
                    case "sleep":
                        insights.addAll(generateSleepInsights(typeData));
                        break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating insights", e);
        }
        
        return insights;
    }
    
    private Map<String, List<PersonalData>> groupByType(List<PersonalData> data) {
        Map<String, List<PersonalData>> grouped = new HashMap<>();
        
        for (PersonalData item : data) {
            String type = item.getType();
            if (!grouped.containsKey(type)) {
                grouped.put(type, new ArrayList<>());
            }
            grouped.get(type).add(item);
        }
        
        return grouped;
    }
    
    private List<Insight> generateWaterInsights(List<PersonalData> waterData) {
        List<Insight> insights = new ArrayList<>();
        
        // Calculate total water intake
        int totalMl = 0;
        for (PersonalData item : waterData) {
            Map<String, Object> data = item.getData();
            if (data.containsKey("volume_ml")) {
                totalMl += (int) data.get("volume_ml");
            }
        }
        
        if (totalMl > 0) {
            insights.add(new Insight(
                "💧",
                "Total water intake: " + (totalMl / 1000.0) + "L",
                totalMl >= 2000 ? "Great hydration!" : "Try to drink more water",
                Insight.Type.ACHIEVEMENT
            ));
        }
        
        return insights;
    }
    
    private List<Insight> generateExerciseInsights(List<PersonalData> exerciseData) {
        List<Insight> insights = new ArrayList<>();
        
        if (exerciseData.size() >= 3) {
            insights.add(new Insight(
                "💪",
                "You exercised " + exerciseData.size() + " times",
                "Consistent exercise routine!",
                Insight.Type.ACHIEVEMENT
            ));
        }
        
        return insights;
    }
    
    private List<Insight> generateMoodInsights(List<PersonalData> moodData) {
        List<Insight> insights = new ArrayList<>();
        
        // Calculate average mood
        int totalRating = 0;
        int count = 0;
        
        for (PersonalData item : moodData) {
            Map<String, Object> data = item.getData();
            if (data.containsKey("mood_rating")) {
                totalRating += (int) data.get("mood_rating");
                count++;
            }
        }
        
        if (count > 0) {
            double avgMood = (double) totalRating / count;
            insights.add(new Insight(
                "😊",
                String.format("Average mood: %.1f/5", avgMood),
                avgMood >= 3.5 ? "You're doing well!" : "Take care of yourself",
                Insight.Type.TREND
            ));
        }
        
        return insights;
    }
    
    private List<Insight> generateSleepInsights(List<PersonalData> sleepData) {
        List<Insight> insights = new ArrayList<>();
        
        // Calculate average sleep
        double totalHours = 0;
        int count = 0;
        
        for (PersonalData item : sleepData) {
            Map<String, Object> data = item.getData();
            if (data.containsKey("hours_slept")) {
                totalHours += (double) data.get("hours_slept");
                count++;
            }
        }
        
        if (count > 0) {
            double avgSleep = totalHours / count;
            insights.add(new Insight(
                "😴",
                String.format("Average sleep: %.1f hours", avgSleep),
                avgSleep >= 7 ? "Good sleep pattern!" : "Try to get more rest",
                Insight.Type.TREND
            ));
        }
        
        return insights;
    }
    
    private String getEmojiForType(String type) {
        switch (type) {
            case "water": return "💧";
            case "exercise": return "💪";
            case "mood": return "😊";
            case "sleep": return "😴";
            case "nutrition": return "🥗";
            case "focus": return "🎯";
            case "relationships": return "💞";
            default: return "📊";
        }
    }
    
    /**
     * Insight data class
     */
    public static class Insight {
        public enum Type {
            FREQUENCY,
            TREND,
            ACHIEVEMENT,
            WARNING,
            SUGGESTION
        }
        
        private final String emoji;
        private final String title;
        private final String description;
        private final Type type;
        private final long timestamp;
        
        public Insight(String emoji, String title, String description, Type type) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getEmoji() { return emoji; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Type getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }
}
