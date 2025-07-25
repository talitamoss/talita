package com.core.talita.dynamic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

/**
 * CollectorSchema - Blueprint for user-created collectors
 * 
 * This defines the structure of a user-created data collector.
 * Users can create schemas through UI without coding.
 */
public class CollectorSchema {
    private final String id;
    private final String name;
    private final String icon;
    private String category = "i"; // default to personal
    private String description;
    private final List<FieldDefinition> fields;
    private final boolean isUserDefined;
    private final long createdAt;
    
    /**
     * Field definition for a collector schema
     */
    public static class FieldDefinition {
        public enum FieldType {
            TEXT,      // Simple text input
            NUMBER,    // Integer number
            DECIMAL,   // Decimal number
            BOOLEAN,   // Yes/No checkbox
            CHOICE,    // Multiple choice
            SCALE,     // Rating scale (1-5, 1-10, etc)
            DATE,      // Date picker
            TIME,      // Time picker
            DURATION,  // Duration in minutes/hours
            PHOTO      // Photo capture
        }
        
        private final String id;
        private final String name;
        private final FieldType type;
        private boolean required = false;
        private String unit;
        private String hint;
        private JSONObject validation;
        
        public FieldDefinition(String name, FieldType type) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.type = type;
            this.validation = new JSONObject();
        }
        
        // Builder methods
        public FieldDefinition required() {
            this.required = true;
            return this;
        }
        
        public FieldDefinition withUnit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public FieldDefinition withHint(String hint) {
            this.hint = hint;
            return this;
        }
        
        public FieldDefinition withRange(int min, int max) {
            try {
                validation.put("min", min);
                validation.put("max", max);
            } catch (JSONException e) {
                // Ignore
            }
            return this;
        }
        
        public FieldDefinition withChoices(String... choices) {
            try {
                JSONArray array = new JSONArray();
                for (String choice : choices) {
                    array.put(choice);
                }
                validation.put("choices", array);
            } catch (JSONException e) {
                // Ignore
            }
            return this;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public FieldType getType() { return type; }
        public boolean isRequired() { return required; }
        public String getUnit() { return unit; }
        public String getHint() { return hint; }
        public JSONObject getValidation() { return validation; }
        
        // JSON serialization
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("type", type.name());
            json.put("required", required);
            if (unit != null) json.put("unit", unit);
            if (hint != null) json.put("hint", hint);
            if (validation.length() > 0) json.put("validation", validation);
            return json;
        }
        
        public static FieldDefinition fromJson(JSONObject json) throws JSONException {
            String name = json.getString("name");
            FieldType type = FieldType.valueOf(json.getString("type"));
            
            FieldDefinition field = new FieldDefinition(name, type);
            
            if (json.has("required")) field.required = json.getBoolean("required");
            if (json.has("unit")) field.unit = json.getString("unit");
            if (json.has("hint")) field.hint = json.getString("hint");
            if (json.has("validation")) field.validation = json.getJSONObject("validation");
            
            return field;
        }
    }
    
    // Constructor
    public CollectorSchema(String name, String icon) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.icon = icon;
        this.fields = new ArrayList<>();
        this.isUserDefined = true;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Private constructor for loading from JSON
    private CollectorSchema(String id, String name, String icon, long createdAt) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.fields = new ArrayList<>();
        this.isUserDefined = true;
        this.createdAt = createdAt;
    }
    
    // Add a field to the schema
    public CollectorSchema addField(FieldDefinition field) {
        fields.add(field);
        return this;
    }
    
    // Setters for optional properties
    public CollectorSchema setCategory(String category) {
        this.category = category;
        return this;
    }
    
    public CollectorSchema setDescription(String description) {
        this.description = description;
        return this;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public List<FieldDefinition> getFields() { return new ArrayList<>(fields); }
    public boolean isUserDefined() { return isUserDefined; }
    public long getCreatedAt() { return createdAt; }
    
    // JSON serialization
    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("icon", icon);
        json.put("category", category);
        if (description != null) json.put("description", description);
        json.put("isUserDefined", isUserDefined);
        json.put("createdAt", createdAt);
        
        JSONArray fieldsArray = new JSONArray();
        for (FieldDefinition field : fields) {
            fieldsArray.put(field.toJson());
        }
        json.put("fields", fieldsArray);
        
        return json.toString();
    }
    
    public static CollectorSchema fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        
        String id = json.getString("id");
        String name = json.getString("name");
        String icon = json.getString("icon");
        long createdAt = json.getLong("createdAt");
        
        CollectorSchema schema = new CollectorSchema(id, name, icon, createdAt);
        
        if (json.has("category")) schema.category = json.getString("category");
        if (json.has("description")) schema.description = json.getString("description");
        
        JSONArray fieldsArray = json.getJSONArray("fields");
        for (int i = 0; i < fieldsArray.length(); i++) {
            FieldDefinition field = FieldDefinition.fromJson(fieldsArray.getJSONObject(i));
            schema.fields.add(field);
        }
        
        return schema;
    }
    
    @Override
    public String toString() {
        return "CollectorSchema{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", fields=" + fields.size() +
                '}';
    }
}
