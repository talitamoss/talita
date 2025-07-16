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
import java.util.List;
import java.util.Map;

public class DataCollectorsSettingsActivity extends AppCompatActivity {
    
    private DataCollectorManager collectorManager;
    private RecyclerView categoriesRecycler;
    private TextView enabledCollectorsText;
    private CategoriesAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collectors_settings);
        
        collectorManager = new DataCollectorManager(this);
        
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
            if (collector.isAvailable(this)) {
                collectorManager.setCollectorEnabled(collector.getDataType(), true);
            }
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    private void disableAllCollectors() {
        for (DataCollector collector : collectorManager.getAllCollectors()) {
            collectorManager.setCollectorEnabled(collector.getDataType(), false);
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }
    
    // Categories adapter
    private static class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Map<String, List<DataCollector>> categories;
        private final CollectorToggleListener listener;
        
        interface CollectorToggleListener {
            void onCollectorToggled(DataCollector collector, boolean enabled);
        }
        
        public CategoriesAdapter(Map<String, List<DataCollector>> categories, CollectorToggleListener listener) {
            this.categories = categories;
            this.listener = listener;
        }
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collector_category, parent, false)) {};
        }
        
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind category and collector data
        }
        
        @Override
        public int getItemCount() {
            return categories.size();
        }
    }
}
