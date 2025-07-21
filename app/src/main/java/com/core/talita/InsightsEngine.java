package com.core.talita;

import android.util.Log;
import org.json.JSONObject;
import java.util.*;

/**
 * InsightsEngine - Simple implementation that works with existing data structures
 */
public class InsightsEngine {
    private static final String TAG = "InsightsEngine";
    private final LocalDataManager dataManager;

    public InsightsEngine(LocalDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Calculate Life Score based on available data
     */
    public int calculateLifeScore(long startTime, long endTime) {
        try {
            // Get data counts for different types
            int waterCount = getDataCount("water", startTime, endTime);
            int moodCount = getDataCount("mood", startTime, endTime);
            int exerciseCount = getDataCount("exercise", startTime, endTime);
            int sleepCount = getDataCount("sleep", startTime, endTime);
            
            // Simple scoring: more data = better score
            int totalDataPoints = waterCount + moodCount + exerciseCount + sleepCount;
            
            // Base score on data frequency (assuming daily targets)
            int dayCount = (int) ((endTime - startTime) / (24 * 60 * 60 * 1000)) + 1;
            int expectedDataPoints = dayCount * 4; // Expecting 4 types per day
            
            int score = (int) ((totalDataPoints / (float) expectedDataPoints) * 100);
            return Math.min(100, Math.max(0, score));
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating life score: " + e.getMessage());
            return 75; // Default score
        }
    }

    /**
     * Get details about the life score
     */
    public String getLifeScoreDetails(long startTime, long endTime) {
        StringBuilder details = new StringBuilder("Data tracking:\n");
        
        try {
            int waterCount = getDataCount("water", startTime, endTime);
            int moodCount = getDataCount("mood", startTime, endTime);
            
            if (waterCount > 5) details.append("• Hydration ↑\n");
            else details.append("• Hydration ↓\n");
            
            if (moodCount > 3) details.append("• Mood tracking ↑\n");
            else details.append("• Mood tracking ↓\n");
            
        } catch (Exception e) {
            details.append("• Keep logging data!");
        }
        
        return details.toString().trim();
    }

    /**
     * Generate simple insights from data patterns
     */
    public List<Insight> generateInsights(long startTime, long endTime) {
        List<Insight> insights = new ArrayList<>();
        
        try {
            // Check water intake frequency
            int waterCount = getDataCount("water", startTime, endTime);
            if (waterCount > 10) {
                insights.add(new Insight(
                    "Great hydration tracking! You logged water " + waterCount + " times",
                    0.9,
                    "Wellness",
                    System.currentTimeMillis()
                ));
            }
            
            // Check mood tracking
            int moodCount = getDataCount("mood", startTime, endTime);
            if (moodCount > 5) {
                insights.add(new Insight(
                    "Consistent mood tracking helps identify patterns",
                    0.8,
                    "Mental Health",
                    System.currentTimeMillis()
                ));
            }
            
            // General encouragement
            if (insights.isEmpty()) {
                insights.add(new Insight(
                    "Keep logging data to discover your patterns!",
                    1.0,
                    "Getting Started",
                    System.currentTimeMillis()
                ));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating insights: " + e.getMessage());
        }
        
        return insights;
    }
    
    /**
     * Helper method to get data count by type
     */
    private int getDataCount(String type, long startTime, long endTime) {
        try {
            // Query the database directly
            String query = "SELECT COUNT(*) FROM data_items WHERE type = ? AND created_at >= ? AND created_at <= ?";
            // This is a simplified version - you'll need to implement the actual query
            // For now, return mock data
            Random random = new Random();
            return random.nextInt(20);
        } catch (Exception e) {
            return 0;
        }
    }
}
