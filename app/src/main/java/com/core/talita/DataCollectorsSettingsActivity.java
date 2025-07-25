package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.api.DataCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Collectors Settings Activity
 * Updated to use the API DataCollector interface
 */
public class DataCollectorsSettingsActivity extends AppCompatActivity {
    
    private DataCollectorManager collectorManager;
    private RecyclerView categoriesRecycler;
    private TextView enabledCollectorsText;
    private CategoriesAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collectors_settings);
        
        collectorManager = DataCollectorManager.getInstance(this);
        
        initializeViews();
        setupCollectorCategories();
        updateStats();
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        categoriesRecycler = findViewById(R.id.categories_recycler);
        enabledCollectorsText = findViewById(R.id.enabled_collectors_text);
        
        // Quick actions
        findViewById(R.id.enable_all_button).setOnClickListener(v -> enableAllCollectors());
        findViewById(R.id.disable_all_button).setOnClickListener(v -> disableAllCollectors());
    }
    
    private void setupCollectorCategories() {
        Map<String, List<DataCollector>> categories = collectorManager.getCollectorsByCategory();
        
        adapter = new CategoriesAdapter(categories, this::onCollectorToggled);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoriesRecycler.setAdapter(adapter);
    }
    
    private void onCollectorToggled(DataCollector collector, boolean enabled) {
        collectorManager.setCollectorEnabled(collector.getDataType(), enabled);
        updateStats();
    }
    
    private void updateStats() {
        DataCollectorManager.CollectionStats stats = collectorManager.getCollectionStats();
        enabledCollectorsText.setText(stats.getSummary());
    }
    
    private void enableAllCollectors() {
        for (DataCollector collector : collectorManager.getAllCollectors()) {
            if (collector.isAvailable()) {
                collector.setEnabled(true);
                collectorManager.setCollectorEnabled(collector.getDataType(), true);
            }
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    private void disableAllCollectors() {
        for (DataCollector collector : collectorManager.getAllCollectors()) {
            collector.setEnabled(false);
            collectorManager.setCollectorEnabled(collector.getDataType(), false);
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    /**
     * Categories adapter for RecyclerView
     */
    private static class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_CATEGORY_HEADER = 0;
        private static final int TYPE_COLLECTOR_ITEM = 1;
        
        private final List<Object> items = new ArrayList<>();
        private final CollectorToggleListener listener;
        
        interface CollectorToggleListener {
            void onCollectorToggled(DataCollector collector, boolean enabled);
        }
        
        public CategoriesAdapter(Map<String, List<DataCollector>> categories, 
                                CollectorToggleListener listener) {
            this.listener = listener;
            
            // Build flat list with headers
            for (Map.Entry<String, List<DataCollector>> entry : categories.entrySet()) {
                items.add(entry.getKey()); // Category header
                items.addAll(entry.getValue()); // Collectors
            }
        }
        
        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_CATEGORY_HEADER : TYPE_COLLECTOR_ITEM;
        }
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            
            if (viewType == TYPE_CATEGORY_HEADER) {
                View view = inflater.inflate(R.layout.item_category_header, parent, false);
                return new CategoryViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_collector_setting, parent, false);
                return new CollectorViewHolder(view);
            }
        }
        
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CategoryViewHolder) {
                String category = (String) items.get(position);
                ((CategoryViewHolder) holder).bind(category);
            } else if (holder instanceof CollectorViewHolder) {
                DataCollector collector = (DataCollector) items.get(position);
                ((CollectorViewHolder) holder).bind(collector, listener);
            }
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        /**
         * ViewHolder for category headers
         */
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleText;
            
            CategoryViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.category_title);
            }
            
            void bind(String category) {
                String displayName = getCategoryDisplayName(category);
                titleText.setText(displayName);
            }
            
            private String getCategoryDisplayName(String category) {
                switch (category.toLowerCase()) {
                    case "i": return "Personal (I)";
                    case "we": return "Social (We)";
                    case "all": return "Universal (All)";
                    default: return category;
                }
            }
        }
        
        /**
         * ViewHolder for collector items
         */
        static class CollectorViewHolder extends RecyclerView.ViewHolder {
            private final TextView emojiText;
            private final TextView nameText;
            private final TextView descriptionText;
            private final Switch enableSwitch;
            
            CollectorViewHolder(View itemView) {
                super(itemView);
                emojiText = itemView.findViewById(R.id.collector_emoji);
                nameText = itemView.findViewById(R.id.collector_name);
                descriptionText = itemView.findViewById(R.id.collector_description);
                enableSwitch = itemView.findViewById(R.id.collector_switch);
            }
            
            void bind(DataCollector collector, CollectorToggleListener listener) {
                emojiText.setText(collector.getEmoji());
                nameText.setText(collector.getDisplayName());
                descriptionText.setText(collector.getDescription());
                
                // Set switch state without triggering listener
                enableSwitch.setOnCheckedChangeListener(null);
                enableSwitch.setChecked(collector.isEnabled());
                
                // Set availability
                boolean available = collector.isAvailable();
                enableSwitch.setEnabled(available);
                itemView.setAlpha(available ? 1.0f : 0.5f);
                
                if (!available) {
                    descriptionText.setText("Not available - missing permissions or sensors");
                }
                
                // Set listener
                enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    listener.onCollectorToggled(collector, isChecked);
                });
            }
        }
    }
}
