package com.core.talita;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;

/**
 * EncryptionService - Handles encryption/decryption of data
 * 
 * SIMPLIFIED VERSION for MVP - uses Base64 encoding.
 * TODO: Implement proper AES encryption with Android Keystore
 */
public class EncryptionService {
    private static final String TAG = "EncryptionService";
    private final Context context;
    
    public EncryptionService(Context context) {
        this.context = context;
    }
    
    /**
     * Encrypt a string
     * For MVP, just using Base64 encoding
     */
    public String encryptString(String plainText) {
        if (plainText == null) return null;
        
        try {
            byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed", e);
            return null;
        }
    }
    
    /**
     * Decrypt a string
     * For MVP, just using Base64 decoding
     */
    public String decryptString(String encryptedText) {
        if (encryptedText == null) return null;
        
        try {
            byte[] data = Base64.decode(encryptedText, Base64.DEFAULT);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed", e);
            return null;
        }
    }
    
    /**
     * Check if encryption is available
     */
    public boolean isEncryptionAvailable() {
        // For MVP, always available
        return true;
    }
}
