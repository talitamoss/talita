package com.core.talita.collectors;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import com.core.talita.AudioData;
import com.core.talita.UniversalDataService;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AudioCollector - Handles all audio recording business logic
 * Follows the Collector pattern for consistency across features
 */
public class AudioCollector {
    private static final String TAG = "AudioCollector";
    
    private final Context context;
    private final UniversalDataService dataService;
    private MediaRecorder recorder;
    private String currentFilePath;
    private long recordingStartTime;
    private boolean isRecording = false;
    
    // Listener for UI updates
    public interface AudioRecordingListener {
        void onRecordingStarted();
        void onRecordingStopped(String audioId, long duration);
        void onRecordingError(String error);
    }
    
    private AudioRecordingListener listener;
    
    public AudioCollector(Context context) {
        this.context = context;
        this.dataService = new UniversalDataService(context);
    }
    
    public void setListener(AudioRecordingListener listener) {
        this.listener = listener;
    }
    
    /**
     * Start recording audio
     */
    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording");
            return;
        }
        
        try {
            // Create audio directory
            File audioDir = new File(context.getFilesDir(), "audio");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            
            // Generate filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            currentFilePath = new File(audioDir, "REC_" + timestamp + ".3gp").getAbsolutePath();
            
            // Setup recorder
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(currentFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            
            recorder.prepare();
            recorder.start();
            
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            
            Log.d(TAG, "Recording started: " + currentFilePath);
            
            if (listener != null) {
                listener.onRecordingStarted();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            if (listener != null) {
                listener.onRecordingError("Failed to start recording: " + e.getMessage());
            }
            cleanup();
        }
    }
    
    /**
     * Stop recording and save
     */
    public void stopRecording() {
        if (!isRecording || recorder == null) {
            Log.w(TAG, "Not recording");
            return;
        }
        
        try {
            recorder.stop();
            long duration = System.currentTimeMillis() - recordingStartTime;
            
            // Save the recording
            String audioId = saveRecording(duration);
            
            if (listener != null) {
                listener.onRecordingStopped(audioId, duration);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop recording", e);
            if (listener != null) {
                listener.onRecordingError("Failed to stop recording: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    /**
     * Cancel recording without saving
     */
    public void cancelRecording() {
        if (!isRecording) return;
        
        cleanup();
        
        // Delete the file
        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    /**
     * Save recording using UniversalDataService
     */
    private String saveRecording(long duration) {
        // Create AudioData
        AudioData audioData = new AudioData(currentFilePath, duration);
        
        // Capture through UniversalDataService (handles encryption, backup, etc.)
        String id = dataService.capture(audioData);
        
        if (id != null) {
            Log.d(TAG, "Audio saved successfully: " + id);
        } else {
            Log.e(TAG, "Failed to save audio");
        }
        
        return id;
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (recorder != null) {
            try {
                recorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing recorder", e);
            }
            recorder = null;
        }
        isRecording = false;
        currentFilePath = null;
    }
    
    /**
     * Check if currently recording
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Get current recording duration in milliseconds
     */
    public long getCurrentDuration() {
        if (isRecording) {
            return System.currentTimeMillis() - recordingStartTime;
        }
        return 0;
    }
}
