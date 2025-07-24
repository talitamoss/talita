package com.core.talita;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.core.talita.cloud.CloudBackupManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Universal Data Service - Central orchestrator for all data operations
 * Handles encryption, storage, and backup for any data type
 */
public class UniversalDataService {
    private static final String TAG = "UniversalDataService";
    private static UniversalDataService instance;
    
    private final Context context;
    private final LocalDataManager localDataManager;
    private final EncryptionService encryptionService;
    private final CloudBackupManager cloudBackupManager;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    private UniversalDataService(Context context) {
        this.context = context.getApplicationContext();
        this.localDataManager = new LocalDataManager(context);
        this.encryptionService = new EncryptionService(context);
        this.cloudBackupManager = CloudBackupManager.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        Log.d(TAG, "Universal Data Service initialized");
    }
    
    public static synchronized UniversalDataService getInstance(Context context) {
        if (instance == null) {
            instance = new UniversalDataService(context);
        }
        return instance;
    }
    
    /**
     * Main entry point for capturing any data type
     */
    public void captureData(UniversalDataType data) {
        if (data == null) {
            Log.e(TAG, "Cannot capture null data");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 1. Validate the data
                if (data instanceof TalitaDataType) {
                    ((TalitaDataType) data).validateData(convertToPersonalData(data));
                }
                
                // 2. Encrypt if needed
                String encryptedJson = encryptionService.encryptData(data.toJson());
                
                // 3. Store locally
                long id = localDataManager.saveData(data.getType(), encryptedJson, 
                    data.getTimestamp(), data.getFilePath());
                
                // 4. Handle file encryption if present
                if (data.getFilePath() != null) {
                    encryptionService.encryptFile(data.getFilePath());
                }
                
                // 5. Queue for cloud backup
                cloudBackupManager.queueForBackup(id, data);
                
                Log.d(TAG, "✅ Data captured: " + data.getType() + " [" + data.getId() + "]");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to capture data", e);
            }
        });
    }
    
    /**
     * Retrieve data by type with automatic decryption
     */
    public List<PersonalData> getDataByType(String type) {
        List<PersonalData> result = new ArrayList<>();
        
        try {
            List<UniversalDataType> encryptedData = localDataManager.getDataByType(type);
            
            for (UniversalDataType data : encryptedData) {
                if (data instanceof PersonalData) {
                    result.add((PersonalData) data);
                } else if (data instanceof UniversalPersonalData) {
                    result.add(convertToPersonalData((UniversalPersonalData) data));
                } else {
                    // Generic UniversalDataType - convert to PersonalData
                    result.add(convertToPersonalData(data));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve data", e);
        }
        
        return result;
    }
    
    /**
     * Get data within a time range
     */
    public List<PersonalData> getDataInRange(long startTime, long endTime) {
        List<PersonalData> result = new ArrayList<>();
        
        try {
            List<UniversalDataType> data = localDataManager.getDataInRange(startTime, endTime);
            for (UniversalDataType item : data) {
                result.add(convertToPersonalData(item));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve data in range", e);
        }
        
        return result;
    }
    
    /**
     * Delete data by ID
     */
    public void deleteData(String dataId) {
        executorService.execute(() -> {
            try {
                localDataManager.deleteData(dataId);
                Log.d(TAG, "Data deleted: " + dataId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete data", e);
            }
        });
    }
    
    /**
     * Get statistics about stored data
     */
    public DataStats getDataStats() {
        return localDataManager.getDataStats();
    }
    
    /**
     * Save PersonalData through the universal pipeline
     * This is a convenience method for plugins
     */
    public void saveData(PersonalData personalData) {
        if (personalData == null) {
            Log.e(TAG, "Cannot save null PersonalData");
            return;
        }
        
        captureData(personalData);
    }
    
    /**
     * Process any UniversalDataType
     */
    public void processData(UniversalDataType data) {
        captureData(data);
    }
    
    /**
     * Search for data across all types
     */
    public List<PersonalData> searchData(String query) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            // Get all data types
            List<String> types = localDataManager.getAllDataTypes();
            
            for (String type : types) {
                List<UniversalDataType> typeData = localDataManager.getDataByType(type);
                
                for (UniversalDataType item : typeData) {
                    // Simple search in display name and summary
                    if (item.getDisplayName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getDisplaySummary().toLowerCase().contains(query.toLowerCase())) {
                        results.add(convertToPersonalData(item));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Search failed", e);
        }
        
        return results;
    }
    
    /**
     * Get recent data of all types
     */
    public List<PersonalData> getRecentData(int limit) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            List<UniversalDataType> recentData = localDataManager.getRecentData(limit);
            for (UniversalDataType data : recentData) {
                results.add(convertToPersonalData(data));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get recent data", e);
        }
        
        return results;
    }
    
    /**
     * Export data for a specific type
     */
    public void exportData(String type, ExportCallback callback) {
        executorService.execute(() -> {
            try {
                List<PersonalData> data = getDataByType(type);
                String exportPath = ExportManager.exportToJson(context, data, type);
                
                mainHandler.post(() -> callback.onSuccess(exportPath));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Get data grouped by type (for UniversalPersonalData)
     */
    public List<PersonalData> getUniversalDataByType(String type) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            List<UniversalPersonalData> data = localDataManager.getUniversalDataByType(type);
            for (UniversalPersonalData item : data) {
                results.add(convertToPersonalData(item));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get universal data", e);
        }
        
        return results;
    }
    
    /**
     * Convert UniversalDataType to PersonalData
     */
    private PersonalData convertToPersonalData(UniversalDataType data) {
        if (data instanceof PersonalData) {
            return (PersonalData) data;
        }
        
        // Create a new PersonalData from UniversalDataType
        PersonalData pd = PersonalData.create(data.getType());
        
        // Copy basic fields
        Map<String, Object> dataMap = data.getMetadata();
        if (dataMap == null) {
            dataMap = new java.util.HashMap<>();
        }
        
        // Add standard fields
        dataMap.put("id", data.getId());
        dataMap.put("displayName", data.getDisplayName());
        dataMap.put("displaySummary", data.getDisplaySummary());
        
        pd.setData(dataMap);
        pd.setLocation(data.getLatitude(), data.getLongitude());
        if (data.getFilePath() != null) {
            pd.setFilePath(data.getFilePath());
        }
        
        return pd;
    }
    
    /**
     * Convert UniversalPersonalData to PersonalData
     */
    private PersonalData convertToPersonalData(UniversalPersonalData upd) {
        PersonalData pd = PersonalData.create(upd.getType());
        pd.setData(upd.getAllData());
        if (upd.getFilePath() != null) {
            pd.setFilePath(upd.getFilePath());
        }
        return pd;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        executorService.shutdown();
    }
    
    /**
     * Data statistics
     */
    public static class DataStats {
        public final long totalEntries;
        public final long totalSize;
        public final Map<String, Long> entriesByType;
        
        public DataStats(long totalEntries, long totalSize, Map<String, Long> entriesByType) {
            this.totalEntries = totalEntries;
            this.totalSize = totalSize;
            this.entriesByType = entriesByType;
        }
    }
    
    /**
     * Export callback
     */
    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
}
