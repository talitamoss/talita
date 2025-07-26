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
    public static final long LOCATION_UPDATE_FASTEST = 30 * 1000; // 30 seconds
    public static final float LOCATION_UPDATE_DISTANCE = 50f; // 50 meters
    
    // Notification Channels
    public static final String NOTIFICATION_CHANNEL_TRACKING = "tracking_channel";
    public static final String NOTIFICATION_CHANNEL_REMINDERS = "reminders_channel";
    public static final String NOTIFICATION_CHANNEL_BACKUP = "backup_channel";
    
    // Permission Request Codes
    public static final int PERMISSION_LOCATION = 100;
    public static final int PERMISSION_AUDIO = 101;
    public static final int PERMISSION_CAMERA = 102;
    public static final int PERMISSION_STORAGE = 103;
    public static final int PERMISSION_ACTIVITY_RECOGNITION = 104;
    public static final int PERMISSION_BACKGROUND_LOCATION = 105;
    
    // Activity Request Codes
    public static final int REQUEST_ENABLE_BLUETOOTH = 200;
    public static final int REQUEST_ENABLE_LOCATION = 201;
    public static final int REQUEST_PICK_FILE = 202;
    public static final int REQUEST_QR_SCAN = 203;
    
    // Data Types
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_NOTE = "note";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_HEALTH = "health";
    public static final String TYPE_MOOD = "mood";
    public static final String TYPE_CUSTOM = "custom";
    
    // Time Constants
    public static final long MINUTE_MILLIS = 60 * 1000;
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    public static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    
    // Limits
    public static final int MAX_AUDIO_DURATION_MINUTES = 30;
    public static final int MAX_PHOTO_SIZE_MB = 10;
    public static final int MAX_EXPORT_ITEMS = 10000;
    public static final int MAX_PLUGIN_COUNT = 50;
    
    // Debug
    public static final boolean DEBUG_LOGGING = false; // Set to true for debug builds
    public static final boolean ENABLE_CRASH_REPORTING = true;
    public static final boolean ENABLE_ANALYTICS = false;
    
    // Features
    public static final boolean FEATURE_CLOUD_BACKUP = true;
    public static final boolean FEATURE_P2P_SYNC = false;
    public static final boolean FEATURE_BLOCKCHAIN = false;
    public static final boolean FEATURE_SOLID_POD = false;
    
    // Private constructor to prevent instantiation
    private AppConstants() {}
}
