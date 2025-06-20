package com.core.talita;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LocationTracker implements LocationListener {

    private final LocationManager locationManager;
    private final Context context;
    private final TextView locationTextView;
    private final MapView mapView;
    private final Polyline polyline;
    private final ArrayList<GeoPoint> routePoints;


    // Constructor
    public LocationTracker(Context context, TextView locationTextView, MapView mapView, Polyline polyline, ArrayList<GeoPoint> routePoints) {
        this.context = context;
        this.locationTextView = locationTextView;
        this.mapView = mapView;
        this.polyline = polyline;
        this.routePoints = routePoints;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
            polyline.setPoints(routePoints);

            // Add marker for the new point
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setTitle("Timestamp: " + System.currentTimeMillis());
            mapView.getOverlays().add(marker);

            mapView.getController().setCenter(geoPoint);
            mapView.getController().setZoom(15);

            locationTextView.setText("Lat: " + latitude + ", Lon: " + longitude);

            long timestamp = System.currentTimeMillis();

            String locationJson = "{\" latitude\":" + latitude + ",\"longitude\":" + longitude + ",\"timestamp\":" + timestamp + "}";


            saveLocationToFile(latitude, longitude, timestamp);
        }

        private void saveLocationToFile(double latitude, double longitude, long timestamp) {
            try {
                JSONObject locationData = new JSONObject();
                locationData.put("latitude", latitude);
                locationData.put("longitude", longitude);
                locationData.put("timestamp", timestamp);

                FileOutputStream fos = context.openFileOutput("location_data.txt", Context.MODE_APPEND);
                fos.write(locationData.toString().getBytes());
                fos.close();

                Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "File write error", Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "JSON formatting error", Toast.LENGTH_SHORT).show();
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
