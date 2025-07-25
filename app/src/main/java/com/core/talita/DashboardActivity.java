package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.api.*;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.core.WaterPlugin;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DashboardActivity - Main screen showing quick add tiles and recent activity
 * Updated to use plugin system instead of hard-coded collectors
 */
public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    
    // UI Components
    private RecyclerView quickAddRecyclerView;
    private RecyclerView recentActivityRecyclerView;
    private TextView tvWaterTotal;
    private TextView statusText;
    
    // Adapters
    private QuickAddAdapter quickAddAdapter;
    private RecentActivityAdapter recentActivityAdapter;
    
    // Services
    private DataCollectorManager collectorManager;
    private PluginManager pluginManager;
    
    // Testing
    private TextView testOutputText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        // Initialize services
        collectorManager = DataCollectorManager.getInstance(this);
        pluginManager = PluginManager.getInstance(this);
        
        // Find views
        quickAddRecyclerView = findViewById(R.id.quick_add_recycler);
        recentActivityRecyclerView = findViewById(R.id.recent_activity_recycler);
        tvWaterTotal = findViewById(R.id.tv_water_total);
        statusText = findViewById(R.id.status_text);
        
        // Setup UI
        setupQuickAddGrid();
        setupRecentActivityFeed();
        updateWaterDisplay();
        
        // Add testing features in debug mode
        if (BuildConfig.DEBUG) {
            addTestingFeatures();
        }
        
        // Start collectors
        collectorManager.startEnabledCollectors();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateWaterDisplay();
        loadRecentActivity();
    }
    
    private void setupQuickAddGrid() {
        // Get plugins that support quick add
        List<DataCollectorPlugin> quickAddPlugins = new ArrayList<>();
        
        for (DataCollectorPlugin plugin : pluginManager.getEnabledPlugins()) {
            if (plugin.supportsQuickAdd()) {
                quickAddPlugins.add(plugin);
            }
        }
        
        // Sort by priority
        quickAddPlugins.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        // Setup adapter
        quickAddAdapter = new QuickAddAdapter(quickAddPlugins, this::onQuickAddSelected);
        quickAddRecyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        quickAddRecyclerView.setAdapter(quickAddAdapter);
    }
    
    private void setupRecentActivityFeed() {
        recentActivityAdapter = new RecentActivityAdapter(new ArrayList<>());
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentActivityRecyclerView.setAdapter(recentActivityAdapter);
    }
    
    private void onQuickAddSelected(DataCollectorPlugin plugin) {
        Log.d(TAG, "Quick add selected: " + plugin.getPluginName());
        
        // Special handling for water plugin (most common)
        if (plugin.getPluginId().equals("core.water")) {
            // Quick water logging
            showWaterQuickAdd();
        } else {
            // Trigger collection for other plugins
            collectorManager.triggerCollection(plugin.getPluginId());
        }
    }
    
    private void showWaterQuickAdd() {
        // Create quick water buttons
        int[] amounts = {100, 250, 500, 750};
        
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        for (int amount : amounts) {
            Button btn = new Button(this);
            btn.setText(amount + "ml");
            btn.setOnClickListener(v -> {
                addWater(amount);
                Toast.makeText(this, "Added " + amount + "ml", Toast.LENGTH_SHORT).show();
            });
            buttonLayout.addView(btn);
        }
        
        // Show in a dialog or bottom sheet
        new android.app.AlertDialog.Builder(this)
            .setTitle("💧 Add Water")
            .setView(buttonLayout)
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void addWater(int amount) {
        Log.d(TAG, "Adding " + amount + "ml water...");
        
        Map<String, Object> data = new HashMap<>();
        data.put("value", amount);
        
        collectorManager.quickLog("core.water", data);
        updateWaterDisplay();
    }
    
    private void updateWaterDisplay() {
        if (tvWaterTotal != null) {
            int total = WaterPlugin.WaterHelper.getTodayTotal(this);
            tvWaterTotal.setText("💧 " + total + "ml today");
        }
    }
    
    private void loadRecentActivity() {
        // Get recent data from UniversalDataService
        UniversalDataService dataService = UniversalDataService.getInstance(this);
        List<PersonalData> recentData = dataService.getRecentData(10);
        
        // Update adapter
        recentActivityAdapter.updateData(recentData);
    }
    
    // ===== TESTING FEATURES =====
    
    private void addTestingFeatures() {
        // Create a test button at the bottom
        Button testButton = new Button(this);
        testButton.setText("🧪 Test Plugin System");
        testButton.setBackgroundColor(0xFF4CAF50);
        testButton.setTextColor(0xFFFFFFFF);
        testButton.setOnClickListener(v -> runQuickAddTest());
        
        // Create test output view
        testOutputText = new TextView(this);
        testOutputText.setTextColor(0xFF00FF00);
        testOutputText.setBackgroundColor(0xFF1A1A1A);
        testOutputText.setPadding(20, 20, 20, 20);
        testOutputText.setVisibility(View.GONE);
        
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView.getChildAt(0) instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) rootView.getChildAt(0);
            if (scrollView.getChildCount() > 0 && scrollView.getChildAt(0) instanceof ViewGroup) {
                ViewGroup mainLayout = (ViewGroup) scrollView.getChildAt(0);
                
                // Create container for test elements
                LinearLayout testContainer = new LinearLayout(this);
                testContainer.setOrientation(LinearLayout.VERTICAL);
                testContainer.addView(testButton);
                testContainer.addView(testOutputText);
                
                mainLayout.addView(testContainer);
            }
        }
    }
    
    private void runQuickAddTest() {
        Log.d(TAG, "=== PLUGIN SYSTEM TEST START ===");
        
        StringBuilder results = new StringBuilder();
        results.append("Plugin System Test Results:\n\n");
        
        try {
            // Test 1: Check services
            results.append("1. Services initialized: ");
            results.append(collectorManager != null && pluginManager != null ? "✅\n" : "❌\n");
            
            // Test 2: Check water plugin
            results.append("2. Water plugin registered: ");
            DataCollectorPlugin waterPlugin = pluginManager.getPlugin("core.water");
            results.append(waterPlugin != null ? "✅\n" : "❌\n");
            
            // Test 3: Water plugin enabled
            results.append("3. Water plugin enabled: ");
            boolean waterEnabled = waterPlugin != null && waterPlugin.isEnabled();
            results.append(waterEnabled ? "✅\n" : "❌\n");
            
            // Test 4: Get initial water total
            int initialTotal = WaterPlugin.WaterHelper.getTodayTotal(this);
            results.append("4. Initial water total: ").append(initialTotal).append("ml\n");
            
            // Test 5: Log water through plugin system
            results.append("5. Testing water logging...\n");
            Map<String, Object> testData = new HashMap<>();
            testData.put("value", 123);
            collectorManager.quickLog("core.water", testData);
            
            // Test 6: Verify new total
            int newTotal = WaterPlugin.WaterHelper.getTodayTotal(this);
            results.append("6. New water total: ").append(newTotal).append("ml ");
            results.append(newTotal == initialTotal + 123 ? "✅\n" : "❌\n");
            
            // Test 7: List all plugins
            results.append("\n7. Available plugins:\n");
            for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
                results.append("   ").append(plugin.getEmoji()).append(" ")
                      .append(plugin.getPluginName()).append(" [")
                      .append(plugin.getPluginId()).append("]\n");
            }
            
            results.append("\n✅ Plugin system test complete!");
            
        } catch (Exception e) {
            results.append("\n❌ Error: ").append(e.getMessage());
            Log.e(TAG, "Test failed", e);
        }
        
        if (testOutputText != null) {
            testOutputText.setText(results.toString());
            testOutputText.setVisibility(View.VISIBLE);
        }
        
        Log.d(TAG, results.toString());
        Log.d(TAG, "=== PLUGIN SYSTEM TEST END ===");
    }
    
    // ===== INNER CLASSES =====
    
    /**
     * Adapter for quick add grid
     */
    private static class QuickAddAdapter extends RecyclerView.Adapter<QuickAddAdapter.ViewHolder> {
        private final List<DataCollectorPlugin> plugins;
        private final OnPluginClickListener listener;
        
        interface OnPluginClickListener {
            void onPluginClick(DataCollectorPlugin plugin);
        }
        
        QuickAddAdapter(List<DataCollectorPlugin> plugins, OnPluginClickListener listener) {
            this.plugins = plugins;
            this.listener = listener;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Create a simple card view for each plugin
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200
            ));
            view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            view.setPadding(20, 20, 20, 20);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DataCollectorPlugin plugin = plugins.get(position);
            holder.textView.setText(plugin.getEmoji() + "\n" + plugin.getPluginName());
            holder.textView.setOnClickListener(v -> listener.onPluginClick(plugin));
        }
        
        @Override
        public int getItemCount() {
            return plugins.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            
            ViewHolder(TextView view) {
                super(view);
                textView = view;
            }
        }
    }
    
    /**
     * Adapter for recent activity feed
     */
    private static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
        private List<PersonalData> activities;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        RecentActivityAdapter(List<PersonalData> activities) {
            this.activities = activities;
        }
        
        void updateData(List<PersonalData> newData) {
            this.activities = newData;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            view.setPadding(20, 10, 20, 10);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PersonalData data = activities.get(position);
            String time = timeFormat.format(new Date(data.getTimestamp()));
            String text = time + " - " + data.getType() + ": " + data.getData().toString();
            holder.textView.setText(text);
        }
        
        @Override
        public int getItemCount() {
            return activities.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            
            ViewHolder(TextView view) {
                super(view);
                textView = view;
            }
        }
    }
}
