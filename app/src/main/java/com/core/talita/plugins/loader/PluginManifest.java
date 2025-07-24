package com.core.talita.plugins.loader;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * PluginManifest - Represents plugin metadata
 * 
 * This class defines the structure of plugin.json files that must be
 * included in every plugin package. It contains all necessary information
 * for loading, verifying, and managing plugins.
 */
public class PluginManifest {
    private static final String TAG = "PluginManifest";
    
    // Required fields
    public final String id;           // Unique plugin identifier (e.g., "com.example.myplugin")
    public final String name;         // Display name
    public final String version;      // Plugin version (semantic versioning)
    public final String minAppVersion; // Minimum app version required
    public final String author;       // Plugin author/developer
    public final String category;     // I, We, or All
    public final String mainClass;    // Main plugin class to instantiate
    
    // Optional fields
    public final String description;  // Plugin description
    public final String website;      // Developer website
    public final String email;        // Support email
    public final List<String> permissions; // Required permissions
    public final List<String> dependencies; // Other required plugins
    public final String signature;    // Plugin signature for verification
    
    // Additional metadata
    public final long buildTime;      // Build timestamp
    public final boolean experimental; // Mark as experimental/beta
    public final int targetSdkVersion; // Target Android SDK version
    
    private PluginManifest(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.version = builder.version;
        this.minAppVersion = builder.minAppVersion;
        this.author = builder.author;
        this.category = builder.category;
        this.mainClass = builder.mainClass;
        this.description = builder.description;
        this.website = builder.website;
        this.email = builder.email;
        this.permissions = new ArrayList<>(builder.permissions);
        this.dependencies = new ArrayList<>(builder.dependencies);
        this.signature = builder.signature;
        this.buildTime = builder.buildTime;
        this.experimental = builder.experimental;
        this.targetSdkVersion = builder.targetSdkVersion;
    }
    
    /**
     * Parse plugin manifest from JSON
     */
    public static PluginManifest fromJson(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject plugin = root.getJSONObject("plugin");
            
            Builder builder = new Builder()
                .setId(plugin.getString("id"))
                .setName(plugin.getString("name"))
                .setVersion(plugin.getString("version"))
                .setMinAppVersion(plugin.getString("minAppVersion"))
                .setAuthor(plugin.getString("author"))
                .setCategory(plugin.getString("category"))
                .setMainClass(plugin.getString("mainClass"));
            
            // Optional fields
            if (plugin.has("description")) {
                builder.setDescription(plugin.getString("description"));
            }
            if (plugin.has("website")) {
                builder.setWebsite(plugin.getString("website"));
            }
            if (plugin.has("email")) {
                builder.setEmail(plugin.getString("email"));
            }
            if (plugin.has("signature")) {
                builder.setSignature(plugin.getString("signature"));
            }
            if (plugin.has("buildTime")) {
                builder.setBuildTime(plugin.getLong("buildTime"));
            }
            if (plugin.has("experimental")) {
                builder.setExperimental(plugin.getBoolean("experimental"));
            }
            if (plugin.has("targetSdkVersion")) {
                builder.setTargetSdkVersion(plugin.getInt("targetSdkVersion"));
            }
            
            // Parse permissions array
            if (plugin.has("permissions")) {
                JSONArray perms = plugin.getJSONArray("permissions");
                List<String> permissions = new ArrayList<>();
                for (int i = 0; i < perms.length(); i++) {
                    permissions.add(perms.getString(i));
                }
                builder.setPermissions(permissions);
            }
            
            // Parse dependencies array
            if (plugin.has("dependencies")) {
                JSONArray deps = plugin.getJSONArray("dependencies");
                List<String> dependencies = new ArrayList<>();
                for (int i = 0; i < deps.length(); i++) {
                    dependencies.add(deps.getString(i));
                }
                builder.setDependencies(dependencies);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing plugin manifest", e);
            return null;
        }
    }
    
    /**
     * Convert manifest to JSON
     */
    public String toJson() {
        try {
            JSONObject plugin = new JSONObject();
            plugin.put("id", id);
            plugin.put("name", name);
            plugin.put("version", version);
            plugin.put("minAppVersion", minAppVersion);
            plugin.put("author", author);
            plugin.put("category", category);
            plugin.put("mainClass", mainClass);
            
            if (description != null) plugin.put("description", description);
            if (website != null) plugin.put("website", website);
            if (email != null) plugin.put("email", email);
            if (signature != null) plugin.put("signature", signature);
            if (buildTime > 0) plugin.put("buildTime", buildTime);
            plugin.put("experimental", experimental);
            plugin.put("targetSdkVersion", targetSdkVersion);
            
            if (!permissions.isEmpty()) {
                plugin.put("permissions", new JSONArray(permissions));
            }
            if (!dependencies.isEmpty()) {
                plugin.put("dependencies", new JSONArray(dependencies));
            }
            
            JSONObject root = new JSONObject();
            root.put("plugin", plugin);
            
            return root.toString(2); // Pretty print with indent
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting manifest to JSON", e);
            return null;
        }
    }
    
    /**
     * Validate manifest data
     */
    public boolean isValid() {
        // Check required fields
        if (id == null || id.isEmpty()) return false;
        if (name == null || name.isEmpty()) return false;
        if (version == null || version.isEmpty()) return false;
        if (minAppVersion == null || minAppVersion.isEmpty()) return false;
        if (author == null || author.isEmpty()) return false;
        if (category == null || category.isEmpty()) return false;
        if (mainClass == null || mainClass.isEmpty()) return false;
        
        // Validate category
        if (!category.equals("I") && !category.equals("We") && !category.equals("All")) {
            return false;
        }
        
        // Validate version format (basic check)
        if (!version.matches("\\d+\\.\\d+\\.\\d+")) {
            return false;
        }
        
        // Validate plugin ID format (reverse domain style)
        if (!id.matches("[a-z0-9_]+(\\.[a-z0-9_]+)+")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if this plugin is compatible with the given app version
     */
    public boolean isCompatibleWith(String appVersion) {
        try {
            String[] minParts = minAppVersion.split("\\.");
            String[] appParts = appVersion.split("\\.");
            
            for (int i = 0; i < Math.min(minParts.length, appParts.length); i++) {
                int min = Integer.parseInt(minParts[i]);
                int app = Integer.parseInt(appParts[i]);
                
                if (app > min) return true;
                if (app < min) return false;
            }
            
            return true; // Equal versions
            
        } catch (Exception e) {
            Log.e(TAG, "Error comparing versions", e);
            return false;
        }
    }
    
    /**
     * Builder pattern for creating manifests
     */
    public static class Builder {
        private String id;
        private String name;
        private String version;
        private String minAppVersion;
        private String author;
        private String category;
        private String mainClass;
        private String description;
        private String website;
        private String email;
        private List<String> permissions = new ArrayList<>();
        private List<String> dependencies = new ArrayList<>();
        private String signature;
        private long buildTime;
        private boolean experimental;
        private int targetSdkVersion = 28; // Default to Android 9
        
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }
        
        public Builder setMinAppVersion(String minAppVersion) {
            this.minAppVersion = minAppVersion;
            return this;
        }
        
        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }
        
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }
        
        public Builder setMainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }
        
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder setWebsite(String website) {
            this.website = website;
            return this;
        }
        
        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder setPermissions(List<String> permissions) {
            this.permissions = new ArrayList<>(permissions);
            return this;
        }
        
        public Builder addPermission(String permission) {
            this.permissions.add(permission);
            return this;
        }
        
        public Builder setDependencies(List<String> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }
        
        public Builder addDependency(String dependency) {
            this.dependencies.add(dependency);
            return this;
        }
        
        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }
        
        public Builder setBuildTime(long buildTime) {
            this.buildTime = buildTime;
            return this;
        }
        
        public Builder setExperimental(boolean experimental) {
            this.experimental = experimental;
            return this;
        }
        
        public Builder setTargetSdkVersion(int targetSdkVersion) {
            this.targetSdkVersion = targetSdkVersion;
            return this;
        }
        
        public PluginManifest build() {
            // Validate required fields
            if (id == null || name == null || version == null || 
                minAppVersion == null || author == null || 
                category == null || mainClass == null) {
                throw new IllegalStateException("Missing required fields in PluginManifest");
            }
            
            return new PluginManifest(this);
        }
    }
    
    @Override
    public String toString() {
        return "PluginManifest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", category='" + category + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
