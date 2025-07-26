package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DataSummaryActivity - Overview of all collected data
 */
public class DataSummaryActivity extends AppCompatActivity {
    
    private static final String TAG = "DataSummaryActivity";
    
    // Views
    private TabLayout timeRangeTabs;
    private TextView summaryText;
    private TextView todayCountText;
    private TextView weekCountText;
    private RecyclerView recentDataRecycler;
    private RecyclerView dataTypesRecycler;
    
    // Data
    private UniversalDataService dataService;
    private RecentSummaryAdapter recentAdapter;
    private DataTypeStatsAdapter statsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_summary);
        
        dataService = UniversalDataService.getInstance(this);
        
        initializeViews();
        setupRecyclers();
        loadSummaryData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadSummaryData();
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        summaryText = findViewById(R.id.summary_text);
        todayCountText = findViewById(R.id.today_count);
        weekCountText = findViewById(R.id.week_count);
        recentDataRecycler = findViewById(R.id.recent_data_recycler);
        dataTypesRecycler = findViewById(R.id.data_types_recycler);
        
        // View all button
        findViewById(R.id.view_all_button).setOnClickListener(v -> {
            startActivity(new Intent(this, DataViewActivity.class));
        });
    }
    
    private void setupRecyclers() {
        // Recent data - horizontal
        recentAdapter = new RecentSummaryAdapter();
        recentDataRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentDataRecycler.setAdapter(recentAdapter);
        
        // Data types - grid
        statsAdapter = new DataTypeStatsAdapter();
        dataTypesRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        dataTypesRecycler.setAdapter(statsAdapter);
    }
    
    private void loadSummaryData() {
        new Thread(() -> {
            try {
                // Time ranges
                long now = System.currentTimeMillis();
                long todayStart = getStartOfDay(now);
                long weekStart = now - (7L * 24 * 60 * 60 * 1000);
                
                // Get data
                List<PersonalData> allData = dataService.getAllData();
                List<PersonalData> todayData = dataService.getDataForTimeRange(todayStart, now);
                List<PersonalData> weekData = dataService.getDataForTimeRange(weekStart, now);
                
                // Calculate stats
                Map<String, Integer> typeStats = calculateTypeStats(allData);
                List<DataTypeStat> statsList = new ArrayList<>();
                
                for (Map.Entry<String, Integer> entry : typeStats.entrySet()) {
                    statsList.add(new DataTypeStat(
                            entry.getKey(),
                            getDisplayNameForType(entry.getKey()),
                            getEmojiForType(entry.getKey()),
                            entry.getValue()
                    ));
                }
                
                // Sort by count
                Collections.sort(statsList, (a, b) -> Integer.compare(b.count, a.count));
                
                // Get recent items (last 10)
                List<PersonalData> recentItems = new ArrayList<>();
                if (allData.size() > 0) {
                    Collections.sort(allData, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    int limit = Math.min(10, allData.size());
                    recentItems = allData.subList(0, limit);
                }
                
                // Update UI
                runOnUiThread(() -> {
                    summaryText.setText(allData.size() + " total data points");
                    todayCountText.setText(String.valueOf(todayData.size()));
                    weekCountText.setText(String.valueOf(weekData.size()));
                    
                    recentAdapter.updateData(recentItems);
                    statsAdapter.updateStats(statsList);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private Map<String, Integer> calculateTypeStats(List<PersonalData> data) {
        Map<String, Integer> stats = new HashMap<>();
        
        for (PersonalData item : data) {
            String type = item.getType();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        
        return stats;
    }
    
    private String getDisplayNameForType(String type) {
        // Convert type to display name
        switch (type) {
            case "water": return "Water";
            case "exercise": return "Exercise";
            case "mood": return "Mood";
            case "sleep": return "Sleep";
            case "location": return "Location";
            case "audio": return "Audio";
            case "steps": return "Steps";
            case "nutrition": return "Nutrition";
            case "focus": return "Focus";
            case "relationships": return "Relationships";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }
    
    private String getEmojiForType(String type) {
        switch (type) {
            case "water": return "💧";
            case "exercise": return "💪";
            case "mood": return "😊";
            case "sleep": return "😴";
            case "location": return "📍";
            case "audio": return "🎙️";
            case "steps": return "👣";
            case "nutrition": return "🥗";
            case "focus": return "🎯";
            case "relationships": return "💞";
            default: return "📊";
        }
    }
    
    /**
     * Recent data item
     */
    private static class RecentSummary {
        final String type;
        final String summary;
        final long timestamp;
        final String emoji;
        
        RecentSummary(PersonalData data) {
            this.type = data.getType();
            this.timestamp = data.getTimestamp();
            this.emoji = getEmojiForType(data.getType());
            
            // Create summary based on type
            Map<String, Object> dataMap = data.getData();
            switch (type) {
                case "water":
                    int ml = (int) dataMap.getOrDefault("volume_ml", 0);
                    this.summary = ml + "ml";
                    break;
                case "exercise":
                    String exerciseType = (String) dataMap.getOrDefault("exercise_type", "Exercise");
                    this.summary = exerciseType;
                    break;
                case "mood":
                    int rating = (int) dataMap.getOrDefault("mood_rating", 3);
                    this.summary = "Mood: " + rating + "/5";
                    break;
                case "sleep":
                    double hours = (double) dataMap.getOrDefault("hours_slept", 0.0);
                    this.summary = String.format("%.1fh sleep", hours);
                    break;
                default:
                    this.summary = type;
            }
        }
        
        private static String getEmojiForType(String type) {
            switch (type) {
                case "water": return "💧";
                case "exercise": return "💪";
                case "mood": return "😊";
                case "sleep": return "😴";
                case "location": return "📍";
                case "audio": return "🎙️";
                case "steps": return "👣";
                default: return "📊";
            }
        }
    }
    
    /**
     * Data type statistics
     */
    private static class DataTypeStat {
        final String type;
        final String displayName;
        final String emoji;
        final int count;
        
        DataTypeStat(String type, String displayName, String emoji, int count) {
            this.type = type;
            this.displayName = displayName;
            this.emoji = emoji;
            this.count = count;
        }
    }
    
    /**
     * Recent summary adapter
     */
    private class RecentSummaryAdapter extends RecyclerView.Adapter<RecentSummaryAdapter.ViewHolder> {
        private List<RecentSummary> items = new ArrayList<>();
        
        void updateData(List<PersonalData> data) {
            items.clear();
            for (PersonalData pd : data) {
                items.add(new RecentSummary(pd));
            }
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_data, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emojiText;
            TextView typeText;
            TextView summaryText;
            TextView timeText;
            
            ViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.emoji);
                typeText = itemView.findViewById(R.id.type_text);
                summaryText = itemView.findViewById(R.id.value_text);
                timeText = itemView.findViewById(R.id.timestamp_text);
            }
            
            void bind(RecentSummary item) {
                emojiText.setText(item.emoji);
                typeText.setText(item.type);
                summaryText.setText(item.summary);
                
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                timeText.setText(sdf.format(new Date(item.timestamp)));
                
                itemView.setOnClickListener(v -> {
                    // TODO: Open detail view
                });
            }
        }
    }
    
    /**
     * Data type stats adapter
     */
    private class DataTypeStatsAdapter extends RecyclerView.Adapter<DataTypeStatsAdapter.ViewHolder> {
        private List<DataTypeStat> stats = new ArrayList<>();
        
        void updateStats(List<DataTypeStat> newStats) {
            this.stats = newStats;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_data_type_card, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(stats.get(position));
        }
        
        @Override
        public int getItemCount() {
            return stats.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emojiText;
            TextView nameText;
            TextView countText;
            
            ViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.type_emoji);
                nameText = itemView.findViewById(R.id.type_name);
                countText = itemView.findViewById(R.id.entry_count);
            }
            
            void bind(DataTypeStat stat) {
                emojiText.setText(stat.emoji);
                nameText.setText(stat.displayName);
                countText.setText(stat.count + " entries");
                
                itemView.setOnClickListener(v -> {
                    // Open filtered view for this type
                    Intent intent = new Intent(DataSummaryActivity.this, DataViewActivity.class);
                    intent.putExtra("filter_type", stat.type);
                    startActivity(intent);
                });
            }
        }
    }
}
