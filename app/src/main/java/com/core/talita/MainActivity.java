package com.core.talita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.core.talita.api.CollectorResult;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MainActivity - Simplified for Water Tracking MVP
 * 
 * Focus on getting water tracking working perfectly before adding complexity.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    
    // Core services
    private UniversalDataService dataService;
    private DataCollectorManager collectorManager;
    private PluginManager pluginManager;
    
    // UI elements
    private TextView todayWaterText;
    private TextView lastLogText;
    private Button quickAdd250Button;
    private Button quickAdd500Button;
    private Button quickAdd1000Button;
    private CardView waterCard;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize services
        initializeServices();
        
        // Setup UI
        initializeViews();
        
        // Check permissions
        checkPermissions();
        
        // Load today's water data
        updateWaterDisplay();
    }
    
    private void initializeServices() {
        Log.d(TAG, "Initializing services...");
        
        // Initialize core services
        dataService = UniversalDataService.getInstance(this);
        pluginManager = PluginManager.getInstance(this);
        collectorManager = DataCollectorManager.getInstance(this);
        
        // Start enabled collectors (just water for now)
        collectorManager.startEnabledCollectors();
        
        Log.d(TAG, "Services initialized");
    }
    
    private void initializeViews() {
        // Water tracking card
        waterCard = findViewById(R.id.water_card);
        todayWaterText = findViewById(R.id.today_water_text);
        lastLogText = findViewById(R.id.last_log_text);
        
        // Quick add buttons
        quickAdd250Button = findViewById(R.id.quick_add_250);
        quickAdd500Button = findViewById(R.id.quick_add_500);
        quickAdd1000Button = findViewById(R.id.quick_add_1000);
        
        // Set click listeners
        quickAdd250Button.setOnClickListener(v -> quickLogWater(250));
        quickAdd500Button.setOnClickListener(v -> quickLogWater(500));
        quickAdd1000Button.setOnClickListener(v -> quickLogWater(1000));
        
        // Settings button
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> openSettings());
        
        // View data button
        Button viewDataButton = findViewById(R.id.view_data_button);
        viewDataButton.setOnClickListener(v -> openDataView());
    }
    
    private void quickLogWater(int amountMl) {
        // Get water plugin
        DataCollectorPlugin waterPlugin = pluginManager.getPlugin("core.water");
        if (waterPlugin == null) {
            Toast.makeText(this, "Water tracking not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create water data
        Map<String, Object> waterData = new HashMap<>();
        waterData.put("amount", amountMl);
        waterData.put("unit", "ml");
        
        // Use the collector to save data
        com.core.talita.api.DataCollector collector = waterPlugin.createCollector(this);
        if (collector != null) {
            collector.initialize(this);
            CollectorResult result = collector.collectQuick(waterData);
            
            if (result.isSuccess()) {
                // Success feedback
                Toast.makeText(this, "Added " + amountMl + "ml 💧", Toast.LENGTH_SHORT).show();
                
                // Update display
                updateWaterDisplay();
                
                // Animate the card
                animateWaterCard();
            } else {
                Toast.makeText(this, "Failed to log water", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateWaterDisplay() {
        // Calculate today's total
        long startOfDay = getStartOfDay();
        long now = System.currentTimeMillis();
        
        List<PersonalData> todayWater = dataService.getDataByType("water", startOfDay, now);
        
        int totalMl = 0;
        long lastLogTime = 0;
        
        for (PersonalData data : todayWater) {
            Map<String, Object> waterData = data.getData();
            if (waterData.containsKey("amount")) {
                totalMl += ((Number) waterData.get("amount")).intValue();
            }
            if (data.getTimestamp() > lastLogTime) {
                lastLogTime = data.getTimestamp();
            }
        }
        
        // Update UI
        todayWaterText.setText(String.format(Locale.getDefault(), "%d ml", totalMl));
        
        if (lastLogTime > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            lastLogText.setText("Last log: " + timeFormat.format(new Date(lastLogTime)));
        } else {
            lastLogText.setText("No logs today");
        }
        
        // Update progress color based on daily goal (2000ml)
        if (totalMl >= 2000) {
            waterCard.setCardBackgroundColor(0xFF4CAF50); // Green
        } else if (totalMl >= 1000) {
            waterCard.setCardBackgroundColor(0xFF2196F3); // Blue
        } else {
            waterCard.setCardBackgroundColor(0xFF9E9E9E); // Grey
        }
    }
    
    private long getStartOfDay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private void animateWaterCard() {
        waterCard.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(100)
            .withEndAction(() -> {
                waterCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void openDataView() {
        Intent intent = new Intent(this, DataViewActivity.class);
        startActivity(intent);
    }
    
    private void checkPermissions() {
        // For water tracking, we don't need special permissions
        // This is here for future features like location tracking
        Log.d(TAG, "Permissions check - none needed for water tracking");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh display when returning to activity
        updateWaterDisplay();
    }
}
