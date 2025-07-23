package com.core.talita.collectors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.core.talita.LocationData;
import com.core.talita.UniversalDataService;

/**
 * LocationCollector - Handles location tracking business logic
 * Clean separation from UI concerns
 */
public class LocationCollector implements LocationListener {
    private static final String TAG = "LocationCollector";
    private static final long MIN_TIME_BETWEEN_UPDATES = 5000; // 5 seconds
    private static final float MIN_DISTANCE_CHANGE = 10; // 10 meters
    
    private final Context context;
    private final UniversalDataService dataService;
    private final LocationManager locationManager;
    
    private Location lastLocation;
    private boolean isTracking = false;
    
    // Listener for UI updates
    public interface LocationListener {
        void onLocationUpdate(Location location, String dataId);
        void onTrackingStarted();
        void onTrackingStopped();
        void onError(String error);
    }
    
    private LocationListener listener;
    
    public LocationCollector(Context context) {
        this.context = context;
        this.dataService = new UniversalDataService(context);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    public void setListener(LocationListener listener) {
        this.listener = listener;
    }
    
    /**
     * Start tracking location
     */
    public void startTracking() {
        if (isTracking) {
            Log.w(TAG, "Already tracking");
            return;
        }
        
        try {
            // Request location updates from both providers
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE,
                    this
                );
            }
            
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE,
                    this
                );
            }
            
            isTracking = true;
            
            if (listener != null) {
                listener.onTrackingStarted();
            }
            
            Log.d(TAG, "Location tracking started");
            
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
            if (listener != null) {
                listener.onError("Location permission required");
            }
        }
    }
    
    /**
     * Stop tracking location
     */
    public void stopTracking() {
        if (!isTracking) {
            return;
        }
        
        try {
            locationManager.removeUpdates(this);
            isTracking = false;
            
            if (listener != null) {
                listener.onTrackingStopped();
            }
            
            Log.d(TAG, "Location tracking stopped");
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location tracking", e);
        }
    }
    
    /**
     * Capture current location once
     */
    public void captureCurrentLocation() {
        try {
            Location location = getLastKnownLocation();
            if (location != null) {
                saveLocation(location);
            } else {
                if (listener != null) {
                    listener.onError("Unable to get current location");
                }
            }
        } catch (SecurityException e) {
            if (listener != null) {
                listener.onError("Location permission required");
            }
        }
    }
    
    /**
     * Get last known location from any provider
     */
    public Location getLastKnownLocation() throws SecurityException {
        Location bestLocation = null;
        
        for (String provider : locationManager.getAllProviders()) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (bestLocation == null || location.getTime() > bestLocation.getTime()) {
                    bestLocation = location;
                }
            }
        }
        
        return bestLocation != null ? bestLocation : lastLocation;
    }
    
    /**
     * Save location using UniversalDataService
     */
    private void saveLocation(Location location) {
        // Create LocationData
        LocationData locationData = new LocationData(
            location.getLatitude(),
            location.getLongitude(),
            location.getAccuracy(),
            location.getProvider(),
            location.getSpeed(),
            location.getBearing()
        );
        
        // Capture through UniversalDataService
        String id = dataService.capture(locationData);
        
        if (id != null) {
            Log.d(TAG, "Location saved: " + id);
            if (listener != null) {
                listener.onLocationUpdate(location, id);
            }
        }
        
        lastLocation = location;
    }
    
    // LocationListener implementation
    
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            saveLocation(location);
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider status changed: " + provider + " - " + status);
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }
    
    /**
     * Check if currently tracking
     */
    public boolean isTracking() {
        return isTracking;
    }
}
