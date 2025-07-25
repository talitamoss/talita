package com.core.talita;

import android.content.Context;
import android.util.Log;
import com.core.talita.cloud.CloudBackupManager;
import com.core.talita.encryption.EncryptionService;
import com.core.talita.encryption.SecureKeyManager;
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
            String encryptedJson = encryptionService.encryptString(jsonData);
            
            // Step 5: Store locally
            String dataId = UUID.randomUUID().toString();
            UniversalDataType dataType = new UniversalDataType(
                data.getType(),
                encryptedJson,
                System.currentTimeMillis(),
                createSummary(data)
            );
            
            long id = localDataManager.saveData(dataType);
            
            if (id != -1) {
                Log.d(TAG, "✅ Data stored locally with ID: " + id);
                
                // Step 6: Queue for backup
                cloudBackupManager.queueForBackup(dataId, data.getType());
                
                return dataId;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to capture data", e);
        }
        
        return null;
    }
    
    /**
     * Process PersonalData (legacy support)
     */
    public void processData(PersonalData data) {
        capture(new PersonalDataAdapter(data));
    }
    
    /**
     * Convert any personal data to universal format
     */
    private UniversalPersonalData convertToUniversal(PersonalDataInterface data) {
        if (data instanceof UniversalPersonalData) {
            return (UniversalPersonalData) data;
        }
        
        // Convert legacy PersonalData to universal format
        Map<String, Object> universalMap = new HashMap<>();
        universalMap.put("type", data.getType());
        universalMap.put("timestamp", data.getTimestamp());
        universalMap.put("data", data.getData());
        universalMap.put("metadata", data.getMetadata());
        
        return new UniversalPersonalData(data.getType(), universalMap);
    }
    
    /**
     * Validate data before processing
     */
    private boolean validateData(UniversalPersonalData data) {
        if (data.getType() == null || data.getType().isEmpty()) {
            Log.e(TAG, "Data type cannot be null or empty");
            return false;
        }
        
        if (data.getData() == null || data.getData().isEmpty()) {
            Log.e(TAG, "Data payload cannot be null or empty");
            return false;
        }
        
        return true;
    }
    
    /**
     * Create a human-readable summary
     */
    private String createSummary(PersonalDataInterface data) {
        String type = data.getType();
        Map<String, Object> dataMap = data.getData();
        
        // Create type-specific summaries
        switch (type) {
            case "water":
                Object amount = dataMap.get("amount");
                return "💧 " + amount + "ml";
                
            case "mood":
                Object mood = dataMap.get("mood");
                Object score = dataMap.get("score");
                return mood + " (" + score + "/5)";
                
            case "location":
                Object lat = dataMap.get("latitude");
                Object lon = dataMap.get("longitude");
                return "📍 Location tracked";
                
            default:
                // Generic summary
                return type + " recorded";
        }
    }
    
    /**
     * Get data statistics
     */
    public DataStats getDataStats() {
        try {
            Map<String, Long> stats = localDataManager.getDataStatsByType();
            long totalCount = localDataManager.getDataCount();
            long totalSize = localDataManager.getTotalDataSize();
            
            return new DataStats(totalCount, totalSize, stats);
        } catch (Exception e) {
            Log.e(TAG, "Error getting data stats", e);
            return new DataStats(0, 0, new HashMap<>());
        }
    }
    
    /**
     * Get data by type
     */
    public List<PersonalData> getDataByType(String type) {
        try {
            List<PersonalData> result = new ArrayList<>();
            List<UniversalDataType> dataList = localDataManager.getDataByType(type);
            
            for (UniversalDataType item : dataList) {
                // Decrypt and convert back
                String decryptedJson = encryptionService.decryptString(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData data = PersonalData.fromJson(decryptedJson);
                    result.add(data);
                }
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error getting data by type", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get recent data
     */
    public List<PersonalData> getRecentData(int limit) {
        try {
            List<PersonalData> result = new ArrayList<>();
            List<UniversalDataType> recentData = localDataManager.getRecentData(limit);
            
            for (UniversalDataType item : recentData) {
                String decryptedJson = encryptionService.decryptString(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData data = PersonalData.fromJson(decryptedJson);
                    result.add(data);
                }
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent data", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get data within a time range
     */
    public List<PersonalData> getDataInRange(long startTime, long endTime) {
        try {
            List<PersonalData> result = new ArrayList<>();
            List<UniversalDataType> dataInRange = localDataManager.getDataInRange(startTime, endTime);
            
            for (UniversalDataType item : dataInRange) {
                String decryptedJson = encryptionService.decryptString(item.getEncryptedData());
                if (decryptedJson != null) {
                    PersonalData data = PersonalData.fromJson(decryptedJson);
                    result.add(data);
                }
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error getting data in range", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Export data for a specific type
     */
    public String exportData(String type) {
        try {
            List<PersonalData> data = getDataByType(type);
            
            if (!data.isEmpty()) {
                // Create export file
                String fileName = type + "_export_" + System.currentTimeMillis() + ".json";
                // Export logic would go here
                return fileName;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting data", e);
        }
        
        return null;
    }
    
    /**
     * Delete all data for a type
     */
    public boolean deleteDataType(String type) {
        try {
            return localDataManager.deleteDataByType(type);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting data type", e);
            return false;
        }
    }
    
    /**
     * Get decrypted data items (for UI display)
     */
    public List<DecryptedDataItem> getDecryptedDataByType(String type) {
        List<DecryptedDataItem> result = new ArrayList<>();
        
        try {
            List<UniversalDataType> dataList = localDataManager.getDataByType(type);
            
            for (UniversalDataType item : dataList) {
                String decryptedJson = encryptionService.decryptString(item.getEncryptedData());
                if (decryptedJson != null) {
                    DecryptedDataItem decrypted = new DecryptedDataItem(
                        item.getId(),
                        item.getType(),
                        decryptedJson,
                        item.getTimestamp(),
                        item.getSummary()
                    );
                    result.add(decrypted);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting decrypted data", e);
        }
        
        return result;
    }
    
    /**
     * Get cloud backup manager
     */
    public CloudBackupManager getCloudBackupManager() {
        return cloudBackupManager;
    }
    
    /**
     * Data statistics
     */
    public static class DataStats {
        public final long totalCount;
        public final long totalSize;
        public final Map<String, Long> countByType;
        
        DataStats(long totalCount, long totalSize, Map<String, Long> countByType) {
            this.totalCount = totalCount;
            this.totalSize = totalSize;
            this.countByType = countByType;
        }
    }
    
    /**
     * Decrypted data item for UI
     */
    public static class DecryptedDataItem {
        public final long id;
        public final String type;
        public final String jsonData;
        public final long timestamp;
        public final String summary;
        
        DecryptedDataItem(long id, String type, String jsonData, long timestamp, String summary) {
            this.id = id;
            this.type = type;
            this.jsonData = jsonData;
            this.timestamp = timestamp;
            this.summary = summary;
        }
    }
    
    /**
     * Adapter to make PersonalData work with PersonalDataInterface
     */
    public static class PersonalDataAdapter implements PersonalDataInterface {
        private final PersonalData personalData;
        
        public PersonalDataAdapter(PersonalData personalData) {
            this.personalData = personalData;
        }
        
        @Override
        public String getType() {
            return personalData.getType();
        }
        
        @Override
        public Map<String, Object> getData() {
            return personalData.getData();
        }
        
        @Override
        public Map<String, Object> getMetadata() {
            return personalData.getMetadata();
        }
        
        @Override
        public long getTimestamp() {
            return personalData.getTimestamp();
        }
    }
}
