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
// Comment these out for now to avoid chart library issues
// import com.github.mikephil.charting.charts.LineChart;
// import com.github.mikephil.charting.components.XAxis;
// import com.github.mikephil.charting.data.Entry;
// import com.github.mikephil.charting.data.LineData;
// import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * My Data Activity - Central analytics and insights hub
 *
 * This is where users discover patterns in their collected data
 * and gain insights into their behaviors and habits.
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
    // private LineChart activityChart;
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
        dataService = new UniversalDataService(this);

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
        // activityChart = findViewById(R.id.activity_chart);
        chartCard = findViewById(R.id.chart_card);
        // setupChart();

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
        // For now, use mock data to test the UI
        loadMockData();
    }

    private void loadMockData() {
        // Mock Life Score
        updateLifeScore(87, "Top factors:\n• Sleep consistency ↑\n• Exercise frequency ↑\n• Water intake ↓");

        // Mock Data Status
        updateDataStatus(2847, 2103);

        // Mock Insights
        List<Insight> mockInsights = new ArrayList<>();
        mockInsights.add(new Insight("Better mood on days with >7000 steps", 0.82, "Movement", System.currentTimeMillis()));
        mockInsights.add(new Insight("Sleep quality improves when water intake >2L", 0.71, "Wellness", System.currentTimeMillis()));
        mockInsights.add(new Insight("Productivity peaks between 10am-12pm on exercise days", 0.68, "Productivity", System.currentTimeMillis()));
        updateInsights(mockInsights);

        // Mock Today Stats
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("steps", 8432);
        todayStats.put("water", 2.3f);
        todayStats.put("mood", "😊");
        todayStats.put("sleep", 7.5f);
        updateTodayStats(todayStats);

        // Hide chart for now
        chartCard.setVisibility(View.GONE);
    }

    private void updateLifeScore(int score, String details) {
        lifeScoreText.setText(String.format("Life Score: %d/100", score));
        lifeScoreDetails.setText(details);

        // Color based on score
        int color = getScoreColor(score);
        lifeScoreText.setTextColor(color);
    }

    private int getScoreColor(int score) {
        if (score >= 80) return getColor(R.color.success_green);
        if (score >= 60) return getColor(R.color.warning_yellow);
        return getColor(R.color.error_red);
    }

    private void updateDataStatus(int total, int backedUp) {
        dataPointsText.setText(String.format("%,d data points", total));

        int pending = total - backedUp;
        if (pending > 0) {
            backupStatusText.setText(String.format("%d pending backup", pending));
            backupStatusText.setTextColor(getColor(R.color.warning_yellow));
        } else {
            backupStatusText.setText("All data backed up ✓");
            backupStatusText.setTextColor(getColor(R.color.success_green));
        }
    }

    private void updateInsights(List<Insight> insights) {
        insightsAdapter.setInsights(insights);
    }

    private void updateTodayStats(Map<String, Object> stats) {
        todayStepsText.setText(String.format("%,d", (int) stats.get("steps")));
        todayWaterText.setText(String.format("%.1fL", (float) stats.get("water")));
        todayMoodText.setText((String) stats.get("mood"));
        todaySleepText.setText(String.format("%.1fh", (float) stats.get("sleep")));
    }

    // Time period enum
    enum TimePeriod {
        TODAY, WEEK, MONTH, ALL
    }
}