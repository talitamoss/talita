package com.core.talita;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Patterns Activity - Where users discover connections in their data
 * Three views: Timeline, Grid, Flow
 */
public class PatternsActivity extends AppCompatActivity {
    private static final String TAG = "PatternsActivity";
    
    // UI Components
    private TabLayout viewModeTabs;
    private ViewPager2 viewPager;
    private ChipGroup timeRangeChips;
    private RecyclerView insightsRecycler;
    private BottomNavigationView bottomNav;
    
    // Data
    private UniversalDataService dataService;
    private PatternAnalyzer patternAnalyzer;
    private List<PatternInsight> insights;
    
    // Time ranges
    private enum TimeRange {
        DAY("Today", 1),
        WEEK("Week", 7),
        MONTH("Month", 30),
        YEAR("Year", 365);
        
        final String label;
        final int days;
        
        TimeRange(String label, int days) {
            this.label = label;
            this.days = days;
        }
    }
    
    private TimeRange currentTimeRange = TimeRange.WEEK;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patterns);
        
        dataService = new UniversalDataService(this);
        patternAnalyzer = new PatternAnalyzer(this);
        
        setupViews();
        setupViewPager();
        setupTimeRangeSelector();
        setupBottomNavigation();
        
        loadPatterns();
    }
    
    private void setupViews() {
        viewModeTabs = findViewById(R.id.view_mode_tabs);
        viewPager = findViewById(R.id.view_pager);
        timeRangeChips = findViewById(R.id.time_range_chips);
        insightsRecycler = findViewById(R.id.insights_recycler);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        // Setup insights recycler
        insightsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupViewPager() {
        PatternsPagerAdapter adapter = new PatternsPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Connect tabs to ViewPager
        new TabLayoutMediator(viewModeTabs, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0: tab.setText("Timeline"); break;
                    case 1: tab.setText("Grid"); break;
                    case 2: tab.setText("Flow"); break;
                }
            }
        ).attach();
        
        // Update insights when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateInsights();
            }
        });
    }
    
    private void setupTimeRangeSelector() {
        for (TimeRange range : TimeRange.values()) {
            Chip chip = new Chip(this);
            chip.setText(range.label);
            chip.setCheckable(true);
            chip.setChecked(range == currentTimeRange);
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentTimeRange = range;
                    loadPatterns();
                }
            });
            
            timeRangeChips.addView(chip);
        }
    }
    
    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_patterns);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_today) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (itemId == R.id.nav_patterns) {
                return true; // Already here
            } else if (itemId == R.id.nav_vault) {
                // Navigate to vault
                return true;
            }
            
            return false;
        });
    }
    
    private void loadPatterns() {
        // Get data for selected time range
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (currentTimeRange.days * 24 * 60 * 60 * 1000L);
        
        List<PersonalData> data = dataService.getDataInRange(startTime, endTime);
        
        // Analyze patterns
        insights = patternAnalyzer.analyzePatterns(data);
        
        // Update all views
        updateAllViews(data);
        updateInsights();
    }
    
    private void updateAllViews(List<PersonalData> data) {
        // Update each view in the ViewPager
        PatternsPagerAdapter adapter = (PatternsPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            adapter.updateData(data);
        }
    }
    
    private void updateInsights() {
        InsightsAdapter adapter = new InsightsAdapter(insights);
        insightsRecycler.setAdapter(adapter);
    }
    
    /**
     * ViewPager adapter for different pattern views
     */
    private static class PatternsPagerAdapter extends RecyclerView.Adapter<PatternsPagerAdapter.ViewHolder> {
        private final Context context;
        private List<PersonalData> data = new ArrayList<>();
        
        PatternsPagerAdapter(Context context) {
            this.context = context;
        }
        
        void updateData(List<PersonalData> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }
        
        @Override
        public int getItemCount() {
            return 3; // Timeline, Grid, Flow
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new View(context); // Placeholder
            
            switch (viewType) {
                case 0: // Timeline
                    view = new TimelineView(context);
                    break;
                case 1: // Grid
                    view = new GridView(context);
                    break;
                case 2: // Flow
                    view = new FlowView(context);
                    break;
            }
            
            view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.itemView instanceof PatternView) {
                ((PatternView) holder.itemView).setData(data);
            }
        }
        
        @Override
        public int getItemViewType(int position) {
            return position;
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
    
    /**
     * Base interface for pattern views
     */
    interface PatternView {
        void setData(List<PersonalData> data);
    }
    
    /**
     * Timeline View - Shows data progression over time
     */
    public static class TimelineView extends View implements PatternView {
        private Paint paint;
        private Paint textPaint;
        private List<PersonalData> data = new ArrayList<>();
        private Map<String, Integer> typeColors;
        
        public TimelineView(Context context) {
            super(context);
            init();
        }
        
        public TimelineView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }
        
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(24f);
            textPaint.setColor(Color.WHITE);
            
            // Define colors for data types
            typeColors = new HashMap<>();
            typeColors.put("water", Color.parseColor("#3B82F6"));
            typeColors.put("exercise", Color.parseColor("#10B981"));
            typeColors.put("mood", Color.parseColor("#8B5CF6"));
            typeColors.put("sleep", Color.parseColor("#6366F1"));
            typeColors.put("location", Color.parseColor("#EC4899"));
        }
        
        @Override
        public void setData(List<PersonalData> newData) {
            this.data = newData;
            invalidate();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            if (data.isEmpty()) return;
            
            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            
            // Draw timeline axis
            paint.setColor(Color.parseColor("#333333"));
            paint.setStrokeWidth(2f);
            canvas.drawLine(padding, height - padding, width - padding, height - padding, paint);
            
            // Group data by type
            Map<String, List<PersonalData>> groupedData = new HashMap<>();
            for (PersonalData item : data) {
                String type = item.getDataType();
                if (!groupedData.containsKey(type)) {
                    groupedData.put(type, new ArrayList<>());
                }
                groupedData.get(type).add(item);
            }
            
            // Draw each data stream
            int streamHeight = (height - 2 * padding) / (groupedData.size() + 1);
            int streamIndex = 0;
            
            for (Map.Entry<String, List<PersonalData>> entry : groupedData.entrySet()) {
                String type = entry.getKey();
                List<PersonalData> items = entry.getValue();
                
                int y = padding + (streamIndex + 1) * streamHeight;
                Integer color = typeColors.get(type);
                if (color == null) color = Color.GRAY;
                
                // Draw data points
                paint.setColor(color);
                Path path = new Path();
                boolean first = true;
                
                for (PersonalData item : items) {
                    float x = padding + ((float) (item.getTimestamp() - getMinTimestamp()) 
                        / (getMaxTimestamp() - getMinTimestamp())) * (width - 2 * padding);
                    
                    // Draw point
                    canvas.drawCircle(x, y, 6f, paint);
                    
                    // Draw line
                    if (first) {
                        path.moveTo(x, y);
                        first = false;
                    } else {
                        path.lineTo(x, y);
                    }
                }
                
                // Draw path
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3f);
                paint.setAlpha(150);
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setAlpha(255);
                
                // Draw type label
                textPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(type, padding + 10, y - 10, textPaint);
                
                streamIndex++;
            }
        }
        
        private long getMinTimestamp() {
            if (data.isEmpty()) return 0;
            long min = Long.MAX_VALUE;
            for (PersonalData item : data) {
                min = Math.min(min, item.getTimestamp());
            }
            return min;
        }
        
        private long getMaxTimestamp() {
            if (data.isEmpty()) return 0;
            long max = Long.MIN_VALUE;
            for (PersonalData item : data) {
                max = Math.max(max, item.getTimestamp());
            }
            return max;
        }
    }
    
    /**
     * Grid View - Shows data density in a calendar-like grid
     */
    public static class GridView extends View implements PatternView {
        private Paint paint;
        private Paint textPaint;
        private List<PersonalData> data = new ArrayList<>();
        private Map<String, Integer> dailyCounts;
        
        public GridView(Context context) {
            super(context);
            init();
        }
        
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(20f);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }
        
        @Override
        public void setData(List<PersonalData> newData) {
            this.data = newData;
            calculateDailyCounts();
            invalidate();
        }
        
        private void calculateDailyCounts() {
            dailyCounts = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            for (PersonalData item : data) {
                String date = dateFormat.format(new Date(item.getTimestamp()));
                dailyCounts.put(date, dailyCounts.getOrDefault(date, 0) + 1);
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int width = getWidth();
            int height = getHeight();
            int padding = 20;
            
            // Calculate grid dimensions
            int cols = 7; // Days of week
            int cellSize = (width - 2 * padding) / cols;
            int rows = (height - 2 * padding) / cellSize;
            
            // Draw grid
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -(rows * cols));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int x = padding + col * cellSize;
                    int y = padding + row * cellSize;
                    
                    String date = dateFormat.format(cal.getTime());
                    int count = dailyCounts.getOrDefault(date, 0);
                    
                    // Draw cell
                    int alpha = Math.min(255, 50 + count * 30);
                    paint.setColor(Color.parseColor("#6366F1"));
                    paint.setAlpha(alpha);
                    
                    canvas.drawRoundRect(
                        x + 2, y + 2, 
                        x + cellSize - 2, y + cellSize - 2,
                        8f, 8f, paint
                    );
                    
                    // Draw day number
                    if (count > 0) {
                        textPaint.setAlpha(255);
                        canvas.drawText(
                            String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
                            x + cellSize / 2,
                            y + cellSize / 2 + 8,
                            textPaint
                        );
                    }
                    
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
        }
    }
    
    /**
     * Flow View - Shows connections between different data types
     */
    public static class FlowView extends View implements PatternView {
        private Paint paint;
        private List<PersonalData> data = new ArrayList<>();
        private List<Node> nodes = new ArrayList<>();
        private List<Connection> connections = new ArrayList<>();
        private ValueAnimator animator;
        
        private class Node {
            String type;
            float x, y;
            float vx, vy;
            int count;
            int color;
            
            void update() {
                x += vx;
                y += vy;
                vx *= 0.99f;
                vy *= 0.99f;
                
                // Bounce off walls
                if (x < 50 || x > getWidth() - 50) vx *= -1;
                if (y < 50 || y > getHeight() - 50) vy *= -1;
            }
        }
        
        private class Connection {
            Node from, to;
            float strength;
        }
        
        public FlowView(Context context) {
            super(context);
            init();
        }
        
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            
            // Start animation
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(50);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(animation -> {
                updateNodes();
                invalidate();
            });
            animator.start();
        }
        
        @Override
        public void setData(List<PersonalData> newData) {
            this.data = newData;
            buildGraph();
            invalidate();
        }
        
        private void buildGraph() {
            nodes.clear();
            connections.clear();
            
            // Create nodes for each data type
            Map<String, Node> nodeMap = new HashMap<>();
            Map<String, Integer> typeCounts = new HashMap<>();
            Map<String, Integer> typeColors = new HashMap<>();
            typeColors.put("water", Color.parseColor("#3B82F6"));
            typeColors.put("exercise", Color.parseColor("#10B981"));
            typeColors.put("mood", Color.parseColor("#8B5CF6"));
            typeColors.put("sleep", Color.parseColor("#6366F1"));
            
            // Count occurrences
            for (PersonalData item : data) {
                typeCounts.put(item.getDataType(), 
                    typeCounts.getOrDefault(item.getDataType(), 0) + 1);
            }
            
            // Create nodes
            Random rand = new Random();
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                Node node = new Node();
                node.type = entry.getKey();
                node.count = entry.getValue();
                node.x = rand.nextFloat() * getWidth();
                node.y = rand.nextFloat() * getHeight();
                node.vx = (rand.nextFloat() - 0.5f) * 2;
                node.vy = (rand.nextFloat() - 0.5f) * 2;
                node.color = typeColors.getOrDefault(node.type, Color.GRAY);
                
                nodes.add(node);
                nodeMap.put(node.type, node);
            }
            
            // Find connections (co-occurrences within time windows)
            long timeWindow = 60 * 60 * 1000; // 1 hour
            
            for (int i = 0; i < data.size(); i++) {
                PersonalData item1 = data.get(i);
                
                for (int j = i + 1; j < data.size(); j++) {
                    PersonalData item2 = data.get(j);
                    
                    if (Math.abs(item1.getTimestamp() - item2.getTimestamp()) < timeWindow
                        && !item1.getDataType().equals(item2.getDataType())) {
                        
                        Node from = nodeMap.get(item1.getDataType());
                        Node to = nodeMap.get(item2.getDataType());
                        
                        if (from != null && to != null) {
                            // Check if connection exists
                            Connection existing = null;
                            for (Connection conn : connections) {
                                if ((conn.from == from && conn.to == to) ||
                                    (conn.from == to && conn.to == from)) {
                                    existing = conn;
                                    break;
                                }
                            }
                            
                            if (existing != null) {
                                existing.strength += 0.1f;
                            } else {
                                Connection conn = new Connection();
                                conn.from = from;
                                conn.to = to;
                                conn.strength = 0.1f;
                                connections.add(conn);
                            }
                        }
                    }
                }
            }
        }
        
        private void updateNodes() {
            // Apply forces
            for (int i = 0; i < nodes.size(); i++) {
                Node n1 = nodes.get(i);
                
                // Repulsion between nodes
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node n2 = nodes.get(j);
                    
                    float dx = n2.x - n1.x;
                    float dy = n2.y - n1.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    
                    if (dist < 200) {
                        float force = (200 - dist) / 200 * 0.5f;
                        dx /= dist;
                        dy /= dist;
                        
                        n1.vx -= dx * force;
                        n1.vy -= dy * force;
                        n2.vx += dx * force;
                        n2.vy += dy * force;
                    }
                }
                
                // Attraction along connections
                for (Connection conn : connections) {
                    if (conn.from == n1 || conn.to == n1) {
                        Node other = (conn.from == n1) ? conn.to : conn.from;
                        
                        float dx = other.x - n1.x;
                        float dy = other.y - n1.y;
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        
                        if (dist > 100) {
                            float force = conn.strength * 0.1f;
                            dx /= dist;
                            dy /= dist;
                            
                            n1.vx += dx * force;
                            n1.vy += dy * force;
                        }
                    }
                }
                
                // Update position
                n1.update();
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            // Draw connections
            paint.setStrokeWidth(2f);
            for (Connection conn : connections) {
                paint.setColor(Color.WHITE);
                paint.setAlpha((int) (conn.strength * 50));
                canvas.drawLine(conn.from.x, conn.from.y, conn.to.x, conn.to.y, paint);
            }
            
            // Draw nodes
            for (Node node : nodes) {
                paint.setColor(node.color);
                paint.setAlpha(200);
                
                float radius = 20 + (float) Math.sqrt(node.count) * 5;
                canvas.drawCircle(node.x, node.y, radius, paint);
                
                // Draw label
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(20f);
                canvas.drawText(node.type, node.x, node.y + 8, paint);
            }
        }
        
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (animator != null) {
                animator.cancel();
            }
        }
    }
    
    /**
     * Pattern insight data class
     */
    public static class PatternInsight {
        public String title;
        public String description;
        public float confidence;
        public String icon;
        
        PatternInsight(String title, String description, float confidence, String icon) {
            this.title = title;
            this.description = description;
            this.confidence = confidence;
            this.icon = icon;
        }
    }
    
    /**
     * Insights adapter
     */
    private static class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.ViewHolder> {
        private final List<PatternInsight> insights;
        
        InsightsAdapter(List<PatternInsight> insights) {
            this.insights = insights;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pattern_insight, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PatternInsight insight = insights.get(position);
            holder.bind(insight);
        }
        
        @Override
        public int getItemCount() {
            return insights.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView iconText;
            TextView titleText;
            TextView descriptionText;
            View confidenceBar;
            
            ViewHolder(View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.insight_icon);
                titleText = itemView.findViewById(R.id.insight_title);
                descriptionText = itemView.findViewById(R.id.insight_description);
                confidenceBar = itemView.findViewById(R.id.confidence_bar);
            }
            
            void bind(PatternInsight insight) {
                iconText.setText(insight.icon);
                titleText.setText(insight.title);
                descriptionText.setText(insight.description);
                
                // Set confidence bar width
                ViewGroup.LayoutParams params = confidenceBar.getLayoutParams();
                params.width = (int) (itemView.getWidth() * insight.confidence);
                confidenceBar.setLayoutParams(params);
            }
        }
    }
}
