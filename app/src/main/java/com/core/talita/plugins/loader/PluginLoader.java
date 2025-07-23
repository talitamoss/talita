package com.core.talita.plugins.loader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginManager;
import dalvik.system.DexClassLoader;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * PluginLoader - Handles dynamic loading of external plugins
 * 
 * This class scans for plugin files (APK/JAR) in designated directories,
 * verifies their integrity, and loads them into the application at runtime.
 * 
 * Security features:
 * - Signature verification
 * - Permission checking
 * - Manifest validation
 * - Safe class loading
 */
public class PluginLoader {
    private static final String TAG = "PluginLoader";
    
    // Plugin directories
    private static final String INTERNAL_PLUGIN_DIR = "plugins";
    private static final String EXTERNAL_PLUGIN_DIR = "TalitaPlugins";
    
    // Plugin file patterns
    private static final String[] PLUGIN_EXTENSIONS = {".apk", ".jar"};
    private static final String PLUGIN_MANIFEST = "plugin.json";
    
    private final Context context;
    private final PluginSecurityManager securityManager;
    private final List<LoadedPlugin> loadedPlugins;
    
    public PluginLoader(Context context) {
        this.context = context;
        this.securityManager = new PluginSecurityManager(context);
        this.loadedPlugins = new ArrayList<>();
    }
    
    /**
     * Scan and load all available plugins
     */
    public void loadAllPlugins() {
        Log.d(TAG, "Starting plugin discovery...");
        
        // Scan internal plugin directory
        File internalDir = new File(context.getFilesDir(), INTERNAL_PLUGIN_DIR);
        if (!internalDir.exists()) {
            internalDir.mkdirs();
        }
        scanDirectory(internalDir);
        
        // Scan external plugin directory (if permission granted)
        File externalDir = new File(android.os.Environment.getExternalStorageDirectory(), EXTERNAL_PLUGIN_DIR);
        if (externalDir.exists() && externalDir.canRead()) {
            scanDirectory(externalDir);
        }
        
        Log.d(TAG, "Plugin discovery complete. Loaded " + loadedPlugins.size() + " plugins");
    }
    
    /**
     * Load a specific plugin file
     */
    public boolean loadPlugin(File pluginFile) {
        Log.d(TAG, "Attempting to load plugin: " + pluginFile.getName());
        
        try {
            // Verify plugin file
            if (!isValidPluginFile(pluginFile)) {
                Log.e(TAG, "Invalid plugin file: " + pluginFile.getName());
                return false;
            }
            
            // Read plugin manifest
            PluginManifest manifest = readPluginManifest(pluginFile);
            if (manifest == null) {
                Log.e(TAG, "Failed to read plugin manifest");
                return false;
            }
            
            // Security checks
            if (!securityManager.verifyPlugin(pluginFile, manifest)) {
                Log.e(TAG, "Plugin failed security verification");
                return false;
            }
            
            // Check version compatibility
            if (!isCompatibleVersion(manifest.minAppVersion)) {
                Log.e(TAG, "Plugin requires app version " + manifest.minAppVersion);
                return false;
            }
            
            // Load plugin classes
            ClassLoader classLoader = createClassLoader(pluginFile);
            if (classLoader == null) {
                Log.e(TAG, "Failed to create class loader");
                return false;
            }
            
            // Instantiate plugin
            DataCollectorPlugin plugin = instantiatePlugin(classLoader, manifest.mainClass);
            if (plugin == null) {
                Log.e(TAG, "Failed to instantiate plugin");
                return false;
            }
            
            // Register with plugin manager
            PluginManager.getInstance(context).registerPlugin(plugin);
            
            // Track loaded plugin
            LoadedPlugin loaded = new LoadedPlugin(pluginFile, manifest, plugin, classLoader);
            loadedPlugins.add(loaded);
            
            Log.d(TAG, "✅ Successfully loaded plugin: " + manifest.name);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading plugin", e);
            return false;
        }
    }
    
    /**
     * Unload a plugin
     */
    public boolean unloadPlugin(String pluginId) {
        LoadedPlugin plugin = findLoadedPlugin(pluginId);
        if (plugin == null) {
            return false;
        }
        
        // Disable in plugin manager
        PluginManager.getInstance(context).setPluginEnabled(pluginId, false);
        
        // Clean up resources
        plugin.cleanup();
        
        // Remove from loaded list
        loadedPlugins.remove(plugin);
        
        Log.d(TAG, "Unloaded plugin: " + pluginId);
        return true;
    }
    
    /**
     * Install a plugin from external source
     */
    public boolean installPlugin(File sourceFile, boolean moveFile) {
        // Verify it's a valid plugin
        if (!isValidPluginFile(sourceFile)) {
            return false;
        }
        
        // Copy to internal plugin directory
        File destDir = new File(context.getFilesDir(), INTERNAL_PLUGIN_DIR);
        File destFile = new File(destDir, sourceFile.getName());
        
        try {
            if (moveFile) {
                sourceFile.renameTo(destFile);
            } else {
                // Copy file
                android.os.FileUtils.copy(new FileInputStream(sourceFile), 
                                        new java.io.FileOutputStream(destFile));
            }
            
            // Load the installed plugin
            return loadPlugin(destFile);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install plugin", e);
            return false;
        }
    }
    
    /**
     * Get list of loaded plugins
     */
    public List<PluginInfo> getLoadedPlugins() {
        List<PluginInfo> info = new ArrayList<>();
        for (LoadedPlugin loaded : loadedPlugins) {
            info.add(new PluginInfo(loaded.manifest, loaded.file));
        }
        return info;
    }
    
    // Private helper methods
    
    private void scanDirectory(File directory) {
        File[] pluginFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String ext : PLUGIN_EXTENSIONS) {
                    if (name.endsWith(ext)) {
                        return true;
                    }
                }
                return false;
            }
        });
        
        if (pluginFiles != null) {
            for (File file : pluginFiles) {
                loadPlugin(file);
            }
        }
    }
    
    private boolean isValidPluginFile(File file) {
        if (!file.exists() || !file.canRead()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        for (String ext : PLUGIN_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    private PluginManifest readPluginManifest(File pluginFile) {
        try {
            // For APK/JAR files, read from archive
            ZipFile zipFile = new ZipFile(pluginFile);
            ZipEntry manifestEntry = zipFile.getEntry(PLUGIN_MANIFEST);
            
            if (manifestEntry == null) {
                Log.e(TAG, "No plugin manifest found");
                return null;
            }
            
            InputStream input = zipFile.getInputStream(manifestEntry);
            byte[] buffer = new byte[(int) manifestEntry.getSize()];
            input.read(buffer);
            input.close();
            zipFile.close();
            
            String jsonStr = new String(buffer);
            return PluginManifest.fromJson(jsonStr);
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading plugin manifest", e);
            return null;
        }
    }
    
    private boolean isCompatibleVersion(String minVersion) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
            String appVersion = packageInfo.versionName;
            
            // Simple version comparison (you might want more sophisticated logic)
            return appVersion.compareTo(minVersion) >= 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private ClassLoader createClassLoader(File pluginFile) {
        try {
            // Use DexClassLoader for loading external code
            String dexPath = pluginFile.getAbsolutePath();
            String optimizedDir = context.getCodeCacheDir().getAbsolutePath();
            ClassLoader parent = context.getClassLoader();
            
            return new DexClassLoader(dexPath, optimizedDir, null, parent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating class loader", e);
            return null;
        }
    }
    
    private DataCollectorPlugin instantiatePlugin(ClassLoader loader, String className) {
        try {
            Class<?> clazz = loader.loadClass(className);
            
            // Verify it extends DataCollectorPlugin
            if (!DataCollectorPlugin.class.isAssignableFrom(clazz)) {
                Log.e(TAG, "Class does not extend DataCollectorPlugin: " + className);
                return null;
            }
            
            // Create instance
            return (DataCollectorPlugin) clazz.newInstance();
            
        } catch (Exception e) {
            Log.e(TAG, "Error instantiating plugin", e);
            return null;
        }
    }
    
    private LoadedPlugin findLoadedPlugin(String pluginId) {
        for (LoadedPlugin loaded : loadedPlugins) {
            if (loaded.plugin.getPluginId().equals(pluginId)) {
                return loaded;
            }
        }
        return null;
    }
    
    /**
     * Container for loaded plugin information
     */
    private static class LoadedPlugin {
        final File file;
        final PluginManifest manifest;
        final DataCollectorPlugin plugin;
        final ClassLoader classLoader;
        
        LoadedPlugin(File file, PluginManifest manifest, 
                    DataCollectorPlugin plugin, ClassLoader classLoader) {
            this.file = file;
            this.manifest = manifest;
            this.plugin = plugin;
            this.classLoader = classLoader;
        }
        
        void cleanup() {
            // Cleanup resources
            plugin.onPluginDisabled(null);
            // Note: Can't really unload classes in Java, they'll be GC'd eventually
        }
    }
    
    /**
     * Plugin information for UI
     */
    public static class PluginInfo {
        public final String id;
        public final String name;
        public final String version;
        public final String author;
        public final String category;
        public final File file;
        public final List<String> permissions;
        
        PluginInfo(PluginManifest manifest, File file) {
            this.id = manifest.id;
            this.name = manifest.name;
            this.version = manifest.version;
            this.author = manifest.author;
            this.category = manifest.category;
            this.file = file;
            this.permissions = manifest.permissions;
        }
    }
}
