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

public class NutritionCollector implements DataCollector {
    private static final String TAG = "NutritionCollector";
    private static final String PREF_KEY = "nutrition_collector_enabled";

    @Override
    public String getDataType() { return "nutrition"; }

    @Override
    public String getDisplayName() { return "Food & Nutrition"; }

    @Override
    public String getIcon() { return "🍽️"; }

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
        Log.d(TAG, "🍽️ Nutrition collector started");
    }

    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "🍽️ Nutrition collector stopped");
    }

    @Override
    public List<String> getRequiredPermissions() {
        return Arrays.asList(); // Could add camera for food photos
    }

    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry
                .setBatteryOptimized(true);
    }

    public static void logMeal(Context context, String mealType, String description, String notes) {
        UniversalPersonalData nutritionData = new UniversalPersonalData("nutrition",
                Map.of(
                        "display_name", getMealIcon(mealType) + " " + mealType,
                        "summary", description,
                        "meal_type", mealType,
                        "description", description,
                        "notes", notes != null ? notes : "",
                        "logged_method", "manual"
                ));

        UniversalDataService dataService = new UniversalDataService(context);
        dataService.capture(new PersonalDataAdapter(nutritionData));

        Log.d(TAG, "🍽️ Meal logged: " + mealType + " - " + description);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("personal_data_collectors", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY, enabled).apply();
    }

    private static String getMealIcon(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast": return "🌅";
            case "lunch": return "☀️";
            case "dinner": return "🌙";
            case "snack": return "🍿";
            default: return "🍽️";
        }
    }
}