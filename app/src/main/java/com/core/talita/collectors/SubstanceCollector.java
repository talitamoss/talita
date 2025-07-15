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

public class SubstanceCollector implements DataCollector {
    private static final String TAG = "SubstanceCollector";
    private static final String PREF_KEY = "substance_collector_enabled";

    @Override
    public String getDataType() { return "substance"; }

    @Override
    public String getDisplayName() { return "Substance Tracking"; }

    @Override
    public String getIcon() { return "🚬"; }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEY, false);
    }

    @Override
    public void startCollection(Context context, DataCollectionCallback callback) {
        Log.d(TAG, "🚬 Substance collector started");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "🚬 Substance collector stopped");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return Arrays.asList();
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry only
                .setBatteryOptimized(true);
    }

    public static void logSubstance(Context context, String substanceType, String amount, String notes) {
        UniversalPersonalData substanceData = new UniversalPersonalData("substance",
                Map.of(
                        "display_name", getSubstanceIcon(substanceType) + " " + substanceType,
                        "summary", "Used " + amount + " " + substanceType,
                        "substance_type", substanceType,
                        "amount", amount,
                        "notes", notes != null ? notes : "",
                        "context", "manual_log"
                ));

        UniversalDataService dataService = new UniversalDataService(context);
        dataService.capture(new PersonalDataAdapter(substanceData));

        Log.d(TAG, "🚬 Substance logged: " + substanceType + " - " + amount);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY, enabled).apply();
    }

    private static String getSubstanceIcon(String substanceType) {
        switch (substanceType.toLowerCase()) {
            case "cigarette": case "smoking": return "🚬";
            case "alcohol": case "beer": case "wine": return "🍺";
            case "coffee": case "caffeine": return "☕";
            case "medication": return "💊";
            default: return "🔸";
        }
    }
}
