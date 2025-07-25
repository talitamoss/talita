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
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * AudioActivity - UI for audio recording feature
 * Updated to use plugin system instead of hard-coded AudioCollector
 */
public class AudioActivity extends AppCompatActivity {
    
    private static final int REQUEST_AUDIO_PERMISSION = 200;
    private static final String AUDIO_PLUGIN_ID = "core.audio";
    
    // UI Components
    private Button recordButton;
    private TextView statusText;
    private TextView durationText;
    private RecyclerView recordingsRecycler;
    
    // Services
    private DataCollectorManager collectorManager;
    private DataCollector audioCollector;
    private UniversalDataService dataService;
    
    // UI State
    private Handler uiHandler;
    private Runnable durationUpdater;
    private boolean isRecording = false;
    private long recordingStartTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        
        // Initialize services
        collectorManager = DataCollectorManager.getInstance(this);
        dataService = UniversalDataService.getInstance(this);
        uiHandler = new Handler(Looper.getMainLooper());
        
        // Try to get audio collector from plugin system
        initializeAudioCollector();
        
        // Setup UI
        initializeViews();
        checkPermissions();
        loadRecordings();
    }
    
    private void initializeAudioCollector() {
        // Check if audio plugin is available
        PluginManager pluginManager = PluginManager.getInstance(this);
        DataCollectorPlugin audioPlugin = pluginManager.getPlugin(AUDIO_PLUGIN_ID);
        
        if (audioPlugin != null) {
            // Create collector from plugin
            audioCollector = audioPlugin.createCollector(this);
            if (audioCollector != null) {
                audioCollector.initialize(this);
            }
        }
        
        // If no plugin available, show message
        if (audioCollector == null) {
            Toast.makeText(this, "Audio recording plugin not available", Toast.LENGTH_LONG).show();
            // For now, create a placeholder
            createPlaceholderCollector();
        }
    }
    
    private void createPlaceholderCollector() {
        // Create a simple placeholder until AudioPlugin is implemented
        audioCollector = new SimpleDataCollector.Builder("audio", "Voice Notes")
            .description("Record voice notes")
            .emoji("🎙️")
            .category("i")
            .inputHint("Recording description")
            .inputType(SimpleDataCollector.InputType.TEXT)
            .build();
        
        audioCollector.initialize(this);
    }
    
    private void initializeViews() {
        recordButton = findViewById(R.id.record_button);
        statusText = findViewById(R.id.status_text);
        durationText = findViewById(R.id.duration_text);
        recordingsRecycler = findViewById(R.id.recordings_recycler);
        
        recordButton.setOnClickListener(v -> toggleRecording());
        
        // Setup recordings list
        recordingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recordingsRecycler.setAdapter(new RecordingsAdapter(new ArrayList<>()));
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_AUDIO_PERMISSION);
        } else {
            onPermissionGranted();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, "Audio permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void onPermissionGranted() {
        statusText.setText("Ready to record");
        recordButton.setEnabled(true);
    }
    
    private void toggleRecording() {
        if (!hasAudioPermission()) {
            checkPermissions();
            return;
        }
        
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }
    
    private void startRecording() {
        // In a real implementation, this would start actual recording
        // For now, simulate with the plugin system
        
        isRecording = true;
        recordingStartTime = System.currentTimeMillis();
        
        recordButton.setText("Stop Recording");
        statusText.setText("Recording...");
        
        // Start duration updates
        durationUpdater = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    updateDuration();
                    uiHandler.postDelayed(this, 100);
                }
            }
        };
        uiHandler.post(durationUpdater);
        
        Toast.makeText(this, "Recording started (simulated)", Toast.LENGTH_SHORT).show();
    }
    
    private void stopRecording() {
        isRecording = false;
        long duration = System.currentTimeMillis() - recordingStartTime;
        
        recordButton.setText("Start Recording");
        statusText.setText("Recording saved");
        
        // Stop duration updates
        if (durationUpdater != null) {
            uiHandler.removeCallbacks(durationUpdater);
        }
        
        // Save recording through plugin system
        saveRecording(duration);
        
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }
    
    private void saveRecording(long durationMs) {
        if (audioCollector != null) {
            // Create audio data
            Map<String, Object> audioData = new HashMap<>();
            audioData.put("duration_ms", durationMs);
            audioData.put("format", "simulated");
            audioData.put("description", "Voice note " + new Date());
            audioData.put("encrypted", true);
            
            CollectorResult result = audioCollector.collectQuick(audioData);
            
            if (result.isSuccess()) {
                loadRecordings();
            } else {
                Toast.makeText(this, "Failed to save recording", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateDuration() {
        long elapsed = System.currentTimeMillis() - recordingStartTime;
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        durationText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }
    
    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void loadRecordings() {
        // Load audio recordings from data service
        List<PersonalData> recordings = dataService.getDataByType("audio");
        
        // Update adapter
        RecordingsAdapter adapter = (RecordingsAdapter) recordingsRecycler.getAdapter();
        if (adapter != null) {
            adapter.updateRecordings(recordings);
        }
        
        statusText.setText(recordings.size() + " recordings");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
        }
        if (audioCollector != null) {
            audioCollector.onDestroy();
        }
    }
    
    /**
     * Adapter for recordings list
     */
    private class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {
        private List<PersonalData> recordings;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
        
        RecordingsAdapter(List<PersonalData> recordings) {
            this.recordings = recordings;
        }
        
        void updateRecordings(List<PersonalData> newRecordings) {
            this.recordings = newRecordings;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            view.setPadding(20, 15, 20, 15);
            view.setTextSize(16);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PersonalData recording = recordings.get(position);
            Map<String, Object> data = recording.getData();
            
            String date = dateFormat.format(new Date(recording.getTimestamp()));
            String description = data.getOrDefault("description", "Voice note").toString();
            
            long durationMs = 0;
            if (data.containsKey("duration_ms")) {
                durationMs = ((Number) data.get("duration_ms")).longValue();
            }
            
            String duration = formatDuration(durationMs);
            
            holder.textView.setText("🎙️ " + description + "\n" + date + " • " + duration);
            holder.textView.setOnClickListener(v -> playRecording(recording));
        }
        
        @Override
        public int getItemCount() {
            return recordings.size();
        }
        
        private String formatDuration(long ms) {
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
        
        private void playRecording(PersonalData recording) {
            Toast.makeText(AudioActivity.this, 
                "Playing: " + recording.getData().getOrDefault("description", "Recording"), 
                Toast.LENGTH_SHORT).show();
            // In real implementation, would decrypt and play audio file
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            
            ViewHolder(TextView view) {
                super(view);
                textView = view;
            }
        }
    }
}
