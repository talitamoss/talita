package com.core.talita;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import java.util.List;

/**
 * Quick Add Test Activity - Alternative test UI for quick add
 * Updated to use plugin system
 */
public class QuickAddTestActivity extends AppCompatActivity {
    
    private LinearLayout pluginListLayout;
    private PluginManager pluginManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        mainLayout.setBackgroundColor(0xFF0A0A0A);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Quick Add Plugin Test");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setPadding(0, 0, 0, 32);
        mainLayout.addView(titleText);
        
        // Plugin list container
        pluginListLayout = new LinearLayout(this);
        pluginListLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(pluginListLayout);
        
        setContentView(mainLayout);
        
        // Initialize plugin manager
        pluginManager = PluginManager.getInstance(this);
        
        // Load plugins
        loadPlugins();
    }
    
    private void loadPlugins() {
        List<DataCollectorPlugin> plugins = pluginManager.getQuickAddPlugins();
        
        for (DataCollectorPlugin plugin : plugins) {
            View pluginView = createPluginView(plugin);
            pluginListLayout.addView(pluginView);
        }
    }
    
    private View createPluginView(DataCollectorPlugin plugin) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(16, 16, 16, 16);
        container.setBackgroundColor(0xFF1A1A1A);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        container.setLayoutParams(params);
        
        // Emoji
        TextView emojiText = new TextView(this);
        emojiText.setText(plugin.getEmoji());
        emojiText.setTextSize(32);
        emojiText.setPadding(0, 0, 24, 0);
        container.addView(emojiText);
        
        // Text container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ));
        
        // Plugin name
        TextView nameText = new TextView(this);
        nameText.setText(plugin.getPluginName());
        nameText.setTextSize(18);
        nameText.setTextColor(0xFFFFFFFF);
        textContainer.addView(nameText);
        
        // Plugin ID
        TextView idText = new TextView(this);
        idText.setText(plugin.getPluginId());
        idText.setTextSize(14);
        idText.setTextColor(0xFF888888);
        textContainer.addView(idText);
        
        container.addView(textContainer);
        
        // Make clickable
        container.setClickable(true);
        container.setFocusable(true);
        container.setOnClickListener(v -> {
            plugin.onQuickAddTapped(this);
        });
        
        return container;
    }
}
