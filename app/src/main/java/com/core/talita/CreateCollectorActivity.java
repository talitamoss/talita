package com.core.talita;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.core.talita.dynamic.*;
import java.util.*;

/**
 * Activity for creating custom collectors
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
        
        addFieldButton.setOnClickListener(v -> addNewField());
        saveButton.setOnClickListener(v -> saveCollector());
        
        // Add one field by default
        addNewField();
    }
    
    private void setupSpinners() {
        // Icon selection
        String[] icons = {"📊", "💧", "🏃", "😊", "💤", "🍽️", "💊", "🎯", "🛶", "🧘", 
                         "📚", "🎨", "🎵", "💰", "🌡️", "📝", "🏋️", "🚴", "🏊", "🧗",
                         "🎮", "📷", "🌱", "🐕", "✈️", "🏠", "🚗", "⏰", "💡", "🔧"};
        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, icons);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(iconAdapter);
        
        // Category selection
        List<String> categories = new ArrayList<>(schemaManager.getAllCategories());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }
    
    private void addNewField() {
        FieldView fieldView = new FieldView(this);
        fieldsContainer.addView(fieldView);
        fieldViews.add(fieldView);
        
        fieldView.setOnRemoveListener(() -> {
            fieldsContainer.removeView(fieldView);
            fieldViews.remove(fieldView);
            
            // Don't allow removing all fields
            if (fieldViews.isEmpty()) {
                addNewField();
            }
        });
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
        
        String category = (String) categorySpinner.getSelectedItem();
        schema.setCategory(category);
        
        // Add all fields
        for (FieldView fieldView : fieldViews) {
            CollectorSchema.FieldDefinition field = fieldView.getFieldDefinition();
            if (field != null) {
                schema.addField(field);
            }
        }
        
        // Save the schema
        schemaManager.addSchema(schema);
        
        // Register with DataCollectorManager
        DataCollectorManager collectorManager = new DataCollectorManager(this);
        collectorManager.addCollector(new DynamicCollector(schema));
        
        Toast.makeText(this, "✅ Collector created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    /**
     * Custom view for defining a field
     */
    private static class FieldView extends LinearLayout {
        private EditText nameInput;
        private EditText unitInput;
        private Spinner typeSpinner;
        private CheckBox requiredCheckbox;
        private ImageButton removeButton;
        private OnRemoveListener removeListener;
        
        interface OnRemoveListener {
            void onRemove();
        }
        
        public FieldView(android.content.Context context) {
            super(context);
            setOrientation(VERTICAL);
            
            // Inflate the field definition layout
            LayoutInflater.from(context).inflate(R.layout.view_field_definition, this, true);
            
            nameInput = findViewById(R.id.field_name);
            unitInput = findViewById(R.id.field_unit);
            typeSpinner = findViewById(R.id.field_type);
            requiredCheckbox = findViewById(R.id.field_required);
            removeButton = findViewById(R.id.remove_field);
            
            setupTypeSpinner();
            
            removeButton.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove();
                }
            });
            
            // Show/hide unit field based on type
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String type = (String) parent.getItemAtPosition(position);
                    boolean showUnit = type.equals("Number") || type.equals("Duration");
                    unitInput.setVisibility(showUnit ? VISIBLE : GONE);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        private void setupTypeSpinner() {
            // User-friendly type names
            String[] typeNames = {
                "Text",         // TEXT
                "Number",       // NUMBER
                "Choice",       // CHOICE
                "Scale",        // SCALE
                "Yes/No",       // BOOLEAN
                "Time",         // TIME
                "Date",         // DATE
                "Duration",     // DURATION
                "Location",     // LOCATION
                "Photo"         // PHOTO
            };
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, typeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
        }
        
        public void setOnRemoveListener(OnRemoveListener listener) {
            this.removeListener = listener;
        }
        
        public CollectorSchema.FieldDefinition getFieldDefinition() {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                return null;
            }
            
            // Map user-friendly names back to enum values
            String selectedType = (String) typeSpinner.getSelectedItem();
            CollectorSchema.FieldDefinition.FieldType type;
            
            switch (selectedType) {
                case "Text": type = CollectorSchema.FieldDefinition.FieldType.TEXT; break;
                case "Number": type = CollectorSchema.FieldDefinition.FieldType.NUMBER; break;
                case "Choice": type = CollectorSchema.FieldDefinition.FieldType.CHOICE; break;
                case "Scale": type = CollectorSchema.FieldDefinition.FieldType.SCALE; break;
                case "Yes/No": type = CollectorSchema.FieldDefinition.FieldType.BOOLEAN; break;
                case "Time": type = CollectorSchema.FieldDefinition.FieldType.TIME; break;
                case "Date": type = CollectorSchema.FieldDefinition.FieldType.DATE; break;
                case "Duration": type = CollectorSchema.FieldDefinition.FieldType.DURATION; break;
                case "Location": type = CollectorSchema.FieldDefinition.FieldType.LOCATION; break;
                case "Photo": type = CollectorSchema.FieldDefinition.FieldType.PHOTO; break;
                default: type = CollectorSchema.FieldDefinition.FieldType.TEXT;
            }
            
            CollectorSchema.FieldDefinition field = new CollectorSchema.FieldDefinition(name, type);
            
            if (requiredCheckbox.isChecked()) {
                field.required();
            }
            
            String unit = unitInput.getText().toString().trim();
            if (!unit.isEmpty()) {
                field.withUnit(unit);
            }
            
            // For scale type, set default range
            if (type == CollectorSchema.FieldDefinition.FieldType.SCALE) {
                field.withRange(1, 5); // Default 1-5 scale
            }
            
            return field;
        }
    }
}
