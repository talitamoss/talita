package com.core.talita;

/**
 * PatternsActivity - TEMPORARILY DISABLED FOR MVP
 * 
 * This activity provides advanced pattern analysis features.
 * Commenting out until PatternAnalyzer inner classes are implemented.
 */
public class PatternsActivity extends androidx.appcompat.app.AppCompatActivity {
    // Activity temporarily disabled for MVP build
    // TODO: Re-enable once PatternAnalyzer.AnalysisResult and related classes are implemented
}

/* ORIGINAL CODE - COMMENTED FOR MVP
package com.core.talita;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatternsActivity extends AppCompatActivity {
    private static final String TAG = "PatternsActivity";
    
    // Core services
    private UniversalDataService dataService;
    private PatternAnalyzer analyzer;
    
    // UI Components
    private Spinner timeRangeSpinner;
    private Spinner patternTypeSpinner;
    private LinearLayout chartsContainer;
    private TextView insightsText;
    private ProgressBar loadingProgress;
    
    // Data
    private long currentStartTime;
    private long currentEndTime;
    private String currentPatternType = "overview";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patterns);
        
        // Initialize services
        dataService = UniversalDataService.getInstance(this);
        analyzer = new PatternAnalyzer();
        
        initializeViews();
        setupSpinners();
        loadInitialData();
    }
    
    private void initializeViews() {
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Spinners
        timeRangeSpinner = findViewById(R.id.time_range_spinner);
        patternTypeSpinner = findViewById(R.id.pattern_type_spinner);
        
        // Content
        chartsContainer = findViewById(R.id.charts_container);
        insightsText = findViewById(R.id.insights_text);
        loadingProgress = findViewById(R.id.loading_progress);
        
        // Action buttons
        findViewById(R.id.refresh_button).setOnClickListener(v -> refreshData());
        findViewById(R.id.export_button).setOnClickListener(v -> exportPatterns());
        findViewById(R.id.share_button).setOnClickListener(v -> shareInsights());
    }
    
    private void setupSpinners() {
        // Time range options
        String[] timeRanges = {"Today", "Last 7 Days", "Last 30 Days", "Last 90 Days", "All Time"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_dropdown_item, timeRanges);
        timeRangeSpinner.setAdapter(timeAdapter);
        
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTimeRange(position);
                refreshData();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Pattern type options
        String[] patternTypes = {"Overview", "Correlations", "Trends", "Anomalies", "Predictions"};
        ArrayAdapter<String> patternAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, patternTypes);
        patternTypeSpinner.setAdapter(patternAdapter);
        
        patternTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePatternType(position);
                refreshData();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void updateTimeRange(int position) {
        Calendar cal = Calendar.getInstance();
        currentEndTime = System.currentTimeMillis();
        
        switch (position) {
            case 0: // Today
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                currentStartTime = cal.getTimeInMillis();
                break;
            case 1: // Last 7 days
                cal.add(Calendar.DAY_OF_MONTH, -7);
                currentStartTime = cal.getTimeInMillis();
                break;
            case 2: // Last 30 days
                cal.add(Calendar.DAY_OF_MONTH, -30);
                currentStartTime = cal.getTimeInMillis();
                break;
            case 3: // Last 90 days
                cal.add(Calendar.DAY_OF_MONTH, -90);
                currentStartTime = cal.getTimeInMillis();
                break;
            case 4: // All time
                currentStartTime = 0;
                break;
        }
    }
    
    private void updatePatternType(int position) {
        String[] types = {"overview", "correlations", "trends", "anomalies", "predictions"};
        currentPatternType = types[position];
    }
    
    private void loadInitialData() {
        // Set default time range
        updateTimeRange(1); // Last week
        refreshData();
    }
    
    private void refreshData() {
        showLoading(true);
        
        // Load data in background
        new Thread(() -> {
            try {
                // Get data for time range
                List<PersonalData> data = dataService.getDataInRange(currentStartTime, currentEndTime);
                
                // Analyze patterns
                PatternAnalyzer.AnalysisResult result = analyzer.analyze(data, currentPatternType);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    displayResults(result);
                    showLoading(false);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing patterns", e);
                runOnUiThread(() -> {
                    showError("Failed to analyze patterns: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }
    
    private void displayResults(PatternAnalyzer.AnalysisResult result) {
        // Clear previous charts
        chartsContainer.removeAllViews();
        
        // Display based on pattern type
        switch (currentPatternType) {
            case "overview":
                displayOverview(result);
                break;
            case "correlations":
                displayCorrelations(result);
                break;
            case "trends":
                displayTrends(result);
                break;
            case "anomalies":
                displayAnomalies(result);
                break;
            case "predictions":
                displayPredictions(result);
                break;
        }
        
        // Update insights
        displayInsights(result.getInsights());
    }
    
    private void displayOverview(PatternAnalyzer.AnalysisResult result) {
        // Create timeline chart
        createTimelineChart(result.getTimelineData());
        
        // Create distribution pie chart
        createDistributionChart(result.getDataDistribution());
        
        // Create activity heatmap
        createActivityHeatmap(result.getActivityData());
    }
    
    private void createTimelineChart(Map<String, List<PatternAnalyzer.TimePoint>> timelineData) {
        LineChart timelineChart = new LineChart(this);
        timelineChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        
        List<ILineDataSet> dataSets = new ArrayList<>();
        int colorIndex = 0;
        int[] colors = {
                Color.parseColor("#FF6B6B"),
                Color.parseColor("#4ECDC4"),
                Color.parseColor("#45B7D1"),
                Color.parseColor("#FFA07A"),
                Color.parseColor("#98D8C8")
        };
        
        for (Map.Entry<String, List<PatternAnalyzer.TimePoint>> entry : timelineData.entrySet()) {
            String dataType = entry.getKey();
            List<PatternAnalyzer.TimePoint> points = entry.getValue();
            
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                entries.add(new Entry(i, points.get(i).value));
            }
            
            LineDataSet dataSet = new LineDataSet(entries, dataType);
            dataSet.setColor(colors[colorIndex % colors.length]);
            dataSet.setCircleColor(colors[colorIndex % colors.length]);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            
            dataSets.add(dataSet);
            colorIndex++;
        }
        
        LineData lineData = new LineData(dataSets);
        timelineChart.setData(lineData);
        
        // Customize chart
        timelineChart.getDescription().setEnabled(false);
        timelineChart.getLegend().setTextColor(Color.WHITE);
        timelineChart.getXAxis().setTextColor(Color.WHITE);
        timelineChart.getAxisLeft().setTextColor(Color.WHITE);
        timelineChart.getAxisRight().setEnabled(false);
        timelineChart.setDrawGridBackground(false);
        
        // Add to container
        CardView card = createChartCard("Activity Timeline");
        ((LinearLayout) card.findViewById(R.id.chart_container)).addView(timelineChart);
        chartsContainer.addView(card);
        
        timelineChart.animateX(1000);
    }
    
    private void createDistributionChart(Map<String, Float> distribution) {
        PieChart distributionChart = new PieChart(this);
        distributionChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : distribution.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Data Distribution");
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        
        // Set colors
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF6B6B"));
        colors.add(Color.parseColor("#4ECDC4"));
        colors.add(Color.parseColor("#45B7D1"));
        colors.add(Color.parseColor("#FFCE56"));
        colors.add(Color.parseColor("#4BC0C0"));
        colors.add(Color.parseColor("#9966FF"));
        dataSet.setColors(colors);
        
        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);
        
        distributionChart.setData(pieData);
        distributionChart.getLegend().setTextColor(Color.WHITE);
        
        // Add to container
        CardView card = createChartCard("Data Distribution");
        ((LinearLayout) card.findViewById(R.id.chart_container)).addView(distributionChart);
        chartsContainer.addView(card);
        
        distributionChart.animateY(1000);
    }
    
    private void displayCorrelations(PatternAnalyzer.AnalysisResult result) {
        // Create correlation matrix visualization
        createCorrelationMatrix(result.getCorrelations());
        
        // Show correlation insights
        createCorrelationCards(result.getTopCorrelations());
    }
    
    private void displayTrends(PatternAnalyzer.AnalysisResult result) {
        // Create trend lines for each data type
        for (Map.Entry<String, PatternAnalyzer.TrendInfo> entry : result.getTrends().entrySet()) {
            createTrendChart(entry.getKey(), entry.getValue());
        }
    }
    
    private void displayAnomalies(PatternAnalyzer.AnalysisResult result) {
        // Highlight anomalies on timeline
        createAnomalyChart(result.getAnomalies());
        
        // List anomaly details
        createAnomalyList(result.getAnomalies());
    }
    
    private void displayPredictions(PatternAnalyzer.AnalysisResult result) {
        // Show prediction charts
        createPredictionChart(result.getPredictions());
        
        // Show confidence levels
        createConfidenceCards(result.getPredictionConfidence());
    }
    
    private CardView createChartCard(String title) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(Color.parseColor("#2A2A2A"));
        card.setRadius(12f);
        card.setCardElevation(4f);
        
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18f);
        titleView.setTextColor(Color.WHITE);
        titleView.setPadding(0, 0, 0, 16);
        content.addView(titleView);
        
        LinearLayout chartContainer = new LinearLayout(this);
        chartContainer.setId(R.id.chart_container);
        content.addView(chartContainer);
        
        card.addView(content);
        return card;
    }
    
    private void createActivityHeatmap(Map<String, Map<Integer, Integer>> activityData) {
        // TODO: Implement heatmap visualization
        // For now, create a simple grid view
        
        LinearLayout heatmapContainer = new LinearLayout(this);
        heatmapContainer.setOrientation(LinearLayout.VERTICAL);
        
        for (Map.Entry<String, Map<Integer, Integer>> entry : activityData.entrySet()) {
            TextView typeLabel = new TextView(this);
            typeLabel.setText(entry.getKey());
            typeLabel.setTextColor(Color.WHITE);
            heatmapContainer.addView(typeLabel);
            
            // Create hour grid
            LinearLayout hourGrid = new LinearLayout(this);
            hourGrid.setOrientation(LinearLayout.HORIZONTAL);
            
            for (int hour = 0; hour < 24; hour++) {
                TextView hourCell = new TextView(this);
                int count = entry.getValue().getOrDefault(hour, 0);
                hourCell.setText(String.valueOf(count));
                hourCell.setTextSize(10f);
                hourCell.setPadding(4, 4, 4, 4);
                
                // Color based on intensity
                int intensity = Math.min(255, count * 50);
                hourCell.setBackgroundColor(Color.argb(intensity, 255, 107, 107));
                
                hourGrid.addView(hourCell);
            }
            
            heatmapContainer.addView(hourGrid);
        }
        
        CardView card = createChartCard("Activity Heatmap");
        LinearLayout content = (LinearLayout) card.getChildAt(0);
        LinearLayout chartContainer = new LinearLayout(this);
        chartContainer.addView(heatmapContainer);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        chartContainer.setLayoutParams(params);
        
        content.addView(chartContainer);
        
        // Create a horizontal scroll view for wide content
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        scrollView.addView(content);
        row.addView(card);
        
        chartsContainer.addView(row);
    }
    
    private void displayInsights(List<String> insights) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Pattern Insights:\n\n");
        
        for (String insight : insights) {
            sb.append("• ").append(insight).append("\n\n");
        }
        
        insightsText.setText(sb.toString());
    }
    
    private void createCorrelationMatrix(Map<String, Map<String, Double>> correlations) {
        // TODO: Implement correlation matrix visualization
        // For now, just log
        Log.d(TAG, "Correlations: " + correlations);
    }
    
    private void createCorrelationCards(List<PatternAnalyzer.Correlation> topCorrelations) {
        for (PatternAnalyzer.Correlation corr : topCorrelations) {
            CardView card = new CardView(this);
            // TODO: Implement correlation card UI
        }
    }
    
    private void createTrendChart(String dataType, PatternAnalyzer.TrendInfo trend) {
        // TODO: Implement trend visualization
        Log.d(TAG, "Trend for " + dataType + ": " + trend);
    }
    
    private void createAnomalyChart(List<PatternAnalyzer.Anomaly> anomalies) {
        // TODO: Implement anomaly visualization
        Log.d(TAG, "Anomalies: " + anomalies.size());
    }
    
    private void createAnomalyList(List<PatternAnalyzer.Anomaly> anomalies) {
        for (PatternAnalyzer.Anomaly anomaly : anomalies) {
            // TODO: Create anomaly list item
        }
    }
    
    private void createPredictionChart(Map<String, List<PatternAnalyzer.Prediction>> predictions) {
        // TODO: Implement prediction visualization
    }
    
    private void createConfidenceCards(Map<String, Double> confidence) {
        // TODO: Implement confidence cards
    }
    
    private void exportPatterns() {
        // TODO: Implement pattern export
        Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void shareInsights() {
        // TODO: Implement sharing
        Toast.makeText(this, "Share feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        chartsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        insightsText.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
*/
