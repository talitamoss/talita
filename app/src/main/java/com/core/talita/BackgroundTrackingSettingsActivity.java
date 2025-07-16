package com.core.talita;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BackgroundTrackingSettingsActivity extends AppCompatActivity {
    
    private TrackingManager trackingManager;
    private SharedPreferences trackingPrefs;
    
    // UI Components
    private Switch mainTrackingSwitch;
    private Switch locationTrackingSwitch;
    private Switch activityRecognitionSwitch;
    private Switch stepCountingSwitch;
    private TextView trackingIntervalText;
    private TextView batteryOptimizationText;
    private RecyclerView permissionsRecycler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_tracking_settings);
        
        trackingManager = new TrackingManager(this);
        trackingPrefs = getSharedPreferences("background_tracking", MODE_PRIVATE);
        
        initializeViews();
        setupControls();
        updateUI();
    }
    
    private void initializeViews() {
        // Header
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Main Controls
        mainTrackingSwitch = findViewById(R.id.switch_main_tracking);
        locationTrackingSwitch = findViewById(R.id.switch_location_tracking);
        activityRecognitionSwitch = findViewById(R.id.switch_activity_recognition);
        stepCountingSwitch = findViewById(R.id.switch_step_counting);
        
        // Status displays
        trackingIntervalText = findViewById(R.id.tracking_interval_text);
        batteryOptimizationText = findViewById(R.id.battery_optimization_text);
        
        // Permissions list
        permissionsRecycler = findViewById(R.id.permissions_recycler);
        setupPermissionsList();
    }
    
    private void setupControls() {
        // Main tracking toggle
        mainTrackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                trackingManager.startTracking();
            } else {
                trackingManager.stopTracking();
            }
            updateSubSettings(isChecked);
            savePreference("main_tracking_enabled", isChecked);
        });
        
        // Individual feature toggles
        locationTrackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("location_enabled", isChecked);
            if (mainTrackingSwitch.isChecked()) {
                // Restart tracking with new settings
                trackingManager.stopTracking();
                trackingManager.startTracking();
            }
        });
        
        activityRecognitionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("activity_recognition_enabled", isChecked);
        });
        
        stepCountingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("step_counting_enabled", isChecked);
        });
        
        // Tracking interval selector
        findViewById(R.id.tracking_interval_card).setOnClickListener(v -> showTrackingIntervalDialog());
        
        // Battery optimization card
        findViewById(R.id.battery_optimization_card).setOnClickListener(v -> showBatteryOptimizationDialog());
    }
    
    private void updateUI() {
        // Load current states
        boolean mainEnabled = trackingManager.isTrackingEnabled();
        boolean locationEnabled = getPreference("location_enabled", true);
        boolean activityEnabled = getPreference("activity_recognition_enabled", true);
        boolean stepEnabled = getPreference("step_counting_enabled", true);
        
        mainTrackingSwitch.setChecked(mainEnabled);
        locationTrackingSwitch.setChecked(locationEnabled);
        activityRecognitionSwitch.setChecked(activityEnabled);
        stepCountingSwitch.setChecked(stepEnabled);
        
        updateSubSettings(mainEnabled);
        updateTrackingInterval();
        updateBatteryStatus();
    }
    
    private void updateSubSettings(boolean mainEnabled) {
        locationTrackingSwitch.setEnabled(mainEnabled);
        activityRecognitionSwitch.setEnabled(mainEnabled);
        stepCountingSwitch.setEnabled(mainEnabled);
    }
    
    private void setupPermissionsList() {
        List<PermissionItem> permissions = new ArrayList<>();
        permissions.add(new PermissionItem("📍", "Fine Location", "ACCESS_FINE_LOCATION", "Required for precise GPS tracking"));
        permissions.add(new PermissionItem("🏃", "Activity Recognition", "ACTIVITY_RECOGNITION", "Detects walking, driving, etc."));
        permissions.add(new PermissionItem("🔋", "Background App Refresh", "FOREGROUND_SERVICE", "Allows continuous tracking"));
        
        PermissionAdapter adapter = new PermissionAdapter(permissions);
        permissionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        permissionsRecycler.setAdapter(adapter);
    }
    
    private void showTrackingIntervalDialog() {
        String[] intervals = {"⚡ High (30s)", "⚖️ Balanced (2m)", "🔋 Battery Saver (5m)", "🐌 Minimal (10m)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tracking Frequency")
            .setItems(intervals, (dialog, which) -> {
                String[] values = {"30", "120", "300", "600"};
                savePreference("tracking_interval_seconds", Integer.parseInt(values[which]));
                updateTrackingInterval();
            })
            .show();
    }
    
    private void showBatteryOptimizationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage("To ensure continuous tracking, disable battery optimization for Talita in your device settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                // Open battery optimization settings
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateTrackingInterval() {
        int interval = getPreference("tracking_interval_seconds", 120);
        String text = interval < 60 ? interval + " seconds" : (interval/60) + " minutes";
        trackingIntervalText.setText("Updates every " + text);
    }
    
    private void updateBatteryStatus() {
        // Check if battery optimization is disabled
        batteryOptimizationText.setText("Battery optimization: Active");
    }
    
    private void savePreference(String key, boolean value) {
        trackingPrefs.edit().putBoolean(key, value).apply();
    }
    
    private void savePreference(String key, int value) {
        trackingPrefs.edit().putInt(key, value).apply();
    }
    
    private boolean getPreference(String key, boolean defaultValue) {
        return trackingPrefs.getBoolean(key, defaultValue);
    }
    
    private int getPreference(String key, int defaultValue) {
        return trackingPrefs.getInt(key, defaultValue);
    }
    
    // Permission list item
    public static class PermissionItem {
        public final String icon;
        public final String name;
        public final String permission;
        public final String description;
        
        public PermissionItem(String icon, String name, String permission, String description) {
            this.icon = icon;
            this.name = name;
            this.permission = permission;
            this.description = description;
        }
    }
    
    // Permission adapter
    private static class PermissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<PermissionItem> permissions;
        
        public PermissionAdapter(List<PermissionItem> permissions) {
            this.permissions = permissions;
        }
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(android.view.LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false)) {};
        }
        
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind permission data to views
        }
        
        @Override
        public int getItemCount() {
            return permissions.size();
        }
    }
}
