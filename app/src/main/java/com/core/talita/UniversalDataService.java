package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.cloud.CloudBackupManager;
import org.json.JSONObject;
import java.util.*;

/**
 * UniversalDataService - Central orchestrator for all data operations
 * 
 * Handles the complete data pipeline:
 * 1. Receive data from collectors
 * 2. Encrypt using hardware-backed keys
 * 3. Store locally in SQLite
 * 4. Queue for cloud backup
 * 
 * Fixed to use proper singleton pattern with getInstance()
 */
public class UniversalDataService {
    private static final String TAG = "UniversalDataService";
    private static UniversalDataService instance;
    
    private final Context context;
    private final EncryptionService encryptionService;
    private final LocalDataManager localDataManager;
    private final CloudBackupManager cloudBackupManager;
    
    /**
     * Private constructor for singleton
     */
    private UniversalDataService(Context context) {
        this.context = context.getApplicationContext();
        this.encryptionService = new EncryptionService(context);
        this.localDataManager = new LocalDataManager(context);
        this.cloudBackupManager = CloudBackupManager.getInstance(context);
        
        Log.d(TAG, "🚀 Universal Data Service initialized");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized UniversalDataService getInstance(Context context) {
        if (instance == null) {
            instance = new UniversalDataService(context);
        }
        return instance;
    }
    
    /**
     * Main entry point - process any personal data through the pipeline
     */
    public String capture(PersonalDataInterface data) {
        try {
            Log.d(TAG, "📥 Capturing data: " + data.getType());
            
            // Step 1: Convert to universal format
            UniversalPersonalData universalData = convertToUniversal(data);
            
            // Step 2: Validate
            if (!validateData(universalData)) {
                Log.e(TAG, "❌ Data validation failed");
                return null;
            }
            
            // Step 3: Serialize to JSON
            String jsonData = universalData.toJson();
            
            // Step 4: Encrypt
            String encryptedJson = encryptionService.encryptData(jsonData);
            
            // Step 5: Store locally
            String dataId = generateDataId();
            boolean saved = localDataManager.saveEncryptedData(
                dataId,
                data.getType(),
                encryptedJson,
                universalData.getFilePath()
            );
            
            if (!saved) {
                Log.e(TAG, "❌ Failed to save data locally");
                return null;
            }
            
            // Step 6: Queue for cloud backup
            if (cloudBackupManager.isEnabled()) {
                // Create a simple wrapper for the backup
                UniversalDataType backupData = new UniversalDataType() {
                    @Override
                    public String getType() { return data.getType(); }
                    
                    @Override
                    public String getId() { return dataId; }
                    
                    @Override
                    public long getTimestamp() { return universalData.getTimestamp(); }
                    
                    @Override
                    public Map<String, Object> getMetadata() { return universalData.getMetadata(); }
                    
                    @Override
                    public String getDisplayName() { return data.getType(); }
                    
                    @Override
                    public String getDisplaySummary() { return "Data entry"; }
                    
                    @Override
                    public double getLatitude() { return 0.0; }
                    
                    @Override
                    public double getLongitude() { return 0.0; }
                };
                cloudBackupManager.queueForBackup(backupData);
            }
            
            Log.d(TAG, "✅ Data captured successfully: " + dataId);
            return dataId;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to capture data", e);
            return null;
        }
    }
    
    /**
     * Alternative capture method that accepts UniversalDataType directly
     */
    public String captureData(UniversalDataType data) {
        try {
            Log.d(TAG, "📥 Capturing data: " + data.getType());
            
            // Create a PersonalDataInterface wrapper
            PersonalDataInterface wrapper = new PersonalDataInterface() {
                @Override
                public String getType() { return data.getType(); }
                
                @Override
                public Map<String, Object> getData() {
                    Map<String, Object> result = new HashMap<>();
                    result.putAll(data.getMetadata());
                    return result;
                }
                
                @Override
                public Map<String, Object> getMetadata() { return data.getMetadata(); }
                
                @Override
                public long getTimestamp() { return data.getTimestamp(); }
            };
            
            return capture(wrapper);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to capture data", e);
            return null;
        }
    }
    
    /**
     * Process data - alias for saveData for compatibility
     */
    public void processData(PersonalData data) {
        saveData(data);
    }
    
    /**
     * Save PersonalData directly
     */
    public void saveData(PersonalData data) {
        if (data == null) {
            Log.e(TAG, "Cannot save null data");
            return;
        }
        
        // Convert PersonalData to PersonalDataInterface and capture it
        PersonalDataInterface pdi = new PersonalDataInterface() {
            @Override
            public String getType() {
                return data.getType();
            }
            
            @Override
            public Map<String, Object> getData() {
                return data.getData();
            }
            
            @Override
            public Map<String, Object> getMetadata() {
                return data.getMetadata();
            }
            
            @Override
            public long getTimestamp() {
                return data.getTimestamp();
            }
        };
        
        capture(pdi);
    }
    
    /**
     * Convert any PersonalDataInterface to UniversalPersonalData
     */
    private UniversalPersonalData convertToUniversal(PersonalDataInterface data) {
        UniversalPersonalData universal = new UniversalPersonalData();
        universal.setType(data.getType());
        universal.setTimestamp(data.getTimestamp());
        universal.setData(data.getData());
        universal.setMetadata(data.getMetadata());
        
        // Extract file path if available
        if (data instanceof UniversalDataType) {
            UniversalDataType udt = (UniversalDataType) data;
            if (udt instanceof LocationData) {
                LocationData ld = (LocationData) udt;
                universal.setLatitude(ld.getLatitude());
                universal.setLongitude(ld.getLongitude());
            }
        }
        
        return universal;
    }
    
    /**
     * Validate data before processing
     */
    private boolean validateData(UniversalPersonalData data) {
        if (data == null) return false;
        if (data.getType() == null || data.getType().isEmpty()) return false;
        if (data.getTimestamp() <= 0) return false;
        return true;
    }
    
    /**
     * Generate unique data ID
     */
    private String generateDataId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Get all data (decrypted)
     */
    public List<PersonalData> getAllData() {
        List<PersonalData> result = new ArrayList<>();
        List<DecryptedDataItem> items = localDataManager.getAllDecryptedData(encryptionService);
        
        for (DecryptedDataItem item : items) {
            try {
                String decryptedJson = encryptionService.decryptData(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData pd = PersonalData.fromJson(decryptedJson);
                    if (pd != null) {
                        result.add(pd);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decrypting item", e);
            }
        }
        
        return result;
    }
    
    /**
     * Get data by type (decrypted)
     */
    public List<PersonalData> getDataByType(String type) {
        List<PersonalData> result = new ArrayList<>();
        List<DecryptedDataItem> items = localDataManager.getDataByType(type, encryptionService);
        
        for (DecryptedDataItem item : items) {
            try {
                String decryptedJson = encryptionService.decryptData(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData pd = PersonalData.fromJson(decryptedJson);
                    if (pd != null) {
                        result.add(pd);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decrypting item", e);
            }
        }
        
        return result;
    }
    
    /**
     * Get data for time range (decrypted)
     */
    public List<PersonalData> getDataForTimeRange(long startTime, long endTime) {
        List<PersonalData> result = new ArrayList<>();
        List<DecryptedDataItem> items = localDataManager.getDataForTimeRange(startTime, endTime, encryptionService);
        
        for (DecryptedDataItem item : items) {
            try {
                String decryptedJson = encryptionService.decryptData(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData pd = PersonalData.fromJson(decryptedJson);
                    if (pd != null) {
                        result.add(pd);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decrypting item", e);
            }
        }
        
        return result;
    }
    
    /**
     * Get data in range - alias for getDataForTimeRange
     */
    public List<PersonalData> getDataInRange(long startTime, long endTime) {
        return getDataForTimeRange(startTime, endTime);
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
        long endOfDay = System.currentTimeMillis();
        
        return getDataInRange(startOfDay, endOfDay);
    }
    
    /**
     * Get decrypted data by type
     * Returns the raw DecryptedDataItem objects (still encrypted)
     */
    public List<DecryptedDataItem> getDecryptedDataByType(String type) {
        return localDataManager.getDataByType(type, encryptionService);
    }
    
    /**
     * Delete data item
     */
    public boolean deleteData(String dataId) {
        try {
            // Delete from local storage
            boolean deleted = localDataManager.deleteData(dataId);
            
            if (deleted) {
                // Remove from backup queue if present
                cloudBackupManager.removeFromQueue(dataId);
                Log.d(TAG, "🗑️ Deleted data: " + dataId);
            }
            
            return deleted;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting data", e);
            return false;
        }
    }
    
    /**
     * Get data count by type
     */
    public Map<String, Integer> getDataCountByType() {
        return localDataManager.getDataCountByType();
    }
    
    /**
     * Get total data count
     */
    public int getTotalDataCount() {
        return localDataManager.getTotalDataCount();
    }
    
    /**
     * Get database size in bytes
     */
    public long getDatabaseSize() {
        return localDataManager.getDatabaseSize();
    }
    
    /**
     * Export all data to JSON
     */
    public String exportAllDataToJson() {
        try {
            List<PersonalData> allData = getAllData();
            JSONObject export = new JSONObject();
            export.put("export_date", System.currentTimeMillis());
            export.put("data_count", allData.size());
            
            // Convert data to JSON array
            List<JSONObject> dataArray = new ArrayList<>();
            for (PersonalData pd : allData) {
                dataArray.add(new JSONObject(pd.toJson()));
            }
            export.put("data", dataArray);
            
            return export.toString(2); // Pretty print with 2-space indent
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting data", e);
            return null;
        }
    }
    
    /**
     * Clear all data (use with caution!)
     */
    public boolean clearAllData() {
        try {
            boolean cleared = localDataManager.clearAllData();
            if (cleared) {
                cloudBackupManager.clearQueue();
                Log.w(TAG, "⚠️ All data cleared!");
            }
            return cleared;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing data", e);
            return false;
        }
    }
}
