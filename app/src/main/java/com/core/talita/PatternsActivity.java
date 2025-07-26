package com.core.talita;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PatternsActivity - Advanced data visualization and pattern analysis
 * 
 * Location: app/src/main/java/com/core/talita/PatternsActivity.java
 */
public class PatternsActivity extends AppCompatActivity {
    private static final String TAG = "PatternsActivity";
    
    // Time range constants
    private static final long DAY_MS = 24 * 60 * 60 * 1000;
    private static final long WEEK_MS = 7 * DAY_MS;
    private static final long MONTH_MS = 30 * DAY_MS;
    
    // UI Components
    private Spinner timeRangeSpinner;
    private Spinner patternTypeSpinner;
    private LinearLayout chartsContainer;
    private TextView insightsText;
    private ProgressBar loadingProgress;
    
    // Data
    private UniversalDataService dataService;
    private PatternAnalyzer analyzer;
    
    // Charts
    private LineChart timelineChart;
    private PieChart distributionChart;
    private RadarChart correlationChart;
    
    // Current state
    private long currentStartTime;
    private long currentEndTime;
    private String currentPatternType = "overview";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patterns);
        
        // Initialize services - FIXED: Using getInstance()
        dataService = UniversalDataService.getInstance(this);
        analyzer = new PatternAnalyzer();
        
        initializeViews();
        setupSpinners();
        loadInitialData();
    }
    
    private void initializeViews() {
        // Navigation
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Spinners
        timeRangeSpinner = findViewById(R.id.time_range_spinner);
        patternTypeSpinner = findViewById(R.id.pattern_type_spinner);
        
        // Containers
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
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Last 24 Hours", "Last Week", "Last Month", "All Time"});
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        ArrayAdapter<String> patternAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Overview", "Correlations", "Trends", "Anomalies", "Predictions"});
        patternAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        long now = System.currentTimeMillis();
        switch (position) {
            case 0: // Last 24 hours
                currentStartTime = now - DAY_MS;
                break;
            case 1: // Last week
                currentStartTime = now - WEEK_MS;
                break;
            case 2: // Last month
                currentStartTime = now - MONTH_MS;
                break;
            case 3: // All time
                currentStartTime = 0;
                break;
        }
        currentEndTime = now;
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
        createDistributionChart(result.getDistributionData());
        
        // Add summary cards
        createSummaryCards(result.getSummaryStats());
    }
    
    private void createTimelineChart(Map<String, List<PatternAnalyzer.TimePoint>> timelineData) {
        // Create chart view
        timelineChart = new LineChart(this);
        timelineChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        
        // Configure chart
        timelineChart.getDescription().setEnabled(false);
        timelineChart.setTouchEnabled(true);
        timelineChart.setDragEnabled(true);
        timelineChart.setScaleEnabled(true);
        timelineChart.setPinchZoom(true);
        timelineChart.setBackgroundColor(Color.parseColor("#1A1A1A"));
        timelineChart.setGridBackgroundColor(Color.parseColor("#1A1A1A"));
        
        // Prepare data sets
        List<ILineDataSet> dataSets = new ArrayList<>();
        int colorIndex = 0;
        int[] colors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GREEN, Color.RED};
        
        for (Map.Entry<String, List<PatternAnalyzer.TimePoint>> entry : timelineData.entrySet()) {
            List<Entry> entries = new ArrayList<>();
            List<PatternAnalyzer.TimePoint> points = entry.getValue();
            
            for (int i = 0; i < points.size(); i++) {
                entries.add(new Entry(i, points.get(i).getValue()));
            }
            
            LineDataSet dataSet = new LineDataSet(entries, entry.getKey());
            dataSet.setColor(colors[colorIndex % colors.length]);
            dataSet.setCircleColor(colors[colorIndex % colors.length]);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setDrawFilled(true);
            dataSet.setFillAlpha(50);
            dataSet.setFillColor(colors[colorIndex % colors.length]);
            
            dataSets.add(dataSet);
            colorIndex++;
        }
        
        // Set data
        LineData lineData = new LineData(dataSets);
        timelineChart.setData(lineData);
        
        // Configure axes
        XAxis xAxis = timelineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        
        timelineChart.getAxisLeft().setTextColor(Color.WHITE);
        timelineChart.getAxisRight().setEnabled(false);
        timelineChart.getLegend().setTextColor(Color.WHITE);
        
        // Add to container
        CardView card = createChartCard("Activity Timeline");
        ((LinearLayout) card.findViewById(R.id.chart_container)).addView(timelineChart);
        chartsContainer.addView(card);
        
        timelineChart.animateX(1000);
    }
    
    private void createDistributionChart(Map<String, Float> distributionData) {
        // Create chart view
        distributionChart = new PieChart(this);
        distributionChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 500));
        
        // Configure chart
        distributionChart.getDescription().setEnabled(false);
        distributionChart.setUsePercentValues(true);
        distributionChart.setExtraOffsets(5, 10, 5, 5);
        distributionChart.setDragDecelerationFrictionCoef(0.95f);
        distributionChart.setDrawHoleEnabled(true);
        distributionChart.setHoleColor(Color.parseColor("#1A1A1A"));
        distributionChart.setTransparentCircleRadius(61f);
        distributionChart.setHoleRadius(58f);
        distributionChart.setRotationAngle(0);
        distributionChart.setRotationEnabled(true);
        distributionChart.setHighlightPerTapEnabled(true);
        
        // Prepare data
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : distributionData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Distribution");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        // Set colors
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF6384"));
        colors.add(Color.parseColor("#36A2EB"));
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
        card.setRadius(8);
        card.setCardElevation(4);
        
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);
        
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(18);
        titleView.setPadding(0, 0, 0, 16);
        content.addView(titleView);
        
        LinearLayout chartContainer = new LinearLayout(this);
        chartContainer.setId(R.id.chart_container);
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(chartContainer);
        
        card.addView(content);
        return card;
    }
    
    private void createSummaryCards(Map<String, Object> stats) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(4, 0, 4, 16);
            card.setLayoutParams(params);
            card.setCardBackgroundColor(Color.parseColor("#2A2A2A"));
            card.setRadius(8);
            
            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(16, 16, 16, 16);
            content.setGravity(android.view.Gravity.CENTER);
            
            TextView value = new TextView(this);
            value.setText(String.valueOf(entry.getValue()));
            value.setTextColor(Color.WHITE);
            value.setTextSize(24);
            content.addView(value);
            
            TextView label = new TextView(this);
            label.setText(entry.getKey());
            label.setTextColor(Color.GRAY);
            label.setTextSize(12);
            content.addView(label);
            
            card.addView(content);
            row.addView(card);
        }
        
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
    }
    
    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    
    // Inner class for network visualization
    private class NetworkVisualization {
        private final Map<String, Node> nodeMap = new HashMap<>();
        private final List<Edge> edges = new ArrayList<>();
        
        public void buildFromData(List<PersonalData> data) {
            // Build nodes for each data type
            for (PersonalData item : data) {
                String type = item.getType();
                if (!nodeMap.containsKey(type)) {
                    nodeMap.put(type, new Node(type));
                }
                nodeMap.get(type).incrementCount();
            }
            
            // Build edges based on temporal proximity
            for (int i = 0; i < data.size() - 1; i++) {
                PersonalData item1 = data.get(i);
                PersonalData item2 = data.get(i + 1);
                
                // If items are within 5 minutes of each other
                if (Math.abs(item1.getTimestamp() - item2.getTimestamp()) < 5 * 60 * 1000
                        && !item1.getType().equals(item2.getType())) {
                    
                    Node from = nodeMap.get(item1.getType());
                    Node to = nodeMap.get(item2.getType());
                    
                    Edge edge = findOrCreateEdge(from, to);
                    edge.incrementWeight();
                }
            }
        }
        
        private Edge findOrCreateEdge(Node from, Node to) {
            for (Edge edge : edges) {
                if ((edge.from == from && edge.to == to) ||
                    (edge.from == to && edge.to == from)) {
                    return edge;
                }
            }
            
            Edge newEdge = new Edge(from, to);
            edges.add(newEdge);
            return newEdge;
        }
        
        class Node {
            String type;
            int count = 0;
            float x, y; // Position for visualization
            
            Node(String type) {
                this.type = type;
                // Random initial position
                this.x = (float) Math.random() * 100;
                this.y = (float) Math.random() * 100;
            }
            
            void incrementCount() {
                count++;
            }
        }
        
        class Edge {
            Node from, to;
            int weight = 0;
            
            Edge(Node from, Node to) {
                this.from = from;
                this.to = to;
            }
            
            void incrementWeight() {
                weight++;
            }
        }
    }
}
