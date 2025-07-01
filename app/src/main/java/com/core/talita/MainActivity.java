package com.core.talita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1; // Changed to handle multiple permissions
    private LocationTracker locationTracker;
    private AudioRecorder audioRecorder; // ADD THIS

    private MapView mapView;
    private Polyline polyline;
    private final ArrayList<GeoPoint> routePoints = new ArrayList<>();
    private TextView locationDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue("com.core.alita");

        setContentView(R.layout.activity_main);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(0.0, 0.0);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(startPoint);

        locationDisplay = findViewById(R.id.location_display);
        TextView locationInfo = findViewById(R.id.location_info); // ADD THIS
        ScrollView locationList = findViewById(R.id.location_display_scrollview);
        Button toggleButton = findViewById(R.id.show_location_button);

        // ADD AUDIO RECORDING COMPONENTS
        TextView audioStatusTextView = findViewById(R.id.audio_status);
        Button recordButton = findViewById(R.id.record_button);

        // Toggle logic for map/location list
        toggleButton.setOnClickListener(v -> {
            if (locationList.getVisibility() == View.VISIBLE) {
                locationList.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                toggleButton.setText("Show Recorded Locations");
            } else {
                locationList.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                toggleButton.setText("Show Map");
            }
        });

        // Initialize polyline
        polyline = new Polyline();
        mapView.getOverlays().add(polyline);

        // Initialize trackers
        locationTracker = new LocationTracker(this, locationInfo, mapView, polyline, routePoints);
        audioRecorder = new AudioRecorder(this, audioStatusTextView, recordButton); // ADD THIS

        // Check and request all permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO  // ADD AUDIO PERMISSION
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
            locationTracker.startTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTracker.stopTracking();
        // Stop recording if it's in progress
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioRecorder.cleanup(); // ADD THIS
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
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }

            if (!audioGranted) {
                Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}