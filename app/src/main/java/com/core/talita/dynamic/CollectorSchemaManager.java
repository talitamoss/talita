package com.core.talita.dynamic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/**
 * Manages user-defined collector schemas
 * Handles persistence, templates, and schema operations
 */
public class CollectorSchemaManager {
    private static final String TAG = "SchemaManager";
    private static final String PREFS_NAME = "collector_schemas";
    private static final String SCHEMAS_KEY = "user_schemas";
    private static final String VERSION_KEY = "schema_version";
    private static final int CURRENT_VERSION = 1;
    
    private final Context context;
    private final Map<String, CollectorSchema> schemas;
    
    public CollectorSchemaManager(Context context) {
        this.context = context;
        this.schemas = new LinkedHashMap<>();
        
        // Load existing schemas
        loadSchemas();
        
        // Add default templates if this is first run
        if (schemas.isEmpty()) {
            Log.d(TAG, "First run - adding default templates");
            addDefaultTemplates();
        }
        
        Log.d(TAG, "Initialized with " + schemas.size() + " schemas");
    }
    
    /**
     * Load schemas from SharedPreferences
     */
    private void loadSchemas() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String schemasJson = prefs.getString(SCHEMAS_KEY, "[]");
            
            JSONArray array = new JSONArray(schemasJson);
            for (int i = 0; i < array.length(); i++) {
                try {
                    String schemaJson = array.getJSONObject(i).toString();
                    CollectorSchema schema = CollectorSchema.fromJson(schemaJson);
                    if (schema != null) {
                        schemas.put(schema.getId(), schema);
                        Log.d(TAG, "Loaded schema: " + schema.getName());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load schema at index " + i, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load schemas", e);
        }
    }
    
    /**
     * Save all schemas to SharedPreferences
     */
    private void saveSchemas() {
        try {
            JSONArray array = new JSONArray();
            for (CollectorSchema schema : schemas.values()) {
                array.put(new JSONObject(schema.toJson()));
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .putString(SCHEMAS_KEY, array.toString())
                .putInt(VERSION_KEY, CURRENT_VERSION)
                .apply();
                
            Log.d(TAG, "Saved " + schemas.size() + " schemas");
        } catch (Exception e) {
            Log.e(TAG, "Failed to save schemas", e);
        }
    }
    
    /**
     * Add a new schema
     */
    public void addSchema(CollectorSchema schema) {
        schemas.put(schema.getId(), schema);
        saveSchemas();
        Log.d(TAG, "Added schema: " + schema.getName());
    }
    
    /**
     * Update an existing schema
     */
    public void updateSchema(CollectorSchema schema) {
        if (schemas.containsKey(schema.getId())) {
            schemas.put(schema.getId(), schema);
            saveSchemas();
            Log.d(TAG, "Updated schema: " + schema.getName());
        }
    }
    
    /**
     * Remove a schema
     */
    public void removeSchema(String schemaId) {
        CollectorSchema removed = schemas.remove(schemaId);
        if (removed != null) {
            saveSchemas();
            Log.d(TAG, "Removed schema: " + removed.getName());
            
            // Also disable the collector
            DynamicCollector.setEnabled(context, schemaId, false);
        }
    }
    
    /**
     * Get a specific schema
     */
    public CollectorSchema getSchema(String schemaId) {
        return schemas.get(schemaId);
    }
    
    /**
     * Get all schemas
     */
    public List<CollectorSchema> getAllSchemas() {
        return new ArrayList<>(schemas.values());
    }
    
    /**
     * Get schemas by category
     */
    public List<CollectorSchema> getSchemasByCategory(String category) {
        List<CollectorSchema> result = new ArrayList<>();
        for (CollectorSchema schema : schemas.values()) {
            if (category.equals(schema.getCategory())) {
                result.add(schema);
            }
        }
        return result;
    }
    
    /**
     * Check if a schema exists
     */
    public boolean hasSchema(String schemaId) {
        return schemas.containsKey(schemaId);
    }
    
    /**
     * Import a schema from JSON
     */
    public CollectorSchema importSchema(String jsonString) {
        try {
            CollectorSchema schema = CollectorSchema.fromJson(jsonString);
            if (schema != null) {
                // Generate new ID to avoid conflicts
                CollectorSchema newSchema = new CollectorSchema(schema.getName(), schema.getIcon());
                newSchema.setCategory(schema.getCategory());
                newSchema.setDescription(schema.getDescription());
                
                // Copy fields
                for (CollectorSchema.FieldDefinition field : schema.getFields()) {
                    newSchema.addField(field);
                }
                
                addSchema(newSchema);
                return newSchema;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to import schema", e);
        }
        return null;
    }
    
    /**
     * Export a schema to JSON
     */
    public String exportSchema(String schemaId) {
        CollectorSchema schema = getSchema(schemaId);
        return schema != null ? schema.toJson() : null;
    }
    
    /**
     * Add default template schemas
     */
    private void addDefaultTemplates() {
        // Basic health templates
        addHealthTemplates();
        
        // Lifestyle templates
        addLifestyleTemplates();
        
        // Hobby templates
        addHobbyTemplates();
        
        // Productivity templates
        addProductivityTemplates();
    }
    
    private void addHealthTemplates() {
        // Blood Pressure Tracker
        CollectorSchema bloodPressure = new CollectorSchema("Blood Pressure", "🩺")
            .setCategory("Health")
            .setDescription("Track blood pressure and heart rate")
            .addField(new CollectorSchema.FieldDefinition("Systolic", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(80, 200)
                .withUnit("mmHg")
                .required())
            .addField(new CollectorSchema.FieldDefinition("Diastolic", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(40, 120)
                .withUnit("mmHg")
                .required())
            .addField(new CollectorSchema.FieldDefinition("Heart Rate", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(40, 200)
                .withUnit("bpm"))
            .addField(new CollectorSchema.FieldDefinition("Arm", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Left", "Right"))
            .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        // Pain Tracker
        CollectorSchema painTracker = new CollectorSchema("Pain Log", "🤕")
            .setCategory("Health")
            .setDescription("Track pain levels and locations")
            .addField(new CollectorSchema.FieldDefinition("Pain Level", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(0, 10)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Location", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Type", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Sharp", "Dull", "Throbbing", "Burning", "Aching"))
            .addField(new CollectorSchema.FieldDefinition("Trigger", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Relief Method", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        schemas.put(bloodPressure.getId(), bloodPressure);
        schemas.put(painTracker.getId(), painTracker);
    }
    
    private void addLifestyleTemplates() {
        // Gratitude Journal
        CollectorSchema gratitude = new CollectorSchema("Gratitude", "🙏")
            .setCategory("Lifestyle")
            .setDescription("Daily gratitude practice")
            .addField(new CollectorSchema.FieldDefinition("Grateful For", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Why", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Mood", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5));
        
        // Dream Journal
        CollectorSchema dreams = new CollectorSchema("Dreams", "💭")
            .setCategory("Lifestyle")
            .setDescription("Record and analyze your dreams")
            .addField(new CollectorSchema.FieldDefinition("Dream Description", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Vividness", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 10))
            .addField(new CollectorSchema.FieldDefinition("Type", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Normal", "Lucid", "Nightmare", "Recurring"))
            .addField(new CollectorSchema.FieldDefinition("Emotions", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Interpretation", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        schemas.put(gratitude.getId(), gratitude);
        schemas.put(dreams.getId(), dreams);
    }
    
    private void addHobbyTemplates() {
        // Book Reading
        CollectorSchema reading = new CollectorSchema("Reading Log", "📚")
            .setCategory("Hobbies")
            .setDescription("Track your reading progress")
            .addField(new CollectorSchema.FieldDefinition("Book Title", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Author", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Pages Read", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(0, 1000))
            .addField(new CollectorSchema.FieldDefinition("Reading Time", CollectorSchema.FieldDefinition.FieldType.DURATION)
                .withUnit("minutes"))
            .addField(new CollectorSchema.FieldDefinition("Rating", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5))
            .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        // Garden Log
        CollectorSchema garden = new CollectorSchema("Garden Log", "🌱")
            .setCategory("Hobbies")
            .setDescription("Track plant growth and garden activities")
            .addField(new CollectorSchema.FieldDefinition("Plant", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Activity", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Planted", "Watered", "Fertilized", "Pruned", "Harvested"))
            .addField(new CollectorSchema.FieldDefinition("Growth Stage", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Seed", "Sprout", "Vegetative", "Flowering", "Fruiting"))
            .addField(new CollectorSchema.FieldDefinition("Health", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5))
            .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Photo", CollectorSchema.FieldDefinition.FieldType.PHOTO));
        
        schemas.put(reading.getId(), reading);
        schemas.put(garden.getId(), garden);
    }
    
    private void addProductivityTemplates() {
        // Pomodoro Sessions
        CollectorSchema pomodoro = new CollectorSchema("Pomodoro", "🍅")
            .setCategory("Productivity")
            .setDescription("Track focused work sessions")
            .addField(new CollectorSchema.FieldDefinition("Task", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Duration", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("25 min", "45 min", "60 min"))
            .addField(new CollectorSchema.FieldDefinition("Completed", CollectorSchema.FieldDefinition.FieldType.BOOLEAN))
            .addField(new CollectorSchema.FieldDefinition("Distractions", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(0, 20))
            .addField(new CollectorSchema.FieldDefinition("Focus Quality", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5));
        
        // Learning Progress
        CollectorSchema learning = new CollectorSchema("Learning", "🎓")
            .setCategory("Productivity")
            .setDescription("Track learning and skill development")
            .addField(new CollectorSchema.FieldDefinition("Subject", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Topic", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Time Spent", CollectorSchema.FieldDefinition.FieldType.DURATION)
                .withUnit("minutes"))
            .addField(new CollectorSchema.FieldDefinition("Type", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Reading", "Video", "Practice", "Project", "Course"))
            .addField(new CollectorSchema.FieldDefinition("Understanding", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 10))
            .addField(new CollectorSchema.FieldDefinition("Key Takeaway", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        schemas.put(pomodoro.getId(), pomodoro);
        schemas.put(learning.getId(), learning);
    }
    
    /**
     * Get all available categories
     */
    public Set<String> getAllCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Health");
        categories.add("Lifestyle");
        categories.add("Hobbies");
        categories.add("Productivity");
        categories.add("Fitness");
        categories.add("Finance");
        categories.add("Other");
        
        // Add any custom categories from schemas
        for (CollectorSchema schema : schemas.values()) {
            String category = schema.getCategory();
            if (category != null && !category.isEmpty()) {
                categories.add(category);
            }
        }
        
        return categories;
    }
    
    /**
     * Test method
     */
    public void logStatus() {
        Log.d(TAG, "=== Schema Manager Status ===");
        Log.d(TAG, "Total schemas: " + schemas.size());
        
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (CollectorSchema schema : schemas.values()) {
            String category = schema.getCategory() != null ? schema.getCategory() : "Uncategorized";
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue() + " schemas");
        }
    }
}
