package com.core.talita

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import org.osmdroid.config.Configuration
import java.io.File

/**
 * TalitaApp - Main application class
 * Initializes core services and configurations
 */
class TalitaApp : Application() {
    
    companion object {
        private const val TAG = "TalitaApp"
        const val NOTIFICATION_CHANNEL_TRACKING = "tracking_channel"
        const val NOTIFICATION_CHANNEL_BACKUP = "backup_channel"
        const val NOTIFICATION_CHANNEL_GENERAL = "general_channel"
        
        lateinit var instance: TalitaApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d(TAG, "Initializing application...")
        
        // Initialize OSMDroid configuration for maps
        initializeOSMDroid()
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize plugin system
        initializePluginSystem()
        
        // Setup crash handler (for production)
        setupCrashHandler()
        
        // Initialize debug mode if enabled
        val prefs = getSharedPreferences("EnhancedSettings", MODE_PRIVATE)
        val debugMode = prefs.getBoolean("debug_logging", false)
        if (debugMode) {
            Log.d(TAG, "Debug mode is enabled")
            // Debug mode is handled per-instance in UniversalDataService
        }
        
        Log.d(TAG, "Application initialized successfully")
    }
    
    private fun initializeOSMDroid() {
        try {
            // Set user agent
            Configuration.getInstance().userAgentValue = packageName
            
            // Set cache location
            val osmdroidBasePath = File(cacheDir, "osmdroid")
            val osmdroidTileCache = File(osmdroidBasePath, "tiles")
            
            Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
            Configuration.getInstance().osmdroidTileCache = osmdroidTileCache
            
            // Create directories if they don't exist
            osmdroidBasePath.mkdirs()
            osmdroidTileCache.mkdirs()
            
            Log.d(TAG, "OSMDroid initialized with cache at: ${osmdroidTileCache.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize OSMDroid", e)
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Tracking channel - for location tracking service
            val trackingChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_TRACKING,
                "Background Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for background location tracking"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            // Backup channel - for cloud backup operations
            val backupChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_BACKUP,
                "Cloud Backup",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for cloud backup progress"
                setShowBadge(false)
            }
            
            // General channel - for general notifications
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            
            // Create all channels
            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(backupChannel)
            notificationManager.createNotificationChannel(generalChannel)
            
            Log.d(TAG, "Notification channels created")
        }
    }
    
    private fun initializePluginSystem() {
        try {
            // Create plugins directory if it doesn't exist
            val pluginsDir = File(filesDir, "plugins")
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs()
                Log.d(TAG, "Created plugins directory")
            }
            
            // Plugin manager will be initialized when first accessed
            // via PluginManager.getInstance(context)
            Log.d(TAG, "Plugin system ready")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize plugin system", e)
        }
    }
    
    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            
            try {
                // Log crash details to a file for debugging
                val crashLog = File(filesDir, "crash_log.txt")
                crashLog.appendText(
                    "\n\n--- Crash at ${System.currentTimeMillis()} ---\n" +
                    "Thread: ${thread.name}\n" +
                    "Exception: ${throwable.message}\n" +
                    "Stack trace:\n${throwable.stackTraceToString()}\n"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write crash log", e)
            }
            
            // Call the default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Get the main application context from anywhere
     */
    fun getAppContext(): Context = applicationContext
    
    /**
     * Clear all app data (for testing/reset)
     */
    fun clearAllData() {
        try {
            // Clear shared preferences
            val prefsDir = File(filesDir.parent, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { it.delete() }
            }
            
            // Clear databases
            val dbDir = File(filesDir.parent, "databases")
            if (dbDir.exists() && dbDir.isDirectory) {
                dbDir.listFiles()?.forEach { it.delete() }
            }
            
            // Clear internal files
            filesDir.listFiles()?.forEach { 
                if (it.isDirectory) {
                    it.deleteRecursively()
                } else {
                    it.delete()
                }
            }
            
            // Clear cache
            cacheDir.deleteRecursively()
            
            Log.d(TAG, "All app data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear app data", e)
        }
    }
}
