package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;
import com.core.talita.plugins.PluginManager;
import java.util.*;

/**
 * PluginManagementActivity - Manage data collector plugins
 */
public class PluginManagementActivity extends AppCompatActivity {
    
    private static final String TAG = "PluginManagement";
    
    private PluginManager pluginManager;
    private RecyclerView pluginsRecycler;
    private TextView activePluginsText;
    private Spinner categoryFilter;
    private PluginAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_management);
        
        pluginManager = PluginManager.getInstance(this);
        
        initializeViews();
        setupCategoryFilter();
        loadPlugins("all");
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        pluginsRecycler = findViewById(R.id.plugins_recycler);
        activePluginsText = findViewById(R.id.active_plugins_text);
        categoryFilter = findViewById(R.id.category_filter);
        
        pluginsRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        // Quick actions
        findViewById(R.id.enable_all_button).setOnClickListener(v -> enableAllPlugins());
        findViewById(R.id.disable_all_button).setOnClickListener(v -> disableAllPlugins());
        findViewById(R.id.reload_plugins_button).setOnClickListener(v -> reloadPlugins());
    }
    
    private void setupCategoryFilter() {
        String[] categories = new String[] {
            "All Categories",
            "I - Personal",
            "It - Environment", 
            "We - Social",
            "Custom"
        };
        
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilter.setAdapter(spinnerAdapter);
        
        categoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = position == 0 ? "all" : getCategoryFromPosition(position);
                loadPlugins(filter);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private String getCategoryFromPosition(int position) {
        switch (position) {
            case 1: return PluginCategories.I;
            case 2: return PluginCategories.IT;
            case 3: return PluginCategories.WE;
            case 4: return "custom";
            default: return "all";
        }
    }
    
    private void loadPlugins(String categoryFilter) {
        List<DataCollectorPlugin> plugins;
        
        if ("all".equals(categoryFilter)) {
            plugins = pluginManager.getAllPlugins();
        } else {
            plugins = new ArrayList<>();
            for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
                if (categoryFilter.equals(plugin.getCategory())) {
                    plugins.add(plugin);
                }
            }
        }
        
        // Sort by priority
        Collections.sort(plugins, (a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        adapter = new PluginAdapter(plugins);
        pluginsRecycler.setAdapter(adapter);
        
        updateStats();
    }
    
    private void updateStats() {
        int total = pluginManager.getAllPlugins().size();
        int enabled = pluginManager.getEnabledPlugins().size();
        activePluginsText.setText(enabled + " of " + total + " plugins active");
    }
    
    private void enableAllPlugins() {
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            plugin.setEnabled(true);
        }
        adapter.notifyDataSetChanged();
        updateStats();
        Toast.makeText(this, "All plugins enabled", Toast.LENGTH_SHORT).show();
    }
    
    private void disableAllPlugins() {
        for (DataCollectorPlugin plugin : pluginManager.getAllPlugins()) {
            plugin.setEnabled(false);
        }
        adapter.notifyDataSetChanged();
        updateStats();
        Toast.makeText(this, "All plugins disabled", Toast.LENGTH_SHORT).show();
    }
    
    private void reloadPlugins() {
        pluginManager.reloadPlugins();
        loadPlugins("all");
        Toast.makeText(this, "Plugins reloaded", Toast.LENGTH_SHORT).show();
    }
    
    private void showPluginInfo(DataCollectorPlugin plugin) {
        String message = "Version: " + plugin.getVersion() + "\n" +
                        "Author: " + plugin.getAuthor() + "\n" +
                        "Category: " + plugin.getCategory() + "\n" +
                        "Priority: " + plugin.getPriority() + "\n\n" +
                        "Features:\n" +
                        "• Quick Add: " + (plugin.supportsQuickAdd() ? "Yes" : "No") + "\n" +
                        "• Scheduling: " + (plugin.supportsScheduling() ? "Yes" : "No") + "\n" +
                        "• Background: " + (plugin.requiresBackgroundTracking() ? "Yes" : "No");
        
        new AlertDialog.Builder(this)
                .setTitle(plugin.getPluginName())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Settings", (dialog, which) -> {
                    if (plugin.hasSettings()) {
                        plugin.openSettings(this);
                    } else {
                        Toast.makeText(this, "No settings available", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    
    /**
     * Plugin Adapter
     */
    private class PluginAdapter extends RecyclerView.Adapter<PluginAdapter.ViewHolder> {
        private final List<DataCollectorPlugin> plugins;
        
        PluginAdapter(List<DataCollectorPlugin> plugins) {
            this.plugins = plugins;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_plugin, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(plugins.get(position));
        }
        
        @Override
        public int getItemCount() {
            return plugins.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emoji;
            TextView name;
            TextView description;
            TextView version;
            Switch enableSwitch;
            ImageButton infoButton;
            
            ViewHolder(View itemView) {
                super(itemView);
                emoji = itemView.findViewById(R.id.plugin_emoji);
                name = itemView.findViewById(R.id.plugin_name);
                description = itemView.findViewById(R.id.plugin_description);
                version = itemView.findViewById(R.id.plugin_version);
                enableSwitch = itemView.findViewById(R.id.plugin_enable_switch);
                infoButton = itemView.findViewById(R.id.plugin_info_button);
            }
            
            void bind(DataCollectorPlugin plugin) {
                emoji.setText(plugin.getEmoji());
                name.setText(plugin.getPluginName());
                description.setText(plugin.getPluginId());
                version.setText("v" + plugin.getVersion());
                
                enableSwitch.setChecked(plugin.isEnabled());
                enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    plugin.setEnabled(isChecked);
                    updateStats();
                });
                
                infoButton.setOnClickListener(v -> showPluginInfo(plugin));
                
                itemView.setOnClickListener(v -> showPluginInfo(plugin));
            }
        }
    }
}
