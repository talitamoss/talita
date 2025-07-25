package com.core.talita.dynamic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.core.talita.R;
import com.core.talita.api.*;
import java.util.*;

/**
 * A collector that works with any CollectorSchema
 * Updated to implement the API DataCollector interface
 */
public class DynamicCollector implements DataCollector {
    private static final String TAG = "DynamicCollector";
    private static final String PREFS_NAME = "user_collectors";
    
    private final CollectorSchema schema;
    private Context context;
    private CollectorSettings settings;
    private boolean isCollecting = false;
    
    public DynamicCollector(CollectorSchema schema) {
        this.schema = schema;
        this.settings = new CollectorSettings.Builder()
            .setAutomatedCollection(false) // User-defined collectors are manual by default
            .setCollectionFrequency(0)
            .build();
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
    public String getDescription() {
        return schema.getDescription() != null ? schema.getDescription() : 
               "User-defined collector for " + schema.getName();
    }
    
    @Override
    public String getEmoji() {
        return schema.getIcon();
    }
    
    @Override
    public String getCategory() {
        return schema.getCategory() != null ? schema.getCategory() : "i";
    }
    
    @Override
    public void initialize(Context context) {
        this.context = context;
        Log.d(TAG, "Initialized dynamic collector: " + schema.getName());
    }
    
    @Override
    public void onDestroy() {
        if (isCollecting) {
            stopAutomatedCollection();
        }
        Log.d(TAG, "Destroyed dynamic collector: " + schema.getName());
    }
    
    @Override
    public boolean isAvailable() {
        // User-defined collectors are always available
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(schema.getId() + "_enabled", true);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(schema.getId() + "_enabled", enabled).apply();
    }
    
    @Override
    public CollectorSettings getSettings() {
        return settings;
    }
    
    @Override
    public void updateSettings(CollectorSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public List<String> getRequiredPermissions() {
        // User-defined collectors typically don't need special permissions
        return new ArrayList<>();
    }
    
    @Override
    public void startAutomatedCollection() {
        // Most user-defined collectors will be manual entry
        // Could add notification reminders here in the future
        isCollecting = true;
        Log.d(TAG, "Started automated collection for: " + schema.getName());
    }
    
    @Override
    public void stopAutomatedCollection() {
        isCollecting = false;
        Log.d(TAG, "Stopped automated collection for: " + schema.getName());
    }
    
    @Override
    public boolean isCollectingAutomatically() {
        return isCollecting;
    }
    
    @Override
    public CollectorResult collect() {
        if (context == null) {
            return CollectorResult.failure(getDataType(), "Collector not initialized");
        }
        
        // Show input dialog based on schema fields
        showInputDialog();
        return CollectorResult.pending(getDataType());
    }
    
    @Override
    public CollectorResult collectQuick(Map<String, Object> data) {
        if (context == null) {
            return CollectorResult.failure(getDataType(), "Collector not initialized");
        }
        
        try {
            // Validate against schema
            Map<String, Object> validatedData = validateData(data);
            
            // Return success with validated data
            return CollectorResult.success(getDataType(), validatedData);
        } catch (Exception e) {
            return CollectorResult.failure(getDataType(), e.getMessage());
        }
    }
    
    /**
     * Show input dialog based on schema fields
     */
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(schema.getName());
        
        // Create custom view for inputs
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        
        // Create input fields based on schema
        Map<String, View> fieldViews = new HashMap<>();
        
        for (CollectorSchema.FieldDefinition field : schema.getFields()) {
            TextView label = new TextView(context);
            label.setText(field.getName() + (field.isRequired() ? " *" : ""));
            label.setPadding(0, 16, 0, 8);
            layout.addView(label);
            
            View inputView = createInputForField(field);
            fieldViews.put(field.getName(), inputView);
            layout.addView(inputView);
        }
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                Map<String, Object> data = collectDataFromViews(fieldViews);
                CollectorResult result = collectQuick(data);
                
                if (result.isSuccess()) {
                    Toast.makeText(context, "✅ Data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "❌ " + result.getErrorMessage(), 
                                 Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error: " + e.getMessage(), 
                             Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Create appropriate input view for field type
     */
    private View createInputForField(CollectorSchema.FieldDefinition field) {
        switch (field.getType()) {
            case NUMBER:
                EditText numberInput = new EditText(context);
                numberInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                numberInput.setHint("Enter number");
                return numberInput;
                
            case DECIMAL:
                EditText decimalInput = new EditText(context);
                decimalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                         android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                decimalInput.setHint("Enter decimal");
                return decimalInput;
                
            case BOOLEAN:
                CheckBox checkBox = new CheckBox(context);
                return checkBox;
                
            case CHOICE:
                Spinner spinner = new Spinner(context);
                // Would populate with choices from field.getValidation()
                return spinner;
                
            case DATE:
                Button dateButton = new Button(context);
                dateButton.setText("Select Date");
                // Would show date picker on click
                return dateButton;
                
            case TIME:
                Button timeButton = new Button(context);
                timeButton.setText("Select Time");
                // Would show time picker on click
                return timeButton;
                
            case TEXT:
            default:
                EditText textInput = new EditText(context);
                textInput.setHint("Enter text");
                return textInput;
        }
    }
    
    /**
     * Collect data from input views
     */
    private Map<String, Object> collectDataFromViews(Map<String, View> fieldViews) 
            throws Exception {
        Map<String, Object> data = new HashMap<>();
        
        for (CollectorSchema.FieldDefinition field : schema.getFields()) {
            View view = fieldViews.get(field.getName());
            Object value = extractValueFromView(view, field);
            
            if (field.isRequired() && (value == null || value.toString().isEmpty())) {
                throw new Exception(field.getName() + " is required");
            }
            
            data.put(field.getName(), value);
        }
        
        return data;
    }
    
    /**
     * Extract value from view based on field type
     */
    private Object extractValueFromView(View view, CollectorSchema.FieldDefinition field) {
        switch (field.getType()) {
            case NUMBER:
                String numText = ((EditText) view).getText().toString();
                return numText.isEmpty() ? null : Integer.parseInt(numText);
                
            case DECIMAL:
                String decText = ((EditText) view).getText().toString();
                return decText.isEmpty() ? null : Double.parseDouble(decText);
                
            case BOOLEAN:
                return ((CheckBox) view).isChecked();
                
            case CHOICE:
                return ((Spinner) view).getSelectedItem();
                
            case TEXT:
            default:
                return ((EditText) view).getText().toString();
        }
    }
    
    /**
     * Validate data against schema
     */
    private Map<String, Object> validateData(Map<String, Object> data) throws Exception {
        Map<String, Object> validated = new HashMap<>();
        
        for (CollectorSchema.FieldDefinition field : schema.getFields()) {
            Object value = data.get(field.getName());
            
            if (field.isRequired() && (value == null || value.toString().isEmpty())) {
                throw new Exception(field.getName() + " is required");
            }
            
            // Add validated value
            if (value != null) {
                validated.put(field.getName(), value);
            }
        }
        
        return validated;
    }
}
