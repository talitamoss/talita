package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * MyDataActivity - Personal data dashboard with insights
 */
public class MyDataActivity extends AppCompatActivity {
    
    private static final String TAG = "MyDataActivity";
    
    // Views
    private TabLayout timeRangeTabs;
    private RecyclerView insightsRecycler;
    private CardView summaryCard;
    private TextView dataPointsText;
    private TextView activeTrackersText;
    private TextView streakText;
    
    // Data
    private UniversalDataService dataService;
    private InsightsEngine insightsEngine;
    private InsightsAdapter insightsAdapter;
    
    // Time ranges
    private static final long DAY_MS = 24 * 60 * 60 * 1000;
    private static final long WEEK_MS = 7 * DAY_MS;
    private static final long MONTH_MS = 30 * DAY_MS;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);
        
        dataService = UniversalDataService.getInstance(this);
        insightsEngine = new InsightsEngine(this);
        
        initializeViews();
        setupTabs();
        setupQuickActions();
        loadDataForTimeRange(DAY_MS);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning
        int selectedTab = timeRangeTabs.getSelectedTabPosition();
        long timeRange = getTimeRangeFromTab(selectedTab);
        loadDataForTimeRange(timeRange);
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        timeRangeTabs = findViewById(R.id.time_range_tabs);
        insightsRecycler = findViewById(R.id.insights_recycler);
        summaryCard = findViewById(R.id.summary_card);
        dataPointsText = findViewById(R.id.data_points_text);
        activeTrackersText = findViewById(R.id.active_trackers_text);
        streakText = findViewById(R.id.streak_text);
        
        // Setup insights recycler
        insightsAdapter = new InsightsAdapter();
        insightsRecycler.setLayoutManager(new LinearLayoutManager(this));
        insightsRecycler.setAdapter(insightsAdapter);
    }
    
    private void setupTabs() {
        timeRangeTabs.addTab(timeRangeTabs.newTab().setText("Today"));
        timeRangeTabs.addTab(timeRangeTabs.newTab().setText("Week"));
        timeRangeTabs.addTab(timeRangeTabs.newTab().setText("Month"));
        
        timeRangeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                long timeRange = getTimeRangeFromTab(tab.getPosition());
                loadDataForTimeRange(timeRange);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupQuickActions() {
        findViewById(R.id.view_all_data_button).setOnClickListener(v -> {
            startActivity(new Intent(this, DataSummaryActivity.class));
        });
        
        findViewById(R.id.export_data_button).setOnClickListener(v -> {
            startActivity(new Intent(this, DataExportSettingsActivity.class));
        });
        
        findViewById(R.id.data_patterns_button).setOnClickListener(v -> {
            startActivity(new Intent(this, PatternsActivity.class));
        });
    }
    
    private long getTimeRangeFromTab(int position) {
        switch (position) {
            case 0: return DAY_MS;
            case 1: return WEEK_MS;
            case 2: return MONTH_MS;
            default: return DAY_MS;
        }
    }
    
    private void loadDataForTimeRange(long rangeMs) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - rangeMs;
        
        // Update summary stats
        updateSummaryStats(startTime, endTime);
        
        // Generate and display insights
        new Thread(() -> {
            List<InsightsEngine.Insight> insights = insightsEngine.generateInsights(startTime, endTime);
            
            runOnUiThread(() -> {
                insightsAdapter.updateInsights(insights);
                
                // Show/hide empty state
                if (insights.isEmpty()) {
                    // Could show empty state view
                }
            });
        }).start();
    }
    
    private void updateSummaryStats(long startTime, long endTime) {
        new Thread(() -> {
            try {
                // Get data for range
                List<PersonalData> data = dataService.getDataForTimeRange(startTime, endTime);
                
                // Count unique data types
                Set<String> uniqueTypes = new HashSet<>();
                for (PersonalData item : data) {
                    uniqueTypes.add(item.getType());
                }
                
                // Calculate streak (simplified - days with any data)
                int streak = calculateStreak(data);
                
                runOnUiThread(() -> {
                    dataPointsText.setText(String.valueOf(data.size()));
                    activeTrackersText.setText(String.valueOf(uniqueTypes.size()));
                    streakText.setText(streak + " days");
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private int calculateStreak(List<PersonalData> data) {
        if (data.isEmpty()) return 0;
        
        // Group by day
        Map<String, Boolean> dayMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (PersonalData item : data) {
            String day = sdf.format(new Date(item.getTimestamp()));
            dayMap.put(day, true);
        }
        
        // Count consecutive days from today backwards
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        
        for (int i = 0; i < 365; i++) {
            String day = sdf.format(cal.getTime());
            if (dayMap.containsKey(day)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    /**
     * Insights Adapter
     */
    private class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.ViewHolder> {
        private List<InsightsEngine.Insight> insights = new ArrayList<>();
        
        void updateInsights(List<InsightsEngine.Insight> newInsights) {
            this.insights = newInsights;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_insight, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InsightsEngine.Insight insight = insights.get(position);
            holder.bind(insight);
        }
        
        @Override
        public int getItemCount() {
            return insights.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emojiText;
            TextView titleText;
            TextView descriptionText;
            
            ViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.insight_icon);
                titleText = itemView.findViewById(R.id.insight_title);
                descriptionText = itemView.findViewById(R.id.insight_description);
            }
            
            void bind(InsightsEngine.Insight insight) {
                emojiText.setText(insight.getEmoji());
                titleText.setText(insight.getTitle());
                descriptionText.setText(insight.getDescription());
                
                // Could add click handling here
                itemView.setOnClickListener(v -> {
                    // Navigate to relevant data view
                });
            }
        }
    }
}
