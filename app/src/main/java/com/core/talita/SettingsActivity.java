package com.core.talita;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchLocationTracking;
    private Switch switchAudioRecording;
    private Switch switchBackgroundTracking;
    private Switch switchCloudBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupListeners();
        loadCurrentSettings();
    }

    private void initializeViews() {
        switchLocationTracking = findViewById(R.id.switch_location_tracking);
        switchAudioRecording = findViewById(R.id.switch_audio_recording);
        switchBackgroundTracking = findViewById(R.id.switch_background_tracking);
        switchCloudBackup = findViewById(R.id.switch_cloud_backup);
    }

    private void setupListeners() {
        switchLocationTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable location tracking
            Toast.makeText(this, "Location tracking " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchAudioRecording.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable audio recording
            Toast.makeText(this, "Audio recording " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchBackgroundTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable background tracking service
            Toast.makeText(this, "Background tracking " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchCloudBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable cloud backup
            Toast.makeText(this, "Cloud backup " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCurrentSettings() {
        // TODO: Load current settings from SharedPreferences or database
        // For now, set defaults
        switchLocationTracking.setChecked(true);
        switchAudioRecording.setChecked(false);
        switchBackgroundTracking.setChecked(false);
        switchCloudBackup.setChecked(false);
    }
}