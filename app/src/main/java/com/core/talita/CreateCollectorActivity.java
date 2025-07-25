package com.core.talita;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.dynamic.*;
import com.core.talita.api.DataCollector;
import com.core.talita.api.CollectorResult;
import java.util.*;

/**
 * Activity for creating custom collectors
 * Updated to work with the new plugin system
 */
public class CreateCollectorActivity extends AppCompatActivity {
    private static final String TAG = "CreateCollector";
    
    private EditText nameInput;
    private EditText descriptionInput;
    private Spinner iconSpinner;
    private Spinner categorySpinner;
    private LinearLayout fieldsContainer;
    private Button addFieldButton;
    private Button saveButton;
    private Button testButton;
    
    private List<FieldView> fieldViews;
    private CollectorSchemaManager schemaManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collector);
        
        schemaManager = new CollectorSchemaManager(this);
        fieldViews = new ArrayList<>();
        
        setupViews();
        setupSpinners();
    }
    
    private void setupViews() {
        nameInput = findViewById(R.id.collector_name);
        descriptionInput = findViewById(R.id.description);
        iconSpinner = findViewById(R.id.icon_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        fieldsContainer = findViewById(R.id.fields_container);
        addFieldButton = findViewById(R.id.add_field_button);
        saveButton = findViewById(R.id.save_collector_button);
        testButton = findViewById(R.id.test_collector_button);
        
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        addFieldButton.setOnClickListener(v -> addNewField());
        saveButton.setOnClickListener(v -> saveCollector());
        
        if (testButton != null) {
            testButton.setOnClickListener(v -> testCollector());
        }
        
        // Add one field by default
        addNewField();
    }
    
    private void setupSpinners() {
        // Icons
        String[] icons = {"💊", "🩺", "💧", "🏃", "🧘", "😊", "🍎", "💤", "📝", "📊", 
                         "🎯", "💡", "🌱", "📚", "🎨", "🎵", "🏋️", "🚶", "🧠", "❤️"};
        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, icons);
        iconSpinner.setAdapter(iconAdapter);
        
        // Categories
        String[] categories = {"i", "we", "all"};
        String[] categoryDisplayNames = {"Personal (I)", "Social (We)", "Universal (All)"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_spinner_dropdown_item, categoryDisplayNames) {
            @Override
            public String getItem(int position) {
                return categories[position];
            }
        };
        categorySpinner.setAdapter(categoryAdapter);
    }
    
    private void addNewField() {
        FieldView fieldView = new FieldView(this);
        fieldView.setOnRemoveListener(() -> {
            fieldsContainer.removeView(fieldView);
            fieldViews.remove(fieldView);
        });
        
        fieldsContainer.addView(fieldView);
        fieldViews.add(fieldView);
    }
    
    private void saveCollector() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a collector name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (fieldViews.isEmpty()) {
            Toast.makeText(this, "Please add at least one field", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create the schema
        String icon = (String) iconSpinner.getSelectedItem();
        CollectorSchema schema = new CollectorSchema(name, icon);
        
        // Set optional properties
        String description = descriptionInput.getText().toString().trim();
        if (!description.isEmpty()) {
            schema.setDescription(description);
        }
        
        // Get the actual category value
        int categoryPosition = categorySpinner.getSelectedItemPosition();
        String[] categories = {"i", "we", "all"};
        schema.setCategory(categories[categoryPosition]);
        
        // Add all fields
        for (FieldView fieldView : fieldViews) {
            CollectorSchema.FieldDefinition field = fieldView.getFieldDefinition();
            if (field != null) {
                schema.addField(field);
            }
        }
        
        // Save the schema
        schemaManager.addSchema(schema);
        
        Toast.makeText(this, "✅ Collector created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void testCollector() {
        // Create temporary schema for testing
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Test Collector";
        }
        
        String icon = (String) iconSpinner.getSelectedItem();
        CollectorSchema testSchema = new CollectorSchema(name, icon);
        
        for (FieldView fieldView : fieldViews) {
            CollectorSchema.FieldDefinition field = fieldView.getFieldDefinition();
            if (field != null) {
                testSchema.addField(field);
            }
        }
        
        // Create and test the collector
        DynamicCollector collector = new DynamicCollector(testSchema);
        collector.initialize(this);
        
        // Trigger collection UI
        CollectorResult result = collector.collect();
        Log.d(TAG, "Test result: " + result);
    }
    
    /**
     * View for creating a field
     */
    private static class FieldView extends LinearLayout {
        private EditText nameInput;
        private Spinner typeSpinner;
        private CheckBox requiredCheck;
        private EditText unitInput;
        private OnRemoveListener removeListener;
        
        interface OnRemoveListener {
            void onRemove();
        }
        
        public FieldView(CreateCollectorActivity activity) {
            super(activity);
            setOrientation(VERTICAL);
            setPadding(16, 16, 16, 16);
            setBackgroundResource(R.drawable.rounded_rectangle);
            
            // Inflate or create views
            setupFieldViews(activity);
        }
        
        private void setupFieldViews(CreateCollectorActivity activity) {
            // Field name
            TextView nameLabel = new TextView(activity);
            nameLabel.setText("Field Name");
            nameLabel.setTextColor(0xFFFFFFFF);
            addView(nameLabel);
            
            nameInput = new EditText(activity);
            nameInput.setHint("e.g., Blood Sugar");
            nameInput.setTextColor(0xFFFFFFFF);
            nameInput.setHintTextColor(0xFF888888);
            addView(nameInput);
            
            // Field type
            TextView typeLabel = new TextView(activity);
            typeLabel.setText("Type");
            typeLabel.setTextColor(0xFFFFFFFF);
            typeLabel.setPadding(0, 16, 0, 0);
            addView(typeLabel);
            
            typeSpinner = new Spinner(activity);
            String[] types = {"Text", "Number", "Decimal", "Yes/No", "Choice", "Scale", "Date", "Time"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, 
                android.R.layout.simple_spinner_dropdown_item, types);
            typeSpinner.setAdapter(adapter);
            addView(typeSpinner);
            
            // Required checkbox
            requiredCheck = new CheckBox(activity);
            requiredCheck.setText("Required field");
            requiredCheck.setTextColor(0xFFFFFFFF);
            requiredCheck.setPadding(0, 16, 0, 0);
            addView(requiredCheck);
            
            // Unit input (optional)
            TextView unitLabel = new TextView(activity);
            unitLabel.setText("Unit (optional)");
            unitLabel.setTextColor(0xFFFFFFFF);
            unitLabel.setPadding(0, 16, 0, 0);
            addView(unitLabel);
            
            unitInput = new EditText(activity);
            unitInput.setHint("e.g., mg/dL, minutes, etc.");
            unitInput.setTextColor(0xFFFFFFFF);
            unitInput.setHintTextColor(0xFF888888);
            addView(unitInput);
            
            // Remove button
            Button removeButton = new Button(activity);
            removeButton.setText("Remove Field");
            removeButton.setTextColor(0xFFFF0000);
            removeButton.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove();
                }
            });
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = 16;
            removeButton.setLayoutParams(params);
            addView(removeButton);
        }
        
        public void setOnRemoveListener(OnRemoveListener listener) {
            this.removeListener = listener;
        }
        
        public CollectorSchema.FieldDefinition getFieldDefinition() {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                return null;
            }
            
            // Map spinner position to field type
            CollectorSchema.FieldDefinition.FieldType type;
            switch (typeSpinner.getSelectedItemPosition()) {
                case 0: type = CollectorSchema.FieldDefinition.FieldType.TEXT; break;
                case 1: type = CollectorSchema.FieldDefinition.FieldType.NUMBER; break;
                case 2: type = CollectorSchema.FieldDefinition.FieldType.DECIMAL; break;
                case 3: type = CollectorSchema.FieldDefinition.FieldType.BOOLEAN; break;
                case 4: type = CollectorSchema.FieldDefinition.FieldType.CHOICE; break;
                case 5: type = CollectorSchema.FieldDefinition.FieldType.SCALE; break;
                case 6: type = CollectorSchema.FieldDefinition.FieldType.DATE; break;
                case 7: type = CollectorSchema.FieldDefinition.FieldType.TIME; break;
                default: type = CollectorSchema.FieldDefinition.FieldType.TEXT;
            }
            
            CollectorSchema.FieldDefinition field = new CollectorSchema.FieldDefinition(name, type);
            
            if (requiredCheck.isChecked()) {
                field.required();
            }
            
            String unit = unitInput.getText().toString().trim();
            if (!unit.isEmpty()) {
                field.withUnit(unit);
            }
            
            return field;
        }
    }
}
