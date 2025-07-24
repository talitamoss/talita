package com.core.talita;

/**
 * PersonalDataType - Enum defining all supported data types
 * 
 * Central registry of all data types in the system.
 * Each type has metadata for UI display and processing.
 */
public enum PersonalDataType {
    // Movement & Location
    LOCATION("location", "📍", "Location", "Geographic coordinates and places"),
    STEPS("steps", "👣", "Steps", "Daily step count"),
    ACTIVITY("activity", "🏃", "Activity", "Physical activity recognition"),
    EXERCISE("exercise", "💪", "Exercise", "Workout and fitness activities"),
    
    // Health & Wellness
    WATER("water", "💧", "Water", "Hydration tracking"),
    MOOD("mood", "😊", "Mood", "Emotional state tracking"),
    SLEEP("sleep", "😴", "Sleep", "Sleep duration and quality"),
    NUTRITION("nutrition", "🍎", "Nutrition", "Food and calorie intake"),
    WEIGHT("weight", "⚖️", "Weight", "Body weight measurements"),
    HEART_RATE("heart_rate", "❤️", "Heart Rate", "Heart rate measurements"),
    
    // Productivity & Time
    SCREEN_TIME("screen_time", "📱", "Screen Time", "Device usage tracking"),
    FOCUS_TIME("focus_time", "🎯", "Focus Time", "Productive time tracking"),
    BREAK_TIME("break_time", "☕", "Break Time", "Rest and break periods"),
    
    // Social & Communication
    SOCIAL_INTERACTION("social", "👥", "Social", "Social interactions and meetings"),
    COMMUNICATION("communication", "💬", "Communication", "Messages and calls"),
    
    // Environmental
    WEATHER("weather", "🌤️", "Weather", "Local weather conditions"),
    NOISE_LEVEL("noise", "🔊", "Noise Level", "Environmental sound levels"),
    AIR_QUALITY("air_quality", "🌬️", "Air Quality", "Air quality measurements"),
    
    // Media & Content
    AUDIO("audio", "🎤", "Audio", "Voice recordings and audio notes"),
    PHOTO("photo", "📷", "Photo", "Pictures and visual memories"),
    NOTE("note", "📝", "Note", "Text notes and thoughts"),
    
    // Financial
    EXPENSE("expense", "💰", "Expense", "Financial transactions"),
    INCOME("income", "💵", "Income", "Money received"),
    
    // Habits & Routines
    HABIT("habit", "🔄", "Habit", "Habit tracking"),
    MEDICATION("medication", "💊", "Medication", "Medication adherence"),
    SUBSTANCE("substance", "🚬", "Substance", "Substance use tracking"),
    
    // Custom
    CUSTOM("custom", "⭐", "Custom", "User-defined data type");
    
    private final String id;
    private final String icon;
    private final String displayName;
    private final String description;
    
    PersonalDataType(String id, String icon, String displayName, String description) {
        this.id = id;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get data type by ID
     */
    public static PersonalDataType fromId(String id) {
        for (PersonalDataType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return CUSTOM;
    }
    
    /**
     * Get category for grouping in UI
     */
    public String getCategory() {
        switch (this) {
            case LOCATION:
            case STEPS:
            case ACTIVITY:
            case EXERCISE:
                return "Movement & Fitness";
                
            case WATER:
            case MOOD:
            case SLEEP:
            case NUTRITION:
            case WEIGHT:
            case HEART_RATE:
                return "Health & Wellness";
                
            case SCREEN_TIME:
            case FOCUS_TIME:
            case BREAK_TIME:
                return "Productivity";
                
            case SOCIAL_INTERACTION:
            case COMMUNICATION:
                return "Social";
                
            case WEATHER:
            case NOISE_LEVEL:
            case AIR_QUALITY:
                return "Environment";
                
            case AUDIO:
            case PHOTO:
            case NOTE:
                return "Media & Notes";
                
            case EXPENSE:
            case INCOME:
                return "Financial";
                
            case HABIT:
            case MEDICATION:
            case SUBSTANCE:
                return "Habits & Routines";
                
            default:
                return "Other";
        }
    }
    
    /**
     * Check if this type requires special permissions
     */
    public boolean requiresPermission() {
        switch (this) {
            case LOCATION:
            case ACTIVITY:
            case AUDIO:
            case PHOTO:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get required Android permission for this type
     */
    public String getRequiredPermission() {
        switch (this) {
            case LOCATION:
                return "android.permission.ACCESS_FINE_LOCATION";
            case ACTIVITY:
                return "android.permission.ACTIVITY_RECOGNITION";
            case AUDIO:
                return "android.permission.RECORD_AUDIO";
            case PHOTO:
                return "android.permission.CAMERA";
            default:
                return null;
        }
    }
}
