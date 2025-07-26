package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TodayActivity - Shows today's data summary
 * 
 * Location: app/src/main/java/com/core/talita/TodayActivity.java
 */
public class TodayActivity extends AppCompatActivity {
    
    private DataCollectorManager collectorManager;
    private UniversalDataService dataService;
    
    private TextView dateText;
    private TextView summaryText;
    private RecyclerView todayDataRecycler;
    private RecyclerView quickAddRecycler;
    private LinearLayout emptyView;
    
    private TodayDataAdapter dataAdapter;
    private QuickAddAdapter quickAddAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        
        // Initialize services - FIXED: Using getInstance()
        collectorManager = DataCollectorManager.getInstance(this);
        dataService = UniversalDataService.getInstance(this);
        
        initializeViews();
        loadTodayData();
        setupQuickAdd();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadTodayData(); // Refresh when returning
    }
    
    private void initializeViews() {
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        dateText = findViewById(R.id.date_text);
        summaryText = findViewById(R.id.summary_text);
        todayDataRecycler = findViewById(R.id.today_data_recycler);
        quickAddRecycler = findViewById(R.id.quick_add_recycler);
        emptyView = findViewById(R.id.empty_view);
        
        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date()));
        
        // Setup recyclers
        dataAdapter = new TodayDataAdapter();
        todayDataRecycler.setLayoutManager(new LinearLayoutManager(this));
        todayDataRecycler.setAdapter(dataAdapter);
        
        quickAddAdapter = new QuickAddAdapter();
        quickAddRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        quickAddRecycler.setAdapter(quickAddAdapter);
        
        // View all button
        findViewById(R.id.view_all_button).setOnClickListener(v -> {
            startActivity(new Intent(this, DataSummaryActivity.class));
        });
    }
    
    private void loadTodayData() {
        new Thread(() -> {
            try {
                // Get today's data
                List<PersonalData> todaysData = dataService.getTodaysData();
                
                // Group by type
                Map<String, Integer> counts = new HashMap<>();
                Map<String, String> lastValues = new HashMap<>();
                
                for (PersonalData data : todaysData) {
                    String type = data.getType();
                    counts.put(type, counts.getOrDefault(type, 0) + 1);
                    
                    // Store last value for display
                    if (data.getData().containsKey("value")) {
                        lastValues.put(type, data.getData().get("value").toString());
                    }
                }
                
                runOnUiThread(() -> {
                    updateSummary(todaysData.size(), counts);
                    dataAdapter.setData(buildSummaryItems(counts, lastValues));
                    emptyView.setVisibility(todaysData.isEmpty() ? View.VISIBLE : View.GONE);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    summaryText.setText("Error loading data");
                });
            }
        }).start();
    }
    
    private void updateSummary(int totalEntries, Map<String, Integer> counts) {
        if (totalEntries == 0) {
            summaryText.setText("No data collected today");
        } else {
            StringBuilder summary = new StringBuilder();
            summary.append(totalEntries).append(" entries today\n");
            
            // Highlight top categories
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                Map.Entry<String, Integer> entry = sorted.get(i);
                summary.append(getEmojiForType(entry.getKey()));
                summary.append(" ").append(entry.getValue()).append(" ");
            }
            
            summaryText.setText(summary.toString());
        }
    }
    
    private List<SummaryItem> buildSummaryItems(Map<String, Integer> counts, Map<String, String> lastValues) {
        List<SummaryItem> items = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            SummaryItem item = new SummaryItem();
            item.type = entry.getKey();
            item.count = entry.getValue();
            item.lastValue = lastValues.get(entry.getKey());
            item.emoji = getEmojiForType(entry.getKey());
            items.add(item);
        }
        
        // Sort by count
        items.sort((a, b) -> b.count.compareTo(a.count));
        
        return items;
    }
    
    private void setupQuickAdd() {
        // Get collectors that support quick add
        List<QuickAddItem> quickAddItems = new ArrayList<>();
        
        // Add common collectors
        quickAddItems.add(new QuickAddItem("water", "Water", "💧"));
        quickAddItems.add(new QuickAddItem("mood", "Mood", "😊"));
        quickAddItems.add(new QuickAddItem("exercise", "Exercise", "💪"));
        
        quickAddAdapter.setItems(quickAddItems);
    }
    
    private String getEmojiForType(String type) {
        switch (type) {
            case "water": return "💧";
            case "mood": return "😊";
            case "exercise": return "💪";
            case "sleep": return "😴";
            case "food": return "🍽️";
            case "location": return "📍";
            default: return "📝";
        }
    }
    
    // Data classes
    
    static class SummaryItem {
        String type;
        String emoji;
        Integer count;
        String lastValue;
    }
    
    static class QuickAddItem {
        String type;
        String name;
        String emoji;
        
        QuickAddItem(String type, String name, String emoji) {
            this.type = type;
            this.name = name;
            this.emoji = emoji;
        }
    }
    
    // Adapters
    
    class TodayDataAdapter extends RecyclerView.Adapter<TodayDataAdapter.ViewHolder> {
        private List<SummaryItem> items = new ArrayList<>();
        
        void setData(List<SummaryItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            CardView card = new CardView(parent.getContext());
            card.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
            card.setCardBackgroundColor(0xFF2A2A2A);
            card.setRadius(8);
            card.setContentPadding(16, 16, 16, 16);
            card.setUseCompatPadding(true);
            
            return new ViewHolder(card);
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
            TextView textView;
            
            ViewHolder(View itemView) {
                super(itemView);
                textView = new TextView(itemView.getContext());
                textView.setTextColor(0xFFFFFFFF);
                ((CardView) itemView).addView(textView);
            }
            
            void bind(SummaryItem item) {
                String text = item.emoji + " " + item.type + ": " + item.count + " entries";
                if (item.lastValue != null) {
                    text += "\nLast: " + item.lastValue;
                }
                textView.setText(text);
            }
        }
    }
    
    class QuickAddAdapter extends RecyclerView.Adapter<QuickAddAdapter.ViewHolder> {
        private List<QuickAddItem> items = new ArrayList<>();
        
        void setItems(List<QuickAddItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            CardView card = new CardView(parent.getContext());
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(
                GridLayoutManager.LayoutParams.MATCH_PARENT,
                200);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);
            card.setCardBackgroundColor(0xFF3C3C3C);
            card.setRadius(8);
            card.setClickable(true);
            card.setFocusable(true);
            
            return new ViewHolder(card);
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
            TextView nameText;
            
            ViewHolder(View itemView) {
                super(itemView);
                
                LinearLayout layout = new LinearLayout(itemView.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(android.view.Gravity.CENTER);
                layout.setPadding(16, 16, 16, 16);
                
                emojiText = new TextView(itemView.getContext());
                emojiText.setTextSize(32);
                emojiText.setGravity(android.view.Gravity.CENTER);
                layout.addView(emojiText);
                
                nameText = new TextView(itemView.getContext());
                nameText.setTextColor(0xFFFFFFFF);
                nameText.setGravity(android.view.Gravity.CENTER);
                layout.addView(nameText);
                
                ((CardView) itemView).addView(layout);
            }
            
            void bind(QuickAddItem item) {
                emojiText.setText(item.emoji);
                nameText.setText(item.name);
                
                itemView.setOnClickListener(v -> {
                    // TODO: Trigger collection for this type
                    android.widget.Toast.makeText(v.getContext(), 
                        "Add " + item.name, android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
