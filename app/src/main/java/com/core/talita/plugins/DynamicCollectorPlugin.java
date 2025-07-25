package com.core.talita.plugins;

import android.content.Context;
import android.content.Intent;
import com.core.talita.CreateCollectorActivity;
import com.core.talita.api.DataCollector;
import com.core.talita.api.QuickAddConfig;
import com.core.talita.dynamic.*;

/**
 * DynamicCollectorPlugin - Bridge between dynamic collectors and plugin system
 * 
 * This special plugin allows users to create custom collectors without coding.
 * It shows up in the plugin list and provides access to the collector creation UI.
 */
public class DynamicCollectorPlugin extends DataCollectorPlugin {
    
    public DynamicCollectorPlugin() {
        // Empty constructor required
    }
    
    @Override
    public String getPluginId() {
        return "core.dynamic";
    }
    
    @Override
    public String getPluginName() {
        return "Custom Collectors";
    }
    
    @Override
    public String getDescription() {
        return "Create your own data collectors without coding";
    }
    
    @Override
    public String getAuthor() {
        return "System";
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
        return "➕";
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority to appear at top
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true; // Shows in quick add grid
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        // This is a special case - we don't create a collector directly
        // Instead, we launch the creation UI
        return null;
    }
    
    @Override
    public void onQuickAddTapped(Context context) {
        // Launch the create collector activity
        Intent intent = new Intent(context, CreateCollectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig.Builder()
            .setTitle("Create New")
            .setDescription("Design a custom collector")
            .setStyle("CARD")
            .build();
    }
    
    /**
     * Get all dynamic collectors as pseudo-plugins
     * This allows dynamic collectors to appear in plugin lists
     */
    public static java.util.List<DataCollectorPlugin> getDynamicCollectorPlugins(Context context) {
        java.util.List<DataCollectorPlugin> plugins = new java.util.ArrayList<>();
        
        CollectorSchemaManager schemaManager = new CollectorSchemaManager(context);
        for (CollectorSchema schema : schemaManager.getAllSchemas()) {
            plugins.add(new DynamicSchemaPlugin(schema));
        }
        
        return plugins;
    }
    
    /**
     * Wrapper to make a CollectorSchema act like a DataCollectorPlugin
     */
    private static class DynamicSchemaPlugin extends DataCollectorPlugin {
        private final CollectorSchema schema;
        
        DynamicSchemaPlugin(CollectorSchema schema) {
            this.schema = schema;
        }
        
        @Override
        public String getPluginId() {
            return "dynamic." + schema.getId();
        }
        
        @Override
        public String getPluginName() {
            return schema.getName();
        }
        
        @Override
        public String getDescription() {
            return schema.getDescription() != null ? schema.getDescription() : 
                   "User-created " + schema.getName() + " collector";
        }
        
        @Override
        public String getAuthor() {
            return "User";
        }
        
        @Override
        public String getVersion() {
            return "1.0.0";
        }
        
        @Override
        public String getCategory() {
            return schema.getCategory();
        }
        
        @Override
        public String getEmoji() {
            return schema.getIcon();
        }
        
        @Override
        public int getPriority() {
            return 50; // Medium priority
        }
        
        @Override
        public boolean supportsQuickAdd() {
            return true;
        }
        
        @Override
        public DataCollector createCollector(Context context) {
            DynamicCollector collector = new DynamicCollector(schema);
            collector.initialize(context);
            return collector;
        }
        
        @Override
        public QuickAddConfig getQuickAddConfig() {
            return new QuickAddConfig.Builder()
                .setTitle(schema.getName())
                .setDescription("Log " + schema.getName())
                .setStyle("TILE")
                .build();
        }
    }
}
