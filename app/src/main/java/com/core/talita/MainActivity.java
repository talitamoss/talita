package com.core.talita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

    // Tracking components
    private LocationTracker locationTracker; // Keep for manual location activity
    private AudioRecorder audioRecorder;
    private TrackingManager trackingManager; // For background tracking management

    private final ArrayList<GeoPoint> routePoints = new ArrayList<>();
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Test collectors
        testCollectorSystem();

        // Set a simple layout for now
        setContentView(R.layout.activity_main);
    }

    private void testCollectorSystem() {
        try {
            Log.d("MainActivity", "🧪 Testing collector system...");

            DataCollectorManager manager = new DataCollectorManager(this);

            DataCollectorManager.CollectionStats stats = manager.getCollectionStats();
            Log.d("MainActivity", "✅ Collector system loaded: " + stats.getSummary());
            Log.d("MainActivity", "📊 Found " + stats.totalCollectors + " collectors");

            // Test water logging
            manager.quickLogWater(250);
            Log.d("MainActivity", "💧 Water logging test completed");

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Collector system failed: " + e.getMessage());
            e.printStackTrace();
        }
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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
                Manifest.permission.ACCESS_BACKGROUND_LOCATION, // For background location
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACTIVITY_RECOGNITION, // For step counting
                Manifest.permission.FOREGROUND_SERVICE // For background service
        };

        boolean allPermissionsGranted = hasRequiredPermissions();

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        } else {
            // Permissions already granted - start the old location tracker for compatibility
            locationTracker.startTracking();
        }
    }

    /**
     * Check if basic permissions are granted (location, audio)
     */
    private boolean hasRequiredPermissions() {
        String[] basicPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };

        for (String permission : basicPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the old location tracker (background service will continue if enabled)
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
        // Note: We don't stop background tracking here - it should continue even when app is closed
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

            // Handle permission results
            if (locationGranted) {
                locationTracker.startTracking();
                Toast.makeText(this, "✅ Location tracking enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Location permission required for tracking", Toast.LENGTH_SHORT).show();
            }

            if (!audioGranted) {
                Toast.makeText(this, "⚠️ Audio permission denied - audio recording disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}