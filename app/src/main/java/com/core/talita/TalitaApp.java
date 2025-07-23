package com.core.talita;

import android.app.Application;
import android.util.Log;
import com.core.talita.plugins.loader.PluginLoader;
import com.core.talita.plugins.bridge.PluginBridge;

/**
 * TalitaApp - Application class
 * Now initializes the plugin system on startup
 */
public class TalitaApp extends Application {
    private static final String TAG = "TalitaApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "🚀 Initializing Talita Application");
        
        // Initialize encryption
        try {
            EncryptionUtils.generateKey();
            Log.d(TAG, "✅ Encryption key initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize encryption", e);
        }
        
        // Initialize plugin system
        initializePluginSystem();
        
        Log.d(TAG, "✅ Talita Application initialized");
    }
    
    private void initializePluginSystem() {
        try {
            Log.d(TAG, "🔌 Initializing plugin system...");
            
            // Initialize plugin bridge for inter-plugin communication
            PluginBridge.getInstance(this);
            
            // Load all installed plugins
            PluginLoader pluginLoader = new PluginLoader(this);
            pluginLoader.loadAllPlugins();
            
            Log.d(TAG, "✅ Plugin system initialized");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize plugin system", e);
            // Don't crash the app if plugin system fails
            // Core functionality should still work
        }
    }
}
