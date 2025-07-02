package com.core.talita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1;
    private LocationTracker locationTracker;
    private AudioRecorder audioRecorder;

    private final ArrayList<GeoPoint> routePoints = new ArrayList<>();
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue("com.core.talita");

        setContentView(R.layout.activity_main);

        // Initialize tracking components (but don't start UI yet)
        polyline = new Polyline();
        locationTracker = new LocationTracker(this, null, null, polyline, routePoints);
        audioRecorder = new AudioRecorder(this, null, null);

        // Setup tile click listeners
        setupTileClickListeners();

        // Check and request permissions
        checkAndRequestPermissions();
    }

    private void setupTileClickListeners() {
        // Location Tile
        CardView locationTile = findViewById(R.id.location_tile);
        locationTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });

        // Audio Tile
        CardView audioTile = findViewById(R.id.audio_tile);
        audioTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
        });

        // Files Tile
        CardView filesTile = findViewById(R.id.files_tile);
        filesTile.setOnClickListener(v -> {
            Toast.makeText(this, "File manager - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open file manager activity
        });

        // Export Tile
        CardView exportTile = findViewById(R.id.export_tile);
        exportTile.setOnClickListener(v -> {
            Toast.makeText(this, "Export data - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open export functionality
        });

        // Settings Tile
        CardView settingsTile = findViewById(R.id.settings_tile);
        settingsTile.setOnClickListener(v -> {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open settings activity
        });

        // Stats Tile
        CardView statsTile = findViewById(R.id.stats_tile);
        statsTile.setOnClickListener(v -> {
            Toast.makeText(this, "Statistics - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open statistics activity
        });

        // Privacy Tile
        CardView privacyTile = findViewById(R.id.privacy_tile);
        privacyTile.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy settings - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open privacy settings
        });

        // Cleanup Tile
        CardView cleanupTile = findViewById(R.id.cleanup_tile);
        cleanupTile.setOnClickListener(v -> {
            Toast.makeText(this, "Data cleanup - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Open cleanup functionality
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        } else {
            // Permissions already granted - start background tracking
            locationTracker.startTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationTracker != null) {
            locationTracker.stopTracking();
        }
        if (audioRecorder != null && audioRecorder.isRecording()) {
            audioRecorder.stopRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecorder != null) {
            audioRecorder.cleanup();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean locationGranted = false;
            boolean audioGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    audioGranted = true;
                }
            }

            if (locationGranted) {
                locationTracker.startTracking();
                Toast.makeText(this, "Location tracking enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }

            if (!audioGranted) {
                Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}