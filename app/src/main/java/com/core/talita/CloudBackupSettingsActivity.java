package com.core.talita;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.cloud.CloudBackupManager;
import com.core.talita.cloud.BackupConfig;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Cloud Backup Settings Activity
 */
public class CloudBackupSettingsActivity extends AppCompatActivity {
    
    private CloudBackupManager cloudManager;
    private UniversalDataService dataService;
    
    private Switch backupSwitch;
    private Switch wifiOnlySwitch;
    private Switch includeMediaSwitch;
    private TextView statusText;
    private TextView lastBackupText;
    private TextView queueSizeText;
    private Button backupNowButton;
    private Button selectProviderButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_backup_settings);
        
        dataService = new UniversalDataService(this);
        cloudManager = dataService.getCloudBackupManager();
        
        initializeViews();
        updateUI();
    }
    
    private void initializeViews() {
        backupSwitch = findViewById(R.id.backup_switch);
        wifiOnlySwitch = findViewById(R.id.wifi_only_switch);
        includeMediaSwitch = findViewById(R.id.include_media_switch);
        statusText = findViewById(R.id.backup_status_text);
        lastBackupText = findViewById(R.id.last_backup_text);
        queueSizeText = findViewById(R.id.queue_size_text);
        backupNowButton = findViewById(R.id.backup_now_button);
        selectProviderButton = findViewById(R.id.select_provider_button);
        
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Set listeners
        backupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cloudManager.setEnabled(isChecked);
            updateUI();
            
            if (isChecked) {
                Toast.makeText(this, "Cloud backup enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cloud backup disabled", Toast.LENGTH_SHORT).show();
            }
        });
        
        wifiOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBackupConfig();
        });
        
        includeMediaSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBackupConfig();
        });
        
        backupNowButton.setOnClickListener(v -> {
            cloudManager.backupNow();
            Toast.makeText(this, "Backup started...", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        selectProviderButton.setOnClickListener(v -> {
            // TODO: Show provider selection dialog
            Toast.makeText(this, "Provider selection coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateUI() {
        CloudBackupManager.BackupStats stats = cloudManager.getBackupStats();
        
        backupSwitch.setChecked(stats.isEnabled);
        
        if (stats.isEnabled) {
            statusText.setText("✅ Cloud backup is active");
            statusText.setTextColor(getColor(R.color.green));
        } else {
            statusText.setText("❌ Cloud backup is disabled");
            statusText.setTextColor(getColor(R.color.red));
        }
        
        // Update last backup time
        if (stats.lastBackupTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String lastBackup = sdf.format(new Date(stats.lastBackupTime));
            lastBackupText.setText("Last backup: " + lastBackup);
        } else {
            lastBackupText.setText("Last backup: Never");
        }
        
        // Update queue size
        queueSizeText.setText("Items in queue: " + stats.queueSize);
        
        // Enable/disable backup now button
        backupNowButton.setEnabled(stats.isEnabled && stats.hasProvider);
    }
    
    private void updateBackupConfig() {
        BackupConfig config = new BackupConfig(
            wifiOnlySwitch.isChecked(),
            includeMediaSwitch.isChecked(),
            3 // retry count
        );
        
        // TODO: Apply config to CloudBackupManager
        Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
