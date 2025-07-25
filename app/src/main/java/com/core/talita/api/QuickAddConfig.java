package com.core.talita.api;

/**
 * QuickAddConfig - Configuration for quick add UI
 * 
 * Defines how a plugin appears in the quick add interface
 * 
 * File path: app/src/main/java/com/core/talita/api/QuickAddConfig.java
 */
public class QuickAddConfig {
    
    // Display properties
    private final String title;
    private final String description;
    private final String style;
    private final String iconUrl;
    private final int iconColor;
    
    // Legacy field names for backward compatibility
    private final String quickAddTitle;
    private final String quickAddDescription;
    
    // Behavior properties
    private final boolean showInGrid;
    private final boolean requiresConfirmation;
    private final int gridPosition;
    
    /**
     * Simple constructor for basic config
     */
    public QuickAddConfig(String title, String description, String style, boolean showInGrid) {
        this.title = title;
        this.description = description;
        this.style = style;
        this.iconUrl = null;
        this.iconColor = 0xFF6366F1; // Default purple
        this.showInGrid = showInGrid;
        this.requiresConfirmation = false;
        this.gridPosition = -1;
        
        // Set legacy fields
        this.quickAddTitle = this.title;
        this.quickAddDescription = this.description;
    }
    
    private QuickAddConfig(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.style = builder.style;
        this.iconUrl = builder.iconUrl;
        this.iconColor = builder.iconColor;
        this.showInGrid = builder.showInGrid;
        this.requiresConfirmation = builder.requiresConfirmation;
        this.gridPosition = builder.gridPosition;
        
        // Set legacy fields
        this.quickAddTitle = this.title;
        this.quickAddDescription = this.description;
    }
    
    // Getters
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getStyle() {
        return style;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public int getIconColor() {
        return iconColor;
    }
    
    public boolean isShowInGrid() {
        return showInGrid;
    }
    
    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }
    
    public int getGridPosition() {
        return gridPosition;
    }
    
    // Legacy getters for backward compatibility
    public String getQuickAddTitle() {
        return quickAddTitle;
    }
    
    public String getQuickAddDescription() {
        return quickAddDescription;
    }
    
    /**
     * Builder for QuickAddConfig
     */
    public static class Builder {
        private String title = "";
        private String description = "";
        private String style = QuickAddStyle.TILE;
        private String iconUrl = null;
        private int iconColor = 0xFF6366F1; // Default purple
        private boolean showInGrid = true;
        private boolean requiresConfirmation = false;
        private int gridPosition = -1; // Auto position
        
        public Builder() {
        }
        
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder setStyle(String style) {
            this.style = style;
            return this;
        }
        
        public Builder setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }
        
        public Builder setIconColor(int iconColor) {
            this.iconColor = iconColor;
            return this;
        }
        
        public Builder setShowInGrid(boolean showInGrid) {
            this.showInGrid = showInGrid;
            return this;
        }
        
        public Builder setRequiresConfirmation(boolean requiresConfirmation) {
            this.requiresConfirmation = requiresConfirmation;
            return this;
        }
        
        public Builder setGridPosition(int gridPosition) {
            this.gridPosition = gridPosition;
            return this;
        }
        
        public QuickAddConfig build() {
            return new QuickAddConfig(this);
        }
    }
}
