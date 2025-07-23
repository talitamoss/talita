package com.core.talita;

import java.util.*;

/**
 * Extension methods for UniversalDataService and LocalDataManager
 * Add these methods to your existing classes
 */
public class DataServiceExtensions {
    
    // Add to UniversalDataService class:
    
    /**
     * Get all data from today
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
     * Get data within a time range
     */
    public List<PersonalData> getDataInRange(long startTime, long endTime) {
        List<PersonalData> result = new ArrayList<>();
        
        // Query your database for data between startTime and endTime
        List<TalitaDataType> dataList = localDataManager.queryDataByTimeRange(startTime, endTime);
        
        // Convert to PersonalData objects
        for (TalitaDataType data : dataList) {
            result.add(new PersonalDataAdapter(data));
        }
        
        return result;
    }
    
    // Add to LocalDataManager class:
    
    /**
     * Get the size of the database file
     */
    public long getDatabaseSize() {
        try {
            // Get the database file
            File dbFile = context.getDatabasePath("talita_db");
            if (dbFile.exists()) {
                return dbFile.length();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting database size", e);
        }
        return 0;
    }
    
    /**
     * Clear all data from the database
     */
    public void clearAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // Delete all records from the data table
            db.delete("personal_data", null, null);
            
            // Also clear any audio files
            File audioDir = new File(context.getFilesDir(), "audio");
            if (audioDir.exists()) {
                File[] files = audioDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            
            Log.d(TAG, "All data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing data", e);
        } finally {
            db.close();
        }
    }
    
    /**
     * Query data by time range
     */
    public List<TalitaDataType> queryDataByTimeRange(long startTime, long endTime) {
        List<TalitaDataType> results = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM personal_data WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(endTime)});
        
        try {
            while (cursor.moveToNext()) {
                String encryptedData = cursor.getString(cursor.getColumnIndex("encrypted_data"));
                String dataType = cursor.getString(cursor.getColumnIndex("data_type"));
                long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                
                // Decrypt and deserialize
                String decryptedJson = encryptionService.decryptData(encryptedData);
                TalitaDataType data = deserializeData(decryptedJson, dataType);
                
                if (data != null) {
                    results.add(data);
                }
            }
        } finally {
            cursor.close();
            db.close();
        }
        
        return results;
    }
}
