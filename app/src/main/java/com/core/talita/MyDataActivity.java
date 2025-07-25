package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * My Data Activity - Central analytics and insights hub
 * Updated to use plugin system instead of hardcoded collectors
 */
public class MyDataActivity extends AppCompatActivity {

    private static final String TAG = "MyDataActivity";

    // Core services
    private LocalDataManager dataManager;
    private InsightsEngine insightsEngine;
    private UniversalDataService dataService;

    // UI Components - Overview Cards
    private TextView lifeScoreText;
    private TextView lifeScoreDetails;
    private TextView dataPointsText;
    private TextView backupStatusText;

    // UI Components - Insights
    private RecyclerView insightsRecyclerView;
    private InsightsAdapter insightsAdapter;

    // UI Components - Charts
    private CardView chartCard;

    // UI Components - Quick Stats
    private TextView todayStepsText;
    private TextView todayWaterText;
    private TextView todayMoodText;
    private TextView todaySleepText;

    // Time period selection
    private Button btnToday, btnWeek, btnMonth, btnAll;
    private TimePeriod currentPeriod = TimePeriod.WEEK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);

        // Initialize services
        dataManager = new LocalDataManager(this);
        insightsEngine = new InsightsEngine(dataManager);
        dataService = UniversalDataService.getInstance(this);

        initializeViews();
        setupTimeButtons();
        loadDataForPeriod(currentPeriod);

        Log.d(TAG, "📊 My Data Activity initialized");
    }

    private void initializeViews() {
        // Header
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Life Score Card
        lifeScoreText = findViewById(R.id.life_score_text);
        lifeScoreDetails = findViewById(R.id.life_score_details);

        // Data Status Card
        dataPointsText = findViewById(R.id.data_points_text);
        backupStatusText = findViewById(R.id.backup_status_text);

        // Time Period Buttons
        btnToday = findViewById(R.id.btn_today);
        btnWeek = findViewById(R.id.btn_week);
        btnMonth = findViewById(R.id.btn_month);
        btnAll = findViewById(R.id.btn_all);

        // Insights RecyclerView
        insightsRecyclerView = findViewById(R.id.insights_recycler_view);
        insightsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        insightsAdapter = new InsightsAdapter();
        insightsRecyclerView.setAdapter(insightsAdapter);

        // Activity Chart
        chartCard = findViewById(R.id.chart_card);
        // Hide chart for now
        chartCard.setVisibility(View.GONE);

        // Quick Stats
        todayStepsText = findViewById(R.id.today_steps);
        todayWaterText = findViewById(R.id.today_water);
        todayMoodText = findViewById(R.id.today_mood);
        todaySleepText = findViewById(R.id.today_sleep);
    }

    private void setupTimeButtons() {
        btnToday.setOnClickListener(v -> {
            currentPeriod = TimePeriod.TODAY;
            updateTimeButtonStates();
            loadDataForPeriod(currentPeriod);
        });

        btnWeek.setOnClickListener(v -> {
            currentPeriod = TimePeriod.WEEK;
            updateTimeButtonStates();
            loadDataForPeriod(currentPeriod);
        });

        btnMonth.setOnClickListener(v -> {
            currentPeriod = TimePeriod.MONTH;
            updateTimeButtonStates();
            loadDataForPeriod(currentPeriod);
        });

        btnAll.setOnClickListener(v -> {
            currentPeriod = TimePeriod.ALL;
            updateTimeButtonStates();
            loadDataForPeriod(currentPeriod);
        });

        // Set initial state
        updateTimeButtonStates();
    }

    private void updateTimeButtonStates() {
        // Reset all buttons
        btnToday.setBackgroundResource(R.drawable.button_outline);
        btnWeek.setBackgroundResource(R.drawable.button_outline);
        btnMonth.setBackgroundResource(R.drawable.button_outline);
        btnAll.setBackgroundResource(R.drawable.button_outline);

        // Highlight selected
        switch (currentPeriod) {
            case TODAY:
                btnToday.setBackgroundResource(R.drawable.button_primary);
                break;
            case WEEK:
                btnWeek.setBackgroundResource(R.drawable.button_primary);
                break;
            case MONTH:
                btnMonth.setBackgroundResource(R.drawable.button_primary);
                break;
            case ALL:
                btnAll.setBackgroundResource(R.drawable.button_primary);
                break;
        }
    }

    private void loadDataForPeriod(TimePeriod period) {
        try {
            // Calculate time range
            long endTime = System.currentTimeMillis();
            long startTime = calculateStartTime(period, endTime);

            // Load overall stats
            loadLifeScore(startTime, endTime);
            loadDataStats();

            // Load insights
            List<InsightsEngine.Insight> insights = insightsEngine.generateInsights(startTime, endTime);
            insightsAdapter.updateInsights(insights);

            // Load quick stats
            loadQuickStats(startTime, endTime);

            Log.d(TAG, "📊 Loaded data for period: " + period);

        } catch (Exception e) {
            Log.e(TAG, "Error loading data", e);
        }
    }

    private long calculateStartTime(TimePeriod period, long endTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endTime);

        switch (period) {
            case TODAY:
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case WEEK:
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case MONTH:
                cal.add(Calendar.MONTH, -1);
                break;
            case ALL:
                return 0; // Beginning of time
        }

        return cal.getTimeInMillis();
    }

    private void loadLifeScore(long startTime, long endTime) {
        // Calculate a simple life score based on data completeness
        int score = insightsEngine.calculateLifeScore(startTime, endTime);
        lifeScoreText.setText(String.valueOf(score));
        
        // Update details
        String period = currentPeriod == TimePeriod.TODAY ? "today" : 
                       currentPeriod == TimePeriod.WEEK ? "this week" :
                       currentPeriod == TimePeriod.MONTH ? "this month" : "overall";
        lifeScoreDetails.setText("Your data sovereignty score " + period);
    }

    private void loadDataStats() {
        UniversalDataService.DataStats stats = dataService.getDataStats();
        
        dataPointsText.setText(formatNumber(stats.totalCount));
        
        // Show backup status
        if (stats.totalCount > 0) {
            backupStatusText.setText("Protected & Encrypted");
            backupStatusText.setTextColor(0xFF4CAF50); // Green
        } else {
            backupStatusText.setText("Start collecting data");
            backupStatusText.setTextColor(0xFFFFFFFF); // White
        }
    }

    private void loadQuickStats(long startTime, long endTime) {
        try {
            // Get data for the period
            List<PersonalData> periodData = dataService.getDataInRange(startTime, endTime);
            
            // Calculate stats by type
            int waterTotal = 0;
            int stepCount = 0;
            String lastMood = "—";
            String lastSleep = "—";
            
            for (PersonalData data : periodData) {
                switch (data.getType()) {
                    case "water":
                        Object amount = data.getValue("amount");
                        if (amount instanceof Number) {
                            waterTotal += ((Number) amount).intValue();
                        }
                        break;
                        
                    case "steps":
                        Object steps = data.getValue("count");
                        if (steps instanceof Number) {
                            stepCount = Math.max(stepCount, ((Number) steps).intValue());
                        }
                        break;
                        
                    case "mood":
                        Object mood = data.getValue("mood");
                        if (mood != null) {
                            lastMood = mood.toString();
                        }
                        break;
                        
                    case "sleep":
                        Object hours = data.getValue("hours");
                        if (hours != null) {
                            lastSleep = hours + "h";
                        }
                        break;
                }
            }
            
            // Update UI
            todayWaterText.setText(waterTotal + "ml");
            todayStepsText.setText(formatNumber(stepCount) + " steps");
            todayMoodText.setText(lastMood);
            todaySleepText.setText(lastSleep);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading quick stats", e);
            // Set defaults
            todayWaterText.setText("—");
            todayStepsText.setText("—");
            todayMoodText.setText("—");
            todaySleepText.setText("—");
        }
    }

    private String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.format("%.1fM", number / 1000000.0);
        }
    }

    // Time period enum
    private enum TimePeriod {
        TODAY, WEEK, MONTH, ALL
    }

    // Adapter for insights
    private static class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.ViewHolder> {
        private List<InsightsEngine.Insight> insights = new ArrayList<>();

        void updateInsights(List<InsightsEngine.Insight> newInsights) {
            this.insights = newInsights;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
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

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView descriptionText;
            TextView emojiText;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.insight_title);
                descriptionText = itemView.findViewById(R.id.insight_description);
                emojiText = itemView.findViewById(R.id.insight_emoji);
            }

            void bind(InsightsEngine.Insight insight) {
                titleText.setText(insight.title);
                descriptionText.setText(insight.description);
                emojiText.setText(insight.emoji);
            }
        }
    }
}
