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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private MapView mapView;
    private LocationTracker locationTracker;
    private TextView currentLocationText;
    private TextView currentCoordinates;
    private TextView locationCount;
    private RecyclerView locationHistoryRecycler;
    private LocationHistoryAdapter adapter;

    private List<LocationRecord> locationHistory = new ArrayList<>();
    private Polyline routePolyline;
    private final ArrayList<GeoPoint> routePoints = new ArrayList<>();
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue("com.core.talita");

        setContentView(R.layout.activity_location);

        initializeViews();
        setupMap();
        setupLocationHistory();
        loadLocationHistory();
        checkLocationPermission();
    }

    private void initializeViews() {
        mapView = findViewById(R.id.location_map);
        currentLocationText = findViewById(R.id.current_location_text);
        currentCoordinates = findViewById(R.id.current_coordinates);
        locationCount = findViewById(R.id.location_count);
        locationHistoryRecycler = findViewById(R.id.location_history_recycler);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Initialize polyline for route
        routePolyline = new Polyline();
        routePolyline.setColor(0xFF4285F4); // Blue color
        routePolyline.setWidth(5f);
        mapView.getOverlays().add(routePolyline);

        // Set default center (will be updated when location is acquired)
        GeoPoint defaultCenter = new GeoPoint(-37.8136, 144.9631); // Melbourne default
        mapView.getController().setCenter(defaultCenter);
    }

    private void setupLocationHistory() {
        adapter = new LocationHistoryAdapter(locationHistory);
        locationHistoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        locationHistoryRecycler.setAdapter(adapter);
    }

    private void loadLocationHistory() {
        try {
            // Use our new database instead of file
            LocalDataManager dataManager = new LocalDataManager(this);
            List<LocalDataManager.DataItem> databaseItems = dataManager.getItemsByType("location");

            locationHistory.clear();
            routePoints.clear();

            for (LocalDataManager.DataItem item : databaseItems) {
                try {
                    JSONObject locationData = new JSONObject(item.dataJson);
                    double latitude = locationData.getDouble("latitude");
                    double longitude = locationData.getDouble("longitude");

                    LocationRecord record = new LocationRecord(latitude, longitude, item.createdAt);
                    locationHistory.add(record);

                    // Add to route points for map display
                    routePoints.add(new GeoPoint(latitude, longitude));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Sort by timestamp (most recent first)
            Collections.sort(locationHistory, (a, b) -> Long.compare(b.timestamp, a.timestamp));

            // Update UI
            updateLocationCount();
            adapter.notifyDataSetChanged();

            // Update map with route
            if (!routePoints.isEmpty()) {
                routePolyline.setPoints(routePoints);

                // Center on most recent location if available
                if (!locationHistory.isEmpty()) {
                    LocationRecord mostRecent = locationHistory.get(0);
                    GeoPoint recentPoint = new GeoPoint(mostRecent.latitude, mostRecent.longitude);
                    mapView.getController().setCenter(recentPoint);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            updateLocationCount();
        }
    }

    private void updateLocationCount() {
        int count = locationHistory.size();
        locationCount.setText(count + " recorded");
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationTracking();
        }
    }

    private void startLocationTracking() {
        // Create a custom location tracker that updates our UI
        locationTracker = new LocationTracker(this, currentLocationText, mapView, routePolyline, routePoints) {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);

                // Update current location display
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                currentLocationText.setText("Current Location: Active");
                currentCoordinates.setText(String.format(Locale.getDefault(),
                        "Lat: %.6f, Lon: %.6f", latitude, longitude));

                // Update/add current location marker
                if (currentLocationMarker != null) {
                    mapView.getOverlays().remove(currentLocationMarker);
                }

                currentLocationMarker = new Marker(mapView);
                currentLocationMarker.setPosition(new GeoPoint(latitude, longitude));
                currentLocationMarker.setTitle("Current Location");
                currentLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                mapView.getOverlays().add(currentLocationMarker);

                // Center map on current location
                mapView.getController().setCenter(new GeoPoint(latitude, longitude));

                // Refresh location history (new location might have been saved)
                loadLocationHistory();
            }
        };

        locationTracker.startTracking();
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
        if (locationTracker != null) {
            locationTracker.stopTracking();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            } else {
                Toast.makeText(this, "Location permission required for tracking", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper class for location records
    public static class LocationRecord {
        public final double latitude;
        public final double longitude;
        public final long timestamp;
        public final String formattedTime;

        public LocationRecord(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
            this.formattedTime = sdf.format(new Date(timestamp));
        }
    }
}