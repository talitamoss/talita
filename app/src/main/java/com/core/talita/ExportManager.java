package com.core.talita;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
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
 * 
 * Supports JSON, CSV, and ZIP archive exports
 */
public class ExportManager {
    private static final String TAG = "ExportManager";
    private static final String EXPORT_DIR = "exports";
    
    /**
     * Export data to JSON format
     */
    public static String exportToJson(Context context, List<PersonalData> dataList, String type) 
            throws Exception {
        
        // Create export directory
        File exportDir = new File(context.getFilesDir(), EXPORT_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // Create filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String filename = type + "_export_" + timestamp + ".json";
        File exportFile = new File(exportDir, filename);
        
        // Build JSON
        JSONObject root = new JSONObject();
        root.put("export_type", type);
        root.put("export_date", System.currentTimeMillis());
        root.put("export_version", "1.0");
        root.put("item_count", dataList.size());
        
        JSONArray items = new JSONArray();
        for (PersonalData data : dataList) {
            items.put(new JSONObject(data.toJson()));
        }
        root.put("data", items);
        
        // Write to file
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write(root.toString(2)); // Pretty print
        }
        
        Log.d(TAG, "Exported " + dataList.size() + " items to: " + exportFile.getAbsolutePath());
        return exportFile.getAbsolutePath();
    }
    
    /**
     * Export data to JSON with progress dialog
     */
    public static String exportToJson(Context context, File exportFile, String type,
                                    boolean includeLocation, boolean includeAudio,
                                    ProgressDialog progressDialog) throws Exception {
        
        UniversalDataService dataService = UniversalDataService.getInstance(context);
        List<PersonalData> dataList = dataService.getDataByType(type);
        
        // Filter based on options
        List<PersonalData> filteredData = new ArrayList<>();
        for (PersonalData data : dataList) {
            String dataType = data.getType();
            
            // Skip location data if not included
            if (!includeLocation && "location".equals(dataType)) {
                continue;
            }
            
            // Skip audio data if not included
            if (!includeAudio && "audio".equals(dataType)) {
                continue;
            }
            
            filteredData.add(data);
        }
        
        // Update progress
        if (progressDialog != null) {
            progressDialog.setMax(filteredData.size());
        }
        
        // Build JSON
        JSONObject root = new JSONObject();
        root.put("export_type", type);
        root.put("export_date", System.currentTimeMillis());
        root.put("export_version", "1.0");
        root.put("item_count", filteredData.size());
        
        JSONArray items = new JSONArray();
        int progress = 0;
        for (PersonalData data : filteredData) {
            items.put(new JSONObject(data.toJson()));
            
            progress++;
            if (progressDialog != null) {
                final int currentProgress = progress;
                progressDialog.setProgress(currentProgress);
            }
        }
        root.put("data", items);
        
        // Write to file
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write(root.toString(2));
        }
        
        return exportFile.getAbsolutePath();
    }
    
    /**
     * Export from content URI (for import functionality)
     */
    public static List<PersonalData> exportToJson(Context context, Uri uri) throws Exception {
        List<PersonalData> importedData = new ArrayList<>();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            
            // Parse JSON
            JSONObject root = new JSONObject(jsonBuilder.toString());
            JSONArray dataArray = root.getJSONArray("data");
            
            for (int i = 0; i < dataArray.length(); i++) {
                String dataJson = dataArray.getJSONObject(i).toString();
                PersonalData data = PersonalData.fromJson(dataJson);
                if (data != null) {
                    importedData.add(data);
                }
            }
        }
        
        return importedData;
    }
    
    /**
     * Export data to CSV format
     */
    public static String exportToCsv(Context context, List<PersonalData> dataList, String type) 
            throws Exception {
        
        // Create export directory
        File exportDir = new File(context.getFilesDir(), EXPORT_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // Create filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String filename = type + "_export_" + timestamp + ".csv";
        File exportFile = new File(exportDir, filename);
        
        // Write CSV
        try (FileWriter writer = new FileWriter(exportFile)) {
            // Write header
            writer.write("ID,Timestamp,Type,Value,Summary\n");
            
            // Write data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (PersonalData data : dataList) {
                writer.write(String.format("%s,%s,%s,%s,%s\n",
                    UUID.randomUUID().toString(),
                    dateFormat.format(new Date(data.getTimestamp())),
                    data.getType(),
                    data.getData().toString().replace(",", ";"), // Escape commas
                    createSummary(data).replace(",", ";")
                ));
            }
        }
        
        Log.d(TAG, "Exported " + dataList.size() + " items to CSV: " + exportFile.getAbsolutePath());
        return exportFile.getAbsolutePath();
    }
    
    /**
     * Export data to ZIP archive
     */
    public static String exportToArchive(Context context, Map<String, List<PersonalData>> dataByType,
                                       boolean includeMetadata) throws Exception {
        
        // Create export directory
        File exportDir = new File(context.getFilesDir(), EXPORT_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // Create filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String filename = "data_archive_" + timestamp + ".zip";
        File exportFile = new File(exportDir, filename);
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(exportFile))) {
            // Add manifest
            if (includeMetadata) {
                addManifestToZip(zos, dataByType);
            }
            
            // Add data files by type
            for (Map.Entry<String, List<PersonalData>> entry : dataByType.entrySet()) {
                String type = entry.getKey();
                List<PersonalData> dataList = entry.getValue();
                
                // Create JSON for this type
                JSONArray items = new JSONArray();
                for (PersonalData data : dataList) {
                    items.put(new JSONObject(data.toJson()));
                }
                
                // Add to ZIP
                ZipEntry zipEntry = new ZipEntry(type + ".json");
                zos.putNextEntry(zipEntry);
                zos.write(items.toString(2).getBytes());
                zos.closeEntry();
            }
        }
        
        Log.d(TAG, "Created archive: " + exportFile.getAbsolutePath());
        return exportFile.getAbsolutePath();
    }
    
    /**
     * Add manifest file to ZIP
     */
    private static void addManifestToZip(ZipOutputStream zos, Map<String, List<PersonalData>> dataByType) 
            throws Exception {
        
        JSONObject manifest = new JSONObject();
        manifest.put("export_date", System.currentTimeMillis());
        manifest.put("export_version", "1.0");
        manifest.put("app_version", BuildConfig.VERSION_NAME);
        
        // Add type summary
        JSONObject typeSummary = new JSONObject();
        for (Map.Entry<String, List<PersonalData>> entry : dataByType.entrySet()) {
            typeSummary.put(entry.getKey(), entry.getValue().size());
        }
        manifest.put("data_types", typeSummary);
        
        ZipEntry manifestEntry = new ZipEntry("manifest.json");
        zos.putNextEntry(manifestEntry);
        zos.write(manifest.toString(2).getBytes());
        zos.closeEntry();
    }
    
    /**
     * Create summary for data
     */
    private static String createSummary(PersonalData data) {
        switch (data.getType()) {
            case "water":
                Object amount = data.getValue("amount");
                return amount != null ? amount + "ml" : "Water logged";
                
            case "mood":
                Object mood = data.getValue("mood");
                return mood != null ? mood.toString() : "Mood logged";
                
            case "exercise":
                Object activity = data.getValue("activity");
                Object duration = data.getValue("duration");
                if (activity != null) {
                    return activity + (duration != null ? " - " + duration + " min" : "");
                }
                return "Exercise logged";
                
            default:
                return data.getType() + " recorded";
        }
    }
}
