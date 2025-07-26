package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * LocalDataManager - SQLite database manager for encrypted data
 * 
 * Features:
 * - Stores encrypted data locally
 * - Efficient querying by type and time range
 * - Tracks file paths for media
 * - Backup queue management
 */
public class LocalDataManager extends SQLiteOpenHelper {
    private static final String TAG = "LocalDataManager";
    private static final String DATABASE_NAME = AppConstants.DATABASE_NAME;
    private static final int DATABASE_VERSION = AppConstants.DATABASE_VERSION;
    
    // Table and columns
    private static final String TABLE_DATA = "personal_data";
    private static final String COL_ID = "id";
    private static final String COL_TYPE = "type";
    private static final String COL_ENCRYPTED_DATA = "encrypted_data";
    private static final String COL_FILE_PATH = "file_path";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_SYNCED = "synced";
    
    private final Context context;
    
    public LocalDataManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_DATA + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_TYPE + " TEXT NOT NULL, " +
                COL_ENCRYPTED_DATA + " TEXT NOT NULL, " +
                COL_FILE_PATH + " TEXT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_CREATED_AT + " INTEGER NOT NULL, " +
                COL_SYNCED + " INTEGER DEFAULT 0)";
        
        db.execSQL(createTable);
        
        // Create indices for performance
        db.execSQL("CREATE INDEX idx_type ON " + TABLE_DATA + "(" + COL_TYPE + ")");
        db.execSQL("CREATE INDEX idx_timestamp ON " + TABLE_DATA + "(" + COL_TIMESTAMP + ")");
        db.execSQL("CREATE INDEX idx_synced ON " + TABLE_DATA + "(" + COL_SYNCED + ")");
        
        Log.d(TAG, "✅ Database created");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
    
    /**
     * Save encrypted data
     */
    public boolean saveEncryptedData(String id, String type, String encryptedData, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(COL_ID, id);
            values.put(COL_TYPE, type);
            values.put(COL_ENCRYPTED_DATA, encryptedData);
            values.put(COL_FILE_PATH, filePath);
            values.put(COL_TIMESTAMP, System.currentTimeMillis());
            values.put(COL_CREATED_AT, System.currentTimeMillis());
            values.put(COL_SYNCED, 0);
            
            long result = db.insertWithOnConflict(TABLE_DATA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return result != -1;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving data", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get all data (returns encrypted items)
     */
    public List<DecryptedDataItem> getAllDecryptedData(EncryptionService encryptionService) {
        List<DecryptedDataItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, null, null, null, null, COL_TIMESTAMP + " DESC");
        
        try {
            while (cursor.moveToNext()) {
                DecryptedDataItem item = cursorToDecryptedItem(cursor);
                items.add(item);
            }
        } finally {
            cursor.close();
            db.close();
        }
        
        return items;
    }
    
    /**
     * Get data by type
     */
    public List<DecryptedDataItem> getDataByType(String type, EncryptionService encryptionService) {
        List<DecryptedDataItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, COL_TYPE + "=?", 
                new String[]{type}, null, null, COL_TIMESTAMP + " DESC");
        
        try {
            while (cursor.moveToNext()) {
                DecryptedDataItem item = cursorToDecryptedItem(cursor);
                items.add(item);
            }
        } finally {
            cursor.close();
            db.close();
        }
        
        return items;
    }
    
    /**
     * Get data for time range
     */
    public List<DecryptedDataItem> getDataForTimeRange(long startTime, long endTime, EncryptionService encryptionService) {
        List<DecryptedDataItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String selection = COL_TIMESTAMP + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime)};
        
        Cursor cursor = db.query(TABLE_DATA, null, selection, selectionArgs, 
                null, null, COL_TIMESTAMP + " DESC");
        
        try {
            while (cursor.moveToNext()) {
                DecryptedDataItem item = cursorToDecryptedItem(cursor);
                items.add(item);
            }
        } finally {
            cursor.close();
            db.close();
        }
        
        return items;
    }
    
    /**
     * Delete data item
     */
    public boolean deleteDataItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        
        try {
            int deleted = db.delete(TABLE_DATA, COL_ID + "=?", new String[]{id});
            return deleted > 0;
        } finally {
            db.close();
        }
    }
    
    /**
     * Delete all data
     */
    public void deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME);
        Log.d(TAG, "🗑️ Database deleted");
    }
    
    /**
     * Mark item as synced
     */
    public void markAsSynced(String id) {
        SQLiteDatabase db = getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(COL_SYNCED, 1);
            db.update(TABLE_DATA, values, COL_ID + "=?", new String[]{id});
        } finally {
            db.close();
        }
    }
    
    /**
     * Get unsynced items
     */
    public List<DecryptedDataItem> getUnsyncedItems() {
        List<DecryptedDataItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, COL_SYNCED + "=0", 
                null, null, null, COL_CREATED_AT + " ASC");
        
        try {
            while (cursor.moveToNext()) {
                DecryptedDataItem item = cursorToDecryptedItem(cursor);
                items.add(item);
            }
        } finally {
            cursor.close();
            db.close();
        }
        
        return items;
    }
    
    /**
     * Get database statistics
     */
    public DatabaseStats getStats() {
        SQLiteDatabase db = getReadableDatabase();
        DatabaseStats stats = new DatabaseStats();
        
        try {
            // Total count
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATA, null);
            if (cursor.moveToFirst()) {
                stats.totalItems = cursor.getInt(0);
            }
            cursor.close();
            
            // Count by type
            cursor = db.rawQuery("SELECT " + COL_TYPE + ", COUNT(*) FROM " + TABLE_DATA + 
                    " GROUP BY " + COL_TYPE, null);
            while (cursor.moveToNext()) {
                stats.countByType.put(cursor.getString(0), cursor.getInt(1));
            }
            cursor.close();
            
            // Oldest timestamp
            cursor = db.rawQuery("SELECT MIN(" + COL_TIMESTAMP + ") FROM " + TABLE_DATA, null);
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                stats.oldestTimestamp = cursor.getLong(0);
            }
            cursor.close();
            
            // Unsynced count
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATA + 
                    " WHERE " + COL_SYNCED + "=0", null);
            if (cursor.moveToFirst()) {
                stats.unsyncedCount = cursor.getInt(0);
            }
            cursor.close();
            
        } finally {
            db.close();
        }
        
        return stats;
    }
    
    /**
     * Convert cursor to DecryptedDataItem
     */
    private DecryptedDataItem cursorToDecryptedItem(Cursor cursor) {
        DecryptedDataItem item = new DecryptedDataItem();
        item.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
        item.setType(cursor.getString(cursor.getColumnIndex(COL_TYPE)));
        item.setEncryptedData(cursor.getString(cursor.getColumnIndex(COL_ENCRYPTED_DATA)));
        item.setFilePath(cursor.getString(cursor.getColumnIndex(COL_FILE_PATH)));
        item.setTimestamp(cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP)));
        item.setCreatedAt(cursor.getLong(cursor.getColumnIndex(COL_CREATED_AT)));
        item.setSynced(cursor.getInt(cursor.getColumnIndex(COL_SYNCED)) == 1);
        return item;
    }
    
    /**
     * Database statistics class
     */
    public static class DatabaseStats {
        public int totalItems = 0;
        public java.util.Map<String, Integer> countByType = new java.util.HashMap<>();
        public long oldestTimestamp = 0;
        public int unsyncedCount = 0;
    }
}
