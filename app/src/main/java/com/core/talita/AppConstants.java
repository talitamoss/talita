package com.core.talita;

/**
 * AppConstants - Central location for app-wide constants
 */
public class AppConstants {
    
    // Database
    public static final String DATABASE_NAME = "personal_data.db";
    public static final int DATABASE_VERSION = 1;
    
    // Shared Preferences
    public static final String PREFS_NAME = "app_preferences";
    
    // Data Types
    public static final String TYPE_WATER = "water";
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_MOOD = "mood";
    public static final String TYPE_EXERCISE = "exercise";
    public static final String TYPE_SLEEP = "sleep";
    
    // Time Constants
    public static final long HOUR_IN_MILLIS = 60 * 60 * 1000;
    public static final long DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS;
    public static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;
    
    // Limits
    public static final int MAX_RECENT_ITEMS = 100;
    public static final int DEFAULT_QUERY_LIMIT = 50;
    
    private AppConstants() {
        // Prevent instantiation
    }
}
