package com.core.talita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.collectors.AudioCollector;
import java.util.List;
import java.util.Locale;

/**
 * AudioActivity - UI for audio recording feature
 * Follows the clean architecture pattern: UI only, logic in AudioCollector
 */
public class AudioActivity extends AppCompatActivity implements AudioCollector.AudioRecordingListener {
    
    private static final int REQUEST_AUDIO_PERMISSION = 200;
    
    // UI Components
    private Button recordButton;
    private TextView statusText;
    private TextView durationText;
    private RecyclerView recordingsRecycler;
    
    // Business Logic
    private AudioCollector audioCollector;
    private UniversalDataService dataService;
    
    // UI State
    private Handler uiHandler;
    private Runnable durationUpdater;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        
        // Initialize services
        audioCollector = new AudioCollector(this);
        audioCollector.setListener(this);
        dataService = new UniversalDataService(this);
        
        // Setup UI
        initializeViews();
        checkPermissions();
        loadRecordings();
    }
    
    private void initializeViews() {
        recordButton = findViewById(R.id.record_button);
        statusText = findViewById(R.id.status_text);
        durationText = findViewById(R.id.duration_text);
        recordingsRecycler = findViewById(R.id.recordings_recycler);
        
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        recordButton.setOnClickListener(v -> toggleRecording());
        
        // Setup recycler view
        recordingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup UI handler for duration updates
        uiHandler = new Handler(Looper.getMainLooper());
        durationUpdater = new Runnable() {
            @Override
            public void run() {
                if (audioCollector.isRecording()) {
                    updateDuration();
                    uiHandler.postDelayed(this, 100);
                }
            }
        };
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_AUDIO_PERMISSION);
        }
    }
    
    private void toggleRecording() {
        if (audioCollector.isRecording()) {
            audioCollector.stopRecording();
        } else {
            if (hasAudioPermission()) {
                audioCollector.startRecording();
            } else {
                Toast.makeText(this, "Audio recording permission required", Toast.LENGTH_SHORT).show();
                checkPermissions();
            }
        }
    }
    
    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void updateDuration() {
        long duration = audioCollector.getCurrentDuration();
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / 1000) / 60;
        durationText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }
    
    private void loadRecordings() {
        List<PersonalData> recordings = dataService.getDataByType("audio");
        
        // Update UI based on recordings
        if (recordings.isEmpty()) {
            recordingsRecycler.setVisibility(View.GONE);
            // Show empty state if needed
        } else {
            recordingsRecycler.setVisibility(View.VISIBLE);
            // Update adapter with recordings
            // recordingsRecycler.setAdapter(new SimpleAudioAdapter(recordings));
        }
    }
    
    // AudioCollector.AudioRecordingListener implementation
    
    @Override
    public void onRecordingStarted() {
        runOnUiThread(() -> {
            recordButton.setText("Stop");
            recordButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_stop, 0, 0, 0);
            statusText.setText("Recording...");
            durationText.setVisibility(View.VISIBLE);
            
            // Start duration updates
            uiHandler.post(durationUpdater);
        });
    }
    
    @Override
    public void onRecordingStopped(String audioId, long duration) {
        runOnUiThread(() -> {
            recordButton.setText("Record");
            recordButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_mic, 0, 0, 0);
            statusText.setText("Recording saved");
            durationText.setVisibility(View.GONE);
            
            // Stop duration updates
            uiHandler.removeCallbacks(durationUpdater);
            
            // Reload recordings
            loadRecordings();
            
            // Show success message
            long seconds = duration / 1000;
            Toast.makeText(this, 
                String.format("Recording saved (%d seconds)", seconds), 
                Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onRecordingError(String error) {
        runOnUiThread(() -> {
            recordButton.setText("Record");
            recordButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_mic, 0, 0, 0);
            statusText.setText("Ready");
            durationText.setVisibility(View.GONE);
            
            // Stop duration updates
            uiHandler.removeCallbacks(durationUpdater);
            
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio recording permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Cancel recording if activity is stopping
        if (audioCollector.isRecording()) {
            audioCollector.cancelRecording();
        }
    }
}
