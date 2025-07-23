package com.core.talita;

import android.content.Context;
import android.util.Log;
import java.util.*;

/**
 * Pattern Analyzer - Discovers meaningful patterns and connections in user data
 * This is where the magic happens - finding the "aha!" moments in data
 */
public class PatternAnalyzer {
    private static final String TAG = "PatternAnalyzer";
    
    private final Context context;
    
    // Pattern detection thresholds
    private static final long TIME_WINDOW_CORRELATION = 2 * 60 * 60 * 1000; // 2 hours
    private static final float MIN_CORRELATION_STRENGTH = 0.3f;
    private static final int MIN_OCCURRENCES = 3;
    
    public PatternAnalyzer(Context context) {
        this.context = context;
    }
    
    /**
     * Analyze data and find patterns
     */
    public List<PatternsActivity.PatternInsight> analyzePatterns(List<PersonalData> data) {
        List<PatternsActivity.PatternInsight> insights = new ArrayList<>();
        
        if (data.size() < 10) {
            insights.add(new PatternsActivity.PatternInsight(
                "Keep tracking!",
                "Add more data to discover patterns",
                0.5f,
                "📊"
            ));
            return insights;
        }
        
        // Run different pattern detection algorithms
        insights.addAll(findCorrelations(data));
        insights.addAll(findTimePatterns(data));
        insights.addAll(findSequencePatterns(data));
        insights.addAll(findAnomalies(data));
        
        // Sort by confidence
        Collections.sort(insights, (a, b) -> Float.compare(b.confidence, a.confidence));
        
        // Keep top insights
        if (insights.size() > 5) {
            insights = insights.subList(0, 5);
        }
        
        return insights;
    }
    
    /**
     * Find correlations between different data types
     */
    private List<PatternsActivity.PatternInsight> findCorrelations(List<PersonalData> data) {
        List<PatternsActivity.PatternInsight> insights = new ArrayList<>();
        
        // Group by type
        Map<String, List<PersonalData>> byType = groupByType(data);
        
        // Check correlations between each pair of types
        for (String type1 : byType.keySet()) {
            for (String type2 : byType.keySet()) {
                if (type1.equals(type2)) continue;
                
                float correlation = calculateCorrelation(
                    byType.get(type1), 
                    byType.get(type2)
                );
                
                if (correlation > MIN_CORRELATION_STRENGTH) {
                    String insight = generateCorrelationInsight(type1, type2, correlation);
                    if (insight != null) {
                        insights.add(new PatternsActivity.PatternInsight(
                            type1 + " ↔ " + type2,
                            insight,
                            correlation,
                            getIconForTypes(type1, type2)
                        ));
                    }
                }
            }
        }
        
        return insights;
    }
    
    /**
     * Find time-based patterns (daily, weekly rhythms)
     */
    private List<PatternsActivity.PatternInsight> findTimePatterns(List<PersonalData> data) {
        List<PatternsActivity.PatternInsight> insights = new ArrayList<>();
        
        // Group by type
        Map<String, List<PersonalData>> byType = groupByType(data);
        
        for (Map.Entry<String, List<PersonalData>> entry : byType.entrySet()) {
            String type = entry.getKey();
            List<PersonalData> items = entry.getValue();
            
            // Check for daily patterns
            Map<Integer, Integer> hourCounts = new HashMap<>();
            for (PersonalData item : items) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(item.getTimestamp());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                hourCounts.put(hour, hourCounts.getOrDefault(hour, 0) + 1);
            }
            
            // Find peak hours
            int maxHour = -1;
            int maxCount = 0;
            for (Map.Entry<Integer, Integer> hourEntry : hourCounts.entrySet()) {
                if (hourEntry.getValue() > maxCount) {
                    maxCount = hourEntry.getValue();
                    maxHour = hourEntry.getKey();
                }
            }
            
            if (maxCount >= MIN_OCCURRENCES) {
                String timeOfDay = getTimeOfDay(maxHour);
                insights.add(new PatternsActivity.PatternInsight(
                    "Peak " + type + " time",
                    "You usually log " + type + " " + timeOfDay,
                    (float) maxCount / items.size(),
                    "⏰"
                ));
            }
            
            // Check for weekly patterns
            Map<Integer, Integer> dayCounts = new HashMap<>();
            for (PersonalData item : items) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(item.getTimestamp());
                int day = cal.get(Calendar.DAY_OF_WEEK);
                dayCounts.put(day, dayCounts.getOrDefault(day, 0) + 1);
            }
            
            // Find patterns in day distribution
            int weekdayCount = 0;
            int weekendCount = 0;
            for (Map.Entry<Integer, Integer> dayEntry : dayCounts.entrySet()) {
                if (dayEntry.getKey() == Calendar.SATURDAY || 
                    dayEntry.getKey() == Calendar.SUNDAY) {
                    weekendCount += dayEntry.getValue();
                } else {
                    weekdayCount += dayEntry.getValue();
                }
            }
            
            if (weekdayCount > weekendCount * 2) {
                insights.add(new PatternsActivity.PatternInsight(
                    "Weekday " + type,
                    type + " happens more on weekdays",
                    0.7f,
                    "📅"
                ));
            } else if (weekendCount > weekdayCount * 2) {
                insights.add(new PatternsActivity.PatternInsight(
                    "Weekend " + type,
                    type + " happens more on weekends",
                    0.7f,
                    "🌴"
                ));
            }
        }
        
        return insights;
    }
    
    /**
     * Find sequence patterns (A often followed by B)
     */
    private List<PatternsActivity.PatternInsight> findSequencePatterns(List<PersonalData> data) {
        List<PatternsActivity.PatternInsight> insights = new ArrayList<>();
        
        // Sort by timestamp
        Collections.sort(data, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        
        // Count sequences
        Map<String, Map<String, Integer>> sequences = new HashMap<>();
        
        for (int i = 0; i < data.size() - 1; i++) {
            PersonalData current = data.get(i);
            PersonalData next = data.get(i + 1);
            
            // Check if they're within time window
            if (next.getTimestamp() - current.getTimestamp() < TIME_WINDOW_CORRELATION) {
                String type1 = current.getDataType();
                String type2 = next.getDataType();
                
                if (!sequences.containsKey(type1)) {
                    sequences.put(type1, new HashMap<>());
                }
                
                Map<String, Integer> followers = sequences.get(type1);
                followers.put(type2, followers.getOrDefault(type2, 0) + 1);
            }
        }
        
        // Find strong sequences
        for (Map.Entry<String, Map<String, Integer>> entry : sequences.entrySet()) {
            String type1 = entry.getKey();
            
            for (Map.Entry<String, Integer> followerEntry : entry.getValue().entrySet()) {
                String type2 = followerEntry.getKey();
                int count = followerEntry.getValue();
                
                if (count >= MIN_OCCURRENCES && !type1.equals(type2)) {
                    insights.add(new PatternsActivity.PatternInsight(
                        type1 + " → " + type2,
                        type1 + " is often followed by " + type2,
                        Math.min(1.0f, count / 10.0f),
                        "🔄"
                    ));
                }
            }
        }
        
        return insights;
    }
    
    /**
     * Find anomalies or unusual patterns
     */
    private List<PatternsActivity.PatternInsight> findAnomalies(List<PersonalData> data) {
        List<PatternsActivity.PatternInsight> insights = new ArrayList<>();
        
        // Group by type and day
        Map<String, Map<String, Integer>> dailyCounts = new HashMap<>();
        
        for (PersonalData item : data) {
            String type = item.getDataType();
            String date = getDateString(item.getTimestamp());
            
            if (!dailyCounts.containsKey(type)) {
                dailyCounts.put(type, new HashMap<>());
            }
            
            Map<String, Integer> counts = dailyCounts.get(type);
            counts.put(date, counts.getOrDefault(date, 0) + 1);
        }
        
        // Find anomalies for each type
        for (Map.Entry<String, Map<String, Integer>> entry : dailyCounts.entrySet()) {
            String type = entry.getKey();
            Map<String, Integer> counts = entry.getValue();
            
            if (counts.size() < 7) continue; // Need at least a week of data
            
            // Calculate average and standard deviation
            double sum = 0;
            for (int count : counts.values()) {
                sum += count;
            }
            double avg = sum / counts.size();
            
            double variance = 0;
            for (int count : counts.values()) {
                variance += Math.pow(count - avg, 2);
            }
            double stdDev = Math.sqrt(variance / counts.size());
            
            // Find outliers
            for (Map.Entry<String, Integer> countEntry : counts.entrySet()) {
                String date = countEntry.getKey();
                int count = countEntry.getValue();
                
                if (count > avg + 2 * stdDev) {
                    insights.add(new PatternsActivity.PatternInsight(
                        "High " + type + " day",
                        "Unusually high " + type + " on " + date,
                        0.8f,
                        "📈"
                    ));
                } else if (count < avg - 2 * stdDev && count == 0) {
                    insights.add(new PatternsActivity.PatternInsight(
                        "Missed " + type,
                        "No " + type + " logged on " + date,
                        0.6f,
                        "📉"
                    ));
                }
            }
        }
        
        return insights;
    }
    
    /**
     * Calculate correlation between two data streams
     */
    private float calculateCorrelation(List<PersonalData> data1, List<PersonalData> data2) {
        int correlations = 0;
        int total = 0;
        
        for (PersonalData item1 : data1) {
            for (PersonalData item2 : data2) {
                long timeDiff = Math.abs(item1.getTimestamp() - item2.getTimestamp());
                
                if (timeDiff < TIME_WINDOW_CORRELATION) {
                    correlations++;
                }
                total++;
            }
        }
        
        if (total == 0) return 0;
        
        // Normalize by the smaller set size
        float normalized = (float) correlations / Math.min(data1.size(), data2.size());
        return Math.min(1.0f, normalized);
    }
    
    /**
     * Generate human-readable correlation insight
     */
    private String generateCorrelationInsight(String type1, String type2, float strength) {
        Map<String, Map<String, String>> insights = new HashMap<>();
        
        // Define known correlations
        Map<String, String> waterInsights = new HashMap<>();
        waterInsights.put("exercise", "Exercise and hydration go hand in hand");
        waterInsights.put("mood", "Good hydration may boost your mood");
        
        Map<String, String> exerciseInsights = new HashMap<>();
        exerciseInsights.put("mood", "Exercise appears to lift your spirits");
        exerciseInsights.put("sleep", "Active days lead to better sleep");
        
        Map<String, String> sleepInsights = new HashMap<>();
        sleepInsights.put("mood", "Sleep quality affects your mood");
        sleepInsights.put("exercise", "Good sleep supports your fitness");
        
        insights.put("water", waterInsights);
        insights.put("exercise", exerciseInsights);
        insights.put("sleep", sleepInsights);
        
        // Check for known insight
        if (insights.containsKey(type1) && insights.get(type1).containsKey(type2)) {
            return insights.get(type1).get(type2);
        }
        if (insights.containsKey(type2) && insights.get(type2).containsKey(type1)) {
            return insights.get(type2).get(type1);
        }
        
        // Generic insight
        return type1 + " and " + type2 + " seem connected";
    }
    
    /**
     * Helper methods
     */
    private Map<String, List<PersonalData>> groupByType(List<PersonalData> data) {
        Map<String, List<PersonalData>> grouped = new HashMap<>();
        
        for (PersonalData item : data) {
            String type = item.getDataType();
            if (!grouped.containsKey(type)) {
                grouped.put(type, new ArrayList<>());
            }
            grouped.get(type).add(item);
        }
        
        return grouped;
    }
    
    private String getTimeOfDay(int hour) {
        if (hour >= 5 && hour < 12) return "in the morning";
        if (hour >= 12 && hour < 17) return "in the afternoon";
        if (hour >= 17 && hour < 21) return "in the evening";
        return "at night";
    }
    
    private String getDateString(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return String.format(Locale.getDefault(), "%d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH));
    }
    
    private String getIconForTypes(String type1, String type2) {
        if ((type1.equals("water") && type2.equals("exercise")) ||
            (type1.equals("exercise") && type2.equals("water"))) {
            return "💪";
        }
        if (type1.contains("mood") || type2.contains("mood")) {
            return "😊";
        }
        if (type1.contains("sleep") || type2.contains("sleep")) {
            return "😴";
        }
        return "🔗";
    }
}
