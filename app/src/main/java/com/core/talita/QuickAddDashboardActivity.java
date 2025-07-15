package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.collectors.WaterCollector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Quick Add Dashboard - Main activity for easy data entry
 *
 * Features:
 * - Quick add buttons for common activities
 * - Recent activity feed
 * - Daily summary stats
 * - Background tracking status
 */
public class QuickAddDashboardActivity extends AppCompatActivity {

    private static final String TAG = "QuickAddDashboard";

    private DataCollectorManager collectorManager;
    private TrackingManager trackingManager;

    // UI Components
    private RecyclerView quickAddRecyclerView;
    private RecyclerView recentActivityRecyclerView;
    private TextView dailyStatsText;
    private TextView trackingStatusText;
    private CardView trackingStatusCard;

    // Adapters
    private QuickAddAdapter quickAddAdapter;
    private RecentActivityAdapter recentActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_add_dashboard);

        // Initialize services
        collectorManager = new DataCollectorManager(this);
        trackingManager = new TrackingManager(this);

        initializeViews();
        setupQuickAddGrid();
        setupRecentActivityFeed();
        updateDashboard();

        Log.d(TAG, "📊 Quick Add Dashboard initialized");
    }

    private void initializeViews() {
        // Quick add grid
        quickAddRecyclerView = findViewById(R.id.quick_add_recycler_view);

        // Recent activity feed
        recentActivityRecyclerView = findViewById(R.id.recent_activity_recycler_view);

        // Stats and status
        dailyStatsText = findViewById(R.id.daily_stats_text);
        trackingStatusText = findViewById(R.id.tracking_status_text);
        trackingStatusCard = findViewById(R.id.tracking_status_card);

        // Navigation buttons
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        Button locationButton = findViewById(R.id.location_button);
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });

        Button audioButton = findViewById(R.id.audio_button);
        audioButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
        });

        // Tracking status card click
        trackingStatusCard.setOnClickListener(v -> toggleBackgroundTracking());
    }

    private void setupQuickAddGrid() {
        List<QuickAddItem> quickAddItems = createQuickAddItems();

        quickAddAdapter = new QuickAddAdapter(quickAddItems, this::handleQuickAdd);
        quickAddRecyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        quickAddRecyclerView.setAdapter(quickAddAdapter);
    }

    private void setupRecentActivityFeed() {
        recentActivityAdapter = new RecentActivityAdapter(new ArrayList<>());
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentActivityRecyclerView.setAdapter(recentActivityAdapter);
    }

    private List<QuickAddItem> createQuickAddItems() {
        List<QuickAddItem> items = new ArrayList<>();

        // Hydration
        items.add(new QuickAddItem("💧", "Water", "water", "250ml"));
        items.add(new QuickAddItem("🥤", "Big Water", "water", "500ml"));
        items.add(new QuickAddItem("☕", "Coffee", "beverage", "1 cup"));

        // Substances
        items.add(new QuickAddItem("🚬", "Cigarette", "substance", "1 cigarette"));
        items.add(new QuickAddItem("🍺", "Beer", "alcohol", "1 beer"));
        items.add(new QuickAddItem("🍷", "Wine", "alcohol", "1 glass"));

        // Food
        items.add(new QuickAddItem("🍽️", "Meal", "food", "meal"));
        items.add(new QuickAddItem("🍎", "Snack", "food", "snack"));
        items.add(new QuickAddItem("💊", "Medication", "medication", "dose"));

        // Exercise
        items.add(new QuickAddItem("🏃", "Run", "exercise", "session"));
        items.add(new QuickAddItem("💪", "Workout", "exercise", "session"));
        items.add(new QuickAddItem("🚶", "Walk", "exercise", "session"));

        // Health & Mood
        items.add(new QuickAddItem("😊", "Mood", "mood", "rating"));
        items.add(new QuickAddItem("💤", "Sleep", "sleep", "hours"));
        items.add(new QuickAddItem("📏", "Weight", "biometric", "measurement"));

        return items;
    }

    private void handleQuickAdd(QuickAddItem item) {
        switch (item.category) {
            case "water":
                handleWaterAdd(item);
                break;
            case "beverage":
                handleBeverageAdd(item);
                break;
            case "substance":
                handleSubstanceAdd(item);
                break;
            case "alcohol":
                handleAlcoholAdd(item);
                break;
            case "food":
                handleFoodAdd(item);
                break;
            case "medication":
                handleMedicationAdd(item);
                break;
            case "exercise":
                handleExerciseAdd(item);
                break;
            case "mood":
                handleMoodAdd(item);
                break;
            case "sleep":
                handleSleepAdd(item);
                break;
            case "biometric":
                handleBiometricAdd(item);
                break;
            default:
                showGenericQuickAdd(item);
        }
    }

    private void handleWaterAdd(QuickAddItem item) {
        // Parse amount from description
        int amount = item.description.equals("250ml") ? 250 : 500;

        // Use existing water collector
        collectorManager.quickLogWater(amount);

        // Show confirmation with today's total
        int todayTotal = WaterCollector.getTodayTotal(this);
        String message = String.format("💧 +%dml water logged (Total: %dml today)", amount, todayTotal);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        updateDashboard();

        Log.d(TAG, "💧 Quick added water: " + amount + "ml");
    }

    private void handleExerciseAdd(QuickAddItem item) {
        // Show duration options
        String[] durations = {"⚡ 5 min", "🏃 15 min", "💪 30 min", "🔥 45 min", "🎯 60 min"};

        new AlertDialog.Builder(this)
                .setTitle("How long was your " + item.name.toLowerCase() + "?")
                .setItems(durations, (dialog, which) -> {
                    String duration = durations[which].substring(2); // Remove emoji

                    collectorManager.quickLogExercise(item.name.toLowerCase(), duration);

                    Toast.makeText(this, item.icon + " " + duration + " " + item.name.toLowerCase() + " logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleMoodAdd(QuickAddItem item) {
        // Show mood scale
        String[] moods = {"😰 Terrible", "😕 Bad", "😐 Okay", "😊 Good", "🤩 Amazing"};

        new AlertDialog.Builder(this)
                .setTitle("How are you feeling?")
                .setItems(moods, (dialog, which) -> {
                    String mood = moods[which];
                    int rating = which + 1; // 1-5 scale

                    collectorManager.quickLogMood(rating, mood.substring(2));

                    Toast.makeText(this, mood + " mood logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleSleepAdd(QuickAddItem item) {
        // Show sleep duration options
        String[] sleepOptions = {"😴 4 hours", "😪 6 hours", "😊 8 hours", "🛌 10 hours"};

        new AlertDialog.Builder(this)
                .setTitle("How much sleep did you get?")
                .setItems(sleepOptions, (dialog, which) -> {
                    String sleepText = sleepOptions[which].substring(2); // Remove emoji
                    double hours = Double.parseDouble(sleepText.split(" ")[0]);

                    collectorManager.quickLogSleep(hours, "good");

                    Toast.makeText(this, "💤 " + sleepText + " sleep logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleFoodAdd(QuickAddItem item) {
        // Show meal type options
        String[] mealOptions = {"🌅 Breakfast", "☀️ Lunch", "🌙 Dinner", "🍿 Snack"};

        new AlertDialog.Builder(this)
                .setTitle("What type of " + item.name.toLowerCase() + "?")
                .setItems(mealOptions, (dialog, which) -> {
                    String mealType = mealOptions[which].substring(2); // Remove emoji

                    collectorManager.quickLogMeal(mealType.toLowerCase(), item.name);

                    Toast.makeText(this, item.icon + " " + mealType + " logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleSubstanceAdd(QuickAddItem item) {
        // Show confirmation dialog for substances
        new AlertDialog.Builder(this)
                .setTitle("Log " + item.name + "?")
                .setMessage("This will be recorded with timestamp for your personal tracking.")
                .setPositiveButton("Yes, Log It", (dialog, which) -> {
                    collectorManager.quickLogSubstance(item.name.toLowerCase(), item.description);

                    Toast.makeText(this, item.icon + " " + item.description + " logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleAlcoholAdd(QuickAddItem item) {
        collectorManager.quickLogSubstance("alcohol", item.description);
        Toast.makeText(this, item.icon + " " + item.description + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleBeverageAdd(QuickAddItem item) {
        collectorManager.quickLogSubstance("beverage", item.description);
        Toast.makeText(this, item.icon + " " + item.description + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleMedicationAdd(QuickAddItem item) {
        collectorManager.quickLogSubstance("medication", item.description);
        Toast.makeText(this, "💊 Medication dose logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleBiometricAdd(QuickAddItem item) {
        Toast.makeText(this, item.icon + " " + item.name + " - Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showGenericQuickAdd(QuickAddItem item) {
        Toast.makeText(this, item.icon + " " + item.name + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void toggleBackgroundTracking() {
        if (trackingManager.isTrackingEnabled()) {
            trackingManager.stopTracking();
            updateTrackingStatus();
        } else {
            trackingManager.startTracking();
            updateTrackingStatus();
        }
    }

    private void updateDashboard() {
        updateDailyStats();
        updateTrackingStatus();
        updateRecentActivity();
    }

    private void updateDailyStats() {
        // Get today's statistics
        int waterToday = WaterCollector.getTodayTotal(this);

        String statsText = String.format("💧 %dml water today", waterToday);
        dailyStatsText.setText(statsText);
    }

    private void updateTrackingStatus() {
        boolean isTracking = trackingManager.isTrackingEnabled();

        if (isTracking) {
            trackingStatusText.setText("🎯 Background tracking active");
            trackingStatusText.setTextColor(0xFF4CAF50); // Green
            trackingStatusCard.setCardBackgroundColor(0xFF2E7D32); // Dark green
        } else {
            trackingStatusText.setText("⏸️ Background tracking paused");
            trackingStatusText.setTextColor(0xFF888888); // Gray
            trackingStatusCard.setCardBackgroundColor(0xFF2A2A2A); // Dark gray
        }
    }

    private void updateRecentActivity() {
        // Create sample recent activities for now
        List<RecentActivityItem> recentItems = new ArrayList<>();
        recentItems.add(new RecentActivityItem("💧", "Water logged", "Just now", System.currentTimeMillis()));
        recentItems.add(new RecentActivityItem("💪", "Exercise logged", "30m ago", System.currentTimeMillis() - 1800000));
        recentItems.add(new RecentActivityItem("😊", "Mood logged", "1h ago", System.currentTimeMillis() - 3600000));

        recentActivityAdapter.updateItems(recentItems);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    /**
     * Data class for quick add items
     */
    public static class QuickAddItem {
        public final String icon;
        public final String name;
        public final String category;
        public final String description;

        public QuickAddItem(String icon, String name, String category, String description) {
            this.icon = icon;
            this.name = name;
            this.category = category;
            this.description = description;
        }
    }

    /**
     * Data class for recent activity items
     */
    public static class RecentActivityItem {
        public final String icon;
        public final String title;
        public final String timeAgo;
        public final long timestamp;

        public RecentActivityItem(String icon, String title, String timeAgo, long timestamp) {
            this.icon = icon;
            this.title = title;
            this.timeAgo = timeAgo;
            this.timestamp = timestamp;
        }
    }

    /**
     * Adapter for quick add grid
     */
    public static class QuickAddAdapter extends RecyclerView.Adapter<QuickAddAdapter.QuickAddViewHolder> {

        public interface QuickAddClickListener {
            void onQuickAddClick(QuickAddItem item);
        }

        private final List<QuickAddItem> items;
        private final QuickAddClickListener listener;

        public QuickAddAdapter(List<QuickAddItem> items, QuickAddClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public QuickAddViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quick_add, parent, false);
            return new QuickAddViewHolder(view);
        }

        @Override
        public void onBindViewHolder(QuickAddViewHolder holder, int position) {
            QuickAddItem item = items.get(position);
            holder.bind(item, listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class QuickAddViewHolder extends RecyclerView.ViewHolder {
            private final TextView iconText;
            private final TextView nameText;
            private final CardView cardView;

            QuickAddViewHolder(View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.quick_add_icon);
                nameText = itemView.findViewById(R.id.quick_add_name);
                cardView = itemView.findViewById(R.id.quick_add_card);
            }

            void bind(QuickAddItem item, QuickAddClickListener listener) {
                iconText.setText(item.icon);
                nameText.setText(item.name);

                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onQuickAddClick(item);
                    }
                });

                // Style the card
                cardView.setCardBackgroundColor(0xFF2A2A2A);
                cardView.setRadius(12);
                cardView.setCardElevation(4);
            }
        }
    }

    /**
     * Adapter for recent activity feed
     */
    public static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder> {

        private List<RecentActivityItem> items;

        public RecentActivityAdapter(List<RecentActivityItem> items) {
            this.items = items;
        }

        public void updateItems(List<RecentActivityItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public RecentActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_activity, parent, false);
            return new RecentActivityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecentActivityViewHolder holder, int position) {
            RecentActivityItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class RecentActivityViewHolder extends RecyclerView.ViewHolder {
            private final TextView iconText;
            private final TextView titleText;
            private final TextView timeText;

            RecentActivityViewHolder(View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.activity_icon);
                titleText = itemView.findViewById(R.id.activity_title);
                timeText = itemView.findViewById(R.id.activity_time);
            }

            void bind(RecentActivityItem item) {
                iconText.setText(item.icon);
                titleText.setText(item.title);
                timeText.setText(item.timeAgo);

                // Style for dark theme
                titleText.setTextColor(0xFFFFFFFF);
                timeText.setTextColor(0xFF888888);
            }
        }
    }
}