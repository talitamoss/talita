package com.core.talita;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AudioActivity - Audio recording with encryption
 * Records audio and automatically encrypts it before storage
 */
public class AudioActivity extends AppCompatActivity {

    private static final String TAG = "AudioActivity";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 200;

    // UI Components
    private ImageButton recordButton;
    private TextView recordingTime;
    private TextView recordingStatus;
    private CardView recordingCard;
    private RecyclerView recordingsRecycler;
    private TextView emptyStateText;

    // Recording
    private MediaRecorder mediaRecorder;
    private String currentRecordingPath;
    private boolean isRecording = false;
    private long recordingStartTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    // Data
    private UniversalDataService dataService;
    private EncryptionService encryptionService;
    private RecordingsAdapter recordingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        dataService = UniversalDataService.getInstance(this);
        encryptionService = new EncryptionService(this);

        initializeViews();
        checkPermissions();
        loadRecordings();
    }

    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        recordButton = findViewById(R.id.record_button);
        recordingTime = findViewById(R.id.recording_time);
        recordingStatus = findViewById(R.id.recording_status);
        recordingCard = findViewById(R.id.recording_card);
        recordingsRecycler = findViewById(R.id.recordings_recycler);
        emptyStateText = findViewById(R.id.empty_state_text);

        recordButton.setOnClickListener(v -> toggleRecording());

        // Setup recycler
        recordingsAdapter = new RecordingsAdapter();
        recordingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recordingsRecycler.setAdapter(recordingsAdapter);

        // Timer runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedMillis = SystemClock.elapsedRealtime() - recordingStartTime;
                    int seconds = (int) (elapsedMillis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    recordingTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio permission required for recording", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }

        try {
            // Create temp file for recording
            File audioDir = new File(getFilesDir(), AppConstants.AUDIO_FOLDER);
            audioDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "AUDIO_" + timestamp + ".m4a";
            File audioFile = new File(audioDir, fileName);
            currentRecordingPath = audioFile.getAbsolutePath();

            // Setup MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(currentRecordingPath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            // Update UI
            isRecording = true;
            recordingStartTime = SystemClock.elapsedRealtime();
            recordButton.setImageResource(R.drawable.ic_stop);
            recordingStatus.setText("Recording...");
            recordingCard.setVisibility(View.VISIBLE);
            
            // Start timer
            timerHandler.postDelayed(timerRunnable, 0);

            Log.d(TAG, "🎙️ Recording started: " + currentRecordingPath);

        } catch (IOException e) {
            Log.e(TAG, "❌ Failed to start recording", e);
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                // Calculate duration
                long duration = SystemClock.elapsedRealtime() - recordingStartTime;

                // Encrypt the audio file
                String encryptedPath = encryptionService.encryptFile(currentRecordingPath);
                
                if (encryptedPath != null) {
                    // Delete original unencrypted file
                    new File(currentRecordingPath).delete();
                    
                    // Save to database
                    saveAudioRecording(encryptedPath, duration);
                    
                    Log.d(TAG, "✅ Recording encrypted and saved");
                } else {
                    Log.e(TAG, "❌ Failed to encrypt recording");
                    Toast.makeText(this, "Failed to encrypt recording", Toast.LENGTH_SHORT).show();
                }

                // Update UI
                isRecording = false;
                recordButton.setImageResource(R.drawable.ic_mic);
                recordingStatus.setText("Tap to record");
                recordingCard.setVisibility(View.GONE);
                timerHandler.removeCallbacks(timerRunnable);
                recordingTime.setText("00:00");

                // Reload recordings
                loadRecordings();

            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to stop recording", e);
                Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAudioRecording(String encryptedPath, long duration) {
        // Create audio data
        Map<String, Object> audioData = new HashMap<>();
        audioData.put("file_path", encryptedPath);
        audioData.put("duration_ms", duration);
        audioData.put("format", "m4a");
        audioData.put("encrypted", true);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("device", android.os.Build.MODEL);
        metadata.put("app_version", BuildConfig.VERSION_NAME);
        
        PersonalData data = new PersonalData("audio", audioData, metadata, System.currentTimeMillis());
        
        // Save via UniversalDataService
        dataService.capture(data);
    }

    private void loadRecordings() {
        new Thread(() -> {
            List<PersonalData> audioData = dataService.getDataByType("audio");
            
            runOnUiThread(() -> {
                if (audioData.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    recordingsRecycler.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recordingsRecycler.setVisibility(View.VISIBLE);
                    recordingsAdapter.setRecordings(audioData);
                }
            });
        }).start();
    }

    /**
     * Recordings Adapter
     */
    private class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {
        private List<PersonalData> recordings = new ArrayList<>();

        public void setRecordings(List<PersonalData> recordings) {
            this.recordings = recordings;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio_recording, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(recordings.get(position));
        }

        @Override
        public int getItemCount() {
            return recordings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateText;
            TextView durationText;
            ImageButton playButton;
            ImageButton shareButton;
            ImageButton deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.recording_date);
                durationText = itemView.findViewById(R.id.recording_duration);
                playButton = itemView.findViewById(R.id.play_button);
                shareButton = itemView.findViewById(R.id.share_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }

            void bind(PersonalData recording) {
                // Format date
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                dateText.setText(sdf.format(new Date(recording.getTimestamp())));

                // Format duration
                Long durationMs = (Long) recording.getData().get("duration_ms");
                if (durationMs != null) {
                    int seconds = (int) (durationMs / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    durationText.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
                }

                // Play button
                playButton.setOnClickListener(v -> playRecording(recording));

                // Share button
                shareButton.setOnClickListener(v -> shareRecording(recording));

                // Delete button
                deleteButton.setOnClickListener(v -> deleteRecording(recording));
            }
        }
    }

    private void playRecording(PersonalData recording) {
        String filePath = (String) recording.getData().get("file_path");
        if (filePath != null) {
            Intent intent = new Intent(this, AudioPlaybackActivity.class);
            intent.putExtra("audio_file_path", filePath);
            intent.putExtra("audio_timestamp", recording.getTimestamp());
            
            Long duration = (Long) recording.getData().get("duration_ms");
            if (duration != null) {
                intent.putExtra("audio_duration", duration.intValue());
            }
            
            startActivity(intent);
        }
    }

    private void shareRecording(PersonalData recording) {
        Toast.makeText(this, "Share feature coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement secure sharing
    }

    private void deleteRecording(PersonalData recording) {
        // TODO: Implement delete with confirmation
        Toast.makeText(this, "Delete feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }
    }
}
