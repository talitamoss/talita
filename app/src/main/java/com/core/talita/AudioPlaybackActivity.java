package com.core.talita;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioPlaybackActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar audioProgress;
    private TextView currentTime;
    private TextView totalTime;
    private TextView playPauseIcon;
    private TextView recordingName;
    private TextView recordingDate;
    private TextView recordingDuration;

    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private String audioFilePath;
    private String audioFilename;
    private long audioTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_playback);

        getIntentData();
        initializeViews();
        setupAudioPlayer();
        setupControls();
        displayRecordingInfo();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        audioFilePath = intent.getStringExtra("audio_file_path");
        audioFilename = intent.getStringExtra("audio_filename");
        audioTimestamp = intent.getLongExtra("audio_timestamp", 0);
    }

    private void initializeViews() {
        audioProgress = findViewById(R.id.audio_progress);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        playPauseIcon = findViewById(R.id.play_pause_icon);
        recordingName = findViewById(R.id.recording_name);
        recordingDate = findViewById(R.id.recording_date);
        recordingDuration = findViewById(R.id.recording_duration);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupAudioPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                int duration = mp.getDuration();
                audioProgress.setMax(duration);
                totalTime.setText(formatTime(duration));

                // Update duration display
                recordingDuration.setText("Duration: " + formatTime(duration));
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseIcon.setText("▶");
                audioProgress.setProgress(0);
                currentTime.setText("00:00");
                stopProgressUpdate();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading audio file", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupControls() {
        // Play/Pause Button
        CardView playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Rewind Button (10 seconds back)
        CardView rewindButton = findViewById(R.id.rewind_button);
        rewindButton.setOnClickListener(v -> {
            if (isPrepared) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = Math.max(0, currentPosition - 10000); // 10 seconds back
                mediaPlayer.seekTo(newPosition);
                audioProgress.setProgress(newPosition);
                currentTime.setText(formatTime(newPosition));
            }
        });

        // Fast Forward Button (10 seconds forward)
        CardView fastForwardButton = findViewById(R.id.fast_forward_button);
        fastForwardButton.setOnClickListener(v -> {
            if (isPrepared) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int newPosition = Math.min(duration, currentPosition + 10000); // 10 seconds forward
                mediaPlayer.seekTo(newPosition);
                audioProgress.setProgress(newPosition);
                currentTime.setText(formatTime(newPosition));
            }
        });

        // SeekBar for scrubbing
        audioProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isPrepared) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Delete Button
        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        // Share/Export Button
        Button shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> exportAudioFile());
    }

    private void displayRecordingInfo() {
        // Create friendly display name
        SimpleDateFormat nameSdf = new SimpleDateFormat("MMM dd - HH:mm", Locale.getDefault());
        String displayName = "Recording " + nameSdf.format(new Date(audioTimestamp));
        recordingName.setText(displayName);

        // Show full date and time
        SimpleDateFormat dateSdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        recordingDate.setText(dateSdf.format(new Date(audioTimestamp)));

        // Duration will be set when audio is prepared
        recordingDuration.setText("Duration: Loading...");
    }

    private void togglePlayPause() {
        if (!isPrepared) {
            Toast.makeText(this, "Audio is still loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseIcon.setText("▶");
            stopProgressUpdate();
        } else {
            mediaPlayer.start();
            isPlaying = true;
            playPauseIcon.setText("⏸");
            startProgressUpdate();
        }
    }

    private void startProgressUpdate() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    audioProgress.setProgress(currentPosition);
                    currentTime.setText(formatTime(currentPosition));
                    progressHandler.postDelayed(this, 1000);
                }
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdate() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recording")
                .setMessage("Are you sure you want to delete this recording? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteRecording())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecording() {
        // Stop playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Delete the file
        File audioFile = new File(audioFilePath);
        if (audioFile.exists()) {
            boolean deleted = audioFile.delete();
            if (deleted) {
                Toast.makeText(this, "Recording deleted", Toast.LENGTH_SHORT).show();
                finish(); // Return to recordings list
            } else {
                Toast.makeText(this, "Failed to delete recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportAudioFile() {
        try {
            File audioFile = new File(audioFilePath);
            Uri fileUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider", audioFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Audio Recording");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Export Audio Recording"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying) {
            togglePlayPause(); // Pause playback when leaving the activity
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgressUpdate();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}