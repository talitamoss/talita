package com.core.talita;

/**
 * TestQuickAddActivity - TEMPORARILY DISABLED FOR MVP
 * 
 * Test activity for quick add functionality.
 * Not needed for production MVP build.
 */
public class TestQuickAddActivity extends androidx.appcompat.app.AppCompatActivity {
    // Test activity disabled for MVP build
}

/* ORIGINAL CODE - COMMENTED FOR MVP
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
        List<DataCollectorPlugin> plugins = pluginManager.getEnabledPlugins();
        int addedCount = 0;
        
        for (DataCollectorPlugin plugin : plugins) {
            if (plugin.supportsQuickAdd()) {
                addQuickAddButton(plugin);
                addedCount++;
            }
        }
        
        statusText.setText("Found " + addedCount + " quick add plugins");
    }
    
    private void addQuickAddButton(DataCollectorPlugin plugin) {
        Button button = new Button(this);
        button.setText(plugin.getEmoji() + "\n" + plugin.getPluginName());
        button.setTextSize(14);
        button.setPadding(20, 20, 20, 20);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> testQuickAdd(plugin));
        
        quickAddGrid.addView(button);
    }
    
    private void testQuickAdd(DataCollectorPlugin plugin) {
        Log.d(TAG, "Testing quick add for: " + plugin.getPluginName());
        
        try {
            // Option 1: Use plugin's quick add handler
            plugin.onQuickAddTapped(this);
            
            // Option 2: Manual quick add with test data
            Map<String, Object> testData = new HashMap<>();
            
            switch (plugin.getPluginId()) {
                case "core.water":
                    testData.put("value", 250);
                    break;
                case "core.mood":
                    testData.put("mood", "happy");
                    testData.put("energy", 8);
                    break;
                case "core.exercise":
                    testData.put("activity", "Running");
                    testData.put("duration", 30);
                    break;
                default:
                    testData.put("test", "data");
            }
            
            collectorManager.quickLog(plugin.getPluginId(), testData);
            
            Toast.makeText(this, "✅ Quick add successful!", Toast.LENGTH_SHORT).show();
            statusText.setText("Last action: " + plugin.getPluginName() + " quick add");
            
        } catch (Exception e) {
            Log.e(TAG, "Quick add failed", e);
            Toast.makeText(this, "❌ Quick add failed: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
}
*/
