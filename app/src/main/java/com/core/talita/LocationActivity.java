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
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * LocationActivity - UI for location tracking
 * Updated to use plugin system instead of hard-coded LocationCollector
 */
public class LocationActivity extends AppCompatActivity {
    
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final String LOCATION_PLUGIN_ID = "core.location";
    
    // UI Components
    private MapView mapView;
    private TextView coordinatesText;
    private TextView statusText;
    private Button trackButton;
    private Button captureButton;
    
    // Services
    private DataCollectorManager collectorManager;
    private DataCollector locationCollector;
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
        collectorManager = DataCollectorManager.getInstance(this);
        dataService = UniversalDataService.getInstance(this);
        
        // Try to get location collector from plugin system
        initializeLocationCollector();
        
        // Initialize UI
        initializeViews();
        checkPermissions();
        
        // Setup map
        setupMap();
    }
    
    private void initializeLocationCollector() {
        // Check if location plugin is available
        PluginManager pluginManager = PluginManager.getInstance(this);
        DataCollectorPlugin locationPlugin = pluginManager.getPlugin(LOCATION_PLUGIN_ID);
        
        if (locationPlugin != null) {
            // Create collector from plugin
            locationCollector = locationPlugin.createCollector(this);
            if (locationCollector != null) {
                locationCollector.initialize(this);
            }
        }
        
        // If no plugin available, show message
        if (locationCollector == null) {
            Toast.makeText(this, "Location tracking plugin not available", Toast.LENGTH_LONG).show();
            // For now, create a placeholder
            createPlaceholderCollector();
        }
    }
    
    private void createPlaceholderCollector() {
        // Create a simple placeholder until LocationPlugin is implemented
        locationCollector = new SimpleDataCollector.Builder("location", "Location")
            .description("Track your location")
            .emoji("📍")
            .category("i")
            .inputHint("Location notes")
            .inputType(SimpleDataCollector.InputType.TEXT)
            .build();
        
        locationCollector.initialize(this);
    }
    
    private void initializeViews() {
        mapView = findViewById(R.id.map_view);
        coordinatesText = findViewById(R.id.coordinates_text);
        statusText = findViewById(R.id.status_text);
        trackButton = findViewById(R.id.track_button);
        captureButton = findViewById(R.id.capture_button);
        
        trackButton.setOnClickListener(v -> toggleTracking());
        captureButton.setOnClickListener(v -> captureLocation());
    }
    
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        
        // Center on default location
        GeoPoint startPoint = new GeoPoint(-34.9285, 138.6007); // Adelaide
        mapView.getController().setCenter(startPoint);
        
        // Add marker
        currentLocationMarker = new Marker(mapView);
        currentLocationMarker.setPosition(startPoint);
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
            onPermissionGranted();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void onPermissionGranted() {
        statusText.setText("Ready to track location");
        showLastLocation();
    }
    
    private void toggleTracking() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        // Check if collector supports automated collection
        if (locationCollector != null && locationCollector.getSettings().isAutomatedCollection()) {
            if (locationCollector.isCollectingAutomatically()) {
                locationCollector.stopAutomatedCollection();
                trackButton.setText("Start Tracking");
                statusText.setText("Tracking stopped");
            } else {
                locationCollector.startAutomatedCollection();
                trackButton.setText("Stop Tracking");
                statusText.setText("Tracking started");
            }
        } else {
            Toast.makeText(this, "Automated tracking not supported", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void captureLocation() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        // Use plugin system to capture location
        if (locationCollector != null) {
            // For now, create mock location data
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", -34.9285);
            locationData.put("longitude", 138.6007);
            locationData.put("accuracy", 10.0);
            locationData.put("source", "manual");
            
            CollectorResult result = locationCollector.collectQuick(locationData);
            
            if (result.isSuccess()) {
                Toast.makeText(this, "Location captured", Toast.LENGTH_SHORT).show();
                loadLocationHistory();
            } else {
                Toast.makeText(this, "Failed to capture location", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void showLastLocation() {
        // In a real implementation, get last location from location provider
        // For now, just center on Adelaide
        GeoPoint adelaide = new GeoPoint(-34.9285, 138.6007);
        updateMapLocation(adelaide);
    }
    
    private void updateMapLocation(GeoPoint location) {
        mapView.getController().setCenter(location);
        currentLocationMarker.setPosition(location);
        currentLocationMarker.setTitle("Current Location");
        
        coordinatesText.setText(String.format("%.6f, %.6f", 
            location.getLatitude(), location.getLongitude()));
    }
    
    private void loadLocationHistory() {
        // Load recent locations using plugin data type
        List<PersonalData> locations = dataService.getDataByType("location");
        statusText.setText(locations.size() + " locations tracked");
        
        // Add markers for recent locations
        for (PersonalData data : locations) {
            try {
                Map<String, Object> locData = data.getData();
                if (locData.containsKey("latitude") && locData.containsKey("longitude")) {
                    double lat = ((Number) locData.get("latitude")).doubleValue();
                    double lon = ((Number) locData.get("longitude")).doubleValue();
                    
                    Marker marker = new Marker(mapView);
                    marker.setPosition(new GeoPoint(lat, lon));
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapView.getOverlays().add(marker);
                }
            } catch (Exception e) {
                // Skip invalid data
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
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCollector != null) {
            locationCollector.onDestroy();
        }
    }
}
