package com.core.talita.plugins.base;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import com.core.talita.api.*;
import java.util.*;

/**
 * SimpleDataCollector - A ready-to-use collector for simple data types
 * 
 * Perfect for plugins that just need basic data collection with a simple input dialog.
 * Handles text, numbers, and basic selections.
 */
public class SimpleDataCollector extends BaseDataCollector {
    
    private final String type;
    private final String displayName;
    private final String description;
    private final String emoji;
    private final String category;
    private final String inputHint;
    private final InputType inputType;
    
    public enum InputType {
        TEXT,
        NUMBER,
        DECIMAL,
        CHOICE
    }

    /**
     * Create a simple collector
     */
    public SimpleDataCollector(String type, String displayName, String description, 
                              String emoji, String category, String inputHint, InputType inputType) {
        super();
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.emoji = emoji;
        this.category = category;
        this.inputHint = inputHint;
        this.inputType = inputType;
    }

    /**
     * Convenience constructor for text input
     */
    public SimpleDataCollector(String type, String displayName, String emoji, String category) {
        this(type, displayName, "Track your " + displayName.toLowerCase(), 
             emoji, category, "Enter value", InputType.TEXT);
    }

    @Override
    public CollectorResult collect() {
        if (context == null) {
            return CollectorResult.failure(type, "Collector not initialized");
        }

        // In a real implementation, this would show a proper UI
        // For now, we'll just create sample data
        Map<String, Object> data = new HashMap<>();
        data.put("value", "Sample " + displayName);
        data.put("timestamp", System.currentTimeMillis());
        data.put("inputType", inputType.name());
        
        return collectQuick(data);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getEmoji() {
        return emoji;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public List<String> getRequiredPermissions() {
        return new ArrayList<>(); // No special permissions
    }

    @Override
    protected CollectorSettings getDefaultSettings() {
        return CollectorSettings.getDefault();
    }

    @Override
    public boolean validateData(Map<String, Object> data) {
        if (!super.validateData(data)) {
            return false;
        }

        Object value = data.get("value");
        if (value == null) {
            return false;
        }

        switch (inputType) {
            case NUMBER:
                try {
                    Integer.parseInt(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case DECIMAL:
                try {
                    Double.parseDouble(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case TEXT:
            case CHOICE:
            default:
                return !value.toString().trim().isEmpty();
        }
    }

    /**
     * Builder for creating SimpleDataCollector instances
     */
    public static class Builder {
        private String type;
        private String displayName;
        private String description;
        private String emoji = "📝";
        private String category = "i";
        private String inputHint = "Enter value";
        private InputType inputType = InputType.TEXT;

        public Builder(String type, String displayName) {
            this.type = type;
            this.displayName = displayName;
            this.description = "Track your " + displayName.toLowerCase();
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder inputHint(String hint) {
            this.inputHint = hint;
            return this;
        }

        public Builder inputType(InputType type) {
            this.inputType = type;
            return this;
        }

        public SimpleDataCollector build() {
            return new SimpleDataCollector(type, displayName, description, 
                                         emoji, category, inputHint, inputType);
        }
    }
}
