package com.core.talita;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Data Summary Activity - Overview of collected data
 * Updated to use plugin system instead of hardcoded collectors
 */
public class DataSummaryActivity extends AppCompatActivity {
    private static final String TAG = "DataSummaryActivity";

    // UI Components
    private TextView totalDataPointsText;
    private TextView todayStatsText;
    private TextView weekStatsText;
    private RecyclerView recentActivityRecycler;
    private RecyclerView dataTypesRecycler;
    
    // Services
    private UniversalDataService dataService;
    private LocalDataManager dataManager;
    
    // Adapters
    private RecentSummaryAdapter recentAdapter;
    private DataTypeStatsAdapter dataTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_summary);

        // Initialize services
        dataService = UniversalDataService.getInstance(this);
        dataManager = new LocalDataManager(this);

        // Setup UI
        initializeViews();
        loadDataStats();
        loadTodayStats();
        loadWeekStats();
        loadRecentActivity();
        loadDataTypeStats();
    }

    private void initializeViews() {
        // Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Stats text views
        totalDataPointsText = findViewById(R.id.total_data_points);
        todayStatsText = findViewById(R.id.today_stats);
        weekStatsText = findViewById(R.id.week_stats);

        // Recent activity recycler
        recentActivityRecycler = findViewById(R.id.recent_activity_recycler);
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentSummaryAdapter();
        recentActivityRecycler.setAdapter(recentAdapter);

        // Data types recycler
        dataTypesRecycler = findViewById(R.id.data_types_recycler);
        dataTypesRecycler.setLayoutManager(new LinearLayoutManager(this, 
            LinearLayoutManager.HORIZONTAL, false));
        dataTypeAdapter = new DataTypeStatsAdapter();
        dataTypesRecycler.setAdapter(dataTypeAdapter);
    }

    private void loadDataStats() {
        try {
            UniversalDataService.DataStats stats = dataService.getDataStats();
            totalDataPointsText.setText(String.format("%,d", stats.totalCount));
        } catch (Exception e) {
            Log.e(TAG, "Error loading data stats", e);
            totalDataPointsText.setText("0");
        }
    }

    private void loadTodayStats() {
        try {
            // Get today's data
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            long now = System.currentTimeMillis();

            List<PersonalData> todayData = dataService.getDataInRange(startOfDay, now);
            
            // Count by type
            Map<String, Integer> typeCounts = new HashMap<>();
            int waterToday = 0;
            
            for (PersonalData data : todayData) {
                String type = data.getType();
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                
                // Sum water intake
                if ("water".equals(type)) {
                    Object amount = data.getValue("amount");
                    if (amount instanceof Number) {
                        waterToday += ((Number) amount).intValue();
                    }
                }
            }

            // Format today's date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
            String todayDate = dateFormat.format(new Date());

            // Count today's activities
            int activitiesToday = todayData.size();

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
            // Get this week's data
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            long weekAgo = cal.getTimeInMillis();
            long now = System.currentTimeMillis();

            List<PersonalData> weekData = dataService.getDataInRange(weekAgo, now);
            
            // Calculate stats
            Set<String> activeDays = new HashSet<>();
            for (PersonalData data : weekData) {
                cal.setTimeInMillis(data.getTimestamp());
                String day = String.format("%d-%d-%d", 
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
                activeDays.add(day);
            }

            String weekStats = String.format(
                    "This Week\n📈 %d total activities • 🗓️ %d active days",
                    weekData.size(), activeDays.size()
            );

            weekStatsText.setText(weekStats);

        } catch (Exception e) {
            Log.e(TAG, "Error loading week stats: " + e.getMessage());
            weekStatsText.setText("Week's data loading...");
        }
    }

    private void loadRecentActivity() {
        try {
            List<PersonalData> recentData = dataService.getRecentData(10);
            List<RecentSummaryItem> recentItems = new ArrayList<>();
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            
            for (PersonalData data : recentData) {
                String emoji = getEmojiForType(data.getType());
                String title = getTitleForType(data.getType());
                String summary = createSummaryForData(data);
                String time = getRelativeTime(data.getTimestamp());
                
                recentItems.add(new RecentSummaryItem(emoji, title, summary, time));
            }

            recentAdapter.updateItems(recentItems);

        } catch (Exception e) {
            Log.e(TAG, "Error loading recent activity: " + e.getMessage());
        }
    }

    private void loadDataTypeStats() {
        try {
            UniversalDataService.DataStats stats = dataService.getDataStats();
            List<DataTypeStatItem> dataTypeStats = new ArrayList<>();

            for (Map.Entry<String, Long> entry : stats.countByType.entrySet()) {
                String type = entry.getKey();
                long count = entry.getValue();
                String emoji = getEmojiForType(type);
                String displayName = getTitleForType(type);
                
                dataTypeStats.add(new DataTypeStatItem(emoji, displayName, count));
            }

            // Sort by count descending
            dataTypeStats.sort((a, b) -> Long.compare(b.count, a.count));
            
            dataTypeAdapter.updateItems(dataTypeStats);

        } catch (Exception e) {
            Log.e(TAG, "Error loading data type stats: " + e.getMessage());
        }
    }

    private String getEmojiForType(String type) {
        switch (type) {
            case "water": return "💧";
            case "mood": return "😊";
            case "exercise": return "💪";
            case "location": return "📍";
            case "sleep": return "😴";
            case "nutrition": return "🍎";
            case "audio": return "🎤";
            default: return "📊";
        }
    }

    private String getTitleForType(String type) {
        switch (type) {
            case "water": return "Water";
            case "mood": return "Mood";
            case "exercise": return "Exercise";
            case "location": return "Location";
            case "sleep": return "Sleep";
            case "nutrition": return "Nutrition";
            case "audio": return "Audio Note";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }

    private String createSummaryForData(PersonalData data) {
        switch (data.getType()) {
            case "water":
                Object amount = data.getValue("amount");
                return amount != null ? amount + "ml" : "Water logged";
                
            case "mood":
                Object mood = data.getValue("mood");
                return mood != null ? mood.toString() : "Mood logged";
                
            case "exercise":
                Object activity = data.getValue("activity");
                return activity != null ? activity.toString() : "Exercise logged";
                
            default:
                return data.getType() + " recorded";
        }
    }

    private String getRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            return (diff / 60000) + " minutes ago";
        } else if (diff < 86400000) { // Less than 1 day
            return (diff / 3600000) + " hours ago";
        } else if (diff < 172800000) { // Less than 2 days
            return "Yesterday";
        } else {
            return (diff / 86400000) + " days ago";
        }
    }

    // Data classes for adapters
    static class RecentSummaryItem {
        final String emoji;
        final String title;
        final String summary;
        final String time;

        RecentSummaryItem(String emoji, String title, String summary, String time) {
            this.emoji = emoji;
            this.title = title;
            this.summary = summary;
            this.time = time;
        }
    }

    static class DataTypeStatItem {
        final String emoji;
        final String name;
        final long count;

        DataTypeStatItem(String emoji, String name, long count) {
            this.emoji = emoji;
            this.name = name;
            this.count = count;
        }
    }

    // Simple adapter implementations
    private static class RecentSummaryAdapter extends RecyclerView.Adapter<RecentSummaryAdapter.ViewHolder> {
        private List<RecentSummaryItem> items = new ArrayList<>();

        void updateItems(List<RecentSummaryItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RecentSummaryItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView emojiText, titleText, summaryText, timeText;

            ViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.emoji_text);
                titleText = itemView.findViewById(R.id.title_text);
                summaryText = itemView.findViewById(R.id.summary_text);
                timeText = itemView.findViewById(R.id.time_text);
            }

            void bind(RecentSummaryItem item) {
                emojiText.setText(item.emoji);
                titleText.setText(item.title);
                summaryText.setText(item.summary);
                timeText.setText(item.time);
            }
        }
    }

    private static class DataTypeStatsAdapter extends RecyclerView.Adapter<DataTypeStatsAdapter.ViewHolder> {
        private List<DataTypeStatItem> items = new ArrayList<>();

        void updateItems(List<DataTypeStatItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_type_stat, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DataTypeStatItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView emojiText, nameText, countText;

            ViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.emoji_text);
                nameText = itemView.findViewById(R.id.name_text);
                countText = itemView.findViewById(R.id.count_text);
            }

            void bind(DataTypeStatItem item) {
                emojiText.setText(item.emoji);
                nameText.setText(item.name);
                countText.setText(String.valueOf(item.count));
            }
        }
    }
}
