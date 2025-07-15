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

public class SleepCollector implements DataCollector {
    private static final String TAG = "SleepCollector";
    private static final String PREF_KEY = "sleep_collector_enabled";

    @Override
    public String getDataType() { return "sleep"; }

    @Override
    public String getDisplayName() { return "Sleep Tracking"; }

    @Override
    public String getIcon() { return "💤"; }

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
        // Could integrate with phone's do-not-disturb or charging patterns
        Log.d(TAG, "💤 Sleep collector started");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "💤 Sleep collector stopped");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return Arrays.asList(); // Could add notification access for automatic detection
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry primarily
                .setBatteryOptimized(true);
    }

    public static void logSleep(Context context, double hoursSlept, String quality, long bedtime, long wakeTime) {
        UniversalPersonalData sleepData = new UniversalPersonalData("sleep",
                Map.of(
                        "display_name", "💤 Sleep Log",
                        "summary", String.format("%.1f hours (%s quality)", hoursSlept, quality),
                        "hours_slept", hoursSlept,
                        "sleep_quality", quality,
                        "bedtime", bedtime,
                        "wake_time", wakeTime,
                        "sleep_efficiency", calculateSleepEfficiency(bedtime, wakeTime, hoursSlept)
                ));

        UniversalDataService dataService = new UniversalDataService(context);
        dataService.capture(new PersonalDataAdapter(sleepData));

        Log.d(TAG, "💤 Sleep logged: " + hoursSlept + " hours, " + quality + " quality");
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY, enabled).apply();
    }

    private static double calculateSleepEfficiency(long bedtime, long wakeTime, double hoursSlept) {
        if (bedtime == 0 || wakeTime == 0) return 0.0;

        double timeInBed = (wakeTime - bedtime) / (1000.0 * 60 * 60); // Convert to hours
        return timeInBed > 0 ? (hoursSlept / timeInBed) * 100 : 0.0;
    }
}