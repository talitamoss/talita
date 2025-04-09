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

import java.io.FileOutputStream;
import java.io.IOException;

public class LocationTracker implements LocationListener {

    private final LocationManager locationManager;

    private final Context context;

    private final TextView locationTextView;

    //Constructor
    public LocationTracker(Context context, TextView locationTextView) {
        this.context = context;
        this.locationTextView = locationTextView;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // Start location tracking
    public void startTracking() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // if permission isn't granted, request it
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Request location updates using GPS
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, //GPS for high accuracy
                    10000, // Update every 10 seconds
                    10,     // Update ever 10 meters
                    this    // LocationListener
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        locationTextView.setText("Lat: " + latitude + ", Lon: " + longitude);

        long timestamp = System.currentTimeMillis();

        saveLocationToFile(latitude, longitude, timestamp);
    }

    private void saveLocationToFile(double latitude, double longitude, long timestamp) {
        String data = "Latitude: " + latitude + ", Longitude: " + longitude + "\n";
        try {
            JSONObject locationData = new JSONObject();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("timestamp", timestamp);

            FileOutputStream fos = context.openFileOutput("location_data.txt", Context.MODE_APPEND);

            fos.write(locationData.toString().getBytes());

            fos.write("\n".getBytes());

            fos.close();

            Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving location", Toast.LENGTH_SHORT).show();
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



