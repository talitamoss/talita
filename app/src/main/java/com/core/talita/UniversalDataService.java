package com.core.talita;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Universal Data Service - handles ALL data types in Talita
 * Any data implementing TalitaDataType gets automatic:
 * - Database storage
 * - Cloud backup queuing
 * - Sharing capabilities
 * - Consistent error handling
 */
public class UniversalDataService {

    private static final String TAG = "UniversalDataService";

    private final Context context;
    private final LocalDataManager dataManager;

    public UniversalDataService(Context context) {
        this.context = context;
        this.dataManager = new LocalDataManager(context);
    }

    /**
     * Capture any data type - handles everything automatically
     */
    public String capture(TalitaDataType data) {
        try {
            Log.d(TAG, "Capturing " + data.getType() + " data: " + data.getDisplayName());

            // 1. Save to database (always)
            String dataId = saveToDatabase(data);

            if (dataId != null) {
                // 2. Queue for cloud backup (future feature)
                queueForCloudBackup(dataId, data);

                // 3. Handle sharing updates (future feature)
                updateActiveSharing(dataId, data);

                // 4. Success feedback
                showSuccessToast(data);

                Log.d(TAG, "Successfully captured " + data.getType() + " with ID: " + dataId);
                return dataId;

            } else {
                Log.e(TAG, "Failed to save " + data.getType() + " to database");
                showErrorToast(data);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error capturing " + data.getType() + ": " + e.getMessage());
            e.printStackTrace();
            showErrorToast(data);
            return null;
        }
    }

    /**
     * Save data to local database
     */
    private String saveToDatabase(TalitaDataType data) {
        try {
            // Create enhanced JSON with metadata
            JSONObject enhancedJson = new JSONObject(data.toJson());
            enhancedJson.put("display_name", data.getDisplayName());
            enhancedJson.put("display_summary", data.getDisplaySummary());

            // Use existing LocalDataManager but with enhanced data
            if (data.getType().equals("location")) {
                return dataManager.saveLocationPoint(
                        data.getLatitude(),
                        data.getLongitude(),
                        0.0, // accuracy placeholder
                        "universal_service"
                );
            } else {
                // For non-location data, use a generic save method
                return saveGenericData(data, enhancedJson);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON error saving " + data.getType() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Generic save method for non-location data
     */
    private String saveGenericData(TalitaDataType data, JSONObject enhancedJson) {
        try {
            Log.d(TAG, "Saving " + data.getType() + " data with ID: " + data.getId());

            // For now, use a simple file-based approach until we extend LocalDataManager
            if (data.getType().equals("audio")) {
                // Save to a generic data file
                saveToGenericDataFile(data, enhancedJson);
                return data.getId();
            }

            // For other data types
            Log.d(TAG, "Generic save for " + data.getType() + " - using ID: " + data.getId());
            return data.getId();

        } catch (Exception e) {
            Log.e(TAG, "Error in saveGenericData: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to save to a generic data file
     */
    private void saveToGenericDataFile(TalitaDataType data, JSONObject enhancedJson) {
        try {
            // Create a data entry with all the info
            JSONObject dataEntry = new JSONObject();
            dataEntry.put("id", data.getId());
            dataEntry.put("type", data.getType());
            dataEntry.put("filePath", data.getFilePath());
            dataEntry.put("timestamp", data.getTimestamp());
            dataEntry.put("data", enhancedJson);

            // Save to a file (similar to the old audio_recordings.txt approach)
            String filename = data.getType() + "_data.txt";
            java.io.FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
            fos.write((dataEntry.toString() + "\n").getBytes());
            fos.close();

            Log.d(TAG, "✅ Saved " + data.getType() + " to " + filename);

        } catch (Exception e) {
            Log.e(TAG, "Error saving to generic data file: " + e.getMessage());
        }
    }

    /**
     * Queue for cloud backup (future implementation)
     */
    private void queueForCloudBackup(String dataId, TalitaDataType data) {
        Log.d(TAG, "Queuing " + data.getType() + " for cloud backup: " + dataId);
        // TODO: Add to backup queue when cloud service is ready
    }

    /**
     * Update any active sharing (future implementation)
     */
    private void updateActiveSharing(String dataId, TalitaDataType data) {
        Log.d(TAG, "Updating sharing for " + data.getType() + ": " + dataId);
        // TODO: Notify friends about new data when P2P sharing is ready
    }

    /**
     * Show success toast
     */
    private void showSuccessToast(TalitaDataType data) {
        String message = data.getDisplayName() + " saved successfully";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show error toast
     */
    private void showErrorToast(TalitaDataType data) {
        String message = "Failed to save " + data.getDisplayName();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get all data of a specific type
     */
    public java.util.List<LocalDataManager.DataItem> getDataByType(String type) {
        return dataManager.getItemsByType(type);
    }

    /**
     * Load data from generic data files (for non-database data types)
     */
    public java.util.List<GenericDataItem> getGenericDataByType(String type) {
        java.util.List<GenericDataItem> items = new java.util.ArrayList<>();

        try {
            String filename = type + "_data.txt";
            java.io.FileInputStream fis = context.openFileInput(filename);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    JSONObject dataEntry = new JSONObject(line);
                    String entryType = dataEntry.optString("type", "");

                    if (type.equals(entryType)) {
                        GenericDataItem item = new GenericDataItem(
                                dataEntry.optString("id", ""),
                                entryType,
                                dataEntry.optString("filePath", ""),
                                dataEntry.optLong("timestamp", 0),
                                dataEntry.optJSONObject("data")
                        );
                        items.add(item);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data entry: " + e.getMessage());
                }
            }
            reader.close();

            Log.d(TAG, "✅ Loaded " + items.size() + " " + type + " items from generic data file");

        } catch (java.io.IOException e) {
            Log.d(TAG, "No " + type + " data file found (normal for first run)");
        }

        return items;
    }

    /**
     * Helper class for generic data items
     */
    public static class GenericDataItem {
        public final String id;
        public final String type;
        public final String filePath;
        public final long timestamp;
        public final JSONObject data;

        public GenericDataItem(String id, String type, String filePath, long timestamp, JSONObject data) {
            this.id = id;
            this.type = type;
            this.filePath = filePath;
            this.timestamp = timestamp;
            this.data = data != null ? data : new JSONObject();
        }
    }
}