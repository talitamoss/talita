package com.core.talita.plugins;

/**
 * Simplified plugin categories based on relationships
 * I • We • All - From individual to universal
 */
public class PluginCategories {
    
    // Main categories
    public static final String I = "I";
    public static final String WE = "We"; 
    public static final String ALL = "All";
    
    /**
     * Get category description
     */
    public static String getCategoryDescription(String category) {
        switch (category) {
            case I:
                return "Your relationship with yourself";
            case WE:
                return "Your relationships with others";
            case ALL:
                return "Your relationship with everything";
            default:
                return "";
        }
    }
    
    /**
     * Get category emoji
     */
    public static String getCategoryEmoji(String category) {
        switch (category) {
            case I:
                return "💫";
            case WE:
                return "🤝";
            case ALL:
                return "🌍";
            default:
                return "📊";
        }
    }
    
    /**
     * Get category color
     */
    public static int getCategoryColor(String category) {
        switch (category) {
            case I:
                return android.graphics.Color.parseColor("#9C27B0"); // Purple
            case WE:
                return android.graphics.Color.parseColor("#FF5722"); // Deep Orange  
            case ALL:
                return android.graphics.Color.parseColor("#00BCD4"); // Cyan
            default:
                return android.graphics.Color.parseColor("#757575"); // Grey
        }
    }
    
    /**
     * Get all categories in order
     */
    public static String[] getAllCategories() {
        return new String[] { I, WE, ALL };
    }
    
    /**
     * Allow users to customize category names
     */
    public static class CustomCategories {
        public String i = I;
        public String we = WE;
        public String all = ALL;
        
        public CustomCategories() {}
        
        public CustomCategories(String i, String we, String all) {
            this.i = i;
            this.we = we;
            this.all = all;
        }
    }
}
