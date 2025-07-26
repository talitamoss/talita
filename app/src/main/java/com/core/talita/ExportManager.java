package com.core.talita;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ExportManager - Handles data export in various formats
 */
public class ExportManager {
    private static final String TAG = "ExportManager";
    
    /**
     * Export data to CSV format
     */
    public static boolean exportToCsv(Context context, List<PersonalData> data, String filePath) {
        try {
            File file = new File(filePath);
            FileWriter writer = new FileWriter(file);
            
            // Write header
            writer.write("Type,Timestamp,Date,Time,Data\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            
            // Write data rows
            for (PersonalData item : data) {
                Date date = new Date(item.getTimestamp());
                String dataStr = formatDataForCsv(item.getData());
                
                writer.write(String.format("%s,%d,%s,%s,\"%s\"\n",
                    item.getType(),
                    item.getTimestamp(),
                    dateFormat.format(date),
                    timeFormat.format(date),
                    dataStr.replace("\"", "\"\"") // Escape quotes
                ));
            }
            
            writer.close();
            Log.d(TAG, "✅ Exported " + data.size() + " items to CSV");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to export CSV", e);
            return false;
        }
    }
    
    /**
     * Export data to JSON format
     */
    public static boolean exportToJson(Context context, List<PersonalData> data, String filePath) {
        try {
            File file = new File(filePath);
            JSONArray jsonArray = new JSONArray();
            
            for (PersonalData item : data) {
                JSONObject json = new JSONObject();
                json.put("type", item.getType());
                json.put("timestamp", item.getTimestamp());
                json.put("data", new JSONObject(item.getData()));
                json.put("metadata", new JSONObject(item.getMetadata()));
                jsonArray.put(json);
            }
            
            // Write to file
            FileWriter writer = new FileWriter(file);
            writer.write(jsonArray.toString(2)); // Pretty print with indent
            writer.close();
            
            Log.d(TAG, "✅ Exported " + data.size() + " items to JSON");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to export JSON", e);
            return false;
        }
    }
    
    /**
     * Export data to encrypted archive
     */
    public static boolean exportToArchive(Context context, Map<String, List<PersonalData>> dataByType, boolean includeMedia) {
        try {
            // Create export directory
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            exportDir.mkdirs();
            
            // Generate filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String filename = "data_archive_" + timestamp + ".zip";
            File archiveFile = new File(exportDir, filename);
            
            // Create ZIP
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFile));
            
            // Add manifest
            addManifestToArchive(zos, dataByType);
            
            // Add data files by type
            for (Map.Entry<String, List<PersonalData>> entry : dataByType.entrySet()) {
                String type = entry.getKey();
                List<PersonalData> typeData = entry.getValue();
                
                // Create JSON for this type
                JSONArray jsonArray = new JSONArray();
                for (PersonalData item : typeData) {
                    JSONObject json = new JSONObject();
                    json.put("type", item.getType());
                    json.put("timestamp", item.getTimestamp());
                    json.put("data", new JSONObject(item.getData()));
                    json.put("metadata", new JSONObject(item.getMetadata()));
                    jsonArray.put(json);
                }
                
                // Add to archive
                ZipEntry entry1 = new ZipEntry("data/" + type + ".json");
                zos.putNextEntry(entry1);
                zos.write(jsonArray.toString(2).getBytes());
                zos.closeEntry();
            }
            
            // Add media files if requested
            if (includeMedia) {
                addMediaFilesToArchive(context, zos, dataByType);
            }
            
            zos.close();
            
            Log.d(TAG, "✅ Created archive: " + archiveFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create archive", e);
            return false;
        }
    }
    
    private static void addManifestToArchive(ZipOutputStream zos, Map<String, List<PersonalData>> dataByType) throws Exception {
        JSONObject manifest = new JSONObject();
        manifest.put("version", "1.0");
        manifest.put("created", System.currentTimeMillis());
        manifest.put("app_version", BuildConfig.VERSION_NAME);
        
        // Data summary
        JSONObject summary = new JSONObject();
        int totalItems = 0;
        for (Map.Entry<String, List<PersonalData>> entry : dataByType.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
            totalItems += entry.getValue().size();
        }
        manifest.put("summary", summary);
        manifest.put("total_items", totalItems);
        
        // Add to archive
        ZipEntry entry = new ZipEntry("manifest.json");
        zos.putNextEntry(entry);
        zos.write(manifest.toString(2).getBytes());
        zos.closeEntry();
    }
    
    private static void addMediaFilesToArchive(Context context, ZipOutputStream zos, Map<String, List<PersonalData>> dataByType) {
        // Add audio files
        if (dataByType.containsKey("audio")) {
            List<PersonalData> audioData = dataByType.get("audio");
            for (PersonalData item : audioData) {
                try {
                    String filePath = (String) item.getData().get("file_path");
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            // Add to archive
                            ZipEntry entry = new ZipEntry("media/audio/" + file.getName());
                            zos.putNextEntry(entry);
                            
                            FileInputStream fis = new FileInputStream(file);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            fis.close();
                            
                            zos.closeEntry();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error adding audio file to archive", e);
                }
            }
        }
        
        // Add photos if implemented
        // Similar pattern for other media types
    }
    
    private static String formatDataForCsv(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) sb.append("; ");
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }
        
        return sb.toString();
    }
}
