package com.core.talita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.List;

/**
 * ActivityRecognitionReceiver - Receives activity updates from Google Play Services
 * 
 * Processes detected activities and stores them through the data pipeline
 */
public class ActivityRecognitionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivityRecognition";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(context, result.getProbableActivities());
        }
    }
    
    private void handleDetectedActivities(Context context, List<DetectedActivity> activities) {
        UniversalDataService dataService = UniversalDataService.getInstance(context);
        
        for (DetectedActivity activity : activities) {
            // Only log activities with reasonable confidence
            if (activity.getConfidence() >= 50) {
                String activityName = getActivityName(activity.getType());
                int confidence = activity.getConfidence();
                
                // Create ActivityData
                ActivityData activityData = new ActivityData(activityName, confidence);
                
                // Only store significant activities to avoid spam
                if (activityData.isSignificant()) {
                    // Convert to PersonalData and store
                    PersonalData personalData = activityData.toPersonalData();
                    dataService.processData(personalData);
                    
                    Log.d(TAG, "Stored activity: " + activityData);
                } else {
                    Log.d(TAG, "Detected low-confidence activity: " + activityData);
                }
            }
        }
    }
    
    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            default:
                return "UNKNOWN";
        }
    }
}
