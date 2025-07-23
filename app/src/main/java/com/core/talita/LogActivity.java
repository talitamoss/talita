package com.core.talita;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import java.util.List;

/**
 * Log Activity - Clean interface for logging any type of data
 * Uses plugin architecture for extensibility
 */
public class LogActivity extends AppCompatActivity {
    private static final String TAG = "LogActivity";
    
    private PluginManager pluginManager;
    private RecyclerView pluginGrid;
    private PluginQuickAddAdapter adapter;
    private View backgroundOverlay;
    private TextView headerTitle;
    private TextView headerSubtitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        
        // Initialize plugin manager
        pluginManager = PluginManager.getInstance(this);
        
        setupViews();
        loadPlugins();
        startAnimations();
    }
    
    private void setupViews() {
        // Background overlay for smooth appearance
        backgroundOverlay = findViewById(R.id.background_overlay);
        
        // Header
        headerTitle = findViewById(R.id.header_title);
        headerSubtitle = findViewById(R.id.header_subtitle);
        
        // Close button
        ImageButton closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());
        
        // Plugin grid
        pluginGrid = findViewById(R.id.plugin_grid);
        pluginGrid.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Settings button
        Button managePluginsButton = findViewById(R.id.manage_plugins_button);
        managePluginsButton.setOnClickListener(v -> {
            // TODO: Open plugin management screen
        });
    }
    
    private void loadPlugins() {
        List<DataCollectorPlugin> quickAddPlugins = pluginManager.getQuickAddPlugins();
        adapter = new PluginQuickAddAdapter(quickAddPlugins, this::onPluginSelected);
        pluginGrid.setAdapter(adapter);
        
        // Update header
        headerTitle.setText("Log Data");
        headerSubtitle.setText(quickAddPlugins.size() + " types available");
    }
    
    private void onPluginSelected(DataCollectorPlugin plugin) {
        // Handle plugin selection based on its quick add style
        DataCollectorPlugin.QuickAddConfig config = plugin.getQuickAddConfig();
        
        switch (config.style) {
            case SIMPLE_TAP:
                handleSimpleTap(plugin);
                break;
            case NUMERIC_INPUT:
                showNumericInput(plugin);
                break;
            case CHOICE_PICKER:
                showChoicePicker(plugin);
                break;
            case DURATION_TIMER:
                showDurationTimer(plugin);
                break;
            case TEXT_NOTE:
                showTextInput(plugin);
                break;
            case VOICE_RECORD:
                startVoiceRecording(plugin);
                break;
            default:
                // Custom handling
                break;
        }
    }
    
    private void handleSimpleTap(DataCollectorPlugin plugin) {
        // Quick add with default value (e.g., 250ml water)
        plugin.createCollector(this).collect();
        
        // Show success feedback
        showSuccessFeedback(plugin.getEmoji() + " Logged!");
        
        // Close after short delay
        backgroundOverlay.postDelayed(this::finish, 1000);
    }
    
    private void showNumericInput(DataCollectorPlugin plugin) {
        // TODO: Show numeric input dialog
    }
    
    private void showChoicePicker(DataCollectorPlugin plugin) {
        // TODO: Show choice picker dialog
    }
    
    private void showDurationTimer(DataCollectorPlugin plugin) {
        // TODO: Show duration timer
    }
    
    private void showTextInput(DataCollectorPlugin plugin) {
        // TODO: Show text input dialog
    }
    
    private void startVoiceRecording(DataCollectorPlugin plugin) {
        // TODO: Start voice recording
    }
    
    private void showSuccessFeedback(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        if (toastView != null) {
            toastView.setBackgroundResource(R.drawable.toast_background);
        }
        toast.show();
    }
    
    private void startAnimations() {
        // Fade in animation
        backgroundOverlay.setAlpha(0f);
        backgroundOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // Slide up animation for content
        ViewGroup content = findViewById(R.id.content_container);
        content.setTranslationY(100f);
        content.animate()
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start();
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }
}
