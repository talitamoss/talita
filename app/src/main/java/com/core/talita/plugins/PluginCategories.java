package com.core.talita.plugins;

/**
 * PluginCategories - Defines the three categories of data collection
 * 
 * Based on the sovereignty philosophy:
 * - I: Personal/Individual data
 * - We: Social/Relationship data  
 * - IT/All: Universal/Environmental data
 */
public class PluginCategories {
    
    // Category constants
    public static final String I = "i";      // Personal sovereignty
    public static final String WE = "we";    // Social sovereignty
    public static final String ALL = "all";  // Universal data
    public static final String IT = "all";   // Alias for ALL (for compatibility)
    
    // Display names
    public static final String I_DISPLAY = "Personal";
    public static final String WE_DISPLAY = "Social";
    public static final String ALL_DISPLAY = "Universal";
    
    // Descriptions
    public static final String I_DESC = "Your personal data - health, mood, habits";
    public static final String WE_DESC = "Relationships and social interactions";
    public static final String ALL_DESC = "Environmental and contextual data";
    
    /**
     * Get display name for category
     */
    public static String getDisplayName(String category) {
        switch (category.toLowerCase()) {
            case I:
                return I_DISPLAY;
            case WE:
                return WE_DISPLAY;
            case ALL:
            case "it":
                return ALL_DISPLAY;
            default:
                return category;
        }
    }
    
    /**
     * Get description for category
     */
    public static String getDescription(String category) {
        switch (category.toLowerCase()) {
            case I:
                return I_DESC;
            case WE:
                return WE_DESC;
            case ALL:
            case "it":
                return ALL_DESC;
            default:
                return "";
        }
    }
    
    /**
     * Validate category
     */
    public static boolean isValidCategory(String category) {
        if (category == null) return false;
        String lower = category.toLowerCase();
        return lower.equals(I) || lower.equals(WE) || lower.equals(ALL) || lower.equals("it");
    }
    
    /**
     * Normalize category (handle variations)
     */
    public static String normalize(String category) {
        if (category == null) return I; // Default to personal
        
        String lower = category.toLowerCase();
        if (lower.equals("it")) return ALL; // Normalize IT to ALL
        if (isValidCategory(lower)) return lower;
        
        return I; // Default to personal
    }
    
    private PluginCategories() {
        // Utility class, prevent instantiation
    }
}
