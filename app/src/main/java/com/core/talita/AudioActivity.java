package com.core.talita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AudioActivity extends AppCompatActivity {
    private static final String TAG = "AudioActivity";
    private static final int REQUEST_AUDIO_PERMISSION = 1;

    private AudioRecorder audioRecorder;
    private TextView recordingStatus;
    private TextView recordingTimer;
    private CardView recordButtonCard;
    private TextView recordButtonIcon;
    private TextView recordButtonText;
    private TextView recordingInstructions;
    private TextView recordingsCount;
    private RecyclerView recordingsRecycler;

    private AudioRecordingsAdapter adapter;
    private List<AudioRecord> audioRecordings = new ArrayList<>();

    // Universal data service with encryption
    private UniversalDataService dataService;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime = 0;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        // Initialize universal data service with encryption
        dataService = new UniversalDataService(this);

        initializeViews();
        setupRecordButton();
        setupRecyclerView();
        loadAudioHistory();
        checkAudioPermission();

        Log.d(TAG, "🔐 AudioActivity initialized with encryption support");
    }

    private void initializeViews() {
        recordingStatus = findViewById(R.id.recording_status);
        recordingTimer = findViewById(R.id.recording_timer);
        recordButtonCard = findViewById(R.id.record_button_card);
        recordButtonIcon = findViewById(R.id.record_button_icon);
        recordButtonText = findViewById(R.id.record_button_text);
        recordingInstructions = findViewById(R.id.recording_instructions);
        recordingsCount = findViewById(R.id.recordings_count);
        recordingsRecycler = findViewById(R.id.recordings_recycler);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecordButton() {
        // Initialize AudioRecorder without UI elements (we'll handle UI ourselves)
        audioRecorder = new AudioRecorder(this, null, null);

        recordButtonCard.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AudioRecordingsAdapter(audioRecordings, this::openPlayback);
        recordingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recordingsRecycler.setAdapter(adapter);
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Audio permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        audioRecorder.startRecording();
        isRecording = true;
        startTime = System.currentTimeMillis();

        updateRecordingUI();
        startTimer();
    }

    private void stopRecording() {
        audioRecorder.stopRecording();
        isRecording = false;

        updateRecordingUI();
        stopTimer();

        // Refresh recordings list using encrypted universal service
        loadAudioHistory();
    }

    private void updateRecordingUI() {
        if (isRecording) {
            recordingStatus.setText("Recording...");
            recordButtonIcon.setText("⏹");
            recordButtonText.setText("STOP");
            recordingInstructions.setText("Tap to stop recording");
            recordingTimer.setVisibility(TextView.VISIBLE);
            recordButtonCard.setCardBackgroundColor(0xFF757575); // Gray when recording
        } else {
            recordingStatus.setText("Ready to Record");
            recordButtonIcon.setText("●");
            recordButtonText.setText("RECORD");
            recordingInstructions.setText("Tap to start recording");
            recordingTimer.setVisibility(TextView.GONE);
            recordButtonCard.setCardBackgroundColor(0xFFFF5252); // Red when ready
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedTime / 1000) % 60;
                    int minutes = (int) (elapsedTime / (1000 * 60));

                    recordingTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void loadAudioHistory() {
        try {
            Log.d(TAG, "🔓 Loading encrypted audio history via Universal Data Service...");

            // 🔐 USE ENCRYPTED UNIVERSAL DATA SERVICE
            List<UniversalDataService.DecryptedDataItem> encryptedAudioData =
                    dataService.getDecryptedDataByType("audio");

            audioRecordings.clear();

            for (UniversalDataService.DecryptedDataItem dataItem : encryptedAudioData) {
                try {
                    String encryptedFilePath = dataItem.encryptedFilePath;
                    long timestamp = dataItem.timestamp;

                    // Parse duration from the decrypted data JSON
                    long durationMs = dataItem.decryptedData.optLong("duration_ms", 0);
                    int durationSeconds = (int) (durationMs / 1000);

                    // For UI purposes, we'll show the original filename but store encrypted path
                    String displayName = "audio_recording.3gp"; // Default name
                    if (encryptedFilePath != null) {
                        String originalName = new File(encryptedFilePath).getName();
                        // Remove .enc extension for display
                        if (originalName.endsWith(".enc")) {
                            displayName = originalName.substring(0, originalName.length() - 4);
                        } else {
                            displayName = originalName;
                        }
                    }

                    // Check if encrypted file exists
                    if (encryptedFilePath != null && new File(encryptedFilePath).exists()) {
                        // Create AudioRecord with encrypted file path (will be decrypted on playback)
                        AudioRecord record = new AudioRecord(
                                displayName,
                                encryptedFilePath, // Store encrypted path
                                timestamp,
                                durationSeconds
                        );
                        audioRecordings.add(record);

                        Log.d(TAG, "🔓 Loaded encrypted audio: " + displayName + " (" + durationSeconds + "s)");
                    } else {
                        Log.w(TAG, "⚠️ Encrypted audio file missing: " + encryptedFilePath);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing encrypted audio data: " + e.getMessage());
                }
            }

            // Sort by timestamp (most recent first)
            Collections.sort(audioRecordings, (a, b) -> Long.compare(b.timestamp, a.timestamp));

            // Update UI
            updateRecordingsCount();
            adapter.notifyDataSetChanged();

            Log.d(TAG, "✅ Loaded " + audioRecordings.size() + " encrypted audio recordings");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to load encrypted audio, trying fallback...", e);

            // Fallback to old unencrypted method
            loadAudioHistoryFallback();
        }
    }

    // Fallback method - keep the old approach as backup
    private void loadAudioHistoryFallback() {
        try {
            Log.d(TAG, "📄 Using fallback JSON file loading...");

            FileInputStream fis = openFileInput("audio_recordings.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            audioRecordings.clear();

            while ((line = reader.readLine()) != null) {
                try {
                    org.json.JSONObject recordingData = new org.json.JSONObject(line);
                    String filename = recordingData.getString("filename");
                    String filepath = recordingData.getString("filepath");
                    long timestamp = recordingData.getLong("timestamp");

                    // Check if file still exists
                    File audioFile = new File(filepath);
                    if (audioFile.exists()) {
                        AudioRecord record = new AudioRecord(filename, filepath, timestamp, 0); // Duration unknown in old format
                        audioRecordings.add(record);
                    }

                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
            reader.close();

            // Sort by timestamp (most recent first)
            Collections.sort(audioRecordings, (a, b) -> Long.compare(b.timestamp, a.timestamp));

            // Update UI
            updateRecordingsCount();
            adapter.notifyDataSetChanged();

            Log.d(TAG, "⚠️ Loaded " + audioRecordings.size() + " recordings via fallback method");

        } catch (IOException e) {
            // File doesn't exist yet - that's ok
            Log.d(TAG, "📄 No audio recordings found (this is normal for first run)");
            updateRecordingsCount();
        }
    }

    private void updateRecordingsCount() {
        int count = audioRecordings.size();
        recordingsCount.setText(count + " recordings");
    }

    private void openPlayback(AudioRecord record) {
        Intent intent = new Intent(this, AudioPlaybackActivity.class);
        intent.putExtra("audio_file_path", record.filepath);
        intent.putExtra("audio_filename", record.filename);
        intent.putExtra("audio_timestamp", record.timestamp);
        intent.putExtra("audio_duration", record.duration);
        startActivity(intent);
    }

    private void checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (audioRecorder != null) {
            audioRecorder.cleanup();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio permission required for recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Enhanced AudioRecord class with duration support
    public static class AudioRecord {
        public final String filename;
        public final String filepath;
        public final long timestamp;
        public final int duration; // in seconds
        public final String formattedTime;
        public final String displayName;
        public final String durationText;

        public AudioRecord(String filename, String filepath, long timestamp, int duration) {
            this.filename = filename;
            this.filepath = filepath;
            this.timestamp = timestamp;
            this.duration = duration;

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            this.formattedTime = sdf.format(new Date(timestamp));

            // Create a friendly display name
            SimpleDateFormat nameSdf = new SimpleDateFormat("MMM dd - HH:mm", Locale.getDefault());
            this.displayName = "Recording " + nameSdf.format(new Date(timestamp));

            // Format duration
            if (duration > 0) {
                int minutes = duration / 60;
                int seconds = duration % 60;
                this.durationText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            } else {
                this.durationText = "Unknown";
            }
        }

        // Backwards compatibility constructor
        public AudioRecord(String filename, String filepath, long timestamp) {
            this(filename, filepath, timestamp, 0);
        }
    }
}