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
                if (data instanceof PersonalDataInterface) {
                    // Validate if needed
                    Log.d(TAG, "Processing data of type: " + data.getType());
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
                if (cloudBackupManager.isBackupEnabled()) {
                    cloudBackupManager.queueForBackup(data);
                }
                
                Log.d(TAG, "Successfully captured data: " + data.getType());
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to capture data", e);
            }
        });
    }
    
    /**
     * Convenience method for PersonalData objects
     */
    public void processData(PersonalData personalData) {
        captureData(new PersonalDataAdapter(personalData));
    }
    
    /**
     * Save data synchronously
     */
    public void saveData(PersonalData data) {
        captureData(new PersonalDataAdapter(data));
    }
    
    /**
     * Get data statistics
     */
    public DataStats getDataStats() {
        try {
            Map<String, Long> stats = localDataManager.getDataStatsByType();
            long totalEntries = stats.values().stream().mapToLong(Long::longValue).sum();
            long totalSize = localDataManager.getTotalDataSize();
            
            return new DataStats(totalEntries, totalSize, stats);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get data stats", e);
            return new DataStats(0, 0, new java.util.HashMap<>());
        }
    }
    
    /**
     * Get data by type
     */
    public List<PersonalData> getDataByType(String type) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            List<UniversalDataType> dataList = localDataManager.getDataByType(type);
            for (UniversalDataType data : dataList) {
                results.add(convertToPersonalData(data));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get data by type: " + type, e);
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
