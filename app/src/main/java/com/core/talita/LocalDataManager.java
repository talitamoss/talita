package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Calendar;

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
     * Save audio recording
     */
    public String saveAudioRecording(String filePath, long durationMs, double latitude, double longitude) {
        String id = UUID.randomUUID().toString();

        try {
            // Create JSON metadata for audio
            JSONObject audioData = new JSONObject();
            audioData.put("duration_ms", durationMs);
            audioData.put("format", "aac");
            audioData.put("latitude", latitude);
            audioData.put("longitude", longitude);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("type", "audio");
            values.put("created_at", System.currentTimeMillis());
            values.put("data_json", audioData.toString());
            values.put("file_path", filePath);
            values.put("cloud_status", "local");

            db.insert("data_items", null, values);
            db.close();

            return id;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Save location point (for LocationTracker compatibility)
     */
    public String saveLocationPoint(double latitude, double longitude, double accuracy, String provider) {
        LocationData locationData = new LocationData(latitude, longitude, accuracy, provider);
        return saveData(locationData);
    }
    
    /**
     * Get decrypted data by type for a time range
     */
    public List<DataItem> getDataByType(String type, long startTime, long endTime) {
        List<DataItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "type = ? AND created_at >= ? AND created_at <= ?";
        String[] args = {type, String.valueOf(startTime), String.valueOf(endTime)};
        
        Cursor cursor = db.query("data_items", null, selection, args, 
                               null, null, "created_at DESC");
        
        while (cursor.moveToNext()) {
            try {
                // Get encrypted JSON
                String encryptedJson = cursor.getString(cursor.getColumnIndex("data_json"));
                
                // Decrypt it
                String decryptedJson = encryptionService.decryptDataTypeJson(encryptedJson);
                
                // Create DataItem with decrypted data
                DecryptedDataItem item = new DecryptedDataItem(
                    cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getLong(cursor.getColumnIndex("created_at")),
                    decryptedJson,
                    cursor.getString(cursor.getColumnIndex("file_path")),
                    cursor.getString(cursor.getColumnIndex("cloud_status"))
                );
                
                items.add(item);
                
            } catch (Exception e) {
                Log.e(TAG, "Error decrypting item: " + e.getMessage());
            }
        }
        
        cursor.close();
        db.close();
        
        Log.d(TAG, "Retrieved " + items.size() + " " + type + " items");
        return items;
    }
    
    /**
     * Get all data of a specific type (for backward compatibility)
     */
    public List<DataItem> getItemsByType(String type) {
        return getDataByType(type, 0, System.currentTimeMillis());
    }
    
    /**
     * Get today's data for a specific type
     */
    public List<DataItem> getTodayData(String type) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long todayEnd = System.currentTimeMillis();
        
        return getDataByType(type, todayStart, todayEnd);
    }
    
    /**
     * Get total count of all data items
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
     * Get count of backed up data items
     */
    public int getBackedUpDataCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM data_items WHERE cloud_status IN ('synced', 'backed_up')";
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
     * Get all data types that have been logged
     */
    public List<String> getAllDataTypes() {
        List<String> types = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT type FROM data_items ORDER BY type";
        Cursor cursor = db.rawQuery(query, null);
        
        while (cursor.moveToNext()) {
            types.add(cursor.getString(0));
        }
        
        cursor.close();
        db.close();
        
        return types;
    }
    
    /**
     * Get aggregated stats for today
     */
    public Map<String, Object> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Water total
        int waterTotal = 0;
        List<DataItem> waterData = getTodayData("water");
        for (DataItem item : waterData) {
            try {
                JSONObject json = new JSONObject(item.getValue());
                waterTotal += json.optInt("volume_ml", 0);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing water data: " + e.getMessage());
            }
        }
        stats.put("water_ml", waterTotal);
        
        // Exercise count
        stats.put("exercise_count", getTodayData("exercise").size());
        
        // Meal count
        stats.put("meal_count", getTodayData("meal").size());
        
        // Latest mood
        List<DataItem> moodData = getTodayData("mood");
        if (!moodData.isEmpty()) {
            try {
                JSONObject json = new JSONObject(moodData.get(0).getValue());
                stats.put("latest_mood", json.optInt("rating", 0));
            } catch (Exception e) {
                stats.put("latest_mood", 0);
            }
        } else {
            stats.put("latest_mood", 0);
        }
        
        return stats;
    }
    
    /**
     * Get readable database (for advanced queries)
     */
    public SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }
    
    /**
     * Get writable database (for advanced operations)
     */
    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }
    
    /**
     * Implementation of DataItem for decrypted data
     */
    private static class DecryptedDataItem implements DataItem {
        private final String id;
        private final String type;
        private final long timestamp;
        private final String decryptedJson;
        private final String filePath;
        private final String cloudStatus;
        
        public DecryptedDataItem(String id, String type, long timestamp,
                               String decryptedJson, String filePath, String cloudStatus) {
            this.id = id;
            this.type = type;
            this.timestamp = timestamp;
            this.decryptedJson = decryptedJson;
            this.filePath = filePath;
            this.cloudStatus = cloudStatus;
        }
        
        @Override
        public String getId() { return id; }
        
        @Override
        public String getType() { return type; }
        
        @Override
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String getValue() { return decryptedJson; }
        
        @Override
        public String getMetadata() {
            try {
                JSONObject meta = new JSONObject();
                meta.put("cloud_status", cloudStatus);
                meta.put("has_file", filePath != null);
                return meta.toString();
            } catch (Exception e) {
                return "{}";
            }
        }
        
        @Override
        public String getDisplayName() {
            return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
        
        @Override
        public boolean isEncrypted() { return true; }
        
        @Override
        public boolean isBackedUp() {
            return "synced".equals(cloudStatus) || "backed_up".equals(cloudStatus);
        }
    }
}
