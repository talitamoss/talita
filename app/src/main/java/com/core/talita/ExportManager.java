package com.core.talita;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ExportManager - Handles exporting data in various formats
 * Fixed to match VaultActivity's usage
 */
public class ExportManager {
    private static final String TAG = "ExportManager";
    
    /**
     * Export data to JSON format - Enhanced version for VaultActivity
     */
    public static void exportToJson(Context context, File outputFile, String dateRange,
                                   boolean includeAudio, boolean encrypted, ProgressDialog progressDialog) {
        new Thread(() -> {
            try {
                if (progressDialog != null) {
                    ((Activity) context).runOnUiThread(() -> progressDialog.show());
                }
                
                // Get data based on date range
                List<PersonalData> data = getDataForExport(context, dateRange);
                
                // Create JSON
                String jsonContent = createJsonExport(data);
                
                // Write to file
                FileWriter writer = new FileWriter(outputFile);
                writer.write(jsonContent);
                writer.close();
                
                // Convert File to Uri for the simple version
                Uri fileUri = Uri.fromFile(outputFile);
                
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Export completed: " + data.size() + " items", 
                        Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Export failed", e);
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Export data to JSON format - Simple version
     */
    public static void exportToJson(Context context, Uri uri) {
        exportToJson(context, new File(uri.getPath()), "all", false, false, null);
    }
    
    /**
     * Export data to CSV format - Enhanced version for VaultActivity
     */
    public static void exportToCsv(Context context, File outputFile, String dateRange,
                                  boolean includeAudio, boolean encrypted, ProgressDialog progressDialog) {
        new Thread(() -> {
            try {
                if (progressDialog != null) {
                    ((Activity) context).runOnUiThread(() -> progressDialog.show());
                }
                
                // Get data based on date range
                List<PersonalData> data = getDataForExport(context, dateRange);
                
                // Create CSV
                PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
                
                // Write header
                writer.println("ID,Date,Timestamp,Type,Value,Summary");
                
                // Write data
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                    Locale.getDefault());
                
                for (PersonalData item : data) {
                    String id = item.hashCode() + "";
                    String date = dateFormat.format(new Date(item.getTimestamp()));
                    String type = escapeCSV(item.getDataType());
                    String value = escapeCSV(String.valueOf(item.getValue()));
                    String summary = escapeCSV(item.getDisplaySummary());
                    
                    writer.println(String.format("%s,%s,%d,%s,%s,%s",
                        id, date, item.getTimestamp(), type, value, summary));
                }
                
                writer.close();
                
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "CSV export completed: " + data.size() + " items", 
                        Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "CSV export failed", e);
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Export data to CSV format - Simple version
     */
    public static void exportToCsv(Context context, Uri uri) {
        exportToCsv(context, new File(uri.getPath()), "all", false, false, null);
    }
    
    /**
     * Export as archive (ZIP) - For VaultActivity
     */
    public static void exportToArchive(Context context, File outputFile, String dateRange,
                                      boolean includeAudio, boolean encrypted, ProgressDialog progressDialog) {
        new Thread(() -> {
            try {
                if (progressDialog != null) {
                    ((Activity) context).runOnUiThread(() -> {
                        progressDialog.setMessage("Creating archive...");
                        progressDialog.show();
                    });
                }
                
                FileOutputStream fos = new FileOutputStream(outputFile);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                
                // Add data.json
                List<PersonalData> data = getDataForExport(context, dateRange);
                String jsonData = createJsonExport(data);
                
                ZipEntry jsonEntry = new ZipEntry("data.json");
                zipOut.putNextEntry(jsonEntry);
                zipOut.write(jsonData.getBytes());
                zipOut.closeEntry();
                
                // Add audio files if requested
                if (includeAudio) {
                    File audioDir = new File(context.getFilesDir(), "audio");
                    if (audioDir.exists()) {
                        File[] audioFiles = audioDir.listFiles();
                        if (audioFiles != null) {
                            int fileCount = 0;
                            for (File file : audioFiles) {
                                if (file.getName().endsWith(".enc") || file.getName().endsWith(".3gp")) {
                                    ZipEntry audioEntry = new ZipEntry("audio/" + file.getName());
                                    zipOut.putNextEntry(audioEntry);
                                    
                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = fis.read(buffer)) > 0) {
                                        zipOut.write(buffer, 0, length);
                                    }
                                    fis.close();
                                    zipOut.closeEntry();
                                    fileCount++;
                                    
                                    final int progress = fileCount;
                                    if (progressDialog != null) {
                                        ((Activity) context).runOnUiThread(() -> 
                                            progressDialog.setMessage("Adding audio files... " + progress));
                                    }
                                }
                            }
                        }
                    }
                }
                
                zipOut.close();
                fos.close();
                
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Archive created successfully", 
                        Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Archive export failed", e);
                ((Activity) context).runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(context, "Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Export as archive - Simple version
     */
    public static void exportAsArchive(Context context, Uri uri) {
        exportToArchive(context, new File(uri.getPath()), "all", true, false, null);
    }
    
    /**
     * Get data for export based on date range
     */
    private static List<PersonalData> getDataForExport(Context context, String dateRange) {
        UniversalDataService dataService = new UniversalDataService(context);
        
        long endTime = System.currentTimeMillis();
        long startTime;
        
        switch (dateRange.toLowerCase()) {
            case "today":
                startTime = getStartOfDay();
                break;
            case "week":
                startTime = endTime - (7L * 24 * 60 * 60 * 1000);
                break;
            case "month":
                startTime = endTime - (30L * 24 * 60 * 60 * 1000);
                break;
            case "year":
                startTime = endTime - (365L * 24 * 60 * 60 * 1000);
                break;
            default:
                startTime = 0; // All data
        }
        
        return dataService.getDataInRange(startTime, endTime);
    }
    
    /**
     * Get start of current day in milliseconds
     */
    private static long getStartOfDay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * Create JSON export string
     */
    private static String createJsonExport(List<PersonalData> data) throws Exception {
        JSONObject root = new JSONObject();
        root.put("export_version", "1.0");
        root.put("export_date", System.currentTimeMillis());
        root.put("app_name", "Talita");
        
        JSONArray dataArray = new JSONArray();
        for (PersonalData item : data) {
            JSONObject obj = new JSONObject();
            obj.put("type", item.getDataType());
            obj.put("timestamp", item.getTimestamp());
            obj.put("summary", item.getDisplaySummary());
            obj.put("value", String.valueOf(item.getValue()));
            
            // Add metadata if it's UniversalPersonalData
            if (item instanceof UniversalPersonalData) {
                UniversalPersonalData universalData = (UniversalPersonalData) item;
                obj.put("metadata", new JSONObject(universalData.getAllData()));
            }
            
            dataArray.put(obj);
        }
        
        root.put("data", dataArray);
        root.put("total_items", data.size());
        
        return root.toString(2);
    }
    
    /**
     * Escape CSV values
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
}
