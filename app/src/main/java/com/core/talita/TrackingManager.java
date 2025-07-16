package com.core.talita;

import android.Manifest;
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
 */
public class TrackingManager {
    private static final String TAG = "TrackingManager";
    private static final String PREFS_NAME = "TalitaTrackingPrefs";
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
     * Start background tracking service
     */
public boolean startTracking() {
    try {
        // ADD PERMISSION CHECK HERE - prevents crash on manual start
        if (!hasLocationPermissions()) {
            Log.w(TAG, "❌ Cannot start tracking - location permissions not granted");
            Toast.makeText(context, "Location permission required for tracking", Toast.LENGTH_LONG).show();
            return false;
        }
        
        Intent serviceIntent = new Intent(context, BackgroundTrackingService.class);

        // Start as foreground service (required for background location on Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // Save preference
        prefs.edit().putBoolean(PREF_TRACKING_ENABLED, true).apply();

        Log.d(TAG, "✅ Background tracking started");
        Toast.makeText(context, "🎯 Background tracking started", Toast.LENGTH_SHORT).show();

        return true;

    } catch (Exception e) {
        Log.e(TAG, "❌ Failed to start background tracking: " + e.getMessage());
        Toast.makeText(context, "❌ Failed to start tracking", Toast.LENGTH_SHORT).show();
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

            Log.d(TAG, "🛑 Background tracking stopped");
            Toast.makeText(context, "🛑 Background tracking stopped", Toast.LENGTH_SHORT).show();

            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to stop background tracking: " + e.getMessage());
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
     * Auto-start tracking if it was previously enabled
     */
	public void autoStartIfEnabled() {
	    if (isTrackingEnabled()) {
       	 // ADD PERMISSION CHECK HERE - prevents crash on Settings open
    		 if (!hasLocationPermissions()) {
    	        Log.w(TAG, "⚠️ Can't auto-start tracking - missing runtime permissions");
     	       // Don't crash, just log and return
     	       return;
        }
        
        Log.d(TAG, "🔄 Auto-starting background tracking");
        startTracking();
    }
}
    /**
     * Get tracking statistics
     */
    public TrackingStats getTrackingStats() {
        // This would query your database for statistics
        // For now, return placeholder data
        return new TrackingStats(
                isTrackingEnabled(),
                System.currentTimeMillis(), // Last update time
                0, // Locations today
                0, // Steps today
                "unknown" // Current activity
        );
    }

    /**
     * Simple data class for tracking statistics
     */
    public static class TrackingStats {
        public final boolean isActive;
        public final long lastUpdate;
        public final int locationsToday;
        public final int stepsToday;
        public final String currentActivity;

        public TrackingStats(boolean isActive, long lastUpdate, int locationsToday,
                             int stepsToday, String currentActivity) {
            this.isActive = isActive;
            this.lastUpdate = lastUpdate;
            this.locationsToday = locationsToday;
            this.stepsToday = stepsToday;
            this.currentActivity = currentActivity;
        }
    }
}
