package com.core.talita;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.collectors.WaterCollector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Simple test activity for Quick Add functionality
 * This is optional - you can delete this file if not needed
 */
public class TestQuickAddActivity extends AppCompatActivity {
    
    private static final String TAG = "QuickAddTest";
    
    private TextView testResultsText;
    private StringBuilder testResults;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple UI for test results
        testResultsText = new TextView(this);
        testResultsText.setPadding(20, 20, 20, 20);
        testResultsText.setTextSize(14);
        setContentView(testResultsText);
        
        testResults = new StringBuilder();
        testResults.append("=== QUICK ADD SYSTEM TEST ===\n\n");
        
        runAllTests();
    }
    
    private void runAllTests() {
        // Test 1: Basic initialization
        test1_Initialization();
        
        // Test 2: Water logging
        test2_WaterLogging();
        
        // Test 3: Data retrieval
        test3_DataRetrieval();
        
        // Display results
        testResultsText.setText(testResults.toString());
    }
    
    private void test1_Initialization() {
        addTestHeader("Test 1: Initialization");
        
        try {
            // Test UniversalDataService
            UniversalDataService dataService = new UniversalDataService(this);
            addResult("UniversalDataService", dataService != null);
            
            // Test DataCollectorManager
            DataCollectorManager manager = new DataCollectorManager(this);
            addResult("DataCollectorManager", manager != null);
            
        } catch (Exception e) {
            addResult("Initialization", false);
            logError("Initialization failed", e);
        }
    }
    
    private void test2_WaterLogging() {
        addTestHeader("Test 2: Water Logging");
        
        try {
            DataCollectorManager manager = new DataCollectorManager(this);
            
            // Get initial total
            int initialTotal = WaterCollector.getTodayTotal(this);
            logVerbose("Initial water total: " + initialTotal + "ml");
            
            // Log 250ml
            manager.quickLogWater(250);
            addResult("quickLogWater(250) called", true);
            
            // Check new total
            int newTotal = WaterCollector.getTodayTotal(this);
            boolean increased = newTotal == initialTotal + 250;
            addResult("Water total increased correctly", increased);
            logVerbose("New total: " + newTotal + "ml (expected: " + (initialTotal + 250) + "ml)");
            
        } catch (Exception e) {
            addResult("Water Logging", false);
            logError("Water logging failed", e);
        }
    }
    
    private void test3_DataRetrieval() {
        addTestHeader("Test 3: Data Retrieval");
        
        try {
            UniversalDataService dataService = new UniversalDataService(this);
            
            // Get all water data
            List<UniversalDataService.DecryptedDataItem> waterData = 
                dataService.getDecryptedDataByType("water");
            
            addResult("Retrieved water data", waterData != null);
            addResult("Has water entries", waterData.size() > 0);
            logVerbose("Total water entries: " + waterData.size());
            
        } catch (Exception e) {
            addResult("Data Retrieval", false);
            logError("Data retrieval failed", e);
        }
    }
    
    // Helper methods
    private void addTestHeader(String header) {
        testResults.append("\n--- ").append(header).append(" ---\n");
        Log.d(TAG, "=== " + header + " ===");
    }
    
    private void addResult(String test, boolean passed) {
        String result = passed ? "✅ PASS" : "❌ FAIL";
        testResults.append(result).append(" - ").append(test).append("\n");
        Log.d(TAG, result + " - " + test);
    }
    
    private void logVerbose(String message) {
        testResults.append("   ℹ️ ").append(message).append("\n");
        Log.v(TAG, message);
    }
    
    private void logError(String message, Exception e) {
        testResults.append("   ⚠️ ERROR: ").append(message).append("\n");
        testResults.append("   ").append(e.getMessage()).append("\n");
        Log.e(TAG, message, e);
    }
}
