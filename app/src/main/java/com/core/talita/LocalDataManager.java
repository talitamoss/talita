package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Calendar;
import java.util.Iterator;

/**
 * LocalDataManager - Handles all database operations
 * Single source of truth for all encrypted data storage
 */
public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    
    private LocalDatabase dbHelper;
    private Context context;
    private EncryptionService encryptionService;
    
    public LocalDataManager(Context context) {
        this.context = context;
        this.dbHelper = new LocalDatabase(context);
        this.encryptionService = new EncryptionService(context);
    }
    
    /**
     * Save data using UniversalDataType interface
     * This ensures ALL data goes to the database
     */
    public String saveData(UniversalDataType data) {
        try {
            // Encrypt the JSON data
            String encryptedJson = encryptionService.encryptDataTypeJson(data.toJson());
            
            // Handle file encryption if needed
            String encryptedFilePath = null;
            if (data.getFilePath() != null && !data.getFilePath().isEmpty()) {
                encryptedFilePath = encryptionService.encryptFile(data.getFilePath());
            }
            
            // Save to database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", data.getId());
            values.put("type", data.getType());
            values.put("created_at", data.getTimestamp());
            values.put("data_json", encryptedJson);  // Encrypted JSON
            values.put("file_path", encryptedFilePath);
            values.put("cloud_status", "local");
            
            long result = db.insert("data_items", null, values);
            db.close();
            
            if (result != -1) {
                Log.d(TAG, "✅ Saved encrypted " + data.getType() + " to database");
                return data.getId();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving data: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Query data by time range
     */
    public List<UniversalDataType> queryDataByTimeRange(long startTime, long endTime) {
        List<UniversalDataType> results = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM data_items WHERE created_at >= ? AND created_at <= ? ORDER BY created_at DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(endTime)});
        
        try {
            while (cursor.moveToNext()) {
                String encryptedData = cursor.getString(cursor.getColumnIndexOrThrow("data_json"));
                String dataType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
                
                // Decrypt and deserialize
                String decryptedJson = encryptionService.decryptData(encryptedData);
                
                // Create a generic UniversalDataType implementation
                UniversalDataType data = new UniversalPersonalData(dataType, parseJsonToMap(decryptedJson));
                results.add(data);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying data by time range", e);
        } finally {
            cursor.close();
            db.close();
        }
        
        return results;
    }
    
    /**
     * Get data by type
     */
    public List<DataItem> getDataByType(String type) {
        List<DataItem> items = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM data_items WHERE type = ? ORDER BY created_at DESC";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        
        try {
            while (cursor.moveToNext()) {
                DataItem item = new DataItem();
                item.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                item.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
                item.setDataJson(cursor.getString(cursor.getColumnIndexOrThrow("data_json")));
                item.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
                items.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting data by type", e);
        } finally {
            cursor.close();
            db.close();
        }
        
        return items;
    }
    
    /**
     * Get the size of the database file
     */
    public long getDatabaseSize() {
        try {
            // Get the database file
            File dbFile = context.getDatabasePath(LocalDatabase.DATABASE_NAME);
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
            db.delete("data_items", null, null);
            db.delete("location_points", null, null);
            
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
     * Save location point (legacy method for compatibility)
     */
    public String saveLocationPoint(double latitude, double longitude, double accuracy, String context) {
        String id = UUID.randomUUID().toString();
        
        try {
            // Create location data
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("accuracy", accuracy);
            locationData.put("context", context);
            locationData.put("timestamp", System.currentTimeMillis());
            
            // Create UniversalDataType
            UniversalPersonalData data = new UniversalPersonalData("location", locationData);
            
            // Save using the universal method
            return saveData(data);
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving location", e);
            return null;
        }
    }
    
    /**
     * Save audio recording (legacy method for compatibility)
     */
    public String saveAudioRecording(String filePath, long durationMs, double latitude, double longitude) {
        String id = UUID.randomUUID().toString();

        try {
            // Create audio data
            Map<String, Object> audioData = new HashMap<>();
            audioData.put("duration_ms", durationMs);
            audioData.put("latitude", latitude);
            audioData.put("longitude", longitude);
            audioData.put("file_path", filePath);
            audioData.put("timestamp", System.currentTimeMillis());
            
            // Create UniversalDataType
            UniversalPersonalData data = new UniversalPersonalData("audio", audioData);
            data.setFilePath(filePath);
            
            // Save using the universal method
            return saveData(data);
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving audio", e);
            return null;
        }
    }
    
    /**
     * Helper method to parse JSON to Map
     */
    private Map<String, Object> parseJsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, jsonObject.get(key));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
        return map;
    }

   /**
     * Get database name constant
     */
    private static final String DATABASE_NAME = "talita_db";
    
    /**
     * Get items by type
     */
    public List<DataItem> getItemsByType(String type) {
        return getDataByType(type);
    }
    
    /**
     * Get total data count
     */
    public int getTotalDataCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM data_items";
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * Get backed up data count
     */
    public int getBackedUpDataCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM data_items WHERE cloud_status = ?";
        Cursor cursor = db.rawQuery(query, new String[]{"backed_up"});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

}
