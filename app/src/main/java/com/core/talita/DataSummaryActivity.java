package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.collectors.WaterCollector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Data Summary Activity - Shows user what they've been up to
 *
 * Features:
 * - Daily/weekly/monthly summaries
 * - Recent activity timeline
 * - Statistics and trends
 * - Quick access to detailed views
 */
public class DataSummaryActivity extends AppCompatActivity {

    private static final String TAG = "DataSummaryActivity";

    private UniversalDataService dataService;
    private DataCollectorManager collectorManager;

    // UI Components
    private TextView todayStatsText;
    private TextView weekStatsText;
    private TextView totalDataPointsText;
    private RecyclerView recentActivityRecycler;
    private RecyclerView dataTypesRecycler;

    // Adapters
    private RecentSummaryAdapter recentAdapter;
    private DataTypeStatsAdapter dataTypesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_summary);

        // Initialize services
        dataService = new UniversalDataService(this);
        collectorManager = new DataCollectorManager(this);

        initializeViews();
        setupRecyclerViews();
        loadSummaryData();

        Log.d(TAG, "📈 Data Summary Activity initialized");
    }

    private void initializeViews() {
        // Stats text views
        todayStatsText = findViewById(R.id.today_stats_text);
        weekStatsText = findViewById(R.id.week_stats_text);
        totalDataPointsText = findViewById(R.id.total_data_points_text);

        // RecyclerViews
        recentActivityRecycler = findViewById(R.id.recent_activity_recycler);
        dataTypesRecycler = findViewById(R.id.data_types_recycler);

        // Navigation buttons
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        });

        // Data type cards
        setupDataTypeCards();
    }

    private void setupDataTypeCards() {
        // Location Card
        CardView locationCard = findViewById(R.id.location_card);
        locationCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });

        // Audio Card
        CardView audioCard = findViewById(R.id.audio_card);
        audioCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
        });

        // All Data Types Card
        CardView allDataCard = findViewById(R.id.all_data_card);
        allDataCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataCollectionActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        // Recent activity
        recentAdapter = new RecentSummaryAdapter(new ArrayList<>());
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentActivityRecycler.setAdapter(recentAdapter);

        // Data types stats
        dataTypesAdapter = new DataTypeStatsAdapter(new ArrayList<>());
        dataTypesRecycler.setLayoutManager(new LinearLayoutManager(this));
        dataTypesRecycler.setAdapter(dataTypesAdapter);
    }

    private void loadSummaryData() {
        // Load today's statistics
        loadTodayStats();

        // Load week statistics
        loadWeekStats();

        // Load recent activity
        loadRecentActivity();

        // Load data type statistics
        loadDataTypeStats();
    }

    private void loadTodayStats() {
        try {
            // Get today's water total
            int waterToday = WaterCollector.getTodayTotal(this);

            // Get today's date
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
            String todayDate = sdf.format(new Date());

            // Count today's activities (simplified for now)
            int activitiesToday = calculateTodayActivities();

            String todayStats = String.format(
                    "%s\n💧 %dml water • 📊 %d activities logged",
                    todayDate, waterToday, activitiesToday
            );

            todayStatsText.setText(todayStats);

        } catch (Exception e) {
            Log.e(TAG, "Error loading today stats: " + e.getMessage());
            todayStatsText.setText("Today's data loading...");
        }
    }

    private void loadWeekStats() {
        try {
            // Calculate this week's stats (simplified)
            int weeklyActivities = calculateWeekActivities();
            int activeDays = calculateActiveDays();

            String weekStats = String.format(
                    "This Week\n📈 %d total activities • 🗓️ %d active days",
                    weeklyActivities, activeDays
            );

            weekStatsText.setText(weekStats);

        } catch (Exception e) {
            Log.e(TAG, "Error loading week stats: " + e.getMessage());
            weekStatsText.setText("Week's data loading...");
        }
    }

    private void loadRecentActivity() {
        try {
            List<RecentSummaryItem> recentItems = new ArrayList<>();

            // Get recent data from different collectors
            // This is simplified - in production you'd query the actual database
            recentItems.add(new RecentSummaryItem("💧", "Water logged", "250ml", "5 minutes ago"));
            recentItems.add(new RecentSummaryItem("📍", "Location recorded", "Home area", "15 minutes ago"));
            recentItems.add(new RecentSummaryItem("💪", "Exercise logged", "30 min walk", "2 hours ago"));
            recentItems.add(new RecentSummaryItem("😊", "Mood logged", "Good mood", "4 hours ago"));
            recentItems.add(new RecentSummaryItem("🎤", "Audio recorded", "Voice memo", "Yesterday"));

            recentAdapter.updateItems(recentItems);

        } catch (Exception e) {
            Log.e(TAG, "Error loading recent activity: " + e.getMessage());
        }
    }

    private void loadDataTypeStats() {
        try {
            List<DataTypeStatItem> dataTypeStats = new ArrayList<>();

            // Get stats for each data type
            dataTypeStats.add(new DataTypeStatItem("💧", "Water", "12 entries", "2.1L today"));
            dataTypeStats.add(new DataTypeStatItem("📍", "Location", "156 points", "3 places visited"));
            dataTypeStats.add(new DataTypeStatItem("🎤", "Audio", "8 recordings", "45 min total"));
            dataTypeStats.add(new DataTypeStatItem("💪", "Exercise", "5 sessions", "2.5 hours this week"));
            dataTypeStats.add(new DataTypeStatItem("😊", "Mood", "7 entries", "Avg: Good"));

            dataTypesAdapter.updateItems(dataTypeStats);

            // Update total data points
            int totalPoints = calculateTotalDataPoints();
            totalDataPointsText.setText(totalPoints + " total data points collected");

        } catch (Exception e) {
            Log.e(TAG, "Error loading data type stats: " + e.getMessage());
        }
    }

    // Helper methods for calculations (simplified for now)
    private int calculateTodayActivities() {
        // In production, query database for today's entries
        return 5; // Placeholder
    }

    private int calculateWeekActivities() {
        // In production, query database for this week's entries
        return 23; // Placeholder
    }

    private int calculateActiveDays() {
        // In production, count unique days with data this week
        return 4; // Placeholder
    }

    private int calculateTotalDataPoints() {
        // In production, count all data entries across all types
        return 487; // Placeholder
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummaryData(); // Refresh data when returning to activity
    }

    // Data classes
    public static class RecentSummaryItem {
        public final String icon;
        public final String title;
        public final String details;
        public final String timeAgo;

        public RecentSummaryItem(String icon, String title, String details, String timeAgo) {
            this.icon = icon;
            this.title = title;
            this.details = details;
            this.timeAgo = timeAgo;
        }
    }

    public static class DataTypeStatItem {
        public final String icon;
        public final String name;
        public final String count;
        public final String summary;

        public DataTypeStatItem(String icon, String name, String count, String summary) {
            this.icon = icon;
            this.name = name;
            this.count = count;
            this.summary = summary;
        }
    }

    // Simplified adapters (you could expand these)
    private static class RecentSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<RecentSummaryItem> items;

        public RecentSummaryAdapter(List<RecentSummaryItem> items) {
            this.items = items;
        }

        public void updateItems(List<RecentSummaryItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Use existing recent activity layout
            return new RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_recent_activity, parent, false)) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind data to views (simplified)
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class DataTypeStatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<DataTypeStatItem> items;

        public DataTypeStatsAdapter(List<DataTypeStatItem> items) {
            this.items = items;
        }

        public void updateItems(List<DataTypeStatItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Use existing recent activity layout
            return new RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_recent_activity, parent, false)) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind data to views (simplified)
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}