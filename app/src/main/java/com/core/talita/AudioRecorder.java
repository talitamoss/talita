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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";
    private final Context context;
    private final TextView statusTextView;
    private final Button recordButton;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String currentRecordingPath;

    // Constructor
    public AudioRecorder(Context context, TextView statusTextView, Button recordButton) {
        this.context = context;
        this.statusTextView = statusTextView;
        this.recordButton = recordButton;
        setupRecordButton();

        // LOG THE AUDIO STORAGE PATHS
        logAudioStoragePaths();
    }

    private void logAudioStoragePaths() {
        File filesDir = context.getFilesDir();
        File recordingsDir = new File(filesDir, "recordings");
        File audioMetaFile = new File(filesDir, "audio_recordings.txt");

        Log.d(TAG, "=== TALITA AUDIO STORAGE PATHS ===");
        Log.d(TAG, "Audio recordings directory: " + recordingsDir.getAbsolutePath());
        Log.d(TAG, "Audio metadata file: " + audioMetaFile.getAbsolutePath());
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

                // Log file details
                File recordingFile = new File(currentRecordingPath);
                Log.d(TAG, "Recording stopped and saved to: " + currentRecordingPath);
                Log.d(TAG, "Recording file size: " + recordingFile.length() + " bytes");
                Log.d(TAG, "Recording file exists: " + recordingFile.exists());

                // Save recording metadata
                saveRecordingMetadata();

                Toast.makeText(context, "Recording saved", Toast.LENGTH_SHORT).show();

            } catch (RuntimeException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to stop recording: " + e.getMessage());
                Toast.makeText(context, "Failed to stop recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        if (isRecording) {
            recordButton.setText("Stop Recording");
            statusTextView.setText("Recording audio...");
        } else {
            recordButton.setText("Start Recording");
            statusTextView.setText("Ready to record");
        }
    }

    private void saveRecordingMetadata() {
        try {
            JSONObject recordingData = new JSONObject();
            recordingData.put("filename", new File(currentRecordingPath).getName());
            recordingData.put("filepath", currentRecordingPath);
            recordingData.put("timestamp", System.currentTimeMillis());
            recordingData.put("duration", "unknown"); // You could calculate this if needed

            File metadataFile = new File(context.getFilesDir(), "audio_recordings.txt");
            FileOutputStream fos = context.openFileOutput("audio_recordings.txt", Context.MODE_APPEND);
            fos.write((recordingData.toString() + "\n").getBytes());
            fos.close();

            Log.d(TAG, "Recording metadata saved to: " + metadataFile.getAbsolutePath());
            Log.d(TAG, "Metadata: " + recordingData.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to save recording metadata: " + e.getMessage());
            Toast.makeText(context, "Failed to save recording metadata", Toast.LENGTH_SHORT).show();
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
}