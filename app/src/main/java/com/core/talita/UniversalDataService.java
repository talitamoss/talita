package com.core.talita;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Universal Data Service - handles ALL data types in Talita with ENCRYPTION
 * Any data implementing TalitaDataType gets automatic:
 * - Hardware-backed encryption
 * - Database storage
 * - Cloud backup queuing
 * - Sharing capabilities
 * - Consistent error handling
 */
public class UniversalDataService {

    private static final String TAG = "UniversalDataService";

    private final Context context;
    private final LocalDataManager dataManager;
    private final EncryptionService encryptionService;

    public UniversalDataService(Context context) {
        this.context = context;
        this.dataManager = new LocalDataManager(context);
        this.encryptionService = new EncryptionService(context);

        Log.d(TAG, "🔐 Universal Data Service initialized with encryption");
        Log.d(TAG, encryptionService.getEncryptionStatus());
    }

    /**
     * Capture any data type - handles encryption and everything automatically
     */
    public String capture(TalitaDataType data) {
        try {
            Log.d(TAG, "🔒 Capturing and encrypting " + data.getType() + " data: " + data.getDisplayName());

            // 1. Encrypt file if it exists
            String encryptedFilePath = data.getFilePath();
            if (encryptedFilePath != null && !encryptedFilePath.isEmpty()) {
                encryptedFilePath = encryptionService.encryptFile(data.getFilePath());
                Log.d(TAG, "🔒 File encrypted: " + encryptedFilePath);
            }

            // 2. Create encrypted version of data with updated file path
            EncryptedDataWrapper encryptedData = new EncryptedDataWrapper(data, encryptedFilePath);

            // 3. Save encrypted data to database
            String dataId = saveToDatabase(encryptedData);

            if (dataId != null) {
                // 4. Queue for cloud backup (future feature)
                queueForCloudBackup(dataId, encryptedData);

                // 5. Handle sharing updates (future feature)
                updateActiveSharing(dataId, encryptedData);

                // 6. Success feedback
                showSuccessToast(data);

                Log.d(TAG, "✅ Successfully captured encrypted " + data.getType() + " with ID: " + dataId);
                return dataId;

            } else {
                Log.e(TAG, "❌ Failed to save encrypted " + data.getType() + " to database");
                showErrorToast(data);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error capturing " + data.getType() + ": " + e.getMessage());
            e.printStackTrace();
            showErrorToast(data);
            return null;
        }
    }

    /**
     * Save encrypted data to local database
     */
    private String saveToDatabase(EncryptedDataWrapper encryptedData) {
        try {
            // Encrypt the JSON metadata
            String encryptedJson = encryptionService.encryptDataType(encryptedData.originalData);

            // Use existing LocalDataManager but with encrypted data
            if (encryptedData.originalData.getType().equals("location")) {
                return dataManager.saveLocationPoint(
                        encryptedData.originalData.getLatitude(),
                        encryptedData.originalData.getLongitude(),
                        0.0, // accuracy placeholder
                        "encrypted_universal_service"
                );
            } else {
                // For non-location data, use generic save method with encryption
                return saveGenericEncryptedData(encryptedData, encryptedJson);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving encrypted data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generic save method for encrypted non-location data
     */
    private String saveGenericEncryptedData(EncryptedDataWrapper encryptedData, String encryptedJson) {
        try {
            Log.d(TAG, "💾 Saving encrypted " + encryptedData.originalData.getType() + " data with ID: " + encryptedData.originalData.getId());

            // Create encrypted data entry
            JSONObject dataEntry = new JSONObject();
            dataEntry.put("id", encryptedData.originalData.getId());
            dataEntry.put("type", encryptedData.originalData.getType());
            dataEntry.put("filePath", encryptedData.encryptedFilePath); // Store encrypted file path
            dataEntry.put("timestamp", encryptedData.originalData.getTimestamp());
            dataEntry.put("encrypted", true);
            dataEntry.put("data", encryptedJson); // This is the encrypted JSON

            // Save to encrypted data file
            String filename = encryptedData.originalData.getType() + "_encrypted_data.txt";
            java.io.FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
            fos.write((dataEntry.toString() + "\n").getBytes());
            fos.close();

            Log.d(TAG, "✅ Saved encrypted " + encryptedData.originalData.getType() + " to " + filename);
            return encryptedData.originalData.getId();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving encrypted generic data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get decrypted data of a specific type
     */
    public java.util.List<DecryptedDataItem> getDecryptedDataByType(String type) {
        java.util.List<DecryptedDataItem> items = new java.util.ArrayList<>();

        try {
            String filename = type + "_encrypted_data.txt";
            java.io.FileInputStream fis = context.openFileInput(filename);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    JSONObject dataEntry = new JSONObject(line);
                    String entryType = dataEntry.optString("type", "");

                    if (type.equals(entryType)) {
                        // Decrypt the data
                        String encryptedJson = dataEntry.optString("data", "");
                        String decryptedJson = encryptionService.decryptDataTypeJson(encryptedJson);

                        // Create decrypted item
                        DecryptedDataItem item = new DecryptedDataItem(
                                dataEntry.optString("id", ""),
                                entryType,
                                dataEntry.optString("filePath", ""), // This is encrypted file path
                                dataEntry.optLong("timestamp", 0),
                                new JSONObject(decryptedJson),
                                encryptionService
                        );
                        items.add(item);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "❌ Error parsing encrypted data entry: " + e.getMessage());
                }
            }
            reader.close();

            Log.d(TAG, "🔓 Loaded and decrypted " + items.size() + " " + type + " items");

        } catch (java.io.IOException e) {
            Log.d(TAG, "📄 No encrypted " + type + " data file found, trying fallback");

            // Fallback to old unencrypted data
            return getUnencryptedFallbackData(type);
        }

        return items;
    }

    /**
     * Fallback to load old unencrypted data
     */
    private java.util.List<DecryptedDataItem> getUnencryptedFallbackData(String type) {
        java.util.List<DecryptedDataItem> items = new java.util.ArrayList<>();

        try {
            // Try old generic data format first
            java.util.List<GenericDataItem> oldItems = getGenericDataByType(type);

            for (GenericDataItem oldItem : oldItems) {
                DecryptedDataItem item = new DecryptedDataItem(
                        oldItem.id,
                        oldItem.type,
                        oldItem.filePath,
                        oldItem.timestamp,
                        oldItem.data,
                        encryptionService
                );
                items.add(item);
            }

            Log.d(TAG, "📄 Loaded " + items.size() + " unencrypted " + type + " items as fallback");

        } catch (Exception e) {
            Log.e(TAG, "❌ Fallback data loading failed: " + e.getMessage());
        }

        return items;
    }

    /**
     * Legacy method for backward compatibility
     */
    public java.util.List<GenericDataItem> getGenericDataByType(String type) {
        java.util.List<GenericDataItem> items = new java.util.ArrayList<>();

        try {
            String filename = type + "_data.txt";
            java.io.FileInputStream fis = context.openFileInput(filename);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    JSONObject dataEntry = new JSONObject(line);
                    String entryType = dataEntry.optString("type", "");

                    if (type.equals(entryType)) {
                        GenericDataItem item = new GenericDataItem(
                                dataEntry.optString("id", ""),
                                entryType,
                                dataEntry.optString("filePath", ""),
                                dataEntry.optLong("timestamp", 0),
                                dataEntry.optJSONObject("data")
                        );
                        items.add(item);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data entry: " + e.getMessage());
                }
            }
            reader.close();

        } catch (java.io.IOException e) {
            Log.d(TAG, "No " + type + " data file found");
        }

        return items;
    }

    /**
     * Queue for cloud backup (future implementation)
     */
    private void queueForCloudBackup(String dataId, EncryptedDataWrapper data) {
        Log.d(TAG, "☁️ Queuing encrypted " + data.originalData.getType() + " for cloud backup: " + dataId);
        // TODO: Add to backup queue when cloud service is ready
        // Data is already encrypted, so cloud backup will be secure
    }

    /**
     * Update any active sharing (future implementation)
     */
    private void updateActiveSharing(String dataId, EncryptedDataWrapper data) {
        Log.d(TAG, "🤝 Updating sharing for encrypted " + data.originalData.getType() + ": " + dataId);
        // TODO: Notify friends about new data when P2P sharing is ready
        // Data is already encrypted, so sharing will be secure
    }

    /**
     * Show success toast
     */
    private void showSuccessToast(TalitaDataType data) {
        String message = "🔒 " + data.getDisplayName() + " encrypted and saved";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show error toast
     */
    private void showErrorToast(TalitaDataType data) {
        String message = "❌ Failed to encrypt " + data.getDisplayName();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get all data of a specific type (legacy method)
     */
    public java.util.List<LocalDataManager.DataItem> getDataByType(String type) {
        return dataManager.getItemsByType(type);
    }

    /**
     * Wrapper class for encrypted data
     */
    private static class EncryptedDataWrapper {
        public final TalitaDataType originalData;
        public final String encryptedFilePath;

        public EncryptedDataWrapper(TalitaDataType originalData, String encryptedFilePath) {
            this.originalData = originalData;
            this.encryptedFilePath = encryptedFilePath;
        }
    }

    /**
     * Helper class for decrypted data items
     */
    public static class DecryptedDataItem {
        public final String id;
        public final String type;
        public final String encryptedFilePath;
        public final long timestamp;
        public final JSONObject decryptedData;
        private final EncryptionService encryptionService;

        public DecryptedDataItem(String id, String type, String encryptedFilePath,
                                 long timestamp, JSONObject decryptedData, EncryptionService encryptionService) {
            this.id = id;
            this.type = type;
            this.encryptedFilePath = encryptedFilePath;
            this.timestamp = timestamp;
            this.decryptedData = decryptedData != null ? decryptedData : new JSONObject();
            this.encryptionService = encryptionService;
        }

        /**
         * Get temporary decrypted file for reading/playback
         * IMPORTANT: Must call cleanupTempFile() when done!
         */
        public String getTempDecryptedFilePath() {
            if (encryptedFilePath == null || encryptedFilePath.isEmpty()) {
                return null;
            }
            return encryptionService.decryptFileToTemp(encryptedFilePath);
        }

        /**
         * Clean up temporary decrypted file
         */
        public void cleanupTempFile(String tempFilePath) {
            encryptionService.cleanupTempFile(tempFilePath);
        }

        /**
         * Check if file is encrypted
         */
        public boolean isFileEncrypted() {
            return encryptionService.isFileEncrypted(encryptedFilePath);
        }
    }

    /**
     * Helper class for generic data items (backward compatibility)
     */
    public static class GenericDataItem {
        public final String id;
        public final String type;
        public final String filePath;
        public final long timestamp;
        public final JSONObject data;

        public GenericDataItem(String id, String type, String filePath, long timestamp, JSONObject data) {
            this.id = id;
            this.type = type;
            this.filePath = filePath;
            this.timestamp = timestamp;
            this.data = data != null ? data : new JSONObject();
        }
    }
}