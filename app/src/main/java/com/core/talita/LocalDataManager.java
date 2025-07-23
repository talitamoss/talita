package com.core.talita;

import android.annotation.SuppressLint;
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
    private static final String DATABASE_NAME = "talita_db";
    
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
        String query = "SELECT * FROM data_items WHERE created_at >= ? AND created_at <= ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(endTime)});
        
        while (cursor.moveToNext()) {
            try {
                @SuppressLint("Range") String encryptedJson = cursor.getString(cursor.getColumnIndex("data_json"));
                String decryptedJson = encryptionService.decryptData(encryptedJson);
                JSONObject jsonObject = new JSONObject(decryptedJson);
                
                Map<String, Object> dataMap = jsonToMap(jsonObject);
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                
                UniversalPersonalData data = new UniversalPersonalData(type, dataMap);
                results.add(data);
                
            } catch (Exception e) {
                Log.e(TAG, "Error parsing data item", e);
            }
        }
        
        cursor.close();
        db.close();
        
        return results;
    }
    
    /**
     * Get data by type
     */
    @SuppressLint("Range")
    public List<DataItem> getDataByType(String type) {
        List<DataItem> results = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query("data_items", null, "type = ?", 
            new String[]{type}, null, null, "created_at DESC");
        
        while (cursor.moveToNext()) {
            DataItem item = new DataItem();
            item.setId(cursor.getString(cursor.getColumnIndex("id")));
            item.setType(cursor.getString(cursor.getColumnIndex("type")));
            item.setDataJson(cursor.getString(cursor.getColumnIndex("data_json")));
            item.setFilePath(cursor.getString(cursor.getColumnIndex("file_path")));
            item.setCreatedAt(cursor.getLong(cursor.getColumnIndex("created_at")));
            results.add(item);
        }
        
        cursor.close();
        db.close();
        
        return results;
    }
    
    /**
     * Delete all data
     */
    public void deleteAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("data_items", null, null);
        db.close();
        Log.d(TAG, "🗑️ All data deleted");
    }
    
    /**
     * Get total database size
     */
    public long getDatabaseSize() {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (dbFile.exists()) {
                return dbFile.length();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting database size", e);
        }
        return 0;
    }
    
    /**
     * Clear all data including files
     */
    public void clearAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // Delete all database records
            db.delete("data_items", null, null);
            db.delete("location_points", null, null);
            
            // Delete audio files
            File audioDir = new File(context.getFilesDir(), "audio");
            if (audioDir.exists()) {
                File[] files = audioDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            
            Log.d(TAG, "✅ All data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing data", e);
        } finally {
            db.close();
        }
    }
    
    /**
     * Save location point
     */
    public String saveLocationPoint(double latitude, double longitude, double accuracy, String context) {
        String id = UUID.randomUUID().toString();
        
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("accuracy", accuracy);
            locationData.put("context", context);
            locationData.put("timestamp", System.currentTimeMillis());
            
            // Create UniversalPersonalData for location
            UniversalPersonalData data = new UniversalPersonalData("location", locationData);
            
            // Save using the universal method
            return saveData(data);
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving location", e);
            return null;
        }
    }
    
    /**
     * Save audio recording
     */
    public String saveAudioRecording(String filePath, long durationMs, double latitude, double longitude) {
        String id = UUID.randomUUID().toString();
        
        try {
            Map<String, Object> audioData = new HashMap<>();
            audioData.put("duration_ms", durationMs);
            audioData.put("latitude", latitude);
            audioData.put("longitude", longitude);
            audioData.put("file_path", filePath);
            audioData.put("timestamp", System.currentTimeMillis());
            
            // Create UniversalPersonalData for audio
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
     * Helper method to convert JSONObject to Map
     */
    private Map<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.get(key));
        }
        return map;
    }
    
    /**
     * Get items by type (for backwards compatibility)
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
