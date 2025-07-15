package com.core.talita.collectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.DataCollector;                    // ← ADD THIS
import com.core.talita.DataCollectionCallback;           // ← ADD THIS
import com.core.talita.CollectorSettings;                // ← ADD THIS
import com.core.talita.UniversalPersonalData;            // ← ADD THIS
import com.core.talita.UniversalDataService;             // ← ADD THIS
import com.core.talita.PersonalDataAdapter;              // ← ADD THIS

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExerciseCollector implements DataCollector {
    private static final String TAG = "ExerciseCollector";
    private static final String PREF_KEY = "exercise_collector_enabled";

    @Override
    public String getDataType() { return "exercise"; }

    @Override
    public String getDisplayName() { return "Exercise & Fitness"; }

    @Override
    public String getIcon() { return "💪"; }

    @Override
    public boolean isAvailable(Context context) {
        // Exercise tracking is always available
        return true;
    }

    @Override
    public boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEY, false);
    }

    @Override
    public void startCollection(Context context, DataCollectionCallback callback) {
        // Exercise is primarily manual entry, but we could add step counter integration
        Log.d(TAG, "💪 Exercise collector started (manual entry mode)");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "💪 Exercise collector stopped");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return Arrays.asList(); // No special permissions needed for manual entry
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry only
                .setBatteryOptimized(true);
    }

    // Static helper methods for quick logging
    public static void logExercise(Context context, String exerciseType, String duration, String intensity) {
        UniversalPersonalData exerciseData = new UniversalPersonalData("exercise",
                Map.of(
                        "display_name", getExerciseIcon(exerciseType) + " " + exerciseType,
                        "summary", duration + " " + exerciseType + " (" + intensity + ")",
                        "exercise_type", exerciseType,
                        "duration", duration,
                        "intensity", intensity,
                        "calories_estimate", estimateCalories(exerciseType, duration)
                ));

        UniversalDataService dataService = new UniversalDataService(context);
        dataService.capture(new PersonalDataAdapter(exerciseData));

        Log.d(TAG, "💪 Exercise logged: " + exerciseType + " for " + duration);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY, enabled).apply();
    }

    private static String getExerciseIcon(String exerciseType) {
        switch (exerciseType.toLowerCase()) {
            case "running": case "run": return "🏃";
            case "walking": case "walk": return "🚶";
            case "cycling": case "bike": return "🚴";
            case "swimming": case "swim": return "🏊";
            case "weightlifting": case "weights": return "🏋️";
            case "yoga": return "🧘";
            case "dancing": case "dance": return "💃";
            default: return "💪";
        }
    }

    private static int estimateCalories(String exerciseType, String duration) {
        // Simple calorie estimation (this would be more sophisticated in production)
        int minutes = parseDurationMinutes(duration);

        switch (exerciseType.toLowerCase()) {
            case "running": return minutes * 12;
            case "walking": return minutes * 5;
            case "cycling": return minutes * 8;
            case "swimming": return minutes * 10;
            case "weightlifting": return minutes * 6;
            case "yoga": return minutes * 3;
            default: return minutes * 7; // Average
        }
    }

    private static int parseDurationMinutes(String duration) {
        try {
            // Parse "30 min", "1 hour", etc.
            String[] parts = duration.toLowerCase().split(" ");
            if (parts.length >= 1) {
                int value = Integer.parseInt(parts[0]);
                if (duration.contains("hour")) {
                    return value * 60;
                } else {
                    return value; // Assume minutes
                }
            }
        } catch (Exception e) {
            // Fallback to 30 minutes
        }
        return 30;
    }
}