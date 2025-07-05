package com.core.talita;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";
    private final Context context;
    private final TextView statusTextView; // Can be null for dashboard mode
    private final Button recordButton; // Can be null for dashboard mode
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String currentRecordingPath;
    private long recordingStartTime;

    // Universal data service
    private UniversalDataService dataService;

    // Constructor - now handles null UI elements for dashboard mode
    public AudioRecorder(Context context, TextView statusTextView, Button recordButton) {
        this.context = context;
        this.statusTextView = statusTextView;
        this.recordButton = recordButton;

        // Initialize universal data service
        this.dataService = new UniversalDataService(context);

        // Only setup button if it exists (for backwards compatibility)
        if (recordButton != null) {
            setupRecordButton();
        }

        // LOG THE AUDIO STORAGE PATHS
        logAudioStoragePaths();
    }

    private void logAudioStoragePaths() {
        File filesDir = context.getFilesDir();
        File recordingsDir = new File(filesDir, "recordings");

        Log.d(TAG, "=== TALITA AUDIO STORAGE PATHS ===");
        Log.d(TAG, "Audio recordings directory: " + recordingsDir.getAbsolutePath());
        Log.d(TAG, "Using Universal Data Service: " + dataService.getClass().getSimpleName());
        Log.d(TAG, "Recordings directory exists: " + recordingsDir.exists());
        Log.d(TAG, "==================================");
    }

    private void setupRecordButton() {
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });
    }

    public void startRecording() {
        // Check for audio permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Audio recording permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create filename with timestamp
            long timestamp = System.currentTimeMillis();
            String fileName = "audio_recording_" + timestamp + ".3gp";
            File recordingsDir = new File(context.getFilesDir(), "recordings");
            if (!recordingsDir.exists()) {
                boolean created = recordingsDir.mkdirs();
                Log.d(TAG, "Created recordings directory: " + created);
            }
            currentRecordingPath = new File(recordingsDir, fileName).getAbsolutePath();

            Log.d(TAG, "Starting recording to: " + currentRecordingPath);

            // Setup MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(currentRecordingPath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            updateUI();

            Log.d(TAG, "Recording started successfully");
            Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to start recording: " + e.getMessage());
            Toast.makeText(context, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;
                updateUI();

                // Calculate recording duration
                long recordingEndTime = System.currentTimeMillis();
                long durationMs = recordingEndTime - recordingStartTime;
                int durationSeconds = (int) (durationMs / 1000);

                // Log file details
                File recordingFile = new File(currentRecordingPath);
                Log.d(TAG, "Recording stopped and saved to: " + currentRecordingPath);
                Log.d(TAG, "Recording file size: " + recordingFile.length() + " bytes");
                Log.d(TAG, "Recording file exists: " + recordingFile.exists());
                Log.d(TAG, "Recording duration: " + durationSeconds + " seconds");

                // 🎉 USE UNIVERSAL DATA SERVICE INSTEAD OF MANUAL FILE HANDLING
                saveRecordingWithUniversalService(recordingFile, durationSeconds);

                Toast.makeText(context, "Recording saved", Toast.LENGTH_SHORT).show();

            } catch (RuntimeException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to stop recording: " + e.getMessage());
                Toast.makeText(context, "Failed to stop recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveRecordingWithUniversalService(File recordingFile, int durationSeconds) {
        try {
            // Get current location (optional - you might want to add location permission)
            double latitude = 0.0;  // TODO: Get actual location if needed
            double longitude = 0.0; // TODO: Get actual location if needed

            // Create AudioData object using universal interface
            AudioData audioData = new AudioData(
                    recordingFile.getAbsolutePath(),
                    durationSeconds * 1000L, // Convert seconds to milliseconds
                    latitude,
                    longitude
            );

            // Use universal data service - handles database, future cloud backup, etc.
            dataService.capture(audioData);

            Log.d(TAG, "✅ Audio recording saved via Universal Data Service");
            Log.d(TAG, "Audio ID: " + audioData.getId());
            Log.d(TAG, "File path: " + audioData.getFilePath());
            Log.d(TAG, "Duration: " + (audioData.getDurationMs() / 1000) + " seconds");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to save via Universal Data Service: " + e.getMessage());
            e.printStackTrace();

            // Fallback to old method if universal service fails
            fallbackSaveRecordingMetadata();
        }
    }

    // Fallback method - keep the old approach as backup
    private void fallbackSaveRecordingMetadata() {
        try {
            // Old JSON file approach as fallback
            org.json.JSONObject recordingData = new org.json.JSONObject();
            recordingData.put("filename", new File(currentRecordingPath).getName());
            recordingData.put("filepath", currentRecordingPath);
            recordingData.put("timestamp", System.currentTimeMillis());
            recordingData.put("duration", "unknown");

            java.io.FileOutputStream fos = context.openFileOutput("audio_recordings.txt", Context.MODE_APPEND);
            fos.write((recordingData.toString() + "\n").getBytes());
            fos.close();

            Log.d(TAG, "⚠️ Used fallback JSON metadata saving");

        } catch (Exception e) {
            Log.e(TAG, "❌ Even fallback saving failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUI() {
        // Only update UI if elements exist (dashboard mode might not have them)
        if (recordButton != null && statusTextView != null) {
            if (isRecording) {
                recordButton.setText("Stop Recording");
                statusTextView.setText("Recording audio...");
            } else {
                recordButton.setText("Start Recording");
                statusTextView.setText("Ready to record");
            }
        }
    }

    // Clean up method - call this when the activity is destroyed
    public void cleanup() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    // Get recordings directory for file management
    public File getRecordingsDirectory() {
        return new File(context.getFilesDir(), "recordings");
    }

    // Check if currently recording
    public boolean isRecording() {
        return isRecording;
    }

    // Method to get all audio recordings via universal service
    public java.util.List<LocalDataManager.DataItem> getAllRecordings() {
        try {
            return dataService.getDataByType("audio");
        } catch (Exception e) {
            Log.e(TAG, "Failed to get recordings via universal service: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}