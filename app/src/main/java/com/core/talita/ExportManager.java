package com.core.talita;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Export Manager - Handles exporting user data in various formats
 */
public class ExportManager {
    private static final String TAG = "ExportManager";
    
    /**
     * Export data to JSON format
     */
    public static void exportToJson(Context context, File outputFile, String dateRange,
                                   boolean includeLocation, boolean includeAudio,
                                   ProgressDialog progressDialog) throws Exception {
        
        UniversalDataService dataService = new UniversalDataService(context);
        List<PersonalData> data = getDataForDateRange(dataService, dateRange);
        
        JSONObject root = new JSONObject();
        root.put("export_version", "1.0");
        root.put("export_date", System.currentTimeMillis());
        root.put("app_version", BuildConfig.VERSION_NAME);
        
        JSONArray dataArray = new JSONArray();
        int total = data.size();
        int processed = 0;
        
        for (PersonalData item : data) {
            // Skip location data if not included
            if (!includeLocation && item.getDataType().equals("location")) {
                continue;
            }
            
            // Skip audio data if not included
            if (!includeAudio && item.getDataType().equals("audio")) {
                continue;
            }
            
            JSONObject dataObject = new JSONObject();
            dataObject.put("type", item.getDataType());
            dataObject.put("timestamp", item.getTimestamp());
            dataObject.put("value", item.getValue());
            dataObject.put("summary", item.getDisplaySummary());
            
            // Add any metadata
            if (item instanceof UniversalPersonalData) {
                UniversalPersonalData universalData = (UniversalPersonalData) item;
                JSONObject metadata = new JSONObject(universalData.getAllData());
                dataObject.put("metadata", metadata);
            }
            
            dataArray.put(dataObject);
            
            processed++;
            final int progress = (processed * 100) / total;
            progressDialog.post(() -> progressDialog.setProgress(progress));
        }
        
        root.put("data", dataArray);
        root.put("total_entries", dataArray.length());
        
        // Write to file
        FileWriter writer = new FileWriter(outputFile);
        writer.write(root.toString(2)); // Pretty print with 2 space indent
        writer.close();
        
        Log.d(TAG, "Exported " + dataArray.length() + " entries to JSON");
    }
    
    /**
     * Export data to CSV format
     */
    public static void exportToCsv(Context context, File outputFile, String dateRange,
                                  boolean includeLocation, boolean includeAudio,
                                  ProgressDialog progressDialog) throws Exception {
        
        UniversalDataService dataService = new UniversalDataService(context);
        List<PersonalData> data = getDataForDateRange(dataService, dateRange);
        
        FileWriter writer = new FileWriter(outputFile);
        
        // Write header
        writer.write("Timestamp,Date,Time,Type,Value,Summary\n");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        int total = data.size();
        int processed = 0;
        
        for (PersonalData item : data) {
            // Skip if needed
            if (!includeLocation && item.getDataType().equals("location")) {
                continue;
            }
            if (!includeAudio && item.getDataType().equals("audio")) {
                continue;
            }
            
            Date date = new Date(item.getTimestamp());
            
            // Escape CSV values
            String type = escapeCsv(item.getDataType());
            String value = escapeCsv(String.valueOf(item.getValue()));
            String summary = escapeCsv(item.getDisplaySummary());
            
            writer.write(String.format("%d,%s,%s,%s,%s,%s\n",
                item.getTimestamp(),
                dateFormat.format(date),
                timeFormat.format(date),
                type,
                value,
                summary
            ));
            
            processed++;
            final int progress = (processed * 100) / total;
            progressDialog.post(() -> progressDialog.setProgress(progress));
        }
        
        writer.close();
        Log.d(TAG, "Exported " + processed + " entries to CSV");
    }
    
    /**
     * Export data to ZIP archive
     */
    public static void exportToArchive(Context context, File outputFile, String dateRange,
                                      boolean includeLocation, boolean includeAudio,
                                      ProgressDialog progressDialog) throws Exception {
        
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
        
        // Export metadata
        ZipEntry metaEntry = new ZipEntry("metadata.json");
        zipOut.putNextEntry(metaEntry);
        
        JSONObject metadata = new JSONObject();
        metadata.put("export_date", System.currentTimeMillis());
        metadata.put("export_version", "1.0");
        metadata.put("date_range", dateRange);
        metadata.put("include_location", includeLocation);
        metadata.put("include_audio", includeAudio);
        
        zipOut.write(metadata.toString(2).getBytes());
        zipOut.closeEntry();
        
        // Export data as JSON
        File tempJson = new File(context.getCacheDir(), "data.json");
        exportToJson(context, tempJson, dateRange, includeLocation, includeAudio, progressDialog);
        
        ZipEntry dataEntry = new ZipEntry("data.json");
        zipOut.putNextEntry(dataEntry);
        
        FileInputStream fis = new FileInputStream(tempJson);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            zipOut.write(buffer, 0, len);
        }
        
        fis.close();
        zipOut.closeEntry();
        
        // If including audio, add audio files
        if (includeAudio) {
            File audioDir = new File(context.getFilesDir(), "audio");
            if (audioDir.exists()) {
                addFolderToZip(audioDir, "audio", zipOut);
            }
        }
        
        zipOut.close();
        tempJson.delete();
        
        Log.d(TAG, "Created archive: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Get data based on date range selection
     */
    private static List<PersonalData> getDataForDateRange(UniversalDataService dataService, 
                                                         String dateRange) {
        long endTime = System.currentTimeMillis();
        long startTime;
        
        switch (dateRange) {
            case "Last 30 days":
                startTime = endTime - (30L * 24 * 60 * 60 * 1000);
                break;
            case "Last 7 days":
                startTime = endTime - (7L * 24 * 60 * 60 * 1000);
                break;
            case "Today":
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                break;
            default: // All time
                startTime = 0;
                break;
        }
        
        return dataService.getDataInRange(startTime, endTime);
    }
    
    /**
     * Escape special characters for CSV
     */
    private static String escapeCsv(String value) {
        if (value == null) return "";
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
    
    /**
     * Add folder contents to zip
     */
    private static void addFolderToZip(File folder, String parentPath, 
                                      ZipOutputStream zipOut) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToZip(file, parentPath + "/" + file.getName(), zipOut);
            } else {
                ZipEntry entry = new ZipEntry(parentPath + "/" + file.getName());
                zipOut.putNextEntry(entry);
                
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, len);
                }
                
                fis.close();
                zipOut.closeEntry();
            }
        }
    }
}
