package com.core.talita;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * UniversalDataService - Central orchestrator for all data operations
 * 
 * SIMPLIFIED VERSION without cloud backup complexity.
 * This handles:
 * - Data encryption
 * - Local storage
 * - Data retrieval
 * 
 * Cloud backup can be added later when needed.
 */
public class UniversalDataService {
    private static final String TAG = "UniversalDataService";
    private static UniversalDataService instance;
    
    private final Context context;
    private final EncryptionService encryptionService;
    private final LocalDataManager localDataManager;
    
    /**
     * Private constructor - use getInstance()
     */
    private UniversalDataService(Context context) {
        this.context = context.getApplicationContext();
        this.encryptionService = new EncryptionService(context);
        this.localDataManager = new LocalDataManager(context);
        
        Log.d(TAG, "UniversalDataService initialized (simplified, no cloud)");
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
     * Main entry point for saving data
     * This is what collectors call
     */
    public boolean saveData(PersonalData data) {
        try {
            Log.d(TAG, "Saving data of type: " + data.getType());
            
            // 1. Encrypt the data
            EncryptedData encrypted = encryptData(data);
            if (encrypted == null) {
                Log.e(TAG, "Failed to encrypt data");
                return false;
            }
            
            // 2. Save to local database
            String savedId = localDataManager.saveData(encrypted);
            if (savedId == null) {
                Log.e(TAG, "Failed to save to local database");
                return false;
            }
            
            // That's it! No cloud backup complexity
            Log.d(TAG, "Data saved successfully: " + savedId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to save data", e);
            return false;
        }
    }
    
    /**
     * Retrieve data by ID
     */
    public PersonalData getData(String id) {
        try {
            EncryptedData encrypted = localDataManager.getData(id);
            if (encrypted == null) {
                return null;
            }
            
            return decryptData(encrypted);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve data", e);
            return null;
        }
    }
    
    /**
     * Retrieve data by type and time range
     */
    public List<PersonalData> getDataByType(String type, long startTime, long endTime) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            List<EncryptedData> encryptedList = localDataManager.getDataByType(type, startTime, endTime);
            
            for (EncryptedData encrypted : encryptedList) {
                PersonalData decrypted = decryptData(encrypted);
                if (decrypted != null) {
                    results.add(decrypted);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve data by type", e);
        }
        
        return results;
    }
    
    /**
     * Get recent data entries
     */
    public List<PersonalData> getRecentData(int limit) {
        List<PersonalData> results = new ArrayList<>();
        
        try {
            List<EncryptedData> encryptedList = localDataManager.getRecentData(limit);
            
            for (EncryptedData encrypted : encryptedList) {
                PersonalData decrypted = decryptData(encrypted);
                if (decrypted != null) {
                    results.add(decrypted);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve recent data", e);
        }
        
        return results;
    }
    
    /**
     * Delete data by ID
     */
    public boolean deleteData(String dataId) {
        try {
            return localDataManager.deleteData(dataId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete data", e);
            return false;
        }
    }
    
    /**
     * Get data statistics by type
     */
    public Map<String, Integer> getDataStats() {
        return localDataManager.getDataCountByType();
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
     * Get database size
     */
    public long getDatabaseSize() {
        return localDataManager.getDatabaseSize();
    }
    
    /**
     * Clear all data
     */
    public boolean clearAllData() {
        try {
            return localDataManager.clearAllData();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear all data", e);
            return false;
        }
    }
    
    /**
     * Export all data (for vault/export features)
     */
    public Map<String, List<PersonalData>> exportAllData() {
        Map<String, List<PersonalData>> exportMap = new HashMap<>();
        
        try {
            // Get all data types
            Map<String, Integer> types = getDataCountByType();
            
            // For each type, get all data
            for (String type : types.keySet()) {
                List<PersonalData> typeData = getDataByType(type, 0, System.currentTimeMillis());
                if (!typeData.isEmpty()) {
                    exportMap.put(type, typeData);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to export data", e);
        }
        
        return exportMap;
    }
    
    // Private helper methods
    
    /**
     * Encrypt PersonalData to EncryptedData
     */
    private EncryptedData encryptData(PersonalData data) {
        try {
            // Convert data to JSON string
            String jsonData = data.toJson();
            
            // Encrypt the JSON
            String encrypted = encryptionService.encryptString(jsonData);
            
            // Create EncryptedData object
            return new EncryptedData(
                data.getId(),
                data.getType(),
                encrypted,
                null, // file path if applicable
                data.getTimestamp()
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt data", e);
            return null;
        }
    }
    
    /**
     * Decrypt EncryptedData to PersonalData
     */
    private PersonalData decryptData(EncryptedData encrypted) {
        try {
            // Decrypt the content
            String decrypted = encryptionService.decryptString(encrypted.getEncryptedContent());
            
            // Parse back to PersonalData
            return PersonalData.fromJson(decrypted);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt data", e);
            return null;
        }
    }
}
