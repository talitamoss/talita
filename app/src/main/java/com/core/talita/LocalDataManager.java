package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LocalDataManager - SQLite database manager for encrypted data
 * 
 * Extended with all missing methods needed by the codebase
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
                COL_ENCRYPTED_DATA + " TEXT, " +
                COL_FILE_PATH + " TEXT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_CREATED_AT + " INTEGER NOT NULL, " +
                COL_SYNCED + " INTEGER DEFAULT 0" +
                ")";
        
        db.execSQL(createTable);
        
        // Create indices for better performance
        db.execSQL("CREATE INDEX idx_type ON " + TABLE_DATA + "(" + COL_TYPE + ")");
        db.execSQL("CREATE INDEX idx_timestamp ON " + TABLE_DATA + "(" + COL_TIMESTAMP + ")");
        db.execSQL("CREATE INDEX idx_synced ON " + TABLE_DATA + "(" + COL_SYNCED + ")");
        
        Log.d(TAG, "Database created successfully");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // For now, just recreate the table
        // In production, you'd want to migrate data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);
    }
    
    /**
     * Save encrypted data
     */
    public String saveData(EncryptedData encryptedData) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COL_ID, encryptedData.getId());
        values.put(COL_TYPE, encryptedData.getType());
        values.put(COL_ENCRYPTED_DATA, encryptedData.getEncryptedContent());
        values.put(COL_FILE_PATH, encryptedData.getFilePath());
        values.put(COL_TIMESTAMP, encryptedData.getTimestamp());
        values.put(COL_CREATED_AT, System.currentTimeMillis());
        values.put(COL_SYNCED, 0);
        
        long result = db.insertWithOnConflict(TABLE_DATA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        
        if (result != -1) {
            Log.d(TAG, "Data saved: " + encryptedData.getId());
            return encryptedData.getId();
        } else {
            Log.e(TAG, "Failed to save data");
            return null;
        }
    }
    
    /**
     * Get data by ID
     */
    public EncryptedData getData(String id) {
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, COL_ID + " = ?", 
                new String[]{id}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            EncryptedData data = cursorToEncryptedData(cursor);
            cursor.close();
            return data;
        }
        
        return null;
    }
    
    /**
     * Get data by type
     */
    public List<EncryptedData> getDataByType(String type, long startTime, long endTime) {
        List<EncryptedData> dataList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String selection = COL_TYPE + " = ? AND " + COL_TIMESTAMP + " >= ? AND " + COL_TIMESTAMP + " <= ?";
        String[] selectionArgs = {type, String.valueOf(startTime), String.valueOf(endTime)};
        
        Cursor cursor = db.query(TABLE_DATA, null, selection, selectionArgs, 
                null, null, COL_TIMESTAMP + " DESC");
        
        while (cursor != null && cursor.moveToNext()) {
            dataList.add(cursorToEncryptedData(cursor));
        }
        
        if (cursor != null) cursor.close();
        
        return dataList;
    }
    
    /**
     * Get recent data
     */
    public List<EncryptedData> getRecentData(int limit) {
        List<EncryptedData> dataList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, null, null, 
                null, null, COL_TIMESTAMP + " DESC", String.valueOf(limit));
        
        while (cursor != null && cursor.moveToNext()) {
            dataList.add(cursorToEncryptedData(cursor));
        }
        
        if (cursor != null) cursor.close();
        
        return dataList;
    }
    
    /**
     * Delete data by ID
     */
    public boolean deleteData(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_DATA, COL_ID + " = ?", new String[]{id});
        return result > 0;
    }
    
    /**
     * Get data count by type
     */
    public Map<String, Integer> getDataCountByType() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT " + COL_TYPE + ", COUNT(*) FROM " + TABLE_DATA + 
                      " GROUP BY " + COL_TYPE;
        
        Cursor cursor = db.rawQuery(query, null);
        
        while (cursor != null && cursor.moveToNext()) {
            String type = cursor.getString(0);
            int count = cursor.getInt(1);
            counts.put(type, count);
        }
        
        if (cursor != null) cursor.close();
        
        return counts;
    }
    
    /**
     * Get total data count
     */
    public int getTotalDataCount() {
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATA, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        
        return 0;
    }
    
    /**
     * Get database size in bytes
     */
    public long getDatabaseSize() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.length();
    }
    
    /**
     * Clear all data
     */
    public boolean clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        
        try {
            db.execSQL("DELETE FROM " + TABLE_DATA);
            Log.d(TAG, "All data cleared");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear data", e);
            return false;
        }
    }
    
    /**
     * Get unsynced data for backup
     */
    public List<EncryptedData> getUnsyncedData(int limit) {
        List<EncryptedData> dataList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DATA, null, COL_SYNCED + " = 0", null, 
                null, null, COL_CREATED_AT + " ASC", String.valueOf(limit));
        
        while (cursor != null && cursor.moveToNext()) {
            dataList.add(cursorToEncryptedData(cursor));
        }
        
        if (cursor != null) cursor.close();
        
        return dataList;
    }
    
    /**
     * Mark data as synced
     */
    public void markAsSynced(String id) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COL_SYNCED, 1);
        
        db.update(TABLE_DATA, values, COL_ID + " = ?", new String[]{id});
    }
    
    /**
     * Helper method to convert cursor to EncryptedData
     */
    private EncryptedData cursorToEncryptedData(Cursor cursor) {
        return new EncryptedData(
            cursor.getString(cursor.getColumnIndex(COL_ID)),
            cursor.getString(cursor.getColumnIndex(COL_TYPE)),
            cursor.getString(cursor.getColumnIndex(COL_ENCRYPTED_DATA)),
            cursor.getString(cursor.getColumnIndex(COL_FILE_PATH)),
            cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP))
        );
    }
}
