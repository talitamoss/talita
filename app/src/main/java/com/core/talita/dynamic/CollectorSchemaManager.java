package com.core.talita.dynamic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/**
 * CollectorSchemaManager - Manages user-created collector schemas
 * 
 * Stores schemas in SharedPreferences and provides templates
 */
public class CollectorSchemaManager {
    private static final String TAG = "SchemaManager";
    private static final String PREFS_NAME = "collector_schemas";
    private static final String SCHEMAS_KEY = "schemas";
    private static final String VERSION_KEY = "version";
    private static final int CURRENT_VERSION = 1;
    
    private final Context context;
    private final Map<String, CollectorSchema> schemas;
    
    public CollectorSchemaManager(Context context) {
        this.context = context.getApplicationContext();
        this.schemas = new LinkedHashMap<>();
        
        loadSchemas();
        
        // Add default templates if none exist
        if (schemas.isEmpty()) {
            addDefaultTemplates();
            saveSchemas();
        }
    }
    
    /**
     * Load schemas from SharedPreferences
     */
    private void loadSchemas() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String schemasJson = prefs.getString(SCHEMAS_KEY, null);
            
            if (schemasJson != null) {
                JSONArray array = new JSONArray(schemasJson);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        CollectorSchema schema = CollectorSchema.fromJson(array.getString(i));
                        schemas.put(schema.getId(), schema);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load schema at index " + i, e);
                    }
                }
                Log.d(TAG, "Loaded " + schemas.size() + " schemas");
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
                array.put(schema.toJson());
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
     * Add default template schemas
     */
    private void addDefaultTemplates() {
        Log.d(TAG, "Adding default templates");
        
        addHealthTemplates();
        addLifestyleTemplates();
        addHobbyTemplates();
        addProductivityTemplates();
    }
    
    private void addHealthTemplates() {
        // Blood Pressure
        CollectorSchema bloodPressure = new CollectorSchema("Blood Pressure", "🩺")
            .setCategory("i")
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
        
        // Pain Tracker
        CollectorSchema painTracker = new CollectorSchema("Pain Log", "🤕")
            .setCategory("i")
            .setDescription("Track pain episodes and management")
            .addField(new CollectorSchema.FieldDefinition("Severity", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 10)
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
            .setCategory("i")
            .setDescription("Daily gratitude practice")
            .addField(new CollectorSchema.FieldDefinition("Grateful For", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Why", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Mood", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5));
        
        // Dream Journal
        CollectorSchema dreams = new CollectorSchema("Dreams", "💭")
            .setCategory("i")
            .setDescription("Record and analyze your dreams")
            .addField(new CollectorSchema.FieldDefinition("Dream Description", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Vividness", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 10))
            .addField(new CollectorSchema.FieldDefinition("Type", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Normal", "Lucid", "Nightmare", "Recurring"))
            .addField(new CollectorSchema.FieldDefinition("Emotions", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        schemas.put(gratitude.getId(), gratitude);
        schemas.put(dreams.getId(), dreams);
    }
    
    private void addHobbyTemplates() {
        // Reading Log
        CollectorSchema reading = new CollectorSchema("Reading", "📚")
            .setCategory("i")
            .setDescription("Track books and reading progress")
            .addField(new CollectorSchema.FieldDefinition("Book Title", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Author", CollectorSchema.FieldDefinition.FieldType.TEXT))
            .addField(new CollectorSchema.FieldDefinition("Pages Read", CollectorSchema.FieldDefinition.FieldType.NUMBER)
                .withRange(1, 1000))
            .addField(new CollectorSchema.FieldDefinition("Rating", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5))
            .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        // Garden Log
        CollectorSchema garden = new CollectorSchema("Garden", "🌱")
            .setCategory("i")
            .setDescription("Track plant care and garden progress")
            .addField(new CollectorSchema.FieldDefinition("Plant", CollectorSchema.FieldDefinition.FieldType.TEXT)
                .required())
            .addField(new CollectorSchema.FieldDefinition("Action", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Watered", "Fertilized", "Pruned", "Planted", "Harvested"))
            .addField(new CollectorSchema.FieldDefinition("Growth Stage", CollectorSchema.FieldDefinition.FieldType.CHOICE)
                .withChoices("Seed", "Sprout", "Vegetative", "Flowering", "Fruiting"))
            .addField(new CollectorSchema.FieldDefinition("Health", CollectorSchema.FieldDefinition.FieldType.SCALE)
                .withRange(1, 5))
            .addField(new CollectorSchema.FieldDefinition("Notes", CollectorSchema.FieldDefinition.FieldType.TEXT));
        
        schemas.put(reading.getId(), reading);
        schemas.put(garden.getId(), garden);
    }
    
    private void addProductivityTemplates() {
        // Pomodoro Sessions
        CollectorSchema pomodoro = new CollectorSchema("Pomodoro", "🍅")
            .setCategory("i")
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
            .setCategory("i")
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
        categories.add("i");    // Personal
        categories.add("we");   // Social
        categories.add("all");  // Universal
        
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
