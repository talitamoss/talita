package com.core.talita.dynamic;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Defines the structure of a user-defined collector
 * This is the foundation of the entire dynamic system
 */
public class CollectorSchema {
    private String id;
    private String name;
    private String icon;
    private String category;
    private String description;
    private List<FieldDefinition> fields;
    private boolean isUserDefined;
    private long createdAt;
    
    /**
     * Represents a single field in the collector
     */
    public static class FieldDefinition {
        public enum FieldType {
            NUMBER,      // Numeric input
            TEXT,        // Text input
            CHOICE,      // Single selection from list
            MULTI_CHOICE,// Multiple selection
            SCALE,       // Rating scale (1-N)
            BOOLEAN,     // Yes/No toggle
            TIME,        // Time picker
            DATE,        // Date picker
            DURATION,    // Duration input (hours/minutes)
            LOCATION,    // GPS coordinates
            PHOTO,       // Camera/gallery
            AUDIO        // Voice note
        }
        
        public String id;
        public String name;
        public String hint;
        public FieldType type;
        public boolean required;
        public JSONObject validation; // Min/max, choices, etc.
        public String unit; // ml, kg, minutes, etc.
        
        public FieldDefinition(String name, FieldType type) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.type = type;
            this.required = false;
            this.validation = new JSONObject();
        }
        
        // Builder pattern for easy configuration
        public FieldDefinition withHint(String hint) {
            this.hint = hint;
            return this;
        }
        
        public FieldDefinition withUnit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public FieldDefinition withRange(double min, double max) {
            try {
                validation.put("min", min);
                validation.put("max", max);
            } catch (Exception e) {
                e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
        
        public FieldDefinition required() {
            this.required = true;
            return this;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public FieldType getType() { return type; }
        public boolean isRequired() { return required; }
        public String getUnit() { return unit; }
        public JSONObject getValidation() { return validation; }
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
    public List<FieldDefinition> getFields() { return fields; }
    public boolean isUserDefined() { return isUserDefined; }
    public long getCreatedAt() { return createdAt; }
    
    // JSON serialization
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("icon", icon);
            json.put("category", category);
            json.put("description", description);
            json.put("isUserDefined", isUserDefined);
            json.put("createdAt", createdAt);
            
            // Serialize fields
            JSONArray fieldsArray = new JSONArray();
            for (FieldDefinition field : fields) {
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("id", field.id);
                fieldJson.put("name", field.name);
                fieldJson.put("hint", field.hint);
                fieldJson.put("type", field.type.name());
                fieldJson.put("required", field.required);
                fieldJson.put("unit", field.unit);
                fieldJson.put("validation", field.validation);
                fieldsArray.put(fieldJson);
            }
            json.put("fields", fieldsArray);
            
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
    
    // JSON deserialization
    public static CollectorSchema fromJson(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            CollectorSchema schema = new CollectorSchema(
                json.getString("name"),
                json.getString("icon")
            );
            
            schema.id = json.getString("id");
            schema.category = json.optString("category");
            schema.description = json.optString("description");
            schema.isUserDefined = json.optBoolean("isUserDefined", true);
            schema.createdAt = json.optLong("createdAt", System.currentTimeMillis());
            
            // Deserialize fields
            JSONArray fieldsArray = json.getJSONArray("fields");
            for (int i = 0; i < fieldsArray.length(); i++) {
                JSONObject fieldJson = fieldsArray.getJSONObject(i);
                
                FieldDefinition field = new FieldDefinition(
                    fieldJson.getString("name"),
		    FieldDefinition.FieldType.valueOf(fieldJson.getString("type"))
                );
                
                field.id = fieldJson.getString("id");
                field.hint = fieldJson.optString("hint");
                field.required = fieldJson.optBoolean("required", false);
                field.unit = fieldJson.optString("unit");
                field.validation = fieldJson.optJSONObject("validation");
                if (field.validation == null) {
                    field.validation = new JSONObject();
                }
                
                schema.fields.add(field);
            }
            
            return schema;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
