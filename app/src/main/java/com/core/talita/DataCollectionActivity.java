package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DataCollectionActivity extends AppCompatActivity {
    private static final String TAG = "DataCollectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        setupCollectorTiles();

        Log.d(TAG, "📊 Data Collection Activity initialized");
    }

    private void setupCollectorTiles() {
        // Water Tile
        CardView waterTile = findViewById(R.id.water_tile);
        waterTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "water");
            startActivity(intent);
        });

        // Exercise Tile
        CardView exerciseTile = findViewById(R.id.exercise_tile);
        exerciseTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "exercise");
            startActivity(intent);
        });

        // Mood Tile
        CardView moodTile = findViewById(R.id.mood_tile);
        moodTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "mood");
            startActivity(intent);
        });

        // Location Tile
        CardView locationTile = findViewById(R.id.location_tile);
        locationTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });

        // Audio Tile
        CardView audioTile = findViewById(R.id.audio_tile);
        audioTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
        });

        // Sleep Tile
        CardView sleepTile = findViewById(R.id.sleep_tile);
        sleepTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "sleep");
            startActivity(intent);
        });

        // Nutrition Tile
        CardView nutritionTile = findViewById(R.id.nutrition_tile);
        nutritionTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "nutrition");
            startActivity(intent);
        });

        // Substance Tile
        CardView substanceTile = findViewById(R.id.substance_tile);
        substanceTile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("focus_collector", "substance");
            startActivity(intent);
        });

        // Coming Soon Tile
        CardView comingSoonTile = findViewById(R.id.coming_soon_tile);
        comingSoonTile.setOnClickListener(v -> {
            Toast.makeText(this, "➕ More collectors coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Back Button
        CardView backTile = findViewById(R.id.back_tile);
        backTile.setOnClickListener(v -> {
            finish(); // Go back to home
        });
    }
}