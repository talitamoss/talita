package com.core.talita.api;

/**
 * Configuration for quick add UI elements
 * 
 * Defines how a plugin appears in the quick add dashboard,
 * including style, default values, and interaction behavior.
 */
public class QuickAddConfig {
    
    /**
     * Visual style for the quick add element
     */
    public enum QuickAddStyle {
        TILE,           // Standard tile with icon and title
        CARD,           // Larger card with more information
        BUTTON,         // Simple button style
        MULTI_CHOICE,   // Multiple choice selector
        NUMERIC_INPUT,  // Number input with +/- buttons
        TEXT_INPUT,     // Text input field
        GRID,           // Grid layout for multiple options
        SIMPLE_TAP     // Just tap to log default value
    }
    
    private final String title;
    private final String description;
    private final QuickAddStyle style;
    private final boolean showInDashboard;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final String[] choices;
    
    // Constructor that accepts all parameters (for compatibility)
    public QuickAddConfig(String title, String description, QuickAddStyle style,
                          boolean showInDashboard, Object defaultValue,
                          Object minValue, Object maxValue, String[] choices) {
        this.title = title;
        this.description = description;
        this.style = style;
        this.showInDashboard = showInDashboard;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.choices = choices;
    }
    
    // Simplified constructor for basic configs (4 parameters)
    public QuickAddConfig(String title, String description, QuickAddStyle style, boolean showInDashboard) {
        this(title, description, style, showInDashboard, null, null, null, null);
    }
    
    // String-based style constructor for compatibility
    public QuickAddConfig(String title, String description, String styleString, boolean showInDashboard) {
        this(title, description, parseStyle(styleString), showInDashboard, null, null, null, null);
    }
    
    private static QuickAddStyle parseStyle(String style) {
        try {
            return QuickAddStyle.valueOf(style.toUpperCase());
        } catch (Exception e) {
            return QuickAddStyle.TILE; // Default
        }
    }
    
    /**
     * Builder for creating QuickAddConfig instances
     */
    public static class Builder {
        private String title = "";
        private String description = "";
        private QuickAddStyle style = QuickAddStyle.TILE;
        private boolean showInDashboard = true;
        private Object defaultValue = null;
        private Object minValue = null;
        private Object maxValue = null;
        private String[] choices = null;
        
        /**
         * Set the title displayed on the quick add element
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        /**
         * Set the description or hint text
         */
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        
        /**
         * Set the visual style using a string
         * Falls back to TILE if invalid
         */
        public Builder setStyle(String style) {
            try {
                this.style = QuickAddStyle.valueOf(style.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.style = QuickAddStyle.TILE;
            }
            return this;
        }
        
        /**
         * Set the visual style using enum
         */
        public Builder setStyle(QuickAddStyle style) {
            this.style = style;
            return this;
        }
        
        /**
         * Set whether this should appear in the main dashboard
         */
        public Builder showInDashboard(boolean show) {
            this.showInDashboard = show;
            return this;
        }
        
        /**
         * Set the default value for input types
         */
        public Builder defaultValue(Object value) {
            this.defaultValue = value;
            return this;
        }
        
        /**
         * Set the range for numeric inputs
         */
        public Builder range(Object min, Object max) {
            this.minValue = min;
            this.maxValue = max;
            return this;
        }
        
        /**
         * Set choices for multi-choice style
         * Automatically sets style to MULTI_CHOICE
         */
        public Builder choices(String... choices) {
            this.choices = choices;
            this.style = QuickAddStyle.MULTI_CHOICE;
            return this;
        }
        
        /**
         * Build the QuickAddConfig instance
         */
        public QuickAddConfig build() {
            return new QuickAddConfig(title, description, style, showInDashboard,
                                    defaultValue, minValue, maxValue, choices);
        }
    }
    
    // Getters
    
    public String getTitle() { 
        return title; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public QuickAddStyle getStyle() { 
        return style; 
    }
    
    public boolean shouldShowInDashboard() { 
        return showInDashboard; 
    }
    
    public Object getDefaultValue() { 
        return defaultValue; 
    }
    
    public Object getMinValue() { 
        return minValue; 
    }
    
    public Object getMaxValue() { 
        return maxValue; 
    }
    
    public String[] getChoices() { 
        return choices; 
    }
    
    /**
     * Check if this config has a numeric range
     */
    public boolean hasRange() {
        return minValue != null && maxValue != null;
    }
    
    /**
     * Check if this config has multiple choices
     */
    public boolean hasChoices() {
        return choices != null && choices.length > 0;
    }
}
