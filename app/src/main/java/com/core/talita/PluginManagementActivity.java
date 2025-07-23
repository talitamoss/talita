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
 * 
 * Features:
 * - Browse available plugins
 * - Search and filter
 * - Install/uninstall plugins
 * - Manage permissions
 * - View security information
 */
public class PluginManagementActivity extends AppCompatActivity {
    private static final String TAG = "PluginManagement";
    
    // UI Components
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SearchView searchView;
    private ProgressBar progressBar;
    
    // Core components
    private PluginManager pluginManager;
    private PluginLoader pluginLoader;
    private PluginRepository repository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_management);
        
        // Initialize components
        pluginManager = PluginManager.getInstance(this);
        pluginLoader = new PluginLoader(this);
        repository = new PluginRepository(this);
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Search
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlugins(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        
        // Tabs and ViewPager
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
        
        // Setup ViewPager
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
                    tab.setText("Featured");
                    tab.setIcon(R.drawable.ic_featured);
                    break;
                case 2:
                    tab.setText("Browse");
                    tab.setIcon(R.drawable.ic_browse);
                    break;
                case 3:
                    tab.setText("Updates");
                    tab.setIcon(R.drawable.ic_updates);
                    break;
            }
        }).attach();
    }
    
    private void loadData() {
        // Load installed plugins
        pluginLoader.loadAllPlugins();
        
        // Load featured plugins
        progressBar.setVisibility(View.VISIBLE);
        repository.getFeaturedPlugins(new PluginRepository.SearchCallback() {
            @Override
            public void onSuccess(List<PluginRepository.PluginListing> results) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    // Update UI with featured plugins
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PluginManagementActivity.this, 
                        "Failed to load featured plugins", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void searchPlugins(String query) {
        progressBar.setVisibility(View.VISIBLE);
        repository.searchPlugins(query, null, new PluginRepository.SearchCallback() {
            @Override
            public void onSuccess(List<PluginRepository.PluginListing> results) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    // Show search results
                    showSearchResults(results);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PluginManagementActivity.this, 
                        "Search failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showSearchResults(List<PluginRepository.PluginListing> results) {
        // Switch to browse tab and show results
        viewPager.setCurrentItem(2);
        // Update browse fragment with search results
    }
    
    /**
     * ViewPager adapter for plugin tabs
     */
    private static class PluginPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        PluginPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }
        
        @Override
        public int getItemCount() {
            return 4; // Installed, Featured, Browse, Updates
        }
        
        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new InstalledPluginsFragment();
                case 1:
                    return new FeaturedPluginsFragment();
                case 2:
                    return new BrowsePluginsFragment();
                case 3:
                    return new UpdatesFragment();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(plugin.getPluginName());
            
            boolean isEnabled = pluginManager.isPluginEnabled(plugin.getPluginId());
            
            List<String> options = new ArrayList<>();
            options.add(isEnabled ? "Disable" : "Enable");
            if (plugin.hasSettings()) {
                options.add("Settings");
            }
            options.add("View Details");
            options.add("Uninstall");
            
            builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                switch (options.get(which)) {
                    case "Enable":
                    case "Disable":
                        pluginManager.setPluginEnabled(plugin.getPluginId(), !isEnabled);
                        loadInstalledPlugins();
                        break;
                    case "Settings":
                        plugin.openSettings(getContext());
                        break;
                    case "View Details":
                        showPluginDetails(plugin);
                        break;
                    case "Uninstall":
                        confirmUninstall(plugin);
                        break;
                }
            });
            
            builder.show();
        }
        
        private void showPluginDetails(DataCollectorPlugin plugin) {
            // Show detailed plugin information
            Intent intent = new Intent(getContext(), PluginDetailsActivity.class);
            intent.putExtra("plugin_id", plugin.getPluginId());
            startActivity(intent);
        }
        
        private void confirmUninstall(DataCollectorPlugin plugin) {
            new AlertDialog.Builder(getContext())
                .setTitle("Uninstall Plugin")
                .setMessage("Are you sure you want to uninstall " + plugin.getPluginName() + "?")
                .setPositiveButton("Uninstall", (dialog, which) -> {
                    // Uninstall plugin
                    PluginLoader loader = new PluginLoader(getContext());
                    loader.unloadPlugin(plugin.getPluginId());
                    loadInstalledPlugins();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
    
    /**
     * Fragment for browsing available plugins
     */
    public static class BrowsePluginsFragment extends androidx.fragment.app.Fragment {
        private RecyclerView recyclerView;
        private PluginListingAdapter adapter;
        private PluginRepository repository;
        private ProgressBar progressBar;
        private Spinner categorySpinner;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_browse_plugins, container, false);
            
            repository = new PluginRepository(getContext());
            
            recyclerView = view.findViewById(R.id.recycler_view);
            progressBar = view.findViewById(R.id.progress_bar);
            categorySpinner = view.findViewById(R.id.category_spinner);
            
            setupCategoryFilter();
            setupRecyclerView();
            loadPlugins();
            
            return view;
        }
        
        private void setupCategoryFilter() {
            List<String> categories = new ArrayList<>();
            categories.add("All Categories");
            categories.add("I - Personal");
            categories.add("We - Relationships");
            categories.add("All - World");
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), 
                android.R.layout.simple_spinner_item, 
                categories
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            
            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String category = position == 0 ? null : 
                        (position == 1 ? "I" : position == 2 ? "We" : "All");
                    filterByCategory(category);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        private void setupRecyclerView() {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new PluginListingAdapter(new ArrayList<>(), listing -> {
                // Handle plugin click - show details
                showPluginDetails(listing);
            });
            recyclerView.setAdapter(adapter);
        }
        
        private void loadPlugins() {
            progressBar.setVisibility(View.VISIBLE);
            repository.searchPlugins("", null, new PluginRepository.SearchCallback() {
                @Override
                public void onSuccess(List<PluginRepository.PluginListing> results) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        adapter.updateListings(results);
                    });
                }
                
                @Override
                public void onError(String error) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load plugins", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        
        private void filterByCategory(String category) {
            progressBar.setVisibility(View.VISIBLE);
            repository.searchPlugins("", category, new PluginRepository.SearchCallback() {
                @Override
                public void onSuccess(List<PluginRepository.PluginListing> results) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        adapter.updateListings(results);
                    });
                }
                
                @Override
                public void onError(String error) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                    });
                }
            });
        }
        
        private void showPluginDetails(PluginRepository.PluginListing listing) {
            Intent intent = new Intent(getContext(), PluginStoreDetailsActivity.class);
            intent.putExtra("plugin_id", listing.id);
            startActivity(intent);
        }
    }
    
    /**
     * Adapter for plugin listings
     */
    private static class PluginListingAdapter extends RecyclerView.Adapter<PluginListingAdapter.ViewHolder> {
        private List<PluginRepository.PluginListing> listings;
        private final OnListingClickListener listener;
        
        interface OnListingClickListener {
            void onListingClick(PluginRepository.PluginListing listing);
        }
        
        PluginListingAdapter(List<PluginRepository.PluginListing> listings, OnListingClickListener listener) {
            this.listings = listings;
            this.listener = listener;
        }
        
        void updateListings(List<PluginRepository.PluginListing> listings) {
            this.listings = listings;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plugin_listing, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PluginRepository.PluginListing listing = listings.get(position);
            
            holder.name.setText(listing.name);
            holder.description.setText(listing.description);
            holder.author.setText("by " + listing.author);
            
            // Rating
            holder.rating.setText(String.format("%.1f ⭐", listing.rating));
            
            // Downloads
            String downloads = listing.downloads < 1000 ? String.valueOf(listing.downloads) :
                listing.downloads < 1000000 ? (listing.downloads / 1000) + "K" :
                (listing.downloads / 1000000) + "M";
            holder.downloads.setText(downloads + " downloads");
            
            // Category
            holder.category.setText(listing.category);
            holder.category.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                PluginCategories.getCategoryColor(listing.category)
            ));
            
            // Click listener
            holder.itemView.setOnClickListener(v -> listener.onListingClick(listing));
        }
        
        @Override
        public int getItemCount() {
            return listings.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, description, author, rating, downloads, category;
            
            ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.plugin_name);
                description = view.findViewById(R.id.plugin_description);
                author = view.findViewById(R.id.plugin_author);
                rating = view.findViewById(R.id.plugin_rating);
                downloads = view.findViewById(R.id.plugin_downloads);
                category = view.findViewById(R.id.plugin_category);
            }
        }
    }
    
    /**
     * Fragment for featured plugins
     */
    public static class FeaturedPluginsFragment extends BrowsePluginsFragment {
        @Override
        protected void loadPlugins() {
            progressBar.setVisibility(View.VISIBLE);
            repository.getFeaturedPlugins(new PluginRepository.SearchCallback() {
                @Override
                public void onSuccess(List<PluginRepository.PluginListing> results) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        adapter.updateListings(results);
                    });
                }
                
                @Override
                public void onError(String error) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load featured plugins", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
    
    /**
     * Fragment for plugin updates
     */
    public static class UpdatesFragment extends androidx.fragment.app.Fragment {
        // Implementation for showing available updates
    }
}
