package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.collectors.*;
import java.util.*;

/**
 * Data Collector Manager - manages all data collectors
 * Works with your existing collector files
 */
public class DataCollectorManager {
    private static final String TAG = "DataCollectorManager";

    private final Context context;
    private final UniversalDataService dataService;
    private final List<DataCollector> collectors;
    private final Map<String, DataCollector> activeCollectors;

    public DataCollectorManager(Context context) {
        this.context = context;
        this.dataService = new UniversalDataService(context);
        this.collectors = new ArrayList<>();
        this.activeCollectors = new HashMap<>();

        registerAllCollectors();

        Log.d(TAG, "📊 Data Collector Manager initialized with " + collectors.size() + " collectors");
    }

    /**
     * Register all your existing collectors
     */
    private void registerAllCollectors() {
        // Register all your existing collectors
        addCollector(new WaterCollector());
        addCollector(new ExerciseCollector());
        addCollector(new MoodCollector());
        addCollector(new NutritionCollector());
        addCollector(new SleepCollector());
        addCollector(new SubstanceCollector());

        Log.d(TAG, "📋 Registered " + collectors.size() + " data collectors");
    }

    /**
     * Add a collector to the system
     */
    public void addCollector(DataCollector collector) {
        collectors.add(collector);
        Log.d(TAG, "➕ Added collector: " + collector.getIcon() + " " + collector.getDisplayName());
    }

    /**
     * Start collecting for all enabled data types
     */
    public void startEnabledCollectors() {
        int startedCount = 0;

        for (DataCollector collector : collectors) {
            if (collector.isEnabled(context) && collector.isAvailable(context)) {
                startCollector(collector);
                startedCount++;
            }
        }

        Log.d(TAG, "🚀 Started " + startedCount + " enabled collectors");
    }

    /**
     * Start a specific collector
     */
    private void startCollector(DataCollector collector) {
        try {
            collector.startCollection(context, new DataCollectionCallback() {
                @Override
                public void onDataCollected(PersonalData data) {
                    // Save through Universal Data Service (automatic encryption)
                    String dataId = dataService.capture(new PersonalDataAdapter(data));

                    if (dataId != null) {
                        Log.d(TAG, "📊 " + collector.getIcon() + " " + data.getDisplaySummary());
                    } else {
                        Log.e(TAG, "❌ Failed to save " + collector.getDisplayName() + " data");
                    }
                }

                @Override
                public void onCollectionError(String error) {
                    Log.e(TAG, "❌ " + collector.getDisplayName() + " error: " + error);
                }
            });

            activeCollectors.put(collector.getDataType(), collector);
            Log.d(TAG, "✅ Started collector: " + collector.getDisplayName());

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start " + collector.getDisplayName() + ": " + e.getMessage());
        }
    }

    /**
     * Stop a specific collector
     */
    public void stopCollector(String dataType) {
        DataCollector collector = activeCollectors.get(dataType);
        if (collector != null) {
            collector.stopCollection(context);
            activeCollectors.remove(dataType);
            Log.d(TAG, "🛑 Stopped collector: " + collector.getDisplayName());
        }
    }

    /**
     * Stop all collectors
     */
    public void stopAllCollectors() {
        for (DataCollector collector : activeCollectors.values()) {
            collector.stopCollection(context);
        }
        activeCollectors.clear();
        Log.d(TAG, "🛑 Stopped all collectors");
    }

    /**
     * Get all available collectors for settings UI
     */
    public List<DataCollector> getAllCollectors() {
        return new ArrayList<>(collectors);
    }

    /**
     * Get collectors organized by category for settings UI
     */
    public Map<String, List<DataCollector>> getCollectorsByCategory() {
        Map<String, List<DataCollector>> categories = new LinkedHashMap<>();

        for (DataCollector collector : collectors) {
            String category = getCollectorCategory(collector);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(collector);
        }

        return categories;
    }

    /**
     * Categorize collectors for organized UI
     */
    private String getCollectorCategory(DataCollector collector) {
        String type = collector.getDataType();

        switch (type) {
            case "location":
            case "steps":
            case "activity":
            case "exercise":
                return "Movement & Fitness";

            case "water":
            case "nutrition":
            case "sleep":
            case "mood":
                return "Lifestyle & Wellness";

            case "substance":
            case "alcohol":
            case "medication":
                return "Substances & Habits";

            case "menstrual":
            case "heartrate":
            case "weight":
                return "Health & Biometrics";

            case "screentime":
            case "appusage":
                return "Digital Wellness";

            default:
                return "Other";
        }
    }

    /**
     * Get collector by data type
     */
    public DataCollector getCollector(String dataType) {
        for (DataCollector collector : collectors) {
            if (collector.getDataType().equals(dataType)) {
                return collector;
            }
        }
        return null;
    }

    /**
     * Check if a collector is currently active
     */
    public boolean isCollectorActive(String dataType) {
        return activeCollectors.containsKey(dataType);
    }

    /**
     * Get statistics about current collection
     */
    public CollectionStats getCollectionStats() {
        int enabledCount = 0;
        int availableCount = 0;

        for (DataCollector collector : collectors) {
            if (collector.isAvailable(context)) {
                availableCount++;
                if (collector.isEnabled(context)) {
                    enabledCount++;
                }
            }
        }

        return new CollectionStats(
                collectors.size(),
                availableCount,
                enabledCount,
                activeCollectors.size(),
                getCollectorsByCategory()
        );
    }

    /**
     * Enable/disable a specific collector
     */
    public void setCollectorEnabled(String dataType, boolean enabled) {
        DataCollector collector = getCollector(dataType);
        if (collector != null) {
            // Store the setting
            context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(dataType + "_enabled", enabled)
                    .apply();

            if (enabled && collector.isAvailable(context)) {
                startCollector(collector);
            } else {
                stopCollector(dataType);
            }

            Log.d(TAG, "⚙️ " + collector.getDisplayName() + " " + (enabled ? "enabled" : "disabled"));
        }
    }

    /**
     * Quick logging methods for easy data entry
     */
    public void quickLogWater(int amountMl) {
        WaterCollector.logWater(context, amountMl);
    }

    public void quickLogExercise(String type, String duration) {
        ExerciseCollector.logExercise(context, type, duration, "moderate");
    }

    public void quickLogMood(int rating, String description) {
        MoodCollector.logMood(context, rating, description, "");
    }

    public void quickLogSleep(double hours, String quality) {
        long now = System.currentTimeMillis();
        long bedtime = now - (long)(hours * 60 * 60 * 1000); // Estimate bedtime
        SleepCollector.logSleep(context, hours, quality, bedtime, now);
    }

    public void quickLogMeal(String mealType, String description) {
        NutritionCollector.logMeal(context, mealType, description, "");
    }

    public void quickLogSubstance(String type, String amount) {
        SubstanceCollector.logSubstance(context, type, amount, "");
    }

    /**
     * Statistics about collection system
     */
    public static class CollectionStats {
        public final int totalCollectors;
        public final int availableCollectors;
        public final int enabledCollectors;
        public final int activeCollectors;
        public final Map<String, List<DataCollector>> collectorsByCategory;

        public CollectionStats(int totalCollectors, int availableCollectors,
                               int enabledCollectors, int activeCollectors,
                               Map<String, List<DataCollector>> collectorsByCategory) {
            this.totalCollectors = totalCollectors;
            this.availableCollectors = availableCollectors;
            this.enabledCollectors = enabledCollectors;
            this.activeCollectors = activeCollectors;
            this.collectorsByCategory = collectorsByCategory;
        }

        public String getSummary() {
            return activeCollectors + "/" + enabledCollectors + " collectors active";
        }
    }
}