package com.core.talita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Receives activity recognition updates from Google Play Services
 * Translates detected activities into readable strings and forwards to background service
 */
public class ActivityRecognitionReceiver extends BroadcastReceiver {

    private static final String TAG = "ActivityRecognition";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            String activityName = getActivityName(mostProbableActivity.getType());
            int confidence = mostProbableActivity.getConfidence();

            Log.d(TAG, "🏃 Activity detected: " + activityName + " (" + confidence + "% confidence)");

            // Only process high-confidence activities
            if (confidence > 50) {
                // Save activity data
                UniversalDataService dataService = new UniversalDataService(context);
                ActivityData activityData = new ActivityData(activityName, confidence, null);
                dataService.capture(activityData);

                // Update background service if running
                updateBackgroundService(context, activityName);
            }
        }
    }

    /**
     * Convert Google's activity types to readable strings
     */
    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.STILL:
                return "stationary";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.ON_BICYCLE:
                return "cycling";
            case DetectedActivity.IN_VEHICLE:
                return "driving";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.TILTING:
                return "tilting";
            default:
                return "unknown";
        }
    }

    /**
     * Update the background service with new activity
     */
    private void updateBackgroundService(Context context, String activity) {
        // Use a static method or singleton to communicate with the service
        // For now, we'll use a simple broadcast
        Intent updateIntent = new Intent("com.core.talita.ACTIVITY_UPDATE");
        updateIntent.putExtra("activity", activity);
        context.sendBroadcast(updateIntent);
    }
}