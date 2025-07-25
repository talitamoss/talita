package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.*;

/**
 * LocalDataManager - Handles local SQLite database operations
 * 
 * Stores encrypted data with metadata for quick queries
 * All actual data is encrypted - only type and timestamp are searchable
 */
public class LocalDataManager {
    private static final String TAG = "LocalDataManager";
    
    private final DatabaseHelper dbHelper;
    private final Context context;
    
    public LocalDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(context);
    }
    
    /**
     * Save encrypted data
     */
    public long saveData(UniversalDataType data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TYPE, data.getType());
            values.put(DatabaseHelper.COLUMN_DATA, data.getEncryptedData());
            values.put(DatabaseHelper.COLUMN_TIMESTAMP, data.getTimestamp());
            values.put(DatabaseHelper.COLUMN_SUMMARY, data.getSummary());
            values.put(DatabaseHelper.COLUMN_SYNCED, 0); // Not synced initially
            
            long id = db.insert(DatabaseHelper.TABLE_DATA, null, values);
            Log.d(TAG, "Saved data with ID: " + id);
            return id;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving data", e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get all data of a specific type
     */
    public List<UniversalDataType> getDataByType(String type) {
        List<UniversalDataType> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA + 
                          " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ?" +
                          " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " DESC";
            
            Cursor cursor = db.rawQuery(query, new String[]{type});
            
            while (cursor.moveToNext()) {
                UniversalDataType data = cursorToData(cursor);
                result.add(data);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting data by type", e);
        } finally {
            db.close();
        }
        
        return result;
    }
    
    /**
     * Get recent data
     */
    public List<UniversalDataType> getRecentData(int limit) {
        List<UniversalDataType> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA + 
                          " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " DESC" +
                          " LIMIT ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
            
            while (cursor.moveToNext()) {
                UniversalDataType data = cursorToData(cursor);
                result.add(data);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent data", e);
        } finally {
            db.close();
        }
        
        return result;
    }
    
    /**
     * Get data within time range
     */
    public List<UniversalDataType> getDataInRange(long startTime, long endTime) {
        List<UniversalDataType> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA + 
                          " WHERE " + DatabaseHelper.COLUMN_TIMESTAMP + " BETWEEN ? AND ?" +
                          " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " DESC";
            
            Cursor cursor = db.rawQuery(query, 
                new String[]{String.valueOf(startTime), String.valueOf(endTime)});
            
            while (cursor.moveToNext()) {
                UniversalDataType data = cursorToData(cursor);
                result.add(data);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting data in range", e);
        } finally {
            db.close();
        }
        
        return result;
    }
    
    /**
     * Get data statistics by type
     */
    public Map<String, Long> getDataStatsByType() {
        Map<String, Long> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String query = "SELECT " + DatabaseHelper.COLUMN_TYPE + ", COUNT(*) as count" +
                          " FROM " + DatabaseHelper.TABLE_DATA +
                          " GROUP BY " + DatabaseHelper.COLUMN_TYPE;
            
            Cursor cursor = db.rawQuery(query, null);
            
            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                long count = cursor.getLong(1);
                stats.put(type, count);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting data stats", e);
        } finally {
            db.close();
        }
        
        return stats;
    }
    
    /**
     * Get total data count
     */
    public long getDataCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long count = 0;
        
        try {
            count = DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_DATA);
        } catch (Exception e) {
            Log.e(TAG, "Error getting data count", e);
        } finally {
            db.close();
        }
        
        return count;
    }
    
    /**
     * Get total data size (approximate based on encrypted data length)
     */
    public long getTotalDataSize() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long totalSize = 0;
        
        try {
            String query = "SELECT SUM(LENGTH(" + DatabaseHelper.COLUMN_DATA + ")) FROM " + 
                          DatabaseHelper.TABLE_DATA;
            
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                totalSize = cursor.getLong(0);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting total data size", e);
        } finally {
            db.close();
        }
        
        return totalSize;
    }
    
    /**
     * Delete data by type
     */
    public boolean deleteDataByType(String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            int deleted = db.delete(DatabaseHelper.TABLE_DATA,
                DatabaseHelper.COLUMN_TYPE + " = ?",
                new String[]{type});
            
            Log.d(TAG, "Deleted " + deleted + " records of type: " + type);
            return deleted > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting data by type", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Mark data as synced
     */
    public void markAsSynced(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SYNCED, 1);
            
            db.update(DatabaseHelper.TABLE_DATA, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
                
        } catch (Exception e) {
            Log.e(TAG, "Error marking as synced", e);
        } finally {
            db.close();
        }
    }
    
    /**
     * Get unsynced data
     */
    public List<UniversalDataType> getUnsyncedData() {
        List<UniversalDataType> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA + 
                          " WHERE " + DatabaseHelper.COLUMN_SYNCED + " = 0" +
                          " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " ASC";
            
            Cursor cursor = db.rawQuery(query, null);
            
            while (cursor.moveToNext()) {
                UniversalDataType data = cursorToData(cursor);
                result.add(data);
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting unsynced data", e);
        } finally {
            db.close();
        }
        
        return result;
    }
    
    /**
     * Helper to convert cursor to data object
     */
    private UniversalDataType cursorToData(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
        String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE));
        String data = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATA));
        long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
        String summary = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SUMMARY));
        
        return new UniversalDataType(id, type, data, timestamp, summary);
    }
    
    /**
     * Database helper class
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "personal_data.db";
        private static final int DATABASE_VERSION = 1;
        
        // Table name
        public static final String TABLE_DATA = "encrypted_data";
        
        // Columns
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TYPE = "data_type";
        public static final String COLUMN_DATA = "encrypted_data";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_SYNCED = "synced";
        
        // Create table SQL
        private static final String CREATE_TABLE = 
            "CREATE TABLE " + TABLE_DATA + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TYPE + " TEXT NOT NULL, " +
            COLUMN_DATA + " TEXT NOT NULL, " +
            COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
            COLUMN_SUMMARY + " TEXT, " +
            COLUMN_SYNCED + " INTEGER DEFAULT 0" +
            ")";
        
        // Indexes for performance
        private static final String CREATE_TYPE_INDEX = 
            "CREATE INDEX idx_type ON " + TABLE_DATA + "(" + COLUMN_TYPE + ")";
            
        private static final String CREATE_TIMESTAMP_INDEX = 
            "CREATE INDEX idx_timestamp ON " + TABLE_DATA + "(" + COLUMN_TIMESTAMP + ")";
            
        private static final String CREATE_SYNCED_INDEX = 
            "CREATE INDEX idx_synced ON " + TABLE_DATA + "(" + COLUMN_SYNCED + ")";
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TYPE_INDEX);
            db.execSQL(CREATE_TIMESTAMP_INDEX);
            db.execSQL(CREATE_SYNCED_INDEX);
            
            Log.d(TAG, "Database created");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Handle database upgrades here
            Log.d(TAG, "Database upgrade from " + oldVersion + " to " + newVersion);
        }
    }
}
