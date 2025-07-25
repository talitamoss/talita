package com.core.talita;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONObject;
import com.core.talita.StepData;

/**
 * Background Tracking Service - Runs in foreground to track location/activity
 * 
 * Features:
 * - Continuous location tracking with battery optimization
 * - Activity recognition (walking, running, driving, etc.)
 * - Step counting integration
 * - Smart tracking intervals based on movement
 */
public class BackgroundTrackingService extends Service implements LocationListener, SensorEventListener {

    private static final String TAG = "BackgroundTracking";
    private static final int NOTIFICATION_ID = AppConstants.TRACKING_NOTIFICATION_ID;
    private static final long LOCATION_INTERVAL_MOVING = 30 * 1000; // 30 seconds when moving
    private static final long LOCATION_INTERVAL_STATIONARY = 5 * 60 * 1000; // 5 minutes when stationary
    private static final float LOCATION_MIN_DISTANCE = 10.0f; // 10 meters

    // Services
    private UniversalDataService dataService;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Handler handler;

    // Sensors
    private Sensor stepCounterSensor;

    // State
    private boolean isTracking = false;
    private boolean isMoving = false;
    private Location lastKnownLocation;
    private long lastLocationTime = 0;
    private int dailyStepCount = 0;
    private int stepCountOffset = 0;
    private String currentActivity = "unknown";
    private int locationsToday = 0;
    private long serviceStartTime;

    @Override
    public void onCreate() {
        super.onCreate();

        serviceStartTime = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());

        // Initialize services
        dataService = new UniversalDataService(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Initialize sensors
        initializeStepCounter();

        Log.d(TAG, "🎯 Background tracking service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        // Start foreground with location service type for Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, createNotification());
        }

        startLocationTracking();

        Log.d(TAG, "🚀 Background tracking started");
        return START_STICKY; // Restart if killed by system
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't bind to this service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
        stopStepTracking();
        Log.d(TAG, "🛑 Background tracking service destroyed");
    }

    /**
     * Initialize step counter sensor
     */
    private void initializeStepCounter() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "📱 Step counter sensor initialized");
        } else {
            Log.w(TAG, "⚠️ Step counter sensor not available on this device");
        }
    }

    /**
     * Start location tracking with smart intervals
     */
    private void startLocationTracking() {
        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "❌ Location permission not granted");
            return;
        }

        try {
            long interval = isMoving ? LOCATION_INTERVAL_MOVING : LOCATION_INTERVAL_STATIONARY;

            // Request location updates from GPS
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    interval,
                    LOCATION_MIN_DISTANCE,
                    this
            );

            // Also use network provider as backup
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    interval * 2, // Less frequent for network
                    LOCATION_MIN_DISTANCE * 2,
                    this
            );

            isTracking = true;
            Log.d(TAG, "📍 Location tracking started with interval: " + interval + "ms");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start location tracking", e);
        }
    }

    /**
     * Stop location tracking
     */
    private void stopLocationTracking() {
        try {
            locationManager.removeUpdates(this);
            isTracking = false;
            Log.d(TAG, "📍 Location tracking stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location tracking", e);
        }
    }

    /**
     * Stop step tracking
     */
    private void stopStepTracking() {
        try {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "👣 Step tracking stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping step tracking", e);
        }
    }

    // LocationListener implementation

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "📍 New location: " + location.getLatitude() + ", " + location.getLongitude());

        // Calculate distance from last location
        float distance = 0;
        if (lastKnownLocation != null) {
            distance = location.distanceTo(lastKnownLocation);
        }

        // Determine if we should save this location
        long currentTime = System.currentTimeMillis();
        boolean significantMovement = distance > LOCATION_MIN_DISTANCE;
        boolean significantTime = currentTime - lastLocationTime > LOCATION_INTERVAL_STATIONARY;

        if (significantMovement || significantTime) {
            // Create location data with all available information
            LocationData locationData = new LocationData.Builder(location.getLatitude(), location.getLongitude())
                    .accuracy(location.getAccuracy())
                    .provider(location.getProvider())
                    .speed(location.getSpeed())
                    .bearing(location.getBearing())
                    .activity(currentActivity, 0)  // Activity confidence would come from activity recognition
                    .steps(dailyStepCount)
                    .build();

            // Save via Universal Data Service (automatic encryption)
            dataService.captureData(locationData);

            locationsToday++;
            lastKnownLocation = location;
            lastLocationTime = currentTime;

            // Update movement state
            isMoving = location.getSpeed() > 1.0; // 1 m/s threshold

            // Update notification
            updateNotification();

            Log.d(TAG, "📍 Location saved: " + locationData.getDisplaySummary());
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

    // SensorEventListener implementation

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            // Calculate daily steps (reset at midnight)
            if (isNewDay()) {
                stepCountOffset = totalSteps;
                dailyStepCount = 0;
            } else {
                dailyStepCount = totalSteps - stepCountOffset;
            }

            // Save step data every 100 steps
            if (dailyStepCount % 100 == 0 && dailyStepCount > 0) {
                StepData stepData = new StepData(dailyStepCount);
                dataService.captureData(stepData);
                Log.d(TAG, "👣 Steps saved: " + dailyStepCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    /**
     * Check if it's a new day
     */
    private boolean isNewDay() {
        Calendar now = Calendar.getInstance();
        Calendar lastCheck = Calendar.getInstance();
        lastCheck.setTimeInMillis(lastLocationTime);

        return now.get(Calendar.DAY_OF_YEAR) != lastCheck.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Create notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_TRACKING,
                    "Background Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows when location tracking is active");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Create foreground notification
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String contentText = String.format("📍 %d locations • 👣 %d steps today",
                locationsToday, dailyStepCount);

        return new NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_TRACKING)
                .setContentTitle("Tracking your day")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Update notification with current stats
     */
    private void updateNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }
}

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("steps", steps);
        return metadata;
    }

    @Override
    public String getType() {
        return "steps";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("type", "steps");
            json.put("steps", steps);
            json.put("timestamp", timestamp);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getLatitude() {
        return 0.0;
    }

    @Override
    public double getLongitude() {
        return 0.0;
    }

    @Override
    public String getDisplayName() {
        return steps + " steps";
    }

    @Override
    public String getDisplaySummary() {
        return getDisplayName();
    }
}
