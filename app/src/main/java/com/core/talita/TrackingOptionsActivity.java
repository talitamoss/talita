package com.core.talita;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

/**
 * TrackingOptionsActivity - Users select which data types to track
 * 
 * This creates a personal experience where users only see
 * the quick-add options they actually want to use.
 */
public class TrackingOptionsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "tracking_preferences";
    private static final String TAG = "TrackingOptions";
    
    private RecyclerView recyclerView;
    private TrackingOptionsAdapter adapter;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_options);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        setupHeader();
        setupTrackingOptions();
    }
    
    private void setupHeader() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText("Choose What to Track");
        
        TextView subtitleText = findViewById(R.id.subtitle_text);
        subtitleText.setText("Select items to add to your Quick Add menu");
    }
    
    private void setupTrackingOptions() {
        recyclerView = findViewById(R.id.tracking_options_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        List<TrackingOption> options = getAvailableTrackingOptions();
        adapter = new TrackingOptionsAdapter(options, this::onOptionToggled);
        recyclerView.setAdapter(adapter);
    }
    
    private List<TrackingOption> getAvailableTrackingOptions() {
        List<TrackingOption> options = new ArrayList<>();
        
        // Core options (always visible on quick add)
        options.add(new TrackingOption("water", "💧", "Water", 
            "Track daily hydration", true, false));
        options.add(new TrackingOption("exercise", "🏃", "Exercise", 
            "Log workouts and activity", true, false));
        options.add(new TrackingOption("meal", "🍽️", "Meals", 
            "Record what you eat", true, false));
        
        // Optional tracking types
        options.add(new TrackingOption("coffee", "☕", "Coffee", 
            "Monitor caffeine intake", false, true));
        options.add(new TrackingOption("sleep", "💤", "Sleep", 
            "Track sleep patterns", false, true));
        options.add(new TrackingOption("mood", "😊", "Mood", 
            "Log emotional well-being", false, true));
        options.add(new TrackingOption("location", "📍", "Location", 
            "Automatic location tracking", false, true));
        options.add(new TrackingOption("audio", "🎤", "Audio Notes", 
            "Voice memos and recordings", false, true));
        options.add(new TrackingOption("medication", "💊", "Medication", 
            "Medicine reminders", false, true));
        options.add(new TrackingOption("meditation", "🧘", "Meditation", 
            "Mindfulness sessions", false, true));
        options.add(new TrackingOption("substance", "🚬", "Substances", 
            "Track habits you want to change", false, true));
        
        // Load user preferences
        for (TrackingOption option : options) {
            if (option.isOptional) {
                option.isEnabled = prefs.getBoolean(option.id + "_enabled", false);
            }
        }
        
        return options;
    }
    
    private void onOptionToggled(TrackingOption option, boolean enabled) {
        // Save preference
        prefs.edit()
            .putBoolean(option.id + "_enabled", enabled)
            .apply();
        
        // Initialize data storage for this type if enabling
        if (enabled) {
            initializeDataType(option.id);
        }
        
        Toast.makeText(this, 
            option.name + (enabled ? " added to Quick Add" : " removed"), 
            Toast.LENGTH_SHORT).show();
    }
    
    private void initializeDataType(String dataType) {
        // This ensures the data type has proper storage setup
        // The UniversalDataService will handle creating encrypted storage
        
        android.util.Log.d(TAG, "Ready to track: " + dataType);
    }
    
    /**
     * Data class for tracking options
     */
    public static class TrackingOption {
        public final String id;
        public final String icon;
        public final String name;
        public final String description;
        public final boolean isCore;  // Core options can't be disabled
        public final boolean isOptional;
        public boolean isEnabled;
        
        public TrackingOption(String id, String icon, String name, 
                            String description, boolean isCore, boolean isOptional) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.isCore = isCore;
            this.isOptional = isOptional;
            this.isEnabled = isCore; // Core options start enabled
        }
    }
    
    /**
     * Get list of enabled tracking types for Quick Add
     */
    public static List<String> getEnabledTrackingTypes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<String> enabled = new ArrayList<>();
        
        // Always include core types
        enabled.add("water");
        enabled.add("exercise"); 
        enabled.add("meal");
        
        // Add user-selected optional types
        String[] optionalTypes = {"coffee", "sleep", "mood", "location", 
                                 "audio", "medication", "meditation", "substance"};
        
        for (String type : optionalTypes) {
            if (prefs.getBoolean(type + "_enabled", false)) {
                enabled.add(type);
            }
        }
        
        return enabled;
    }
}
