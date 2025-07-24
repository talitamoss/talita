package com.core.talita;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

/**
 * Manages background tracking service lifecycle
 * Handles starting/stopping and user preferences
 * 
 * Updated to use generic preference names
 */
public class TrackingManager {
    private static final String TAG = "TrackingManager";
    private static final String PREFS_NAME = "tracking_prefs"; // Changed from TalitaTrackingPrefs
    private static final String PREF_TRACKING_ENABLED = "tracking_enabled";
    
    private final Context context;
    private final SharedPreferences prefs;

    public TrackingManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
               == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if we have background location permission (Android 10+)
     */
    private boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Not needed on older versions
    }
    
    /**
     * Start background tracking service
     */
    public boolean startTracking() {
        try {
            // Check permissions first
            if (!hasLocationPermissions()) {
                Log.w(TAG, "❌ Cannot start tracking - location permissions not granted");
                Toast.makeText(context, "Location permission required for tracking", Toast.LENGTH_LONG).show();
                return false;
            }
            
            // Check background permission on Android 10+
            if (!hasBackgroundLocationPermission()) {
                Log.w(TAG, "⚠️ Background location permission not granted");
                Toast.makeText(context, "Background location access required for continuous tracking", Toast.LENGTH_LONG).show();
                // Can still start, but tracking may stop when app is in background
            }
            
            Intent serviceIntent = new Intent(context, BackgroundTrackingService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            // Save preference
            prefs.edit().putBoolean(PREF_TRACKING_ENABLED, true).apply();
            
            Log.d(TAG, "✅ Background tracking service started");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start tracking service", e);
            Toast.makeText(context, "Failed to start tracking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Stop background tracking service
     */
    public boolean stopTracking() {
        try {
            Intent serviceIntent = new Intent(context, BackgroundTrackingService.class);
            context.stopService(serviceIntent);
            
            // Save preference
            prefs.edit().putBoolean(PREF_TRACKING_ENABLED, false).apply();
            
            Log.d(TAG, "🛑 Background tracking service stopped");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to stop tracking service", e);
            return false;
        }
    }

    /**
     * Check if tracking is currently enabled
     */
    public boolean isTrackingEnabled() {
        return prefs.getBoolean(PREF_TRACKING_ENABLED, false);
    }

    /**
     * Check if tracking service is actually running
     */
    @SuppressLint("ServiceCast")
    public boolean isServiceRunning() {
        // This is a simplified check - in production you might want to use
        // ActivityManager to check running services
        return isTrackingEnabled();
    }

    /**
     * Toggle tracking on/off
     */
    public boolean toggleTracking() {
        if (isTrackingEnabled()) {
            return stopTracking();
        } else {
            return startTracking();
        }
    }

    /**
     * Get tracking status message
     */
    public String getTrackingStatus() {
        if (!hasLocationPermissions()) {
            return "Location permission required";
        } else if (isTrackingEnabled()) {
            return "Tracking active";
        } else {
            return "Tracking disabled";
        }
    }

    /**
     * Get detailed tracking info for UI
     */
    public TrackingInfo getTrackingInfo() {
        return new TrackingInfo(
            isTrackingEnabled(),
            hasLocationPermissions(),
            hasBackgroundLocationPermission(),
            getLastTrackingTime(),
            getTrackingDataCount()
        );
    }

    private long getLastTrackingTime() {
        return prefs.getLong("last_tracking_time", 0);
    }

    private int getTrackingDataCount() {
        return prefs.getInt("tracking_data_count", 0);
    }

    /**
     * Update tracking statistics
     */
    public void updateTrackingStats() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_tracking_time", System.currentTimeMillis());
        editor.putInt("tracking_data_count", getTrackingDataCount() + 1);
        editor.apply();
    }

    /**
     * Clear all tracking preferences
     */
    public void clearPreferences() {
        prefs.edit().clear().apply();
    }

    /**
     * Inner class for tracking information
     */
    public static class TrackingInfo {
        public final boolean isEnabled;
        public final boolean hasLocationPermission;
        public final boolean hasBackgroundPermission;
        public final long lastTrackingTime;
        public final int dataCount;

        public TrackingInfo(boolean isEnabled, boolean hasLocationPermission, 
                          boolean hasBackgroundPermission, long lastTrackingTime, int dataCount) {
            this.isEnabled = isEnabled;
            this.hasLocationPermission = hasLocationPermission;
            this.hasBackgroundPermission = hasBackgroundPermission;
            this.lastTrackingTime = lastTrackingTime;
            this.dataCount = dataCount;
        }
    }
}
