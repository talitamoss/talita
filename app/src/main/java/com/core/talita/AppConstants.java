package com.core.talita;

/**
 * AppConstants - Centralized configuration and constants
 * 
 * All app-wide constants in one place for easy rebranding
 * and configuration management.
 */
public class AppConstants {
    
    // App Identity (Easy to rebrand)
    public static final String APP_NAME = "Data Sovereignty";
    public static final String APP_SHORT_NAME = "DataSov";
    public static final String APP_PACKAGE_PREFIX = "com.core";
    
    // SharedPreferences Keys
    public static final String PREFS_MAIN = "app_settings";
    public static final String PREFS_TRACKING = "tracking_prefs";
    public static final String PREFS_DASHBOARD = "dashboard_prefs";
    public static final String PREFS_SECURITY = "security_prefs";
    public static final String PREFS_PLUGINS = "plugin_prefs";
    
    // Database
    public static final String DATABASE_NAME = "personal_data.db";
    public static final int DATABASE_VERSION = 1;
    
    // Encryption
    public static final String KEY_ALIAS = "personal_data_key";
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 128;
    
    // File Storage
    public static final String AUDIO_FOLDER = "audio_recordings";
    public static final String PHOTO_FOLDER = "photos";
    public static final String EXPORT_FOLDER = "exports";
    public static final String BACKUP_FOLDER = "backups";
    public static final String PLUGIN_FOLDER = "plugins";
    
    // Tracking Service
    public static final int TRACKING_NOTIFICATION_ID = 1001;
    public static final long LOCATION_UPDATE_INTERVAL = 2 * 60 * 1000; // 2 minutes
    public static final long LOCATION_FASTEST_INTERVAL = 30 * 1000; // 30 seconds
    public static final float LOCATION_MIN_DISTANCE = 10.0f; // 10 meters
    
    // Backup Service
    public static final int BACKUP_NOTIFICATION_ID = 1002;
    public static final long BACKUP_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    public static final int BACKUP_RETRY_COUNT = 3;
    
    // UI Constants
    public static final int ANIMATION_DURATION = 300; // milliseconds
    public static final int QUICK_ADD_COLUMNS = 3;
    public static final int MAX_RECENT_ITEMS = 50;
    
    // Plugin System
    public static final String PLUGIN_MANIFEST_FILE = "plugin.json";
    public static final String PLUGIN_API_VERSION = "1.0.0";
    public static final String[] PLUGIN_CATEGORIES = {"I", "We", "All"};
    
    // Data Limits
    public static final int MAX_AUDIO_DURATION = 5 * 60; // 5 minutes in seconds
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
    public static final int MAX_EXPORT_ITEMS = 10000;
    
    // Time Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    public static final String DISPLAY_TIME_FORMAT = "h:mm a";
    
    // Intent Extras
    public static final String EXTRA_DATA_TYPE = "data_type";
    public static final String EXTRA_DATA_ID = "data_id";
    public static final String EXTRA_PLUGIN_ID = "plugin_id";
    public static final String EXTRA_TIME_PERIOD = "time_period";
    
    // Request Codes
    public static final int REQUEST_LOCATION_PERMISSION = 100;
    public static final int REQUEST_AUDIO_PERMISSION = 101;
    public static final int REQUEST_CAMERA_PERMISSION = 102;
    public static final int REQUEST_ACTIVITY_RECOGNITION = 103;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 104;
    public static final int REQUEST_BACKGROUND_LOCATION = 105;
    
    // Result Codes
    public static final int RESULT_DATA_COLLECTED = 200;
    public static final int RESULT_PLUGIN_INSTALLED = 201;
    public static final int RESULT_EXPORT_COMPLETE = 202;
    
    // Debug Mode
    public static final boolean DEBUG_LOGGING = BuildConfig.DEBUG;
    public static final boolean DEBUG_ENCRYPTION = false; // Set to true to disable encryption for debugging
    
    // Cloud Providers
    public static final String PROVIDER_GOOGLE_DRIVE = "google_drive";
    public static final String PROVIDER_DROPBOX = "dropbox";
    public static final String PROVIDER_SOLID_POD = "solid_pod";
    
    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new AssertionError("AppConstants should not be instantiated");
    }
}
