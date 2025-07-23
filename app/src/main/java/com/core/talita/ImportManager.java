package com.core.talita;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ImportManager - Handles importing data from various formats
 * Supports JSON, CSV, and ZIP archive imports
 */
public class ImportManager {
    private static final String TAG = "ImportManager";
    
    /**
     * Import data from URI
     */
    public static void importData(Context context, Uri uri, boolean replaceAll) {
        new Thread(() -> {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Importing data...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            
            ((Activity) context).runOnUiThread(() -> progressDialog.show());
            
            try {
                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(uri);
                String fileName = getFileName(context, uri);
                
                if (fileName.endsWith(".json")) {
                    importFromJson(context, inputStream, replaceAll, progressDialog);
                } else if (fileName.endsWith(".csv")) {
                    importFromCsv(context, inputStream, replaceAll, progressDialog);
                } else if (fileName.endsWith(".zip")) {
                    importFromArchive(context, inputStream, replaceAll, progressDialog);
                }
                
                inputStream.close();
                
                // Success notification
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Import completed successfully", 
                        Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Import failed", e);
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Import failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Import from JSON format
     */
    private static void importFromJson(Context context, InputStream inputStream, 
                                      boolean replaceAll, ProgressDialog progressDialog) 
                                      throws Exception {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();
        
        JSONObject root = new JSONObject(jsonBuilder.toString());
        
        // Check version compatibility
        String exportVersion = root.optString("export_version", "1.0");
        if (!isVersionCompatible(exportVersion)) {
            throw new Exception("Incompatible export version: " + exportVersion);
        }
        
        // Initialize services
        LocalDataManager localDataManager = new LocalDataManager(context);
        UniversalDataService dataService = new UniversalDataService(context);
        
        if (replaceAll) {
            localDataManager.deleteAllData();
        }
        
        // Import data array
        JSONArray dataArray = root.getJSONArray("data");
        int total = dataArray.length();
        
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            
            String type = dataObject.getString("type");
            long timestamp = dataObject.getLong("timestamp");
            
            // Create data map
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("timestamp", timestamp);
            dataMap.put("display_name", dataObject.optString("summary", type));
            
            // Add metadata if exists
            if (dataObject.has("metadata")) {
                JSONObject metadata = dataObject.getJSONObject("metadata");
                Iterator<String> keys = metadata.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    dataMap.put(key, metadata.get(key));
                }
            }
            
            // Create and save data
            UniversalPersonalData data = new UniversalPersonalData(type, dataMap);
            dataService.capture(new PersonalDataAdapter(data));
            
            // Update progress
            final int progress = ((i + 1) * 100) / total;
            ((Activity) context).runOnUiThread(() -> progressDialog.setProgress(progress));
        }
        
        Log.d(TAG, "Imported " + total + " items from JSON");
    }
    
    /**
     * Import from CSV format
     */
    private static void importFromCsv(Context context, InputStream inputStream,
                                     boolean replaceAll, ProgressDialog progressDialog)
                                     throws Exception {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        UniversalDataService dataService = new UniversalDataService(context);
        LocalDataManager localDataManager = new LocalDataManager(context);
        
        if (replaceAll) {
            localDataManager.deleteAllData();
        }
        
        // Skip header
        String line = reader.readLine();
        
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        
        int total = lines.size();
        int processed = 0;
        
        for (String csvLine : lines) {
            String[] parts = parseCSVLine(csvLine);
            if (parts.length >= 6) {
                long timestamp = Long.parseLong(parts[2]);
                String type = parts[3];
                String value = parts[4];
                String summary = parts[5];
                
                // Create data map
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("timestamp", timestamp);
                dataMap.put("value", value);
                dataMap.put("display_name", summary);
                
                // Create and save data
                UniversalPersonalData data = new UniversalPersonalData(type, dataMap);
                dataService.capture(new PersonalDataAdapter(data));
            }
            
            processed++;
            final int progress = (processed * 100) / total;
            ((Activity) context).runOnUiThread(() -> progressDialog.setProgress(progress));
        }
        
        Log.d(TAG, "Imported " + processed + " items from CSV");
    }
    
    /**
     * Import from ZIP archive
     */
    private static void importFromArchive(Context context, InputStream inputStream,
                                         boolean replaceAll, ProgressDialog progressDialog)
                                         throws Exception {
        
        // Create temp directory
        File tempDir = new File(context.getCacheDir(), "import_" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        // Extract archive
        ZipInputStream zipIn = new ZipInputStream(inputStream);
        ZipEntry entry;
        
        while ((entry = zipIn.getNextEntry()) != null) {
            File file = new File(tempDir, entry.getName());
            
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            
            zipIn.closeEntry();
        }
        zipIn.close();
        
        // Import data.json if exists
        File dataFile = new File(tempDir, "data.json");
        if (dataFile.exists()) {
            FileInputStream fis = new FileInputStream(dataFile);
            importFromJson(context, fis, replaceAll, progressDialog);
            fis.close();
        }
        
        // Import audio files if exist
        File audioDir = new File(tempDir, "audio");
        if (audioDir.exists()) {
            importAudioFiles(context, audioDir);
        }
        
        // Clean up temp directory
        deleteRecursive(tempDir);
        
        Log.d(TAG, "Imported archive successfully");
    }
    
    /**
     * Import audio files from directory
     */
    private static void importAudioFiles(Context context, File sourceDir) {
        File destDir = new File(context.getFilesDir(), "audio");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".enc")) {
                File destFile = new File(destDir, file.getName());
                try {
                    copyFile(file, destFile);
                    Log.d(TAG, "Imported audio file: " + file.getName());
                } catch (IOException e) {
                    Log.e(TAG, "Failed to import audio: " + file.getName(), e);
                }
            }
        }
    }
    
    /**
     * Parse CSV line handling quoted values
     */
    private static String[] parseCSVLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
    
    /**
     * Check if export version is compatible
     */
    private static boolean isVersionCompatible(String version) {
        // For now, only support version 1.0
        return "1.0".equals(version);
    }
    
    /**
     * Get file name from URI
     */
    private static String getFileName(Context context, Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1) {
                return path.substring(lastSlash + 1);
            }
        }
        return "unknown";
    }
    
    /**
     * Copy file
     */
    private static void copyFile(File source, File dest) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        
        fis.close();
        fos.close();
    }
    
    /**
     * Delete directory recursively
     */
    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
