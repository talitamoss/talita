package com.core.talita;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.cloud.CloudBackupManager;
import java.util.ArrayList;
import java.util.List;

public class CloudBackupSettingsActivity extends AppCompatActivity {
    
    private CloudBackupManager cloudManager;
    private UniversalDataService dataService;
    
    // UI Components
    private Switch backupEnabledSwitch;
    private Switch autoBackupSwitch;
    private Switch wifiOnlySwitch;
    private Switch chargingOnlySwitch;
    private TextView backupStatusText;
    private TextView lastBackupText;
    private TextView pendingBackupsText;
    private RecyclerView providersRecycler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_backup_settings);
        
        dataService = new UniversalDataService(this);
        cloudManager = dataService.getCloudBackupManager();
        
        initializeViews();
        setupControls();
        setupProvidersList();
        updateUI();
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Switches
        backupEnabledSwitch = findViewById(R.id.switch_backup_enabled);
        autoBackupSwitch = findViewById(R.id.switch_auto_backup);
        wifiOnlySwitch = findViewById(R.id.switch_wifi_only);
        chargingOnlySwitch = findViewById(R.id.switch_charging_only);
        
        // Status displays
        backupStatusText = findViewById(R.id.backup_status_text);
        lastBackupText = findViewById(R.id.last_backup_text);
        pendingBackupsText = findViewById(R.id.pending_backups_text);
        
        // Lists
        providersRecycler = findViewById(R.id.providers_recycler);
        
        // Action buttons
        findViewById(R.id.manual_backup_button).setOnClickListener(v -> triggerManualBackup());
        findViewById(R.id.test_connection_button).setOnClickListener(v -> testCloudConnection());
        findViewById(R.id.backup_history_button).setOnClickListener(v -> showBackupHistory());
    }
    
    private void setupControls() {
        backupEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cloudManager.setBackupEnabled(isChecked);
            updateSubSettings(isChecked);
            updateUI();
        });
        
        autoBackupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cloudManager.setAutoBackupEnabled(isChecked);
        });
        
        wifiOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBackupConfig();
        });
        
        chargingOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBackupConfig();
        });
    }
    
    private void setupProvidersList() {
        List<CloudProviderOption> providers = new ArrayList<>();
        providers.add(new CloudProviderOption("☁️", "Google Drive", "google_drive", "Store encrypted data in Google Drive"));
        providers.add(new CloudProviderOption("📦", "Dropbox", "dropbox", "Store encrypted data in Dropbox"));
        providers.add(new CloudProviderOption("🔒", "Greenhost", "greenhost", "Privacy-focused European hosting"));
        providers.add(new CloudProviderOption("🌐", "Solid Pod", "solid", "Decentralized web storage"));
        providers.add(new CloudProviderOption("⚡", "Custom WebDAV", "webdav", "Your own cloud server"));
        
        CloudProviderAdapter adapter = new CloudProviderAdapter(providers, this::onProviderSelected);
        providersRecycler.setLayoutManager(new LinearLayoutManager(this));
        providersRecycler.setAdapter(adapter);
    }
    
    private void updateUI() {
        // Update switch states and status text
        backupStatusText.setText("🔒 All backups are encrypted");
        lastBackupText.setText("Last backup: Never");
        pendingBackupsText.setText("0 items pending backup");
    }
    
    private void updateSubSettings(boolean enabled) {
        autoBackupSwitch.setEnabled(enabled);
        wifiOnlySwitch.setEnabled(enabled);
        chargingOnlySwitch.setEnabled(enabled);
    }
    
    private void updateBackupConfig() {
        CloudBackupManager.BackupConfig config = new CloudBackupManager.BackupConfig(
            wifiOnlySwitch.isChecked(),
            chargingOnlySwitch.isChecked(),
            3, // retry attempts
            5000, // retry delay
            2 // max concurrent
        );
        cloudManager.setBackupConfig(config);
    }
    
    private void triggerManualBackup() {
        cloudManager.processBackupQueue();
        // Show progress dialog
    }
    
    private void testCloudConnection() {
        // Test connection to selected provider
    }
    
    private void showBackupHistory() {
        // Show backup history activity
    }
    
    private void onProviderSelected(CloudProviderOption provider) {
        // Configure selected cloud provider
    }
    
    // Cloud provider option data class
    public static class CloudProviderOption {
        public final String icon;
        public final String name;
        public final String id;
        public final String description;
        
        public CloudProviderOption(String icon, String name, String id, String description) {
            this.icon = icon;
            this.name = name;
            this.id = id;
            this.description = description;
        }
    }
    
    // Cloud provider adapter
    private static class CloudProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<CloudProviderOption> providers;
        private final ProviderSelectionListener listener;
        
        interface ProviderSelectionListener {
            void onProviderSelected(CloudProviderOption provider);
        }
        
        public CloudProviderAdapter(List<CloudProviderOption> providers, ProviderSelectionListener listener) {
            this.providers = providers;
            this.listener = listener;
        }
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(android.view.LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false)) {};
        }
        
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind provider data to views
        }
        
        @Override
        public int getItemCount() {
            return providers.size();
        }
    }
}
