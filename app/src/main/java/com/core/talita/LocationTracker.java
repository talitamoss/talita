package com.core.talita;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.core.talita.LocationData;
import com.core.talita.UniversalDataService;

public class LocationTracker implements LocationListener {

    private static final String TAG = "LocationTracker";
    private final LocationManager locationManager;
    private final Context context;
    private final TextView locationTextView;
    private final MapView mapView;
    private final Polyline polyline;
    private final ArrayList<GeoPoint> routePoints;
    private final LocalDataManager dataManager;


    // Constructor
    public LocationTracker(Context context, TextView locationTextView, MapView mapView, Polyline polyline, ArrayList<GeoPoint> routePoints) {
        this.context = context;
        this.locationTextView = locationTextView;
        this.mapView = mapView;
        this.polyline = polyline;
        this.routePoints = routePoints;
        this.dataManager = new LocalDataManager(context); // ADD THIS LINE
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // LOG THE STORAGE PATHS
        logStoragePaths();
    }

    private void logStoragePaths() {
        File filesDir = context.getFilesDir();
        File locationFile = new File(filesDir, "location_data.txt");
        File recordingsDir = new File(filesDir, "recordings");
        File audioMetaFile = new File(filesDir, "audio_recordings.txt");

        Log.d(TAG, "=== TALITA DATA STORAGE PATHS ===");
        Log.d(TAG, "App files directory: " + filesDir.getAbsolutePath());
        Log.d(TAG, "Location data file: " + locationFile.getAbsolutePath());
        Log.d(TAG, "Audio recordings directory: " + recordingsDir.getAbsolutePath());
        Log.d(TAG, "Audio metadata file: " + audioMetaFile.getAbsolutePath());
        Log.d(TAG, "================================");

        // Also show in UI for easy viewing
        Toast.makeText(context, "Storage: " + filesDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    // Start location tracking
    public void startTracking() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission isn't granted, request it
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Request location updates using GPS
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, // GPS for high accuracy
                    10000, // Update every 10 seconds
                    10,    // Update every 10 meters
                    this   // LocationListener
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Stop location tracking
    public void stopTracking() {
        locationManager.removeUpdates(this);
    }

    // This method is called when the location is updated
    @Override
    public void onLocationChanged(Location location) {
        // Get current location
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        // Add to route points
        routePoints.add(geoPoint);

        // Update polyline
        if (polyline != null) {
            polyline.setPoints(routePoints);
        }

        // Add marker for the new point
        if (mapView != null && mapView.getContext() != null) {
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setTitle("Timestamp: " + System.currentTimeMillis());
            mapView.getOverlays().add(marker);

            mapView.getController().setCenter(geoPoint);
            mapView.getController().setZoom(15);
        }

        if (locationTextView != null) {
            locationTextView.setText("Lat: " + latitude + ", Lon: " + longitude);
        }

        long timestamp = System.currentTimeMillis();

        saveLocationToDatabase(latitude, longitude, timestamp);
    }

    private void saveLocationToDatabase(double latitude, double longitude, long timestamp) {
        try {
            // Create LocationData using our new universal system
            LocationData locationData = new LocationData(latitude, longitude, 10.0, "gps");

            // Use UniversalDataService to handle everything automatically
            UniversalDataService dataService = new UniversalDataService(context);
            String dataId = dataService.capture(locationData);

            if (dataId != null) {
                Log.d(TAG, "Location saved via Universal Service with ID: " + dataId);
                // Success toast is handled automatically by UniversalDataService
            } else {
                Log.e(TAG, "Failed to save location via Universal Service");
                // Error toast is handled automatically by UniversalDataService
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in universal location save: " + e.getMessage());
            Toast.makeText(context, "Location save error", Toast.LENGTH_SHORT).show();
        }
    }

    // These methods are required by the LocationListener interface, but we're not using them here
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}