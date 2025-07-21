package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
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
 * Dashboard Activity - Main activity for easy data entry
 * Renamed from QuickAddDashboardActivity
 *
 * Features:
 * - Quick add buttons for common activities
 * - Recent activity feed
 * - Daily summary stats
 * - Background tracking status
 * - TEST MODE for debugging
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

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

    // Test mode components
    private boolean testMode = false;
    private TextView testOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize services
        collectorManager = new DataCollectorManager(this);
        trackingManager = new TrackingManager(this);

        // Enable water collector by default for testing
        WaterCollector.setEnabled(this, true);

        initializeViews();
        setupQuickAddGrid();
        setupRecentActivityFeed();
        updateDashboard();

        // Add testing features
        addTestingFeatures();
        
        // Run minimal test in background
        runMinimalTest();

        // Check for focus collector from intent
        String focusCollector = getIntent().getStringExtra("focus_collector");
        if (focusCollector != null) {
            focusOnCollector(focusCollector);
        }

        Log.d(TAG, "📊 Dashboard Activity initialized");
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

        // Navigation buttons - these are CardViews, not Buttons!
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        CardView locationButton = findViewById(R.id.location_button);
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });

        CardView audioButton = findViewById(R.id.audio_button);
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

    private void addTestingFeatures() {
        // Create a test button at the bottom
        Button testButton = new Button(this);
        testButton.setText("🧪 Test Quick Add");
        testButton.setBackgroundColor(0xFF4CAF50);
        testButton.setTextColor(0xFFFFFFFF);
        testButton.setOnClickListener(v -> runQuickAddTest());
        
        // Create test output view
        testOutputText = new TextView(this);
        testOutputText.setTextColor(0xFF00FF00);
        testOutputText.setBackgroundColor(0xFF1A1A1A);
        testOutputText.setPadding(20, 20, 20, 20);
        testOutputText.setVisibility(View.GONE);
        
ViewGroup rootView = findViewById(android.R.id.content);
if (rootView.getChildAt(0) instanceof ScrollView) {
    ScrollView scrollView = (ScrollView) rootView.getChildAt(0);
    // ScrollView can only have one child, so get its child (LinearLayout)
    if (scrollView.getChildCount() > 0 && scrollView.getChildAt(0) instanceof ViewGroup) {
        ViewGroup mainLayout = (ViewGroup) scrollView.getChildAt(0);
        
        // Create container for test elements
        LinearLayout testContainer = new LinearLayout(this);
        testContainer.setOrientation(LinearLayout.VERTICAL);
        testContainer.addView(testButton);
        testContainer.addView(testOutputText);
        
        mainLayout.addView(testContainer);
    }
}
    }

    private void runQuickAddTest() {
        Log.d(TAG, "=== QUICK ADD TEST START ===");
        
        StringBuilder results = new StringBuilder();
        results.append("Quick Add Test Results:\n\n");
        
        try {
            // Test 1: Check services
            results.append("1. Services initialized: ");
            results.append(collectorManager != null ? "✅\n" : "❌\n");
            
            // Test 2: Check water collector
            results.append("2. Water collector enabled: ");
	    WaterCollector waterCollector = new WaterCollector();
	    boolean waterEnabled = waterCollector.isEnabled(this);
            results.append(waterEnabled ? "✅\n" : "❌ (enabling now...)\n");
            
            if (!waterEnabled) {
                WaterCollector.setEnabled(this, true);
            }
            
            // Test 3: Get initial water total
            int initialTotal = WaterCollector.getTodayTotal(this);
            results.append("3. Initial water total: ").append(initialTotal).append("ml\n");
            
            // Test 4: Log water
            results.append("4. Adding 100ml water... ");
            collectorManager.quickLogWater(100);
            
            // Test 5: Check new total
            int newTotal = WaterCollector.getTodayTotal(this);
            boolean success = newTotal == initialTotal + 100;
            results.append(success ? "✅\n" : "❌\n");
            results.append("   New total: ").append(newTotal).append("ml\n");
            
            // Test 6: Check data persistence
            try {
                UniversalDataService dataService = new UniversalDataService(this);
                List<UniversalDataService.DecryptedDataItem> waterData = 
                    dataService.getDecryptedDataByType("water");
                results.append("5. Data entries saved: ").append(waterData.size()).append(" ✅\n");
            } catch (Exception e) {
                results.append("5. Data persistence: ❌ ").append(e.getMessage()).append("\n");
            }
            
        } catch (Exception e) {
            results.append("\n❌ Error: ").append(e.getMessage());
            Log.e(TAG, "Test error", e);
        }
        
        // Show results
        new AlertDialog.Builder(this)
            .setTitle("Quick Add Test Results")
            .setMessage(results.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Clear Water Data", (d, w) -> {
                WaterCollector.clearTodayData(this);
                updateDashboard();
                Toast.makeText(this, "Water data cleared", Toast.LENGTH_SHORT).show();
            })
            .show();
        
        // Update the dashboard to reflect changes
        updateDashboard();
        
        Log.d(TAG, "=== QUICK ADD TEST END ===");
        Log.d(TAG, results.toString());
    }

    private void runMinimalTest() {
        Log.d("QuickTest", "=== MINIMAL TEST ===");
        try {
            // Enable water collector
            WaterCollector.setEnabled(this, true);
            
            // Test direct water logging
            int before = WaterCollector.getTodayTotal(this);
            Log.d("QuickTest", "Water total before: " + before + "ml");
            
            // Log 1ml to test without affecting user's real data much
            WaterCollector.logWater(this, 1);
            
            int after = WaterCollector.getTodayTotal(this);
            Log.d("QuickTest", "Water total after: " + after + "ml");
            Log.d("QuickTest", "Test result: " + (after == before + 1 ? "✅ WORKING" : "❌ NOT WORKING"));
            
        } catch (Exception e) {
            Log.e("QuickTest", "Test failed", e);
        }
    }

    private void focusOnCollector(String collectorType) {
        // Show a quick action for the specific collector
        Toast.makeText(this, "📊 Focus on " + collectorType + " collection", Toast.LENGTH_SHORT).show();

        // Could scroll to or highlight the specific collector
        // For now, just show a toast
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

        try {
            // Log for debugging
            Log.d(TAG, "handleWaterAdd: Adding " + amount + "ml");
            
            // Use existing water collector
            collectorManager.quickLogWater(amount);

            // Show confirmation with today's total
            int todayTotal = WaterCollector.getTodayTotal(this);
            String message = String.format("💧 +%dml water logged (Total: %dml today)", amount, todayTotal);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            updateDashboard();

            Log.d(TAG, "💧 Quick added water: " + amount + "ml - Success!");
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding water", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                    int rating = which + 1; // 1-5 scale
                    String moodText = moods[which].substring(2); // Remove emoji

                    collectorManager.quickLogMood(rating, moodText);

                    Toast.makeText(this, "Mood logged: " + moods[which], Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleSleepAdd(QuickAddItem item) {
        // Show sleep duration options
        String[] durations = {"😴 4h", "😔 5h", "🙂 6h", "😊 7h", "😃 8h", "🤩 9h+"};

        new AlertDialog.Builder(this)
                .setTitle("How many hours did you sleep?")
                .setItems(durations, (dialog, which) -> {
                    double hours = 4 + which; // 4-9+ hours
                    String quality = which < 2 ? "poor" : which < 4 ? "fair" : "good";

                    collectorManager.quickLogSleep(hours, quality);

                    Toast.makeText(this, "Sleep logged: " + durations[which], Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleBeverageAdd(QuickAddItem item) {
        // For now, just log as water with note
        collectorManager.quickLogWater(250); // Standard cup
        Toast.makeText(this, item.icon + " " + item.name + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleSubstanceAdd(QuickAddItem item) {
        collectorManager.quickLogSubstance(item.name.toLowerCase(), item.description);
        Toast.makeText(this, item.icon + " " + item.name + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleAlcoholAdd(QuickAddItem item) {
        collectorManager.quickLogSubstance(item.name.toLowerCase(), item.description);
        Toast.makeText(this, item.icon + " " + item.name + " logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleFoodAdd(QuickAddItem item) {
        // Show meal options
        String[] mealTypes = {"🌅 Breakfast", "☀️ Lunch", "🌆 Dinner", "🍿 Snack"};

        new AlertDialog.Builder(this)
                .setTitle("What type of meal?")
                .setItems(mealTypes, (dialog, which) -> {
                    String mealType = mealTypes[which].substring(2); // Remove emoji
                    collectorManager.quickLogMeal(mealType.toLowerCase(), item.description);
                    Toast.makeText(this, mealTypes[which] + " logged", Toast.LENGTH_SHORT).show();
                    updateDashboard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleMedicationAdd(QuickAddItem item) {
        // For now, generic medication logging
        collectorManager.quickLogSubstance("medication", item.description);
        Toast.makeText(this, item.icon + " Medication logged", Toast.LENGTH_SHORT).show();
        updateDashboard();
    }

    private void handleBiometricAdd(QuickAddItem item) {
        // Placeholder for weight/other biometrics
        Toast.makeText(this, "🚧 " + item.name + " logging coming soon!", Toast.LENGTH_SHORT).show();
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
