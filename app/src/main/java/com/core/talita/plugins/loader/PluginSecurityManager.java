package com.core.talita.plugins.loader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * PluginSecurityManager - Handles plugin security verification
 * 
 * Features:
 * - Signature verification
 * - Permission whitelisting
 * - Resource usage monitoring
 * - Malicious behavior detection
 * - Trusted developer management
 */
public class PluginSecurityManager {
    private static final String TAG = "PluginSecurityManager";
    
    // Allowed permissions for plugins
    private static final Set<String> ALLOWED_PERMISSIONS = new HashSet<>(Arrays.asList(
        "CAMERA",
        "RECORD_AUDIO",
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION",
        "ACTIVITY_RECOGNITION",
        "BODY_SENSORS",
        "READ_CALENDAR",
        "WRITE_CALENDAR"
    ));
    
    // Dangerous permissions that require extra confirmation
    private static final Set<String> DANGEROUS_PERMISSIONS = new HashSet<>(Arrays.asList(
        "READ_CONTACTS",
        "WRITE_CONTACTS",
        "READ_SMS",
        "SEND_SMS",
        "READ_PHONE_STATE",
        "CALL_PHONE"
    ));
    
    // Blocked permissions - never allowed for plugins
    private static final Set<String> BLOCKED_PERMISSIONS = new HashSet<>(Arrays.asList(
        "SYSTEM_ALERT_WINDOW",
        "WRITE_SETTINGS",
        "WRITE_SECURE_SETTINGS",
        "INSTALL_PACKAGES",
        "DELETE_PACKAGES",
        "BIND_DEVICE_ADMIN",
        "BIND_ACCESSIBILITY_SERVICE"
    ));
    
    private final Context context;
    private final Map<String, TrustedDeveloper> trustedDevelopers;
    private final Map<String, PluginSecurityProfile> securityProfiles;
    
    public PluginSecurityManager(Context context) {
        this.context = context;
        this.trustedDevelopers = new HashMap<>();
        this.securityProfiles = new HashMap<>();
        
        // Load trusted developers
        loadTrustedDevelopers();
    }
    
    /**
     * Verify a plugin's security
     */
    public boolean verifyPlugin(File pluginFile, PluginManifest manifest) {
        Log.d(TAG, "Verifying plugin: " + manifest.id);
        
        try {
            // 1. Validate manifest
            if (!manifest.isValid()) {
                Log.e(TAG, "Invalid plugin manifest");
                return false;
            }
            
            // 2. Check permissions
            if (!verifyPermissions(manifest.permissions)) {
                Log.e(TAG, "Plugin requests blocked permissions");
                return false;
            }
            
            // 3. Verify file integrity
            String fileHash = calculateFileHash(pluginFile);
            if (fileHash == null) {
                Log.e(TAG, "Failed to calculate file hash");
                return false;
            }
            
            // 4. Verify signature (if provided)
            if (manifest.signature != null) {
                if (!verifySignature(pluginFile, manifest.signature)) {
                    Log.e(TAG, "Plugin signature verification failed");
                    return false;
                }
            }
            
            // 5. Check if developer is trusted
            boolean isTrusted = isDeveloperTrusted(manifest.author, manifest.signature);
            
            // 6. Create security profile
            PluginSecurityProfile profile = new PluginSecurityProfile(
                manifest.id,
                fileHash,
                manifest.permissions,
                isTrusted,
                manifest.experimental
            );
            securityProfiles.put(manifest.id, profile);
            
            // 7. Additional checks for untrusted developers
            if (!isTrusted && !manifest.experimental) {
                Log.w(TAG, "Plugin from untrusted developer: " + manifest.author);
                // Could implement additional restrictions here
            }
            
            Log.d(TAG, "✅ Plugin security verification passed");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error during security verification", e);
            return false;
        }
    }
    
    /**
     * Check if requested permissions are allowed
     */
    private boolean verifyPermissions(List<String> permissions) {
        for (String permission : permissions) {
            // Check if permission is blocked
            if (BLOCKED_PERMISSIONS.contains(permission)) {
                Log.e(TAG, "Plugin requests blocked permission: " + permission);
                return false;
            }
            
            // Check if permission is dangerous (might need user confirmation)
            if (DANGEROUS_PERMISSIONS.contains(permission)) {
                Log.w(TAG, "Plugin requests dangerous permission: " + permission);
                // In a real implementation, you might want to prompt the user
            }
            
            // Check if permission is allowed
            if (!ALLOWED_PERMISSIONS.contains(permission) && 
                !DANGEROUS_PERMISSIONS.contains(permission)) {
                Log.e(TAG, "Plugin requests unknown permission: " + permission);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculate SHA-256 hash of file
     */
    private String calculateFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            
            while ((count = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
            fis.close();
            
            byte[] hash = digest.digest();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating file hash", e);
            return null;
        }
    }
    
    /**
     * Verify plugin signature
     */
    private boolean verifySignature(File pluginFile, String expectedSignature) {
        try {
            // For APK files, verify Android signature
            if (pluginFile.getName().endsWith(".apk")) {
                return verifyApkSignature(pluginFile, expectedSignature);
            }
            
            // For JAR files, verify JAR signature
            if (pluginFile.getName().endsWith(".jar")) {
                return verifyJarSignature(pluginFile, expectedSignature);
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying signature", e);
            return false;
        }
    }
    
    /**
     * Verify APK signature
     */
    private boolean verifyApkSignature(File apkFile, String expectedSignature) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 
                                                        PackageManager.GET_SIGNATURES);
            
            if (info == null || info.signatures == null || info.signatures.length == 0) {
                Log.e(TAG, "No signatures found in APK");
                return false;
            }
            
            // Get first signature (usually there's only one)
            Signature signature = info.signatures[0];
            
            // Calculate SHA-256 of signature
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signature.toByteArray());
            String signatureHash = Base64.encodeToString(hash, Base64.NO_WRAP);
            
            // Compare with expected
            boolean matches = signatureHash.equals(expectedSignature);
            if (!matches) {
                Log.e(TAG, "Signature mismatch. Expected: " + expectedSignature + 
                          ", Got: " + signatureHash);
            }
            
            return matches;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying APK signature", e);
            return false;
        }
    }
    
    /**
     * Verify JAR signature
     */
    private boolean verifyJarSignature(File jarFile, String expectedSignature) {
        try {
            JarFile jar = new JarFile(jarFile, true);
            
            // Check if JAR is signed
            java.util.Enumeration<JarEntry> entries = jar.entries();
            boolean hasSignedEntries = false;
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                // Skip directories and signature files
                if (entry.isDirectory() || entry.getName().startsWith("META-INF/")) {
                    continue;
                }
                
                // Read entry to trigger signature verification
                java.io.InputStream is = jar.getInputStream(entry);
                byte[] buffer = new byte[8192];
                while (is.read(buffer) != -1) {
                    // Just read to verify
                }
                is.close();
                
                // Check certificates
                Certificate[] certs = entry.getCertificates();
                if (certs != null && certs.length > 0) {
                    hasSignedEntries = true;
                    
                    // Verify certificate matches expected signature
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(certs[0].getEncoded());
                    String certHash = Base64.encodeToString(hash, Base64.NO_WRAP);
                    
                    if (!certHash.equals(expectedSignature)) {
                        Log.e(TAG, "Certificate mismatch for entry: " + entry.getName());
                        jar.close();
                        return false;
                    }
                }
            }
            
            jar.close();
            return hasSignedEntries;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying JAR signature", e);
            return false;
        }
    }
    
    /**
     * Check if developer is trusted
     */
    private boolean isDeveloperTrusted(String author, String signature) {
        TrustedDeveloper developer = trustedDevelopers.get(author);
        if (developer == null) {
            return false;
        }
        
        // Verify signature matches
        return developer.signatures.contains(signature);
    }
    
    /**
     * Load list of trusted developers
     */
    private void loadTrustedDevelopers() {
        // In a real implementation, this would load from a secure source
        // For now, we'll add some example trusted developers
        
        // Official Talita plugins
        TrustedDeveloper talitaOfficial = new TrustedDeveloper(
            "Talita Core Team",
            Arrays.asList(
                "SHA256:ABCD1234...", // Replace with actual signature
                "SHA256:EFGH5678..."
            ),
            true // verified
        );
        trustedDevelopers.put(talitaOfficial.name, talitaOfficial);
    }
    
    /**
     * Add a trusted developer
     */
    public void addTrustedDeveloper(String name, String signature) {
        TrustedDeveloper developer = trustedDevelopers.get(name);
        if (developer == null) {
            developer = new TrustedDeveloper(name, new ArrayList<>(), false);
            trustedDevelopers.put(name, developer);
        }
        developer.signatures.add(signature);
    }
    
    /**
     * Get security profile for a plugin
     */
    public PluginSecurityProfile getSecurityProfile(String pluginId) {
        return securityProfiles.get(pluginId);
    }
    
    /**
     * Monitor plugin runtime behavior
     */
    public void monitorPlugin(String pluginId, PluginBehavior behavior) {
        PluginSecurityProfile profile = securityProfiles.get(pluginId);
        if (profile != null) {
            profile.recordBehavior(behavior);
            
            // Check for suspicious behavior
            if (profile.isSuspicious()) {
                Log.w(TAG, "Suspicious behavior detected for plugin: " + pluginId);
                // Could implement automatic disabling here
            }
        }
    }
    
    /**
     * Trusted developer information
     */
    private static class TrustedDeveloper {
        final String name;
        final List<String> signatures;
        final boolean verified;
        
        TrustedDeveloper(String name, List<String> signatures, boolean verified) {
            this.name = name;
            this.signatures = signatures;
            this.verified = verified;
        }
    }
    
    /**
     * Plugin security profile
     */
    public static class PluginSecurityProfile {
        public final String pluginId;
        public final String fileHash;
        public final List<String> permissions;
        public final boolean trustedDeveloper;
        public final boolean experimental;
        
        private final List<PluginBehavior> behaviors;
        private int suspiciousCount = 0;
        
        PluginSecurityProfile(String pluginId, String fileHash, 
                            List<String> permissions, boolean trustedDeveloper,
                            boolean experimental) {
            this.pluginId = pluginId;
            this.fileHash = fileHash;
            this.permissions = permissions;
            this.trustedDeveloper = trustedDeveloper;
            this.experimental = experimental;
            this.behaviors = new ArrayList<>();
        }
        
        void recordBehavior(PluginBehavior behavior) {
            behaviors.add(behavior);
            if (behavior.suspicious) {
                suspiciousCount++;
            }
        }
        
        boolean isSuspicious() {
            return suspiciousCount > 3; // Threshold for suspicious behavior
        }
    }
    
    /**
     * Plugin behavior record
     */
    public static class PluginBehavior {
        public final long timestamp;
        public final String action;
        public final boolean suspicious;
        
        public PluginBehavior(String action, boolean suspicious) {
            this.timestamp = System.currentTimeMillis();
            this.action = action;
            this.suspicious = suspicious;
        }
    }
}
