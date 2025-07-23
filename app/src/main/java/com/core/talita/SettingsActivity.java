package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.cloud.CloudBackupManager;
import com.core.talita.plugins.loader.PluginLoader;
import com.core.talita.plugins.repository.PluginRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings Activity - Main settings navigation hub
 * Now includes Plugin Store integration
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    
    // Plugin components
    private PluginRepository pluginRepository;
    private PluginLoader pluginLoader;
    private TextView pluginUpdateBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "🚀 Starting Settings Activity...");
            
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "📱 Layout set successfully");
            
            // Initialize plugin components
            pluginRepository = new PluginRepository(this);
            pluginLoader = new PluginLoader(this);
            
            setupBackButton();
            setupNavigationCards();
            checkForPluginUpdates();
            
            Log.d(TAG, "🎉 Settings Activity initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Fatal error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBackButton() {
        Button backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupNavigationCards() {
        Log.d(TAG, "🔗 Setting up navigation to sub-settings...");
        
        // Background Tracking Settings
        View backgroundTrackingCard = findViewById(R.id.background_tracking_card);
        if (backgroundTrackingCard != null) {
            backgroundTrackingCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, BackgroundTrackingSettingsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ background_tracking_card not found");
        }
        
        // Cloud Backup Settings  
        View cloudBackupCard = findViewById(R.id.cloud_backup_card);
        if (cloudBackupCard != null) {
            cloudBackupCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, CloudBackupSettingsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ cloud_backup_card not found");
        }
        
        // Data Collectors Settings
        View dataCollectorsCard = findViewById(R.id.data_collectors_card);
        if (dataCollectorsCard != null) {
            dataCollectorsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DataCollectorsSettingsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ data_collectors_card not found");
        }
        
        // Plugin Store - NEW!
        View pluginStoreCard = findViewById(R.id.plugin_store_card);
        if (pluginStoreCard != null) {
            pluginStoreCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, PluginManagementActivity.class);
                startActivity(intent);
            });
            
            // Setup update badge
            pluginUpdateBadge = findViewById(R.id.plugin_update_badge);
        } else {
            Log.w(TAG, "⚠️ plugin_store_card not found");
        }
        
        // Data Export Settings
        View dataExportCard = findViewById(R.id.data_export_card);
        if (dataExportCard != null) {
            dataExportCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DataExportSettingsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ data_export_card not found");
        }
        
        // Security & Privacy Settings
        View securityPrivacyCard = findViewById(R.id.security_privacy_card);
        if (securityPrivacyCard != null) {
            securityPrivacyCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, SecurityPrivacySettingsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ security_privacy_card not found");
        }
        
        // About Settings
        View aboutCard = findViewById(R.id.about_card);
        if (aboutCard != null) {
            aboutCard.setOnClickListener(v -> {
                // Create AboutSettingsActivity if it doesn't exist
                Toast.makeText(this, "About: Talita v1.0", Toast.LENGTH_SHORT).show();
                // Uncomment when AboutSettingsActivity is created:
                // Intent intent = new Intent(this, AboutSettingsActivity.class);
                // startActivity(intent);
            });
        } else {
            Log.w(TAG, "⚠️ about_card not found");
        }
        
        Log.d(TAG, "✅ Navigation cards setup complete");
    }
    
    private void checkForPluginUpdates() {
        // Get installed plugins
        List<PluginRepository.InstalledPlugin> installed = new ArrayList<>();
        for (PluginLoader.PluginInfo info : pluginLoader.getLoadedPlugins()) {
            installed.add(new PluginRepository.InstalledPlugin(info.id, info.version));
        }
        
        // Check for updates
        pluginRepository.checkForUpdates(installed, new PluginRepository.UpdatesCallback() {
            @Override
            public void onSuccess(List<PluginRepository.PluginUpdate> updates) {
                runOnUiThread(() -> {
                    if (pluginUpdateBadge != null) {
                        if (updates.size() > 0) {
                            pluginUpdateBadge.setVisibility(View.VISIBLE);
                            pluginUpdateBadge.setText(String.valueOf(updates.size()));
                        } else {
                            pluginUpdateBadge.setVisibility(View.GONE);
                        }
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to check for plugin updates: " + error);
                runOnUiThread(() -> {
                    if (pluginUpdateBadge != null) {
                        pluginUpdateBadge.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update any status indicators if needed
        updateStatusIndicators();
        // Recheck for plugin updates
        checkForPluginUpdates();
    }
    
    private void updateStatusIndicators() {
        // Update status text on cards if they exist
        TextView collectorsStatus = findViewById(R.id.collectors_status_text_card);
        if (collectorsStatus != null) {
            // This would get the actual count from DataCollectorManager
            collectorsStatus.setText("12 active");
        }
        
        // Update plugin count
        TextView pluginCountText = findViewById(R.id.plugin_count_text);
        if (pluginCountText != null) {
            int pluginCount = PluginManager.getInstance(this).getAllPlugins().size();
            pluginCountText.setText(pluginCount + " plugins installed");
        }
        
        // Update other status indicators as needed
    }
}
