package com.core.talita;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VaultActivity - Data management and export
 */
public class VaultActivity extends AppCompatActivity {
    
    private static final String TAG = "VaultActivity";
    
    private UniversalDataService dataService;
    private LocalDataManager localDataManager;
    private TextView totalDataItemsText;
    private TextView databaseSizeText;
    private TextView oldestDataText;
    private TextView exportStatusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        
        dataService = UniversalDataService.getInstance(this);
        localDataManager = new LocalDataManager(this);
        
        initializeViews();
        setupExportOptions();
        updateDataStats();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateDataStats();
    }
    
    private void initializeViews() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        totalDataItemsText = findViewById(R.id.total_data_items_text);
        databaseSizeText = findViewById(R.id.database_size_text);
        oldestDataText = findViewById(R.id.oldest_data_text);
        exportStatusText = findViewById(R.id.export_status_text);
    }
    
    private void setupExportOptions() {
        // Export as CSV
        CardView exportCsvCard = findViewById(R.id.export_csv_card);
        exportCsvCard.setOnClickListener(v -> showExportDialog("csv"));
        
        // Export as JSON
        CardView exportJsonCard = findViewById(R.id.export_json_card);
        exportJsonCard.setOnClickListener(v -> showExportDialog("json"));
        
        // Export encrypted archive
        CardView exportArchiveCard = findViewById(R.id.export_archive_card);
        exportArchiveCard.setOnClickListener(v -> showExportDialog("archive"));
        
        // Clear all data
        CardView clearDataCard = findViewById(R.id.clear_data_card);
        clearDataCard.setOnClickListener(v -> showClearDataDialog());
        
        // Import data
        CardView importDataCard = findViewById(R.id.import_data_card);
        importDataCard.setOnClickListener(v -> showImportDialog());
    }
    
    private void updateDataStats() {
        new Thread(() -> {
            try {
                // Get stats
                Map<String, Integer> stats = dataService.getDataStats();
                int totalItems = 0;
                for (int count : stats.values()) {
                    totalItems += count;
                }
                
                // Database size
                File dbFile = getDatabasePath(AppConstants.DATABASE_NAME);
                long dbSize = dbFile.exists() ? dbFile.length() : 0;
                String sizeStr = formatFileSize(dbSize);
                
                // Oldest data
                List<PersonalData> allData = dataService.getAllData();
                String oldestStr = "No data";
                if (!allData.isEmpty()) {
                    // Sort by timestamp
                    Collections.sort(allData, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
                    PersonalData oldest = allData.get(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    oldestStr = sdf.format(new Date(oldest.getTimestamp()));
                }
                
                final int items = totalItems;
                final String size = sizeStr;
                final String oldest = oldestStr;
                
                runOnUiThread(() -> {
                    totalDataItemsText.setText(items + " items");
                    databaseSizeText.setText(size);
                    oldestDataText.setText(oldest);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private void showExportDialog(String format) {
        String[] options = {"All Time", "Last 7 Days", "Last 30 Days", "Custom Range"};
        
        new AlertDialog.Builder(this)
                .setTitle("Select Export Range")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportData(format, "all", true, true);
                            break;
                        case 1:
                            exportData(format, "week", true, true);
                            break;
                        case 2:
                            exportData(format, "month", true, true);
                            break;
                        case 3:
                            // TODO: Show date picker dialog
                            Toast.makeText(this, "Custom range coming soon", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }
    
    private void exportData(String format, String range, boolean includeLocation, boolean includeMedia) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Exporting data...");
        progress.show();
        
        new Thread(() -> {
            try {
                // Create export directory
                File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
                exportDir.mkdirs();
                
                // Generate filename
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timestamp = sdf.format(new Date());
                String filename = "data_export_" + timestamp + "." + format;
                File exportFile = new File(exportDir, filename);
                
                // Get data based on range
                List<PersonalData> dataToExport;
                if ("all".equals(range)) {
                    dataToExport = dataService.getAllData();
                } else {
                    long endTime = System.currentTimeMillis();
                    long startTime = endTime;
                    
                    switch (range) {
                        case "week":
                            startTime = endTime - (7L * 24 * 60 * 60 * 1000);
                            break;
                        case "month":
                            startTime = endTime - (30L * 24 * 60 * 60 * 1000);
                            break;
                    }
                    
                    dataToExport = dataService.getDataForTimeRange(startTime, endTime);
                }
                
                // Export based on format
                boolean success = false;
                switch (format) {
                    case "csv":
                        success = ExportManager.exportToCsv(this, dataToExport, exportFile.getAbsolutePath());
                        break;
                    case "json":
                        success = ExportManager.exportToJson(this, dataToExport, exportFile.getAbsolutePath());
                        break;
                    case "archive":
                        Map<String, List<PersonalData>> exportMap = dataService.exportAllData();
                        success = ExportManager.exportToArchive(this, exportMap, includeMedia);
                        break;
                }
                
                final boolean exportSuccess = success;
                final File finalFile = exportFile;
                
                runOnUiThread(() -> {
                    progress.dismiss();
                    
                    if (exportSuccess) {
                        exportStatusText.setText("Last export: " + new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(new Date()));
                        
                        // Offer to share
                        new AlertDialog.Builder(this)
                                .setTitle("Export Complete")
                                .setMessage("Data exported to:\n" + finalFile.getName())
                                .setPositiveButton("Share", (d, w) -> shareFile(finalFile))
                                .setNegativeButton("OK", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(shareIntent, "Share Export"));
    }
    
    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Clear All Data")
                .setMessage("This will permanently delete all your data. This cannot be undone.\n\nAre you sure?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    // Double confirmation
                    new AlertDialog.Builder(this)
                            .setTitle("Final Confirmation")
                            .setMessage("This is your last chance. Delete everything?")
                            .setPositiveButton("Yes, Delete All", (d, w) -> clearAllData())
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void clearAllData() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Clearing data...");
        progress.show();
        
        new Thread(() -> {
            try {
                // Clear database
                localDataManager.deleteDatabase();
                
                // Clear encrypted files
                File encryptedDir = new File(getFilesDir(), "encrypted");
                if (encryptedDir.exists()) {
                    deleteRecursive(encryptedDir);
                }
                
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                    updateDataStats();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(this, "Error clearing data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
    
    private void showImportDialog() {
        Toast.makeText(this, "Import feature coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement import functionality
    }
}
