package com.core.talita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.core.talita.collectors.LocationCollector;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.List;

/**
 * LocationActivity - UI for location tracking
 * Clean architecture: UI only, logic in LocationCollector
 */
public class LocationActivity extends AppCompatActivity implements LocationCollector.LocationListener {
    
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    
    // UI Components
    private MapView mapView;
    private TextView coordinatesText;
    private TextView statusText;
    private Button trackButton;
    private Button captureButton;
    
    // Business Logic
    private LocationCollector locationCollector;
    private UniversalDataService dataService;
    
    // Map
    private Marker currentLocationMarker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        
        setContentView(R.layout.activity_location);
        
        // Initialize services
        locationCollector = new LocationCollector(this);
        locationCollector.setListener(this);
        dataService = new UniversalDataService(this);
        
        // Setup UI
        initializeViews();
        checkPermissions();
        loadLocationHistory();
    }
    
    private void initializeViews() {
        mapView = findViewById(R.id.map_view);
        coordinatesText = findViewById(R.id.coordinates_text);
        statusText = findViewById(R.id.status_text);
        trackButton = findViewById(R.id.track_button);
        captureButton = findViewById(R.id.capture_button);
        
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Setup map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        
        // Setup buttons
        trackButton.setOnClickListener(v -> toggleTracking());
        captureButton.setOnClickListener(v -> captureLocation());
        
        // Initialize marker
        currentLocationMarker = new Marker(mapView);
        currentLocationMarker.setIcon(getDrawable(R.drawable.ic_location));
        currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(currentLocationMarker);
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
        } else {
            // Try to show last known location
            showLastLocation();
        }
    }
    
    private void toggleTracking() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        if (locationCollector.isTracking()) {
            locationCollector.stopTracking();
        } else {
            locationCollector.startTracking();
        }
    }
    
    private void captureLocation() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        locationCollector.captureCurrentLocation();
    }
    
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void showLastLocation() {
        try {
            Location location = locationCollector.getLastKnownLocation();
            if (location != null) {
                updateMapLocation(location);
            }
        } catch (SecurityException e) {
            // Permission not granted
        }
    }
    
    private void updateMapLocation(Location location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setCenter(point);
        currentLocationMarker.setPosition(point);
        currentLocationMarker.setTitle("Current Location");
        
        coordinatesText.setText(String.format("%.6f, %.6f", 
            location.getLatitude(), location.getLongitude()));
    }
    
    private void loadLocationHistory() {
        // Load recent locations
        List<PersonalData> locations = dataService.getDataByType("location");
        statusText.setText(locations.size() + " locations tracked");
    }
    
    // LocationCollector.LocationListener implementation
    
    @Override
    public void onLocationUpdate(Location location, String dataId) {
        runOnUiThread(() -> {
            updateMapLocation(location);
            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
            loadLocationHistory();
        });
    }
    
    @Override
    public void onTrackingStarted() {
        runOnUiThread(() -> {
            trackButton.setText("Stop Tracking");
            statusText.setText("Tracking location...");
        });
    }
    
    @Override
    public void onTrackingStopped() {
        runOnUiThread(() -> {
            trackButton.setText("Start Tracking");
            statusText.setText("Tracking stopped");
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLastLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        
        // Stop tracking when paused
        if (locationCollector.isTracking()) {
            locationCollector.stopTracking();
        }
    }
}
