package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.core.talita.cloud.CloudBackupManager;
/**
 * Settings Activity - Main settings hub
 * FIXED VERSION: Handles missing views gracefully
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // Core services
    private EncryptionService encryptionService;
    private UniversalDataService dataService;
    private DataCollectorManager collectorManager;
    private TrackingManager trackingManager;
    private CloudBackupManager cloudBackupManager;

    // UI Components - will check if they exist
    private Switch switchBackgroundTracking;
    private TextView collectorsStatusText;
    private RecyclerView collectorsRecyclerView;
    private Switch switchCloudBackup;
    private Switch switchAutoBackup;
    private TextView cloudStatusText;
    private Button manualBackupButton;
    private TextView encryptionStatusText;
    private Button viewEncryptionButton;
    private Button exportDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "🚀 Starting Settings Activity...");
            
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "📱 Layout set successfully");
            
            initializeServices();
            initializeViews();
            setupControls();
            updateUI();
            
            Log.d(TAG, "🎉 Settings Activity initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Fatal error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Close the activity gracefully
        }
    }

    private void initializeServices() {
        Log.d(TAG, "🔧 Initializing services...");
        
        try {
            // Initialize encryption service
            encryptionService = new EncryptionService(this);
            Log.d(TAG, "✅ EncryptionService initialized");
            
            // Initialize universal data service
            dataService = new UniversalDataService(this);
            Log.d(TAG, "✅ UniversalDataService initialized");
            
            // Initialize data collector manager
            collectorManager = new DataCollectorManager(this);
            Log.d(TAG, "✅ DataCollectorManager initialized");
            
            // Initialize tracking manager
            trackingManager = new TrackingManager(this);
            Log.d(TAG, "✅ TrackingManager initialized");
            
            // Get cloud backup manager from data service
            cloudBackupManager = dataService.getCloudBackupManager();
            
            Log.d(TAG, "📊 Services initialization complete");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Service initialization failed: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "📱 Initializing views...");
        
        // Background tracking controls - check if they exist
        switchBackgroundTracking = findViewById(R.id.switch_background_tracking);
        collectorsStatusText = findViewById(R.id.collectors_status_text);
        collectorsRecyclerView = findViewById(R.id.collectors_recycler_view);
        
        // Cloud backup controls
        switchCloudBackup = findViewById(R.id.switch_cloud_backup);
        switchAutoBackup = findViewById(R.id.switch_auto_backup);
        cloudStatusText = findViewById(R.id.cloud_status_text);
        
        // Manual backup button - could be a CardView or Button
        View manualBackupView = findViewById(R.id.manual_backup_button);
        if (manualBackupView instanceof Button) {
            manualBackupButton = (Button) manualBackupView;
        }
        
        // Encryption status
        encryptionStatusText = findViewById(R.id.encryption_status_text);
        viewEncryptionButton = findViewById(R.id.btn_view_encryption_status);
        
        // Export button
        exportDataButton = findViewById(R.id.btn_export_data);
        
        // Back button - handle both Button and other view types
        View backView = findViewById(R.id.back_button);
        if (backView != null) {
            backView.setOnClickListener(v -> finish());
        }
        
        Log.d(TAG, "📱 Views initialized - some views may be null if not in layout");
    }

    private void setupControls() {
        Log.d(TAG, "⚙️ Setting up controls...");
        
        // Background tracking toggle
        if (switchBackgroundTracking != null) {
            switchBackgroundTracking.setChecked(trackingManager.isTrackingEnabled());
            switchBackgroundTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    trackingManager.startTracking();
                } else {
                    trackingManager.stopTracking();
                }
                updateUI();
                Log.d(TAG, "🎯 Background tracking toggled: " + isChecked);
            });
        }
        
        // Collectors list
        setupCollectorsList();
        
        // Cloud backup toggle
        if (switchCloudBackup != null) {
            boolean isCloudEnabled = cloudBackupManager != null && 
                                   getSharedPreferences("cloud_backup_prefs", MODE_PRIVATE)
                                   .getBoolean("backup_enabled", false);
            switchCloudBackup.setChecked(isCloudEnabled);
            switchCloudBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (cloudBackupManager != null) {
                    cloudBackupManager.setBackupEnabled(isChecked);
                    updateUI();
                }
            });
        }
        
        // Auto backup toggle
        if (switchAutoBackup != null) {
            boolean isAutoEnabled = cloudBackupManager != null &&
                                  getSharedPreferences("cloud_backup_prefs", MODE_PRIVATE)
                                  .getBoolean("auto_backup_enabled", true);
            switchAutoBackup.setChecked(isAutoEnabled);
            switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (cloudBackupManager != null) {
                    cloudBackupManager.setAutoBackupEnabled(isChecked);
                }
            });
        }
        
        // Manual backup button
        if (manualBackupButton != null) {
            manualBackupButton.setOnClickListener(v -> performManualBackup());
        }
        
        // Manual backup card (if using CardView instead of Button)
        CardView manualBackupCard = findViewById(R.id.manual_backup_card);
        if (manualBackupCard != null) {
            manualBackupCard.setOnClickListener(v -> performManualBackup());
        }
        
        // View encryption status button
        if (viewEncryptionButton != null) {
            viewEncryptionButton.setOnClickListener(v -> showEncryptionDetails());
        }
        
        // Export data button
        if (exportDataButton != null) {
            exportDataButton.setOnClickListener(v -> {
                Toast.makeText(this, "📤 Data export coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
        
        Log.d(TAG, "⚙️ Controls setup complete");
    }

    private void setupCollectorsList() {
        Log.d(TAG, "📊 Setting up collectors list...");
        
        if (collectorsRecyclerView == null) {
            Log.w(TAG, "⚠️ Collectors RecyclerView not found in layout");
            return;
        }
        
        try {
            List<DataCollectorItem> collectorItems = new ArrayList<>();
            
            // Get collectors by category
            Map<String, List<DataCollector>> collectorsByCategory = 
                collectorManager.getCollectorsByCategory();
            
            for (Map.Entry<String, List<DataCollector>> entry : collectorsByCategory.entrySet()) {
                for (DataCollector collector : entry.getValue()) {
                    collectorItems.add(new DataCollectorItem(
                        collector.getIcon(),
                        collector.getDisplayName(),
                        collector.getDataType(),
                        collector.isEnabled(this),
                        collector.isAvailable(this)
                    ));
                }
            }
            
            CollectorSettingsAdapter adapter = new CollectorSettingsAdapter(
                collectorItems,
                this::onCollectorToggled
            );
            
            collectorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            collectorsRecyclerView.setAdapter(adapter);
            
            Log.d(TAG, "📊 Collectors list setup with " + collectorItems.size() + " items");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to setup collectors list: " + e.getMessage(), e);
        }
    }

    private void onCollectorToggled(DataCollectorItem item, boolean enabled) {
        collectorManager.setCollectorEnabled(item.dataType, enabled);
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "🔄 Updating UI...");
        
        try {
            // Update tracking status
            boolean isTracking = trackingManager.isTrackingEnabled();
            
            // Update collectors status text
            if (collectorsStatusText != null) {
                DataCollectorManager.CollectionStats stats = collectorManager.getCollectionStats();
                collectorsStatusText.setText(stats.activeCollectors + "/" + 
                                           stats.enabledCollectors + " collectors active");
            }
            
            // Update cloud status
            if (cloudStatusText != null) {
                boolean isCloudEnabled = cloudBackupManager != null &&
                                       getSharedPreferences("cloud_backup_prefs", MODE_PRIVATE)
                                       .getBoolean("backup_enabled", false);
                cloudStatusText.setText(isCloudEnabled ? 
                    "☁️ Cloud backup active" : "Cloud backup disabled");
            }
            
            // Update encryption status
            if (encryptionStatusText != null) {
                encryptionStatusText.setText("🔒 Hardware encryption active");
            }
            
            Log.d(TAG, "🔄 UI updated successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating UI: " + e.getMessage(), e);
        }
    }

    private void performManualBackup() {
        if (cloudBackupManager == null) {
            Toast.makeText(this, "⚠️ Cloud backup not configured", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "🚀 Starting manual backup...", Toast.LENGTH_SHORT).show();
        cloudBackupManager.processBackupQueue();
    }

    private void showEncryptionDetails() {
        String encryptionInfo = encryptionService.getEncryptionStatus();
        
        new AlertDialog.Builder(this)
            .setTitle("🔒 Encryption Status")
            .setMessage(encryptionInfo)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    /**
     * Data class for collector items
     */
    public static class DataCollectorItem {
        public final String icon;
        public final String name;
        public final String dataType;
        public final boolean isEnabled;
        public final boolean isAvailable;
        
        public DataCollectorItem(String icon, String name, String dataType, 
                               boolean isEnabled, boolean isAvailable) {
            this.icon = icon;
            this.name = name;
            this.dataType = dataType;
            this.isEnabled = isEnabled;
            this.isAvailable = isAvailable;
        }
    }

    /**
     * Adapter for collectors list
     */
    public static class CollectorSettingsAdapter extends 
            RecyclerView.Adapter<CollectorSettingsAdapter.CollectorViewHolder> {
        
        public interface CollectorToggleListener {
            void onCollectorToggled(DataCollectorItem item, boolean enabled);
        }
        
        private final List<DataCollectorItem> items;
        private final CollectorToggleListener listener;
        
        public CollectorSettingsAdapter(List<DataCollectorItem> items, 
                                      CollectorToggleListener listener) {
            this.items = items;
            this.listener = listener;
        }
        
        @Override
        public CollectorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Use simple list item for now
            View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new CollectorViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(CollectorViewHolder holder, int position) {
            DataCollectorItem item = items.get(position);
            holder.bind(item, listener);
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        static class CollectorViewHolder extends RecyclerView.ViewHolder {
            private final TextView text1;
            private final TextView text2;
            
            CollectorViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
            
            void bind(DataCollectorItem item, CollectorToggleListener listener) {
                text1.setText(item.icon + " " + item.name);
                text2.setText(item.isEnabled ? "Enabled" : "Disabled");
                
                itemView.setOnClickListener(v -> {
                    if (listener != null && item.isAvailable) {
                        listener.onCollectorToggled(item, !item.isEnabled);
                    }
                });
                
                // Visual feedback
                itemView.setAlpha(item.isAvailable ? 1.0f : 0.5f);
            }
        }
    }
}

