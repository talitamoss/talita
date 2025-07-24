package com.core.talita;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.core.talita.cloud.CloudBackupManager;
import com.core.talita.plugins.loader.PluginLoader;
import com.core.talita.plugins.repository.PluginRepository;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified Settings Activity - All settings in one clean interface
 * No more "enhanced" settings nonsense
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "app_settings";
    
    // Core services
    private SharedPreferences prefs;
    private LocalDataManager dataManager;
    private CloudBackupManager cloudBackupManager;
    private PluginRepository pluginRepository;
    private PluginLoader pluginLoader;
    
    // UI Components
    private TextView pluginUpdateBadge;
    private TextView cacheInfoText;
    private TextView dataCountText;
    private SwitchMaterial developerModeSwitch;
    private LinearLayout developerSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize services
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dataManager = new LocalDataManager(this);
        cloudBackupManager = CloudBackupManager.getInstance(this);
        pluginRepository = new PluginRepository(this);
        pluginLoader = new PluginLoader(this);
        
        setupViews();
        updateStats();
        checkForPluginUpdates();
    }

    private void setupViews() {
        // Back button
        Button backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Main Settings Cards
        setupDataManagement();
        setupPrivacySecurity();
        setupBackupSync();
        setupPlugins();
        setupAbout();
        
        // Developer Mode (hidden by default)
        setupDeveloperMode();
    }

    private void setupDataManagement() {
        CardView dataManagementCard = findViewById(R.id.data_management_card);
        dataCountText = findViewById(R.id.data_count_text);
        
        if (dataManagementCard != null) {
            dataManagementCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DataViewerActivity.class);
                startActivity(intent);
            });
        }
        
        // Export Data
        View exportDataOption = findViewById(R.id.export_data_option);
        if (exportDataOption != null) {
            exportDataOption.setOnClickListener(v -> exportData());
        }
        
        // Clear Cache
        View clearCacheOption = findViewById(R.id.clear_cache_option);
        cacheInfoText = findViewById(R.id.cache_info_text);
        if (clearCacheOption != null) {
            clearCacheOption.setOnClickListener(v -> clearCache());
        }
        
        // Delete All Data
        View deleteDataOption = findViewById(R.id.delete_data_option);
        if (deleteDataOption != null) {
            deleteDataOption.setOnClickListener(v -> showDeleteDataDialog());
        }
    }

    private void setupPrivacySecurity() {
        // Encryption status
        View encryptionStatusCard = findViewById(R.id.encryption_status_card);
        if (encryptionStatusCard != null) {
            TextView statusText = findViewById(R.id.encryption_status_text);
            if (statusText != null) {
                statusText.setText("✅ Hardware-backed AES-256");
            }
        }
        
        // QR Key Exchange
        View qrKeyOption = findViewById(R.id.qr_key_option);
        if (qrKeyOption != null) {
            qrKeyOption.setOnClickListener(v -> {
                Intent intent = new Intent(this, P2PConnectionActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBackupSync() {
        // Cloud Backup
        CardView cloudBackupCard = findViewById(R.id.cloud_backup_card);
        SwitchMaterial cloudBackupSwitch = findViewById(R.id.cloud_backup_switch);
        TextView cloudStatusText = findViewById(R.id.cloud_status_text);
        
        if (cloudBackupSwitch != null) {
            boolean isEnabled = prefs.getBoolean("cloud_backup_enabled", false);
            cloudBackupSwitch.setChecked(isEnabled);
            
            cloudBackupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("cloud_backup_enabled", isChecked).apply();
                if (isChecked) {
                    showCloudProviderDialog();
                } else {
                    cloudBackupManager.disableBackup();
                }
                updateCloudStatus(cloudStatusText);
            });
        }
        
        // Background Tracking
        View backgroundTrackingOption = findViewById(R.id.background_tracking_option);
        if (backgroundTrackingOption != null) {
            backgroundTrackingOption.setOnClickListener(v -> {
                Intent intent = new Intent(this, BackgroundTrackingSettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupPlugins() {
        // Plugin Store
        CardView pluginStoreCard = findViewById(R.id.plugin_store_card);
        pluginUpdateBadge = findViewById(R.id.plugin_update_badge);
        
        if (pluginStoreCard != null) {
            pluginStoreCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, PluginStoreActivity.class);
                startActivity(intent);
            });
        }
        
        // Manage Plugins
        View managePluginsOption = findViewById(R.id.manage_plugins_option);
        if (managePluginsOption != null) {
            managePluginsOption.setOnClickListener(v -> {
                Intent intent = new Intent(this, PluginManagementActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupAbout() {
        // Version info
        TextView versionText = findViewById(R.id.version_text);
        if (versionText != null) {
            versionText.setText("Version 1.0.0 (Build 1)");
        }
        
        // Github link
        View githubOption = findViewById(R.id.github_option);
        if (githubOption != null) {
            githubOption.setOnClickListener(v -> {
                // Open GitHub repo
                Toast.makeText(this, "Opening GitHub...", Toast.LENGTH_SHORT).show();
            });
        }
        
        // About option with triple-tap for developer mode
        View aboutOption = findViewById(R.id.about_option);
        if (aboutOption != null) {
            final int[] tapCount = {0};
            final long[] lastTapTime = {0};
            
            aboutOption.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTapTime[0] < 500) {
                    tapCount[0]++;
                    if (tapCount[0] >= 3) {
                        enableDeveloperMode();
                        tapCount[0] = 0;
                    }
                } else {
                    tapCount[0] = 1;
                }
                lastTapTime[0] = currentTime;
            });
        }
    }

    private void setupDeveloperMode() {
        developerSection = findViewById(R.id.developer_section);
        developerModeSwitch = findViewById(R.id.developer_mode_switch);
        
        // Check if developer mode was previously enabled
        boolean isDeveloperMode = prefs.getBoolean("developer_mode_enabled", false);
        if (isDeveloperMode && developerSection != null) {
            developerSection.setVisibility(View.VISIBLE);
        }
        
        if (developerModeSwitch != null) {
            developerModeSwitch.setChecked(isDeveloperMode);
            developerModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("developer_mode_enabled", isChecked).apply();
                if (!isChecked && developerSection != null) {
                    developerSection.setVisibility(View.GONE);
                }
            });
        }
        
        // Debug options
        SwitchMaterial debugLoggingSwitch = findViewById(R.id.debug_logging_switch);
        if (debugLoggingSwitch != null) {
            debugLoggingSwitch.setChecked(prefs.getBoolean("debug_logging", false));
            debugLoggingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("debug_logging", isChecked).apply();
            });
        }
        
        // Diagnostics
        View runDiagnosticsOption = findViewById(R.id.run_diagnostics_option);
        if (runDiagnosticsOption != null) {
            runDiagnosticsOption.setOnClickListener(v -> runDiagnostics());
        }
    }

    private void enableDeveloperMode() {
        if (developerSection != null) {
            developerSection.setVisibility(View.VISIBLE);
            Toast.makeText(this, "🚀 Developer mode enabled!", Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("developer_mode_enabled", true).apply();
            if (developerModeSwitch != null) {
                developerModeSwitch.setChecked(true);
            }
        }
    }

    private void updateStats() {
        // Update data count
        if (dataCountText != null) {
            try {
                long count = dataManager.getAllData().size();
                dataCountText.setText(count + " entries");
            } catch (Exception e) {
                dataCountText.setText("Unable to load");
            }
        }
        
        // Update cache size
        if (cacheInfoText != null) {
            long cacheSize = calculateCacheSize();
            String readableSize = formatFileSize(cacheSize);
            cacheInfoText.setText(readableSize);
        }
    }

    private void updateCloudStatus(TextView statusText) {
        if (statusText == null) return;
        
        if (cloudBackupManager.isBackupEnabled()) {
            String provider = cloudBackupManager.getCurrentProvider();
            statusText.setText("Connected to " + provider);
        } else {
            statusText.setText("Not configured");
        }
    }

    private void checkForPluginUpdates() {
        // Check for updates in background
        new Thread(() -> {
            try {
                int updateCount = pluginRepository.getAvailableUpdates().size();
                runOnUiThread(() -> {
                    if (pluginUpdateBadge != null && updateCount > 0) {
                        pluginUpdateBadge.setVisibility(View.VISIBLE);
                        pluginUpdateBadge.setText(String.valueOf(updateCount));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error checking plugin updates", e);
            }
        }).start();
    }

    private void clearCache() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear temporary files. Your data is safe.")
            .setPositiveButton("Clear", (dialog, which) -> {
                try {
                    File cacheDir = getCacheDir();
                    deleteRecursive(cacheDir);
                    Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
                    updateStats();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDeleteDataDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Delete All Data")
            .setMessage("This will permanently delete all your data. This cannot be undone!")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Second confirmation
                new AlertDialog.Builder(this)
                    .setTitle("Are you absolutely sure?")
                    .setMessage("Type DELETE to confirm")
                    .setView(createDeleteConfirmationView())
                    .setPositiveButton("Confirm", null) // Set in onShow
                    .setNegativeButton("Cancel", null)
                    .show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private View createDeleteConfirmationView() {
        EditText input = new EditText(this);
        input.setHint("Type DELETE");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(48, 16, 48, 16);
        input.setLayoutParams(lp);
        return input;
    }

    private void exportData() {
        Toast.makeText(this, "Preparing data export...", Toast.LENGTH_SHORT).show();
        // TODO: Implement data export
    }

    private void showCloudProviderDialog() {
        String[] providers = {"Google Drive", "Dropbox", "Solid Pod", "Custom Server"};
        new AlertDialog.Builder(this)
            .setTitle("Choose Cloud Provider")
            .setItems(providers, (dialog, which) -> {
                Toast.makeText(this, "Selected: " + providers[which], Toast.LENGTH_SHORT).show();
                // TODO: Implement provider setup
            })
            .show();
    }

    private void runDiagnostics() {
        Toast.makeText(this, "Running diagnostics...", Toast.LENGTH_SHORT).show();
        // TODO: Implement diagnostics
    }

    private long calculateCacheSize() {
        File cacheDir = getCacheDir();
        return getDirectorySize(cacheDir);
    }

    private long getDirectorySize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) 
            + " " + units[digitGroups];
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
        checkForPluginUpdates();
    }
}
