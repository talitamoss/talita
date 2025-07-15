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

public class MoodCollector implements DataCollector {
    private static final String TAG = "MoodCollector";
    private static final String PREF_KEY = "mood_collector_enabled";

    @Override
    public String getDataType() { return "mood"; }

    @Override
    public String getDisplayName() { return "Mood & Emotions"; }

    @Override
    public String getIcon() { return "😊"; }

    @Override
    public boolean isAvailable(Context context) {
        return true; // Always available
    }

    @Override
    public boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEY, false);
    }

    @Override
    public void startCollection(Context context, DataCollectionCallback callback) {
        // Could add daily mood reminders
        Log.d(TAG, "😊 Mood collector started");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "😊 Mood collector stopped");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return Arrays.asList(); // No permissions needed
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry
                .setBatteryOptimized(true);
    }

    public static void logMood(Context context, int rating, String description, String notes) {
        UniversalPersonalData moodData = new UniversalPersonalData("mood",
                Map.of(
                        "display_name", "Mood Check-in",
                        "summary", "Feeling " + description + " (" + rating + "/5)",
                        "mood_rating", rating,
                        "mood_description", description,
                        "notes", notes != null ? notes : "",
                        "mood_emoji", getMoodEmoji(rating)
                ));

        UniversalDataService dataService = new UniversalDataService(context);
        dataService.capture(new PersonalDataAdapter(moodData));

        Log.d(TAG, "😊 Mood logged: " + rating + "/5 - " + description);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY, enabled).apply();
    }

    private static String getMoodEmoji(int rating) {
        switch (rating) {
            case 1: return "😰";
            case 2: return "😕";
            case 3: return "😐";
            case 4: return "😊";
            case 5: return "🤩";
            default: return "😐";
        }
    }
}