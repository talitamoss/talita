package com.core.talita;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

/**
 * Background Tracking Service - Continuously collects user data
 *
 * Features:
 * - Foreground service (survives app closure)
 * - Location tracking with smart intervals
 * - Step counting with daily totals
 * - Activity recognition (walking, driving, etc.)
 * - Battery-optimized collection
 * - Automatic encryption via Universal Data Service
 */
public class BackgroundTrackingService extends Service implements LocationListener, SensorEventListener {

    private static final String TAG = "BackgroundTracking";
    private static final String CHANNEL_ID = "TalitaTracking";
    private static final int NOTIFICATION_ID = 1001;

    // Tracking intervals (in milliseconds)
    private static final long LOCATION_INTERVAL_STATIONARY = 5 * 60 * 1000; // 5 minutes when stationary
    private static final long LOCATION_INTERVAL_MOVING = 30 * 1000; // 30 seconds when moving
    private static final long LOCATION_MIN_DISTANCE = 10; // 10 meters minimum distance

    // Data collection state
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private UniversalDataService dataService;
    private Handler handler;

    // Tracking state
    private Location lastKnownLocation;
    private long lastLocationTime = 0;
    private int dailyStepCount = 0;
    private int stepCountOffset = 0; // For daily reset
    private String currentActivity = "unknown";
    private boolean isMoving = false;

    // Statistics
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
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

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    interval,
                    LOCATION_MIN_DISTANCE,
                    this
            );

            // Also try network provider as backup
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    interval * 2, // Less frequent for network
                    LOCATION_MIN_DISTANCE * 2,
                    this
            );

            Log.d(TAG, "📍 Location tracking started (interval: " + (interval/1000) + "s)");

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
        }
    }

    /**
     * Stop location tracking
     */
    private void stopLocationTracking() {
        try {
            locationManager.removeUpdates(this);
            Log.d(TAG, "📍 Location tracking stopped");
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Error stopping location tracking: " + e.getMessage());
        }
    }

    /**
     * Stop step tracking
     */
    private void stopStepTracking() {
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "👣 Step tracking stopped");
        }
    }

    /**
     * Handle location updates
     */
    @Override
    public void onLocationChanged(Location location) {
        long currentTime = System.currentTimeMillis();

        // Avoid spam - minimum time between locations
        if (currentTime - lastLocationTime < 10000) { // 10 seconds minimum
            return;
        }

        // Calculate distance from last location
        float distance = 0;
        if (lastKnownLocation != null) {
            distance = location.distanceTo(lastKnownLocation);
        }

        // Only save if significant movement or time passed
        boolean significantMovement = distance > LOCATION_MIN_DISTANCE;
        boolean significantTime = currentTime - lastLocationTime > LOCATION_INTERVAL_STATIONARY;

        if (significantMovement || significantTime) {
            // Create enhanced location data
            EnhancedLocationData locationData = new EnhancedLocationData(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAccuracy(),
                    location.getProvider(),
                    location.getSpeed(),
                    location.getBearing(),
                    currentActivity,
                    dailyStepCount
            );

            // Save via Universal Data Service (automatic encryption)
            String dataId = dataService.capture(locationData);

            if (dataId != null) {
                locationsToday++;
                lastKnownLocation = location;
                lastLocationTime = currentTime;

                // Update movement state
                isMoving = location.getSpeed() > 1.0; // 1 m/s threshold

                // Update notification
                updateNotification();

                Log.d(TAG, "📍 Location saved: " + locationData.getDisplaySummary() +
                        " (Activity: " + currentActivity + ", Steps: " + dailyStepCount + ")");
            }
        }
    }

    /**
     * Handle step counter updates
     */
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
                StepData stepData = new StepData(dailyStepCount, lastKnownLocation);
                dataService.capture(stepData);

                Log.d(TAG, "👣 Step milestone: " + dailyStepCount + " daily steps");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counter
    }

    /**
     * Update activity from activity recognition
     */
    public void updateActivity(String activity) {
        if (!activity.equals(currentActivity)) {
            currentActivity = activity;

            // Adjust tracking based on activity
            boolean wasMoving = isMoving;
            isMoving = activity.equals("walking") || activity.equals("running") ||
                    activity.equals("on_bicycle") || activity.equals("in_vehicle");

            // Restart location tracking with new interval if movement state changed
            if (wasMoving != isMoving) {
                stopLocationTracking();
                startLocationTracking();
            }

            Log.d(TAG, "🏃 Activity changed to: " + activity + " (Moving: " + isMoving + ")");
        }
    }

    /**
     * Check if it's a new day (for step counter reset)
     */
    private boolean isNewDay() {
        // Simple check - in production you'd want to handle timezone changes
        long currentDay = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        long serviceDay = serviceStartTime / (24 * 60 * 60 * 1000);
        return currentDay > serviceDay;
    }

    /**
     * Create notification channel for Android 8+
     */
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Talita Background Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Continuously tracks your location and activity");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Create persistent notification
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, DataCollectionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Talita is tracking")
                .setContentText("📍 " + locationsToday + " locations • 👣 " + dailyStepCount + " steps")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
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

    // Required LocationListener methods
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "📍 Location provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "📍 Location provider disabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "📍 Location provider status changed: " + provider + " status: " + status);
    }
}