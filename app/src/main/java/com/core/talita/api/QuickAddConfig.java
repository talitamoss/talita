package com.core.talita.api;

/**
 * QuickAddConfig - Configuration for quick add functionality
 * Defines how a data collector appears and behaves in quick add UI
 */
public class QuickAddConfig {
    
    public enum QuickAddStyle {
        SIMPLE_BUTTON,      // Just a button that triggers collection
        NUMERIC_INPUT,      // Quick numeric input (water, weight, etc)
        SLIDER,            // Slider input (mood, energy level)
        DURATION_TIMER,    // Start/stop timer (exercise, focus)
        TEXT_NOTE,         // Quick text note
        MULTI_CHOICE,      // Multiple choice buttons
        CARD               // Card style display
    }
    
    private final String title;
    private final String hint;
    private final QuickAddStyle style;
    private final boolean showInDashboard;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final String[] choices;
    
    /**
     * Simple constructor for basic configs
     */
    public QuickAddConfig(String title, String hint, QuickAddStyle style, boolean showInDashboard) {
        this.title = title;
        this.hint = hint;
        this.style = style;
        this.showInDashboard = showInDashboard;
        this.defaultValue = null;
        this.minValue = null;
        this.maxValue = null;
        this.choices = null;
    }
    
    /**
     * Full constructor with all options
     */
    public QuickAddConfig(String title, String hint, QuickAddStyle style, 
                         boolean showInDashboard, Object defaultValue,
                         Object minValue, Object maxValue, String[] choices) {
        this.title = title;
        this.hint = hint;
        this.style = style;
        this.showInDashboard = showInDashboard;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.choices = choices;
    }
    
    // Builder pattern for easier construction
    public static class Builder {
        private String title;
        private String hint;
        private QuickAddStyle style = QuickAddStyle.SIMPLE_BUTTON;
        private boolean showInDashboard = true;
        private Object defaultValue;
        private Object minValue;
        private Object maxValue;
        private String[] choices;
        
        public Builder(String title) {
            this.title = title;
            this.hint = "Tap to add";
        }
        
        public Builder() {
            this.title = "Quick Add";
            this.hint = "Tap to add";
        }
        
        public Builder hint(String hint) {
            this.hint = hint;
            return this;
        }
        
        public Builder style(QuickAddStyle style) {
            this.style = style;
            return this;
        }
        
        public Builder showInDashboard(boolean show) {
            this.showInDashboard = show;
            return this;
        }
        
        public Builder defaultValue(Object value) {
            this.defaultValue = value;
            return this;
        }
        
        public Builder range(Object min, Object max) {
            this.minValue = min;
            this.maxValue = max;
            return this;
        }
        
        public Builder choices(String... choices) {
            this.choices = choices;
            this.style = QuickAddStyle.MULTI_CHOICE;
            return this;
        }
        
        public QuickAddConfig build() {
            return new QuickAddConfig(title, hint, style, showInDashboard,
                                    defaultValue, minValue, maxValue, choices);
        }
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getHint() { return hint; }
    public QuickAddStyle getStyle() { return style; }
    public boolean shouldShowInDashboard() { return showInDashboard; }
    public Object getDefaultValue() { return defaultValue; }
    public Object getMinValue() { return minValue; }
    public Object getMaxValue() { return maxValue; }
    public String[] getChoices() { return choices; }
}
