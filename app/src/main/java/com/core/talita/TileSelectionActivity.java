package com.core.talita;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for users to select which data tiles appear on their dashboard
 */
public class TileSelectionActivity extends AppCompatActivity {

    private RecyclerView tileOptionsRecycler;
    private TileSelectionAdapter adapter;
    private List<TileOption> tileOptions;
    private SharedPreferences dashboardPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_selection);

        dashboardPrefs = getSharedPreferences("dashboard_tiles", MODE_PRIVATE);

        initializeViews();
        setupTileOptions();
        setupRecyclerView();
    }

    private void initializeViews() {
        tileOptionsRecycler = findViewById(R.id.tile_options_recycler);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupTileOptions() {
        tileOptions = new ArrayList<>();

        // Core wellness tiles
        tileOptions.add(new TileOption("💤", "Sleep", "sleep", "Track sleep duration and quality"));
        tileOptions.add(new TileOption("🏃", "Movement", "movement", "Log exercises and activities"));
        tileOptions.add(new TileOption("💧", "Hydration", "hydration", "Track water intake"));
        tileOptions.add(new TileOption("😊", "Mood", "mood", "Record emotional state"));

        // Nutrition & substances
        tileOptions.add(new TileOption("🍽️", "Nutrition", "nutrition", "Log meals and food"));
        tileOptions.add(new TileOption("☕", "Caffeine", "caffeine", "Track coffee and tea"));
        tileOptions.add(new TileOption("🍺", "Alcohol", "alcohol", "Log alcoholic beverages"));
        tileOptions.add(new TileOption("🚬", "Smoking", "smoking", "Track cigarettes and tobacco"));
        tileOptions.add(new TileOption("💊", "Medication", "medication", "Log medications and supplements"));

        // Health & biometrics
        tileOptions.add(new TileOption("📏", "Weight", "weight", "Track body weight"));
        tileOptions.add(new TileOption("❤️", "Heart Rate", "heartrate", "Monitor heart rate"));
        tileOptions.add(new TileOption("🌡️", "Temperature", "temperature", "Track body temperature"));
        tileOptions.add(new TileOption("🩸", "Blood Pressure", "bloodpressure", "Monitor blood pressure"));

        // Mental & productivity
        tileOptions.add(new TileOption("📚", "Reading", "reading", "Track reading time"));
        tileOptions.add(new TileOption("📱", "Screen Time", "screentime", "Monitor device usage"));
        tileOptions.add(new TileOption("🧘", "Meditation", "meditation", "Log mindfulness sessions"));
        tileOptions.add(new TileOption("💭", "Thoughts", "thoughts", "Record thoughts and notes"));

        // Social & activities
        tileOptions.add(new TileOption("👥", "Social", "social", "Track social interactions"));
        tileOptions.add(new TileOption("🎵", "Music", "music", "Log music listening"));
        tileOptions.add(new TileOption("🎮", "Gaming", "gaming", "Track gaming sessions"));
        tileOptions.add(new TileOption("💰", "Expenses", "expenses", "Log spending"));
    }

    private void setupRecyclerView() {
        adapter = new TileSelectionAdapter(tileOptions, this::onTileToggled);
        tileOptionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        tileOptionsRecycler.setAdapter(adapter);
    }

    private void onTileToggled(TileOption tile, boolean enabled) {
        dashboardPrefs.edit()
                .putBoolean("tile_" + tile.id, enabled)
                .apply();
    }

    /**
     * Data class for tile options
     */
    public static class TileOption {
        public final String icon;
        public final String name;
        public final String id;
        public final String description;

        public TileOption(String icon, String name, String id, String description) {
            this.icon = icon;
            this.name = name;
            this.id = id;
            this.description = description;
        }
    }

    /**
     * Adapter for tile selection list
     */
    public static class TileSelectionAdapter extends RecyclerView.Adapter<TileSelectionAdapter.TileViewHolder> {

        public interface TileToggleListener {
            void onTileToggled(TileOption tile, boolean enabled);
        }

        private final List<TileOption> tiles;
        private final TileToggleListener listener;

        public TileSelectionAdapter(List<TileOption> tiles, TileToggleListener listener) {
            this.tiles = tiles;
            this.listener = listener;
        }

        @Override
        public TileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tile_selection, parent, false);
            return new TileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TileViewHolder holder, int position) {
            TileOption tile = tiles.get(position);
            holder.bind(tile, listener);
        }

        @Override
        public int getItemCount() {
            return tiles.size();
        }

        static class TileViewHolder extends RecyclerView.ViewHolder {
            private final TextView iconText;
            private final TextView nameText;
            private final TextView descriptionText;
            private final Switch toggleSwitch;

            TileViewHolder(View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.tile_icon);
                nameText = itemView.findViewById(R.id.tile_name);
                descriptionText = itemView.findViewById(R.id.tile_description);
                toggleSwitch = itemView.findViewById(R.id.tile_toggle);
            }

            void bind(TileOption tile, TileToggleListener listener) {
                iconText.setText(tile.icon);
                nameText.setText(tile.name);
                descriptionText.setText(tile.description);

                // Get current state from preferences
                SharedPreferences prefs = itemView.getContext()
                        .getSharedPreferences("dashboard_tiles", Context.MODE_PRIVATE);
                boolean isEnabled = prefs.getBoolean("tile_" + tile.id, false);
                toggleSwitch.setChecked(isEnabled);

                toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onTileToggled(tile, isChecked);
                    }
                });
            }
        }
    }
}