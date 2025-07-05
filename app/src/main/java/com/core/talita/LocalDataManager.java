package com.core.talita;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalDataManager {

    private TalitaDataBase dbHelper;
    private Context context;

    public LocalDataManager(Context context) {
        this.context = context;
        this.dbHelper = new TalitaDataBase(context);
    }

    // Save audio recording
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

    // Save location point
    public String saveLocationPoint(double latitude, double longitude, double accuracy, String provider) {
        String id = UUID.randomUUID().toString();

        try {
            // Create JSON metadata for location
            JSONObject locationData = new JSONObject();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("accuracy", accuracy);
            locationData.put("provider", provider);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("type", "location");
            values.put("created_at", System.currentTimeMillis());
            values.put("data_json", locationData.toString());
            values.put("cloud_status", "local");

            db.insert("data_items", null, values);
            db.close();

            return id;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get all items of a specific type
    public List<DataItem> getItemsByType(String type) {
        List<DataItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("data_items", null, "type = ?", new String[]{type}, null, null, "created_at DESC");

        while (cursor.moveToNext()) {
            DataItem item = new DataItem();
            item.id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            item.type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            item.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
            item.dataJson = cursor.getString(cursor.getColumnIndexOrThrow("data_json"));
            item.filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
            item.cloudStatus = cursor.getString(cursor.getColumnIndexOrThrow("cloud_status"));
            items.add(item);
        }

        cursor.close();
        db.close();
        return items;
    }

    // Simple data class to hold items
    public static class DataItem {
        public String id;
        public String type;
        public long createdAt;
        public String dataJson;
        public String filePath;
        public String cloudStatus;
    }

    // Create a new session
    public String createSession(String name) {
        String id = UUID.randomUUID().toString();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("started_at", System.currentTimeMillis());

        db.insert("sessions", null, values);
        db.close();

        return id;
    }

    // Update cloud backup status
    public void updateCloudStatus(String itemId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cloud_status", status);

        db.update("data_items", values, "id = ?", new String[]{itemId});
        db.close();
    }
}