package com.core.talita;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Encryption Service - Encrypts ALL data types in Talita
 *
 * Features:
 * - Transparent encryption for all TalitaDataType objects
 * - File encryption for audio/media files
 * - JSON metadata encryption
 * - Secure file handling (no plaintext temp files)
 */
public class EncryptionService {

    private static final String TAG = "EncryptionService";
    private static final String ENCRYPTED_FILE_EXTENSION = ".enc";
    private static final String METADATA_ENCRYPTION_KEY = "encrypted_data";

    private final Context context;
    private final SecureKeyManager keyManager;

    public EncryptionService(Context context) {
        this.context = context;
        this.keyManager = new SecureKeyManager(context);

        // Verify encryption is working
        if (!keyManager.isEncryptionAvailable()) {
            throw new RuntimeException("Hardware encryption not available");
        }

        Log.d(TAG, "✅ Encryption service initialized");
        Log.d(TAG, keyManager.getEncryptionInfo());
    }

    /**
     * Encrypt TalitaDataType object (metadata)
     * Returns encrypted JSON with original structure preserved
     */
    public String encryptDataType(TalitaDataType data) {
        try {
            // Get the original JSON
            String originalJson = data.toJson();

            // Encrypt the JSON content
            byte[] encryptedBytes = keyManager.encryptString(originalJson);
            String encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

            // Create wrapper JSON with metadata
            JSONObject wrapper = new JSONObject();
            wrapper.put("type", data.getType());
            wrapper.put("id", data.getId());
            wrapper.put("timestamp", data.getTimestamp());
            wrapper.put("encrypted", true);
            wrapper.put(METADATA_ENCRYPTION_KEY, encryptedBase64);
            wrapper.put("display_name", data.getDisplayName()); // Keep for UI

            Log.d(TAG, "🔒 Encrypted " + data.getType() + " metadata (" + encryptedBytes.length + " bytes)");
            return wrapper.toString();

        } catch (JSONException e) {
            Log.e(TAG, "❌ Failed to encrypt data type: " + e.getMessage());
            throw new RuntimeException("Data encryption failed", e);
        }
    }

    /**
     * Decrypt TalitaDataType JSON back to original format
     */
    public String decryptDataTypeJson(String encryptedJson) {
        try {
            JSONObject wrapper = new JSONObject(encryptedJson);

            // Check if this is encrypted data
            if (!wrapper.optBoolean("encrypted", false)) {
                Log.d(TAG, "📄 Data is not encrypted, returning as-is");
                return encryptedJson; // Not encrypted, return original
            }

            // Decrypt the content
            String encryptedBase64 = wrapper.getString(METADATA_ENCRYPTION_KEY);
            byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT);
            String decryptedJson = keyManager.decryptToString(encryptedBytes);

            Log.d(TAG, "🔓 Decrypted " + wrapper.optString("type") + " metadata");
            return decryptedJson;

        } catch (JSONException e) {
            Log.e(TAG, "❌ Failed to decrypt data type: " + e.getMessage());
            throw new RuntimeException("Data decryption failed", e);
        }
    }

    /**
     * Encrypt file in place (for audio files, photos, etc.)
     * Original file is securely deleted
     */
    public String encryptFile(String originalFilePath) {
        if (originalFilePath == null || originalFilePath.isEmpty()) {
            return originalFilePath;
        }

        File originalFile = new File(originalFilePath);
        if (!originalFile.exists()) {
            Log.w(TAG, "⚠️ File not found for encryption: " + originalFilePath);
            return originalFilePath;
        }

        String encryptedFilePath = originalFilePath + ENCRYPTED_FILE_EXTENSION;
        File encryptedFile = new File(encryptedFilePath);

        try {
            Log.d(TAG, "🔒 Encrypting file: " + originalFile.getName() + " (" + originalFile.length() + " bytes)");

            // Read original file
            byte[] fileData = readFileSecurely(originalFile);

            // Encrypt the data
            byte[] encryptedData = keyManager.encrypt(fileData);

            // Write encrypted file
            writeFileSecurely(encryptedFile, encryptedData);

            // Securely delete original file
            secureDeleteFile(originalFile);

            Log.d(TAG, "✅ File encrypted: " + encryptedFile.getName() + " (" + encryptedFile.length() + " bytes)");
            return encryptedFilePath;

        } catch (Exception e) {
            Log.e(TAG, "❌ File encryption failed: " + e.getMessage());

            // Clean up on failure
            if (encryptedFile.exists()) {
                encryptedFile.delete();
            }

            throw new RuntimeException("File encryption failed", e);
        }
    }

    /**
     * Decrypt file (for reading/playback)
     * Returns path to temporary decrypted file
     */
    public String decryptFileToTemp(String encryptedFilePath) {
        if (encryptedFilePath == null || !encryptedFilePath.endsWith(ENCRYPTED_FILE_EXTENSION)) {
            Log.d(TAG, "📄 File is not encrypted: " + encryptedFilePath);
            return encryptedFilePath; // Not encrypted
        }

        File encryptedFile = new File(encryptedFilePath);
        if (!encryptedFile.exists()) {
            Log.w(TAG, "⚠️ Encrypted file not found: " + encryptedFilePath);
            return encryptedFilePath;
        }

        // Create temp file for decrypted content
        String tempFilePath = encryptedFilePath.replace(ENCRYPTED_FILE_EXTENSION, ".temp");
        File tempFile = new File(tempFilePath);

        try {
            Log.d(TAG, "🔓 Decrypting file: " + encryptedFile.getName());

            // Read encrypted file
            byte[] encryptedData = readFileSecurely(encryptedFile);

            // Decrypt the data
            byte[] decryptedData = keyManager.decrypt(encryptedData);

            // Write temp file
            writeFileSecurely(tempFile, decryptedData);

            // Clear sensitive data
            Arrays.fill(decryptedData, (byte) 0);

            Log.d(TAG, "✅ File decrypted to temp: " + tempFile.getName());
            return tempFilePath;

        } catch (Exception e) {
            Log.e(TAG, "❌ File decryption failed: " + e.getMessage());

            // Clean up on failure
            if (tempFile.exists()) {
                tempFile.delete();
            }

            throw new RuntimeException("File decryption failed", e);
        }
    }

    /**
     * Clean up temporary decrypted files
     */
    public void cleanupTempFile(String tempFilePath) {
        if (tempFilePath != null && tempFilePath.endsWith(".temp")) {
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                secureDeleteFile(tempFile);
                Log.d(TAG, "🧹 Cleaned up temp file: " + tempFile.getName());
            }
        }
    }

    /**
     * Read file securely into memory
     */
    private byte[] readFileSecurely(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int bytesRead = fis.read(data);

            if (bytesRead != data.length) {
                throw new IOException("Failed to read complete file");
            }

            return data;
        }
    }

    /**
     * Write file securely
     */
    private void writeFileSecurely(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
            fos.getFD().sync(); // Force write to disk
        }
    }

    /**
     * Securely delete file (overwrite then delete)
     */
    private void secureDeleteFile(File file) {
        try {
            if (file.exists()) {
                // Overwrite with random data
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] randomData = new byte[(int) file.length()];
                    new java.security.SecureRandom().nextBytes(randomData);
                    fos.write(randomData);
                    fos.flush();
                    fos.getFD().sync();
                }

                // Delete the file
                boolean deleted = file.delete();
                Log.d(TAG, deleted ? "🗑️ Securely deleted: " + file.getName() : "⚠️ Failed to delete: " + file.getName());
            }
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Secure delete failed: " + e.getMessage());
            // Still try normal delete
            file.delete();
        }
    }

    /**
     * Check if a file is encrypted
     */
    public boolean isFileEncrypted(String filePath) {
        return filePath != null && filePath.endsWith(ENCRYPTED_FILE_EXTENSION);
    }

    /**
     * Get encryption status info
     */
    public String getEncryptionStatus() {
        return keyManager.getEncryptionInfo();
    }
}