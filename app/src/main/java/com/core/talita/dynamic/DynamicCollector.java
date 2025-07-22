package com.core.talita.dynamic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.core.talita.*;
import java.util.*;

/**
 * A collector that works with any CollectorSchema
 * This is the magic - one collector class that can handle ANY user-defined schema
 */
public class DynamicCollector implements DataCollector {
    private static final String TAG = "DynamicCollector";
    private static final String PREFS_NAME = "user_collectors";
    
    private final CollectorSchema schema;
    
    public DynamicCollector(CollectorSchema schema) {
        this.schema = schema;
    }
    
    @Override
    public String getDataType() {
        // Prefix with "custom_" to distinguish from built-in collectors
        return "custom_" + schema.getId();
    }
    
    @Override
    public String getDisplayName() {
        return schema.getName();
    }
    
    @Override
    public String getIcon() {
        return schema.getIcon();
    }
    
    @Override
    public boolean isAvailable(Context context) {
        // User-defined collectors are always available
        return true;
    }
    
    @Override
    public boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(schema.getId() + "_enabled", true); // Default to enabled
    }
    
    @Override
    public void startCollection(Context context, DataCollectionCallback callback) {
        // Most user-defined collectors will be manual entry
        // Could add notification reminders here in the future
        Log.d(TAG, "Started dynamic collector: " + schema.getName());
    }
    
    @Override
    public void stopCollection(Context context) {
        Log.d(TAG, "Stopped dynamic collector: " + schema.getName());
    }
    
    @Override
    public List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // Check if any fields require special permissions
        for (CollectorSchema.FieldDefinition field : schema.getFields()) {
            switch (field.getType()) {
                case LOCATION:
                    if (!permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                    break;
                case PHOTO:
                    if (!permissions.contains(android.Manifest.permission.CAMERA)) {
                        permissions.add(android.Manifest.permission.CAMERA);
                    }
                    break;
                case AUDIO:
                    if (!permissions.contains(android.Manifest.permission.RECORD_AUDIO)) {
                        permissions.add(android.Manifest.permission.RECORD_AUDIO);
                    }
                    break;
            }
        }
        
        return permissions;
    }
    
    @Override
    public CollectorSettings getSettings() {
        return new CollectorSettings()
                .setFrequency(0) // Manual entry
                .setBatteryOptimized(true);
    }
    
    /**
     * Static method to log data for any dynamic collector
     * This is what gets called when user enters data
     */
    public static void logData(Context context, CollectorSchema schema, Map<String, Object> fieldValues) {
        Log.d(TAG, "Logging data for schema: " + schema.getName());
        
        // Build the data map
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("display_name", schema.getIcon() + " " + schema.getName());
        dataMap.put("schema_id", schema.getId());
        dataMap.put("schema_name", schema.getName());
        dataMap.put("timestamp", System.currentTimeMillis());
        
        // Add all field values
        dataMap.putAll(fieldValues);
        
        // Create summary from first few fields
        String summary = createSummary(schema, fieldValues);
        dataMap.put("summary", summary);
        
        // Save through Universal Data Service
        try {
            UniversalPersonalData data = new UniversalPersonalData("custom_" + schema.getId(), dataMap);
            UniversalDataService dataService = new UniversalDataService(context);
            String dataId = dataService.capture(new PersonalDataAdapter(data));
            
            if (dataId != null) {
                Log.d(TAG, "✅ Dynamic data saved: " + summary);
            } else {
                Log.e(TAG, "❌ Failed to save dynamic data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving dynamic data", e);
        }
    }
    
    /**
     * Create a human-readable summary from the field values
     */
    private static String createSummary(CollectorSchema schema, Map<String, Object> fieldValues) {
        StringBuilder summary = new StringBuilder();
        int fieldCount = 0;
        
        for (CollectorSchema.FieldDefinition field : schema.getFields()) {
            String fieldId = field.getId();
            
            if (fieldValues.containsKey(fieldId) && fieldCount < 3) {
                if (fieldCount > 0) {
                    summary.append(", ");
                }
                
                summary.append(field.getName()).append(": ");
                Object value = fieldValues.get(fieldId);
                
                if (value != null) {
                    summary.append(value.toString());
                    
                    // Add unit if present
                    String unit = field.getUnit();
                    if (unit != null && !unit.isEmpty()) {
                        summary.append(" ").append(unit);
                    }
                }
                
                fieldCount++;
            }
        }
        
        // If no fields, return a default summary
        if (fieldCount == 0) {
            return "Entry logged";
        }
        
        return summary.toString();
    }
    
    /**
     * Enable/disable this dynamic collector
     */
    public static void setEnabled(Context context, String schemaId, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(schemaId + "_enabled", enabled).apply();
        
        Log.d(TAG, "Dynamic collector " + schemaId + " " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Get the schema for this collector
     */
    public CollectorSchema getSchema() {
        return schema;
    }
    
    /**
     * Quick test method for dynamic collectors
     */
    public static void testDynamicCollector(Context context) {
        Log.d(TAG, "=== TESTING DYNAMIC COLLECTOR ===");
        
        try {
            // Create a test schema
            CollectorSchema testSchema = new CollectorSchema("Blood Pressure", "🩺")
                .setDescription("Track blood pressure readings")
                .addField(new CollectorSchema.FieldDefinition("Systolic", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                    .withRange(80, 200)
                    .withUnit("mmHg")
                    .required())
                .addField(new CollectorSchema.FieldDefinition("Diastolic", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                    .withRange(40, 120)
                    .withUnit("mmHg")
                    .required())
                .addField(new CollectorSchema.FieldDefinition("Pulse", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                    .withRange(40, 200)
                    .withUnit("bpm"))
                .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT)
                    .withHint("Any symptoms or context"));
            
            // Create a dynamic collector
            DynamicCollector collector = new DynamicCollector(testSchema);
            
            Log.d(TAG, "Created collector: " + collector.getDisplayName());
            Log.d(TAG, "Data type: " + collector.getDataType());
            Log.d(TAG, "Required permissions: " + collector.getRequiredPermissions());
            
            // Test logging data
            Map<String, Object> testData = new HashMap<>();
            testData.put(testSchema.getFields().get(0).getId(), 120); // Systolic
            testData.put(testSchema.getFields().get(1).getId(), 80);  // Diastolic
            testData.put(testSchema.getFields().get(2).getId(), 72);  // Pulse
            testData.put(testSchema.getFields().get(3).getId(), "Feeling good");
            
            // Log the data
            logData(context, testSchema, testData);
            
            Log.d(TAG, "✅ Dynamic collector test complete!");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Dynamic collector test failed", e);
        }
    }
}
