package com.core.talita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivityRecognitionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(context, result);
        }
    }

    private void handleDetectedActivities(Context context, ActivityRecognitionResult result) {
        UniversalDataService dataService = new UniversalDataService(context);
        
        // Get the most probable activity
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        int confidence = mostProbableActivity.getConfidence();
        
        if (confidence > 75) { // Only log high confidence activities
            String activityName = getActivityName(mostProbableActivity.getType());
            
            try {
                // Create ActivityData with correct constructor (2 parameters)
                ActivityData activityData = new ActivityData(activityName, confidence);
                
                // Save using UniversalDataService
                String id = dataService.capture(activityData);
                
                if (id != null) {
                    Log.d(TAG, "Activity saved: " + activityName + " (" + confidence + "%)");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to save activity", e);
            }
        }
    }

    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.UNKNOWN:
            default:
                return "Unknown";
        }
    }
}
