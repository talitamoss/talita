package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;
import com.core.talita.plugins.PluginManager;
import com.core.talita.plugins.loader.PluginLoader;
import com.core.talita.plugins.loader.PluginSecurityManager;
import com.core.talita.plugins.repository.PluginRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PluginManagementActivity - Main UI for plugin store
 * Shows installed plugins and coming soon features
 */
public class PluginManagementActivity extends AppCompatActivity {
    private static final String TAG = "PluginManagement";
    
    // UI Components
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    
    // Core components
    private PluginManager pluginManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_management);
        
        // Initialize components
        pluginManager = PluginManager.getInstance(this);
        
        setupUI();
    }
    
    private void setupUI() {
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Hide search for now (coming soon)
        View searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setVisibility(View.GONE);
        }
        
        // Settings button - show coming soon dialog
        View settingsButton = findViewById(R.id.settings_button);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> showComingSoonDialog());
        }
        
        // Tabs and ViewPager
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
        
        // Setup ViewPager with only installed plugins for now
        PluginPagerAdapter adapter = new PluginPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Connect tabs to ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Installed");
                    tab.setIcon(R.drawable.ic_installed);
                    break;
                case 1:
                    tab.setText("Coming Soon");
                    tab.setIcon(R.drawable.ic_updates);
                    break;
            }
        }).attach();
    }
    
    private void showComingSoonDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🚀 Coming Soon!")
            .setMessage("Developer features coming soon:\n\n" +
                       "• Submit your own plugins\n" +
                       "• Plugin development SDK\n" +
                       "• Community marketplace\n" +
                       "• Revenue sharing\n\n" +
                       "Stay tuned!")
            .setPositiveButton("OK", null)
            .show();
    }
    
    /**
     * Simplified ViewPager adapter
     */
    private static class PluginPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        PluginPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }
        
        @Override
        public int getItemCount() {
            return 2; // Installed + Coming Soon
        }
        
        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new InstalledPluginsFragment();
                case 1:
                    return new ComingSoonFragment();
                default:
                    return new InstalledPluginsFragment();
            }
        }
    }
    
    /**
     * Fragment for installed plugins
     */
    public static class InstalledPluginsFragment extends androidx.fragment.app.Fragment {
        private RecyclerView recyclerView;
        private InstalledPluginAdapter adapter;
        private PluginManager pluginManager;
        private TextView emptyText;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_installed_plugins, container, false);
            
            pluginManager = PluginManager.getInstance(getContext());
            
            recyclerView = view.findViewById(R.id.recycler_view);
            emptyText = view.findViewById(R.id.empty_text);
            
            setupRecyclerView();
            loadInstalledPlugins();
            
            return view;
        }
        
        private void setupRecyclerView() {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new InstalledPluginAdapter(new ArrayList<>(), plugin -> {
                // Handle plugin click
                showPluginOptions(plugin);
            });
            recyclerView.setAdapter(adapter);
        }
        
        private void loadInstalledPlugins() {
            List<DataCollectorPlugin> plugins = pluginManager.getAllPlugins();
            
            if (plugins.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                adapter.updatePlugins(plugins);
            }
        }
        
        private void showPluginOptions(DataCollectorPlugin plugin) {
            boolean isEnabled = pluginManager.isPluginEnabled(plugin.getPluginId());
            
            new AlertDialog.Builder(getContext())
                .setTitle(plugin.getEmoji() + " " + plugin.getPluginName())
                .setMessage("Version: " + plugin.getPluginVersion() + "\n" +
                          "Author: " + plugin.getAuthor() + "\n" +
                          "Category: " + plugin.getCategory() + "\n" +
                          "Status: " + (isEnabled ? "Enabled" : "Disabled"))
                .setPositiveButton(isEnabled ? "Disable" : "Enable", (dialog, which) -> {
                    pluginManager.setPluginEnabled(plugin.getPluginId(), !isEnabled);
                    loadInstalledPlugins();
                    Toast.makeText(getContext(), 
                        plugin.getPluginName() + (isEnabled ? " disabled" : " enabled"), 
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
        }
    }
    
    /**
     * Coming Soon Fragment
     */
    public static class ComingSoonFragment extends androidx.fragment.app.Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_coming_soon, container, false);
            
            // Setup coming soon cards
            setupComingSoonCard(view, R.id.card_marketplace, 
                "Plugin Marketplace", 
                "Browse and install community plugins",
                "🛍️");
                
            setupComingSoonCard(view, R.id.card_developer, 
                "Developer Portal", 
                "Create and publish your own plugins",
                "👩‍💻");
                
            setupComingSoonCard(view, R.id.card_sdk, 
                "Plugin SDK", 
                "Tools and libraries for plugin development",
                "🛠️");
                
            setupComingSoonCard(view, R.id.card_revenue, 
                "Revenue Sharing", 
                "Monetize your plugins",
                "💰");
            
            return view;
        }
        
        private void setupComingSoonCard(View parent, int cardId, String title, String desc, String emoji) {
            CardView card = parent.findViewById(cardId);
            if (card != null) {
                TextView titleView = card.findViewById(android.R.id.text1);
                TextView descView = card.findViewById(android.R.id.text2);
                TextView emojiView = card.findViewById(android.R.id.icon);
                
                if (titleView != null) titleView.setText(title);
                if (descView != null) descView.setText(desc);
                if (emojiView != null) emojiView.setText(emoji);
                
                card.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Coming soon!", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    
    /**
     * Adapter for installed plugins
     */
    private static class InstalledPluginAdapter extends RecyclerView.Adapter<InstalledPluginAdapter.ViewHolder> {
        private List<DataCollectorPlugin> plugins;
        private final OnPluginClickListener listener;
        
        interface OnPluginClickListener {
            void onPluginClick(DataCollectorPlugin plugin);
        }
        
        InstalledPluginAdapter(List<DataCollectorPlugin> plugins, OnPluginClickListener listener) {
            this.plugins = plugins;
            this.listener = listener;
        }
        
        void updatePlugins(List<DataCollectorPlugin> plugins) {
            this.plugins = plugins;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_installed_plugin, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DataCollectorPlugin plugin = plugins.get(position);
            
            holder.emoji.setText(plugin.getEmoji());
            holder.name.setText(plugin.getPluginName());
            holder.version.setText("v" + plugin.getPluginVersion());
            holder.author.setText("by " + plugin.getAuthor());
            
            // Category badge
            holder.category.setText(plugin.getCategory());
            holder.category.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                PluginCategories.getCategoryColor(plugin.getCategory())
            ));
            
            // Enabled state
            boolean isEnabled = PluginManager.getInstance(holder.itemView.getContext())
                .isPluginEnabled(plugin.getPluginId());
            holder.status.setText(isEnabled ? "Enabled" : "Disabled");
            holder.status.setTextColor(isEnabled ? 0xFF4CAF50 : 0xFF757575);
            
            // Click listener
            holder.itemView.setOnClickListener(v -> listener.onPluginClick(plugin));
        }
        
        @Override
        public int getItemCount() {
            return plugins.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView emoji, name, version, author, category, status;
            
            ViewHolder(View view) {
                super(view);
                emoji = view.findViewById(R.id.plugin_emoji);
                name = view.findViewById(R.id.plugin_name);
                version = view.findViewById(R.id.plugin_version);
                author = view.findViewById(R.id.plugin_author);
                category = view.findViewById(R.id.plugin_category);
                status = view.findViewById(R.id.plugin_status);
            }
        }
    }
}
