package com.core.talita;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.concurrent.TimeUnit;

/**
 * LocationTracker - Simple location tracking utility
 * Used for one-time location captures
 */
public class LocationTracker implements LocationListener {
    
    private static final String TAG = "LocationTracker";
    private static final long TIMEOUT_MS = 30000; // 30 seconds
    private static final float MIN_ACCURACY = 50.0f; // 50 meters
    
    private final Context context;
    private final LocationManager locationManager;
    private LocationCallback callback;
    private Location bestLocation;
    private long startTime;
    
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }
    
    public LocationTracker(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * Request current location
     */
    public void getCurrentLocation(LocationCallback callback) {
        this.callback = callback;
        this.startTime = System.currentTimeMillis();
        this.bestLocation = null;
        
        // Check permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }
        
        // Check if location services are enabled
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (!gpsEnabled && !networkEnabled) {
            callback.onLocationError("Location services disabled");
            return;
        }
        
        try {
            // Request updates from available providers
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, 0, this
                );
                
                // Get last known location
                Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastGps != null && isBetterLocation(lastGps, bestLocation)) {
                    bestLocation = lastGps;
                }
            }
            
            if (networkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0, 0, this
                );
                
                // Get last known location
                Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastNetwork != null && isBetterLocation(lastNetwork, bestLocation)) {
                    bestLocation = lastNetwork;
                }
            }
            
            // If we have a recent accurate location, use it immediately
            if (bestLocation != null && 
                bestLocation.getAccuracy() < MIN_ACCURACY &&
                (System.currentTimeMillis() - bestLocation.getTime()) < TimeUnit.MINUTES.toMillis(5)) {
                
                stopTracking();
                callback.onLocationReceived(bestLocation);
                return;
            }
            
            // Otherwise wait for updates with timeout
            new Thread(() -> {
                try {
                    Thread.sleep(TIMEOUT_MS);
                    stopTracking();
                    
                    if (bestLocation != null) {
                        callback.onLocationReceived(bestLocation);
                    } else {
                        callback.onLocationError("Location timeout");
                    }
                } catch (InterruptedException e) {
                    // Cancelled
                }
            }).start();
            
        } catch (SecurityException e) {
            callback.onLocationError("Security exception: " + e.getMessage());
        } catch (Exception e) {
            callback.onLocationError("Location error: " + e.getMessage());
        }
    }
    
    /**
     * Stop tracking
     */
    public void stopTracking() {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping location updates", e);
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, bestLocation)) {
            bestLocation = location;
            
            // If accuracy is good enough, deliver immediately
            if (location.getAccuracy() < MIN_ACCURACY) {
                stopTracking();
                if (callback != null) {
                    callback.onLocationReceived(location);
                    callback = null;
                }
            }
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    @Override
    public void onProviderEnabled(String provider) {}
    
    @Override
    public void onProviderDisabled(String provider) {}
    
    /**
     * Determines whether one Location reading is better than the current Location fix
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }
        
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TimeUnit.MINUTES.toMillis(2);
        boolean isSignificantlyOlder = timeDelta < -TimeUnit.MINUTES.toMillis(2);
        boolean isNewer = timeDelta > 0;
        
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }
        
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());
        
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
    /**
     * Quick method to save a location
     */
    public static void captureCurrentLocation(Context context) {
        LocationTracker tracker = new LocationTracker(context);
        tracker.getCurrentLocation(new LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                // Create LocationData using the Builder pattern
                LocationData locationData = new LocationData.Builder(
                    location.getLatitude(),
                    location.getLongitude()
                )
                .accuracy(location.getAccuracy())
                .provider(location.getProvider())
                .speed(location.getSpeed())
                .bearing(location.getBearing())
                .build();
                
                UniversalDataService dataService = UniversalDataService.getInstance(context);
                dataService.captureData(locationData);
                
                Log.d(TAG, "📍 Location captured: " + location.getLatitude() + ", " + location.getLongitude());
            }
            
            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Failed to capture location: " + error);
            }
        });
    }
}
