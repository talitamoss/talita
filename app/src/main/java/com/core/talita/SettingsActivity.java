// Updated SettingsActivity.java - Navigation-focused version
// Remove or comment out the old inline control references

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

/**
 * Settings Activity - Main settings navigation hub
 * Navigation-focused version for Option 3
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "🚀 Starting Settings Activity...");
            
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "📱 Layout set successfully");
            
            setupBackButton();
            setupNavigationCards();
            
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update any status indicators if needed
        updateStatusIndicators();
    }
    
    private void updateStatusIndicators() {
        // Update status text on cards if they exist
        TextView collectorsStatus = findViewById(R.id.collectors_status_text_card);
        if (collectorsStatus != null) {
            // This would get the actual count from DataCollectorManager
            collectorsStatus.setText("12 active");
        }
        
        // Update other status indicators as needed
    }
}
