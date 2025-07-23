package com.core.talita;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import com.core.talita.cloud.CloudBackupManager;
import java.util.*;

/**
 * Universal Data Service - handles ALL data types in Talita with ENCRYPTION and CLOUD BACKUP
 * Any data implementing UniversalDataType gets automatic:
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
    private final CloudBackupManager cloudBackupManager;

    public UniversalDataService(Context context) {
        this.context = context;
        this.dataManager = new LocalDataManager(context);
        this.encryptionService = new EncryptionService(context);
        this.cloudBackupManager = new CloudBackupManager(context);

        Log.d(TAG, "🔐☁️ Universal Data Service initialized with encryption and cloud backup");
        Log.d(TAG, encryptionService.getEncryptionStatus());
    }

    /**
     * Capture any data type - handles encryption and everything automatically
     */
    public String capture(UniversalDataType data) {
        try {
            Log.d(TAG, "🔒 Capturing and encrypting " + data.getType() + " data: " + data.getDisplayName());

            // 1. Encrypt file if it exists (for audio, photos, etc.)
            String encryptedFilePath = data.getFilePath();
            if (encryptedFilePath != null && !encryptedFilePath.isEmpty()) {
                encryptedFilePath = encryptionService.encryptFile(data.getFilePath());
                Log.d(TAG, "🔒 File encrypted: " + encryptedFilePath);
            }

            // 2. Create encrypted version of data with updated file path
            EncryptedDataWrapper encryptedData = new EncryptedDataWrapper(data, encryptedFilePath);

            // 3. Save to DATABASE (not files!)
            String dataId = dataManager.saveData(data);

            if (dataId != null) {
                // 4. Queue for cloud backup
                queueForCloudBackup(dataId, encryptedData);

                // 5. Handle sharing updates (future feature)
                updateActiveSharing(dataId, encryptedData);

                // 6. Show success
                showSuccessToast(data);
                
                return dataId;
            } else {
                showErrorToast(data);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to capture data", e);
            showErrorToast(data);
            return null;
        }
    }

    /**
     * Query data across time range (for activity views, reports, etc.)
     */
    public List<PersonalData> queryDataInRange(long startTime, long endTime) {
        List<PersonalData> result = new ArrayList<>();
        
        try {
            List<UniversalDataType> dataList = dataManager.queryDataByTimeRange(startTime, endTime);
            
            for (UniversalDataType data : dataList) {
                result.add(new PersonalDataAdapter(data));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting data in range", e);
        }
        
        return result;
    }

    /**
     * Get data in range (alias for queryDataInRange)
     */
    public List<PersonalData> getDataInRange(long startTime, long endTime) {
        return queryDataInRange(startTime, endTime);
    }

    /**
     * Get today's data
     */
    public List<PersonalData> getTodaysData() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        
        return getDataInRange(startOfDay, endOfDay);
    }

    /**
     * Queue for cloud backup
     */
    private void queueForCloudBackup(String dataId, EncryptedDataWrapper data) {
        Log.d(TAG, "☁️ Queuing encrypted " + data.originalData.getType() + " for cloud backup: " + dataId);
        cloudBackupManager.queueForBackup(data.originalData);
    }

    /**
     * Update any active sharing (future implementation)
     */
    private void updateActiveSharing(String dataId, EncryptedDataWrapper data) {
        Log.d(TAG, "🤝 Updating sharing for encrypted " + data.originalData.getType() + ": " + dataId);
        // TODO: Notify friends about new data when P2P sharing is ready
    }

    /**
     * Show success toast
     */
    private void showSuccessToast(UniversalDataType data) {
        String message = "🔒 " + data.getDisplayName() + " encrypted and saved";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show error toast
     */
    private void showErrorToast(UniversalDataType data) {
        String message = "❌ Failed to encrypt " + data.getDisplayName();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get decrypted data of a specific type
     */
    public List<DecryptedDataItem> getDecryptedDataByType(String type) {
        List<DecryptedDataItem> items = new ArrayList<>();

        try {
            List<DataItem> rawItems = dataManager.getDataByType(type);
            
            for (DataItem item : rawItems) {
                // Decrypt the JSON data
                String decryptedJson = encryptionService.decryptData(item.getDataJson());
                JSONObject jsonData = new JSONObject(decryptedJson);
                
                DecryptedDataItem decryptedItem = new DecryptedDataItem(
                    item.getId(),
                    item.getType(),
                    item.getFilePath(),
                    item.getCreatedAt(),
                    jsonData,
                    encryptionService
                );
                
                items.add(decryptedItem);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting decrypted data", e);
        }

        return items;
    }

    /**
     * Get cloud backup status
     */
    public boolean isCloudBackupEnabled() {
        return cloudBackupManager.isEnabled();
    }

    public void setAutoBackupEnabled(boolean enabled) {
        cloudBackupManager.setAutoBackupEnabled(enabled);
        Log.d(TAG, enabled ? 
            "🔄 Auto cloud backup enabled" : "⏸️ Auto cloud backup disabled");
    }

    /**
     * Get cloud backup manager
     */
    public CloudBackupManager getCloudBackupManager() {
        return cloudBackupManager;
    }

    /**
     * Get data by type (for backwards compatibility)
     */
    public List<PersonalData> getDataByType(String type) {
        List<PersonalData> results = new ArrayList<>();
        List<DecryptedDataItem> items = getDecryptedDataByType(type);
        
        for (DecryptedDataItem item : items) {
            try {
                Map<String, Object> dataMap = new HashMap<>();
                Iterator<String> keys = item.decryptedData.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataMap.put(key, item.decryptedData.get(key));
                }
                
                UniversalPersonalData data = new UniversalPersonalData(type, dataMap);
                results.add(data);
            } catch (Exception e) {
                Log.e(TAG, "Error converting data", e);
            }
        }
        
        return results;
    }

    /**
     * Wrapper class for encrypted data
     */
    private static class EncryptedDataWrapper {
        public final UniversalDataType originalData;
        public final String encryptedFilePath;

        public EncryptedDataWrapper(UniversalDataType originalData, String encryptedFilePath) {
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
}
