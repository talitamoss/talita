package com.core.talita;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.collectors.WaterCollector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuickAddTestActivity extends AppCompatActivity {
    
    private TextView tvWaterTotal;
    private TextView tvStatus;
    private TextView tvLog;
    private DataCollectorManager collectorManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickadd_test);
        
        // Initialize
        collectorManager = new DataCollectorManager(this);
        WaterCollector.setEnabled(this, true);
        
        // Find views
        tvWaterTotal = findViewById(R.id.tv_water_total);
        tvStatus = findViewById(R.id.tv_status);
        tvLog = findViewById(R.id.tv_log);
        
        // Water buttons
        findViewById(R.id.btn_water_100).setOnClickListener(v -> addWater(100));
        findViewById(R.id.btn_water_250).setOnClickListener(v -> addWater(250));
        findViewById(R.id.btn_water_500).setOnClickListener(v -> addWater(500));
        
        // Test buttons
        findViewById(R.id.btn_clear_data).setOnClickListener(v -> clearData());
        findViewById(R.id.btn_test_all).setOnClickListener(v -> runTests());
        
        updateDisplay();
        log("Quick Add Test initialized");
    }
    
    private void addWater(int amount) {
        log("Adding " + amount + "ml water...");
        
        try {
            // Log the water
            collectorManager.quickLogWater(amount);
            
            // Update display
            updateDisplay();
            
            log("✅ Successfully added " + amount + "ml");
            setStatus("Added " + amount + "ml water");
            
        } catch (Exception e) {
            log("❌ Error: " + e.getMessage());
            setStatus("Error: " + e.getMessage());
        }
    }
    
    private void updateDisplay() {
        int total = WaterCollector.getTodayTotal(this);
        tvWaterTotal.setText("Today's total: " + total + "ml");
    }
    
    private void clearData() {
        WaterCollector.clearTodayData(this);
        updateDisplay();
        log("Cleared today's water data");
        setStatus("Data cleared");
    }
    
    private void runTests() {
        log("=== RUNNING TESTS ===");
        
        // Clear data first
        WaterCollector.clearTodayData(this);
        
        // Test 1: Initial state
        int initial = WaterCollector.getTodayTotal(this);
        log("Test 1 - Initial: " + initial + "ml " + (initial == 0 ? "✅" : "❌"));
        
        // Test 2: Add 100ml
        collectorManager.quickLogWater(100);
        int after100 = WaterCollector.getTodayTotal(this);
        log("Test 2 - After 100ml: " + after100 + "ml " + (after100 == 100 ? "✅" : "❌"));
        
        // Test 3: Add 250ml
        collectorManager.quickLogWater(250);
        int after350 = WaterCollector.getTodayTotal(this);
        log("Test 3 - After +250ml: " + after350 + "ml " + (after350 == 350 ? "✅" : "❌"));
        
        // Test 4: Check data service
        try {
            UniversalDataService ds = new UniversalDataService(this);
            var items = ds.getDecryptedDataByType("water");
            log("Test 4 - Data entries: " + items.size() + " " + (items.size() >= 2 ? "✅" : "❌"));
        } catch (Exception e) {
            log("Test 4 - Data service: ❌ " + e.getMessage());
        }
        
        log("=== TESTS COMPLETE ===");
        updateDisplay();
    }
    
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String currentLog = tvLog.getText().toString();
        tvLog.setText(currentLog + timestamp + " - " + message + "\n");
    }
    
    private void setStatus(String status) {
        tvStatus.setText(status);
    }
}
