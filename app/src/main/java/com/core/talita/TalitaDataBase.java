package com.core.talita;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TalitaDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "talita.db";
    private static final int DATABASE_VERSION = 1;

    // We'll add the table creation SQL here

    public TalitaDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Core data items table
        String createDataItemsTable =
                "CREATE TABLE data_items (" +
                        "id TEXT PRIMARY KEY, " +
                        "type TEXT NOT NULL, " +
                        "created_at INTEGER NOT NULL, " +
                        "data_json TEXT NOT NULL, " +
                        "file_path TEXT, " +
                        "cloud_status TEXT DEFAULT 'local'" +
                        ");";

        // Sessions table
        String createSessionsTable =
                "CREATE TABLE sessions (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT, " +
                        "started_at INTEGER NOT NULL, " +
                        "ended_at INTEGER" +
                        ");";

        // Session items linking table
        String createSessionItemsTable =
                "CREATE TABLE session_items (" +
                        "session_id TEXT, " +
                        "item_id TEXT, " +
                        "PRIMARY KEY (session_id, item_id)" +
                        ");";

        // Create the tables
        db.execSQL(createDataItemsTable);
        db.execSQL(createSessionsTable);
        db.execSQL(createSessionItemsTable);

        // Create indexes for performance
        db.execSQL("CREATE INDEX idx_data_items_type ON data_items(type);");
        db.execSQL("CREATE INDEX idx_data_items_created_at ON data_items(created_at);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, simple upgrade - drop and recreate
        // In production, we'd do proper migrations
        db.execSQL("DROP TABLE IF EXISTS session_items");
        db.execSQL("DROP TABLE IF EXISTS sessions");
        db.execSQL("DROP TABLE IF EXISTS data_items");
        onCreate(db);
    }
}