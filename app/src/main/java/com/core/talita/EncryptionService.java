package com.core.talita;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * EncryptionService - Handles all encryption/decryption operations
 * Uses Android Hardware Security Module for unextractable keys
 */
public class EncryptionService {
    private static final String TAG = "EncryptionService";
    private static final String KEYSTORE_ALIAS = "TalitaMainKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    
    private final Context context;
    private SecretKey secretKey;
    
    public EncryptionService(Context context) {
        this.context = context;
        initializeEncryption();
    }
    
    /**
     * Initialize encryption with hardware-backed key
     */
    private void initializeEncryption() {
        try {
            secretKey = getOrCreateKey();
            Log.d(TAG, "✅ Encryption initialized with hardware-backed key");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize encryption", e);
        }
    }
    
    /**
     * Get or create hardware-backed encryption key
     */
    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        // Check if key exists
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            KeyStore.SecretKeyEntry secretKeyEntry = 
                (KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null);
            return secretKeyEntry.getSecretKey();
        }
        
        // Generate new key
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            
        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build();
            
        keyGenerator.init(keySpec);
        return keyGenerator.generateKey();
    }
    
    /**
     * Encrypt a file and return encrypted file path
     */
    public String encryptFile(String inputFilePath) {
        if (inputFilePath == null || inputFilePath.isEmpty()) {
            return null;
        }
        
        try {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                Log.e(TAG, "Input file does not exist: " + inputFilePath);
                return null;
            }
            
            // Create encrypted file path
            String encryptedFileName = UUID.randomUUID().toString() + ".enc";
            File encryptedDir = new File(context.getFilesDir(), "encrypted");
            encryptedDir.mkdirs();
            File encryptedFile = new File(encryptedDir, encryptedFileName);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            
            // Read and encrypt file
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(encryptedFile);
            
            // Write IV first
            fos.write(iv.length);
            fos.write(iv);
            
            // Encrypt file content
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    fos.write(encrypted);
                }
            }
            
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                fos.write(finalBlock);
            }
            
            fis.close();
            fos.close();
            
            Log.d(TAG, "✅ File encrypted: " + encryptedFile.getAbsolutePath());
            return encryptedFile.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ File encryption failed", e);
            return null;
        }
    }
    
    /**
     * Decrypt file to temporary location for playback
     */
    public String decryptFileToTemp(String encryptedFilePath) {
        if (encryptedFilePath == null || encryptedFilePath.isEmpty()) {
            return null;
        }
        
        try {
            File encryptedFile = new File(encryptedFilePath);
            if (!encryptedFile.exists()) {
                Log.e(TAG, "Encrypted file does not exist: " + encryptedFilePath);
                return null;
            }
            
            // Create temp file
            File tempDir = new File(context.getCacheDir(), "temp_decrypt");
            tempDir.mkdirs();
            File tempFile = new File(tempDir, UUID.randomUUID().toString() + ".temp");
            
            // Read encrypted file
            FileInputStream fis = new FileInputStream(encryptedFile);
            
            // Read IV
            int ivLength = fis.read();
            byte[] iv = new byte[ivLength];
            fis.read(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            // Decrypt to temp file
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decrypted = cipher.update(buffer, 0, bytesRead);
                if (decrypted != null) {
                    fos.write(decrypted);
                }
            }
            
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                fos.write(finalBlock);
            }
            
            fis.close();
            fos.close();
            
            Log.d(TAG, "✅ File decrypted to temp: " + tempFile.getAbsolutePath());
            return tempFile.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ File decryption failed", e);
            return null;
        }
    }
    
    /**
     * Encrypt string data
     */
    public String encryptData(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            String ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT);
            String encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            
            return ivBase64 + ":" + encryptedBase64;
            
        } catch (Exception e) {
            throw new RuntimeException("Data encryption failed", e);
        }
    }
    
    /**
     * Decrypt encrypted data string
     */
    public String decryptData(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return null;
            }
            
            // Extract IV and encrypted content
            String[] parts = encryptedData.split(":");
            if (parts.length != 2) {
                Log.e(TAG, "Invalid encrypted data format");
                return null;
            }
            
            byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
            byte[] encryptedBytes = Base64.decode(parts[1], Base64.DEFAULT);
            
            // Get the key
            SecretKey key = getOrCreateKey();
            
            // Decrypt
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }

    /**
     * Clean up temporary decrypted files
     */
    public void cleanupTempFile(String tempFilePath) {
        if (tempFilePath != null && tempFilePath.endsWith(".temp")) {
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                tempFile.delete();
                Log.d(TAG, "🧹 Cleaned up temp file: " + tempFilePath);
            }
        }
    }
    
    /**
     * Check if a file is encrypted
     */
    public boolean isFileEncrypted(String filePath) {
        return filePath != null && filePath.endsWith(".enc");
    }
    
    /**
     * Get encryption status
     */
    public String getEncryptionStatus() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                return "🔐 Hardware-backed encryption active";
            } else {
                return "⚠️ Encryption key not found";
            }
        } catch (Exception e) {
            return "❌ Encryption error: " + e.getMessage();
        }
    }
    
    /**
     * Encrypt JSON data from UniversalDataType
     */
    public String encryptDataTypeJson(String jsonData) {
        return encryptData(jsonData);
    }
}
