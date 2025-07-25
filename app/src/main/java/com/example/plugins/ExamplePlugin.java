package com.example.plugins;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.core.talita.api.*;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.HashMap;
import java.util.Map;

/**
 * ExamplePlugin - Demonstration of how to create a custom plugin
 * 
 * This example shows a gratitude journal plugin
 */
public class ExamplePlugin extends DataCollectorPlugin {
    private static final String TAG = "ExamplePlugin";
    
    @Override
    public String getPluginId() {
        return "example.gratitude";
    }
    
    @Override
    public String getPluginName() {
        return "Gratitude Journal";
    }
    
    @Override
    public String getDescription() {
        return "Track what you're grateful for each day";
    }
    
    @Override
    public String getAuthor() {
        return "Example Developer";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getCategory() {
        return "i"; // Personal category
    }
    
    @Override
    public String getEmoji() {
        return "🙏";
    }
    
    @Override
    public int getPriority() {
        return 75; // Higher priority
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true;
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig.Builder()
            .setTitle("Gratitude")
            .setDescription("What are you grateful for?")
            .setStyle(QuickAddConfig.QuickAddStyle.CARD)
            .setIconColor(Color.parseColor("#FF6B6B"))
            .build();
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new GratitudeCollector();
    }
    
    @Override
    public void onQuickAddTapped(Context context) {
        // Show custom UI for gratitude entry
        showGratitudeDialog(context);
    }
    
    @Override
    protected void onInitialize(PluginContext context) {
        super.onInitialize(context);
        log("Gratitude plugin initialized");
        
        // Check if user has used this before
        boolean hasUsedBefore = context.getBooleanSetting("has_used", false);
        if (!hasUsedBefore) {
            context.showToast("Welcome to Gratitude Journal! 🙏");
            context.putSetting("has_used", true);
        }
    }
    
    /**
     * Show gratitude entry dialog
     */
    private void showGratitudeDialog(Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("🙏 Gratitude Entry");
        
        // Create input layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        
        TextView prompt = new TextView(context);
        prompt.setText("What are you grateful for today?");
        prompt.setTextSize(16);
        layout.addView(prompt);
        
        EditText input = new EditText(context);
        input.setHint("I'm grateful for...");
        input.setMinLines(3);
        layout.addView(input);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String gratitude = input.getText().toString().trim();
            if (!gratitude.isEmpty()) {
                // Save through collector
                GratitudeCollector collector = new GratitudeCollector();
                collector.initialize(context);
                
                Map<String, Object> data = new HashMap<>();
                data.put("gratitude", gratitude);
                data.put("mood", "grateful");
                
                CollectorResult result = collector.collectQuick(data);
                if (result.isSuccess()) {
                    Toast.makeText(context, "✅ Gratitude saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Custom collector for gratitude entries
     */
    private static class GratitudeCollector extends BaseDataCollector {
        
        @Override
        public String getDataType() {
            return "gratitude";
        }
        
        @Override
        public String getDisplayName() {
            return "Gratitude Journal";
        }
        
        @Override
        public String getDescription() {
            return "Daily gratitude entries";
        }
        
        @Override
        public String getEmoji() {
            return "🙏";
        }
        
        @Override
        public String getCategory() {
            return "i";
        }
        
        @Override
        protected boolean checkDeviceCapabilities() {
            // No special capabilities needed
            return true;
        }
        
        @Override
        protected void onStartCollection() {
            // Not used for manual entry
        }
        
        @Override
        protected void onStopCollection() {
            // Not used for manual entry
        }
        
        @Override
        protected CollectorResult performCollection() {
            // This would show UI, but for now return pending
            return CollectorResult.pending(getDataType());
        }
        
        @Override
        protected CollectorResult performQuickCollection(Map<String, Object> data) {
            if (!data.containsKey("gratitude")) {
                return CollectorResult.failure(getDataType(), "No gratitude text provided");
            }
            
            // Store the data
            handleCollectedData(data);
            
            return CollectorResult.success(getDataType(), data);
        }
    }
}
