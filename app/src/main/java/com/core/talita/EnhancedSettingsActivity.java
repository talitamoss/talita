package com.core.talita;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Enhanced Settings Activity - Advanced configuration options
 */
public class EnhancedSettingsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "EnhancedSettings";
    private SharedPreferences prefs;
    
    // UI Components
    private SwitchMaterial developerModeSwitch;
    private SwitchMaterial debugLoggingSwitch;
    private SwitchMaterial performanceMonitorSwitch;
    private SwitchMaterial experimentalFeaturesSwitch;
    private CardView clearCacheCard;
    private CardView resetSettingsCard;
    private CardView diagnosticsCard;
    private CardView advancedEncryptionCard;
    private TextView cacheInfoText;
    private TextView appVersionText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_settings);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        setupViews();
        loadSettings();
    }
    
    private void setupViews() {
        // Back button
        Button backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        // Developer options
        developerModeSwitch = findViewById(R.id.developer_mode_switch);
        debugLoggingSwitch = findViewById(R.id.debug_logging_switch);
        performanceMonitorSwitch = findViewById(R.id.performance_monitor_switch);
        experimentalFeaturesSwitch = findViewById(R.id.experimental_features_switch);
        
        // Action cards
        clearCacheCard = findViewById(R.id.clear_cache_card);
        resetSettingsCard = findViewById(R.id.reset_settings_card);
        diagnosticsCard = findViewById(R.id.diagnostics_card);
        advancedEncryptionCard = findViewById(R.id.advanced_encryption_card);
        
        // Info texts
        cacheInfoText = findViewById(R.id.cache_info_text);
        appVersionText = findViewById(R.id.app_version_text);
        
        // Set version info
        if (appVersionText != null) {
            appVersionText.setText("Version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        }
        
        // Set listeners
        if (developerModeSwitch != null) {
            developerModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("developer_mode", isChecked).apply();
                updateDeveloperOptions(isChecked);
                Toast.makeText(this, isChecked ? "Developer mode enabled" : "Developer mode disabled", 
                    Toast.LENGTH_SHORT).show();
            });
        }
        
        if (debugLoggingSwitch != null) {
            debugLoggingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("debug_logging", isChecked).apply();
                // Debug logging is handled per-instance in UniversalDataService
                Toast.makeText(this, isChecked ? "Debug logging enabled" : "Debug logging disabled", 
                    Toast.LENGTH_SHORT).show();
            });
        }
        
        if (performanceMonitorSwitch != null) {
            performanceMonitorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("performance_monitor", isChecked).apply();
            });
        }
        
        if (experimentalFeaturesSwitch != null) {
            experimentalFeaturesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("experimental_features", isChecked).apply();
                if (isChecked) {
                    showExperimentalWarning();
                }
            });
        }
        
        if (clearCacheCard != null) {
            clearCacheCard.setOnClickListener(v -> clearCache());
        }
        
        if (resetSettingsCard != null) {
            resetSettingsCard.setOnClickListener(v -> resetAllSettings());
        }
        
        if (diagnosticsCard != null) {
            diagnosticsCard.setOnClickListener(v -> runDiagnostics());
        }
        
        if (advancedEncryptionCard != null) {
            advancedEncryptionCard.setOnClickListener(v -> openAdvancedEncryption());
        }
        
        updateCacheInfo();
    }
    
    private void loadSettings() {
        boolean developerMode = prefs.getBoolean("developer_mode", false);
        boolean debugLogging = prefs.getBoolean("debug_logging", false);
        boolean performanceMonitor = prefs.getBoolean("performance_monitor", false);
        boolean experimentalFeatures = prefs.getBoolean("experimental_features", false);
        
        if (developerModeSwitch != null) {
            developerModeSwitch.setChecked(developerMode);
        }
        if (debugLoggingSwitch != null) {
            debugLoggingSwitch.setChecked(debugLogging);
        }
        if (performanceMonitorSwitch != null) {
            performanceMonitorSwitch.setChecked(performanceMonitor);
        }
        if (experimentalFeaturesSwitch != null) {
            experimentalFeaturesSwitch.setChecked(experimentalFeatures);
        }
        
        updateDeveloperOptions(developerMode);
    }
    
    private void updateDeveloperOptions(boolean enabled) {
        // Show/hide developer options based on state
        View developerSection = findViewById(R.id.developer_options_section);
        if (developerSection != null) {
            developerSection.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
        
        // Enable/disable switches
        if (debugLoggingSwitch != null) {
            debugLoggingSwitch.setEnabled(enabled);
        }
        if (performanceMonitorSwitch != null) {
            performanceMonitorSwitch.setEnabled(enabled);
        }
        if (diagnosticsCard != null) {
            diagnosticsCard.setEnabled(enabled);
            diagnosticsCard.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
    
    private void updateCacheInfo() {
        try {
            // Calculate cache size
            long cacheSize = 0;
            java.io.File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                cacheSize = calculateDirectorySize(cacheDir);
            }
            
            // Format size
            String sizeText = formatFileSize(cacheSize);
            if (cacheInfoText != null) {
                cacheInfoText.setText("Cache size: " + sizeText);
            }
        } catch (Exception e) {
            if (cacheInfoText != null) {
                cacheInfoText.setText("Cache size: Unknown");
            }
        }
    }
    
    private long calculateDirectorySize(java.io.File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += calculateDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }
    
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    private void clearCache() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will delete all temporary files. Are you sure?")
            .setPositiveButton("Clear", (dialog, which) -> {
                try {
                    // Clear cache directory
                    java.io.File cacheDir = getCacheDir();
                    if (cacheDir != null && cacheDir.exists()) {
                        deleteRecursive(cacheDir);
                    }
                    
                    updateCacheInfo();
                    Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteRecursive(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] files = file.listFiles();
            if (files != null) {
                for (java.io.File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
    
    private void resetAllSettings() {
        new AlertDialog.Builder(this)
            .setTitle("Reset All Settings")
            .setMessage("This will reset all app settings to defaults. Your data will not be affected. Continue?")
            .setPositiveButton("Reset", (dialog, which) -> {
                // Clear all preferences
                getSharedPreferences("AppSettings", MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                
                // Reload defaults
                loadSettings();
                
                Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void runDiagnostics() {
        // Simple diagnostics implementation
        StringBuilder report = new StringBuilder();
        report.append("Diagnostics Report\n\n");
        
        // Check encryption
        try {
            SecureKeyManager keyManager = new SecureKeyManager(this);
            report.append("✅ Encryption: Available\n");
        } catch (Exception e) {
            report.append("❌ Encryption: " + e.getMessage() + "\n");
        }
        
        // Check storage
        try {
            LocalDataManager dataManager = new LocalDataManager(this);
            long count = dataManager.getDataCount();
            report.append("✅ Storage: " + count + " records\n");
        } catch (Exception e) {
            report.append("❌ Storage: " + e.getMessage() + "\n");
        }
        
        // Check permissions
        report.append("\nPermissions:\n");
        checkPermission(report, android.Manifest.permission.ACCESS_FINE_LOCATION, "Location");
        checkPermission(report, android.Manifest.permission.RECORD_AUDIO, "Audio");
        checkPermission(report, android.Manifest.permission.CAMERA, "Camera");
        checkPermission(report, android.Manifest.permission.ACTIVITY_RECOGNITION, "Activity");
        
        // Show results
        new AlertDialog.Builder(this)
            .setTitle("Diagnostics")
            .setMessage(report.toString())
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void checkPermission(StringBuilder report, String permission, String name) {
        boolean granted = checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        report.append(granted ? "✅ " : "❌ ").append(name).append("\n");
    }
    
    private void showExperimentalWarning() {
        new AlertDialog.Builder(this)
            .setTitle("Experimental Features")
            .setMessage("Experimental features may be unstable and could cause data loss. Use at your own risk.")
            .setPositiveButton("I Understand", null)
            .show();
    }
    
    private void openAdvancedEncryption() {
        // TODO: Open advanced encryption settings
        Toast.makeText(this, "Advanced encryption settings coming soon", Toast.LENGTH_SHORT).show();
    }
}
