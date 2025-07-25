package com.core.talita;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Quick Add Activity - For testing quick add functionality
 * Updated to use plugin system
 */
public class TestQuickAddActivity extends AppCompatActivity {
    private static final String TAG = "TestQuickAdd";
    
    private GridLayout quickAddGrid;
    private TextView statusText;
    private PluginManager pluginManager;
    private DataCollectorManager collectorManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_quick_add);
        
        pluginManager = PluginManager.getInstance(this);
        collectorManager = DataCollectorManager.getInstance(this);
        
        setupViews();
        loadQuickAddButtons();
    }
    
    private void setupViews() {
        quickAddGrid = findViewById(R.id.quick_add_grid);
        statusText = findViewById(R.id.status_text);
        
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v -> {
            quickAddGrid.removeAllViews();
            loadQuickAddButtons();
        });
    }
    
    private void loadQuickAddButtons() {
        List<DataCollectorPlugin> quickAddPlugins = pluginManager.getQuickAddPlugins();
        
        statusText.setText("Found " + quickAddPlugins.size() + " quick add plugins");
        
        for (DataCollectorPlugin plugin : quickAddPlugins) {
            Button button = createQuickAddButton(plugin);
            quickAddGrid.addView(button);
        }
    }
    
    private Button createQuickAddButton(DataCollectorPlugin plugin) {
        Button button = new Button(this);
        button.setText(plugin.getEmoji() + "\n" + plugin.getPluginName());
        button.setTextSize(14);
        button.setPadding(16, 16, 16, 16);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> testQuickAdd(plugin));
        
        return button;
    }
    
    private void testQuickAdd(DataCollectorPlugin plugin) {
        Log.d(TAG, "Testing quick add for: " + plugin.getPluginName());
        
        try {
            // Test with sample data based on plugin type
            Map<String, Object> testData = createTestData(plugin.getPluginId());
            
            if (testData != null) {
                // Use quick log
                collectorManager.quickLog(plugin.getPluginId(), testData);
                
                String message = "✅ Quick logged: " + plugin.getPluginName();
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                statusText.setText(message);
            } else {
                // Use regular collection UI
                plugin.onQuickAddTapped(this);
                statusText.setText("Triggered UI for: " + plugin.getPluginName());
            }
            
        } catch (Exception e) {
            String error = "❌ Error: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            statusText.setText(error);
            Log.e(TAG, "Quick add error", e);
        }
    }
    
    private Map<String, Object> createTestData(String pluginId) {
        Map<String, Object> data = new HashMap<>();
        
        // Create test data based on plugin type
        if (pluginId.contains("water")) {
            data.put("amount", 250); // 250ml
            return data;
        } else if (pluginId.contains("mood")) {
            data.put("mood", "Happy");
            data.put("score", 4);
            return data;
        } else if (pluginId.contains("exercise")) {
            data.put("activity", "Walking");
            data.put("duration", 30);
            return data;
        }
        
        // Return null for plugins that need UI
        return null;
    }
}
