package com.core.talita;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.core.content.FileProvider;
import android.app.ProgressDialog;

/**
 * Vault Activity - User's data sovereignty center
 * Import, export, and manage data privacy
 */
public class VaultActivity extends AppCompatActivity {
    private static final String TAG = "VaultActivity";
    private static final int PICK_FILE_REQUEST = 1001;
    
    // UI Components
    private TextView storageUsedText;
    private ProgressBar storageProgressBar;
    private CardView exportJsonCard;
    private CardView exportCsvCard;
    private CardView exportArchiveCard;
    private CardView importDataCard;
    private Switch encryptionSwitch;
    private TextView encryptionStatusText;
    private TextView lastBackupText;
    private Button clearDataButton;
    private BottomNavigationView bottomNav;
    
    // Services
    private UniversalDataService dataService;
    private LocalDataManager localDataManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        
        dataService = new UniversalDataService(this);
        localDataManager = new LocalDataManager(this);
        
        setupViews();
        updateStorageInfo();
        NavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_vault);
    }
    
    private void setupViews() {
        // Find views
        storageUsedText = findViewById(R.id.storage_used_text);
        storageProgressBar = findViewById(R.id.storage_progress);
        exportJsonCard = findViewById(R.id.export_json_card);
        exportCsvCard = findViewById(R.id.export_csv_card);
        exportArchiveCard = findViewById(R.id.export_archive_card);
        importDataCard = findViewById(R.id.import_data_card);
        encryptionSwitch = findViewById(R.id.encryption_switch);
        encryptionStatusText = findViewById(R.id.encryption_status_text);
        lastBackupText = findViewById(R.id.last_backup_text);
        clearDataButton = findViewById(R.id.clear_data_button);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        // Set click listeners
        exportJsonCard.setOnClickListener(v -> exportData("json"));
        exportCsvCard.setOnClickListener(v -> exportData("csv"));
        exportArchiveCard.setOnClickListener(v -> exportData("archive"));
        importDataCard.setOnClickListener(v -> importData());
        clearDataButton.setOnClickListener(v -> showClearDataDialog());
        
        // Encryption is always on
        encryptionSwitch.setChecked(true);
        encryptionSwitch.setEnabled(false);
        encryptionStatusText.setText("AES-256-GCM • Hardware-backed");
        
        // Update last backup time
        updateLastBackupTime();
    }
    
    private void updateStorageInfo() {
        // Calculate storage used
        long totalSize = localDataManager.getDatabaseSize();
        String sizeText = formatFileSize(totalSize);
        
        storageUsedText.setText(sizeText + " encrypted");
        
        // Update progress bar (assume 100MB limit for now)
        long maxSize = 100 * 1024 * 1024; // 100MB
        int progress = (int) ((totalSize * 100) / maxSize);
        storageProgressBar.setProgress(Math.min(progress, 100));
    }
    
    private void exportData(String format) {
        // Show export dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Data");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_options, null);
        CheckBox includeLocationCheck = dialogView.findViewById(R.id.include_location_check);
        CheckBox includeAudioCheck = dialogView.findViewById(R.id.include_audio_check);
        Spinner dateRangeSpinner = dialogView.findViewById(R.id.date_range_spinner);
        
        // Setup date range options
        String[] dateRanges = {"All time", "Last 30 days", "Last 7 days", "Today"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, dateRanges);
        dateRangeSpinner.setAdapter(adapter);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Export", (dialog, which) -> {
            boolean includeLocation = includeLocationCheck.isChecked();
            boolean includeAudio = includeAudioCheck.isChecked();
            String dateRange = (String) dateRangeSpinner.getSelectedItem();
            
            performExport(format, includeLocation, includeAudio, dateRange);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void performExport(String format, boolean includeLocation, 
                              boolean includeAudio, String dateRange) {
        // Show progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Exporting data...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        
        // Export in background
        new Thread(() -> {
            try {
                // Create export file
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", 
                    Locale.getDefault()).format(new Date());
                String fileName = "data_export_" + timestamp;
                
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "YourApp");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                File exportFile = null;
                switch (format) {
                    case "json":
                        exportFile = new File(exportDir, fileName + ".json");
                        ExportManager.exportToJson(this, exportFile, dateRange, 
                            includeLocation, includeAudio, progressDialog);
                        break;
                    case "csv":
                        exportFile = new File(exportDir, fileName + ".csv");
                        ExportManager.exportToCsv(this, exportFile, dateRange, 
                            includeLocation, includeAudio, progressDialog);
                        break;
                    case "archive":
                        exportFile = new File(exportDir, fileName + ".zip");
                        ExportManager.exportToArchive(this, exportFile, dateRange, 
                            includeLocation, includeAudio, progressDialog);
                        break;
                }
                
                File finalExportFile = exportFile;
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showExportSuccess(finalExportFile);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void showExportSuccess(File exportFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Complete");
        builder.setMessage("Data exported to:\n" + exportFile.getPath());
        builder.setPositiveButton("Share", (dialog, which) -> {
            shareFile(exportFile);
        });
        builder.setNegativeButton("OK", null);
        builder.show();
    }
    
    private void shareFile(File file) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri fileUri = FileProvider.getUriForFile(this, 
            getPackageName() + ".fileprovider", file);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share export"));
    }
    
    private void importData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
            Intent.createChooser(intent, "Select file to import"), 
            PICK_FILE_REQUEST
        );
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                performImport(fileUri);
            }
        }
    }
    
    private void performImport(Uri fileUri) {
        // Show import options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Options");
        builder.setMessage("How would you like to import this data?");
        builder.setPositiveButton("Merge", (dialog, which) -> {
            ImportManager.importData(this, fileUri, false);
        });
        builder.setNeutralButton("Replace", (dialog, which) -> {
            showReplaceWarning(fileUri);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showReplaceWarning(Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Warning");
        builder.setMessage("This will DELETE all existing data and replace it with the imported data. This cannot be undone!");
        builder.setPositiveButton("Replace All", (dialog, which) -> {
            ImportManager.importData(this, fileUri, true);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showClearDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All Data");
        builder.setMessage("Are you sure? This will permanently delete all your data. This cannot be undone!");
        builder.setPositiveButton("Clear All", (dialog, which) -> {
            // Double confirmation
            AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
            confirmBuilder.setTitle("Final Confirmation");
            confirmBuilder.setMessage("Type 'DELETE' to confirm");
            
            EditText input = new EditText(this);
            confirmBuilder.setView(input);
            
            confirmBuilder.setPositiveButton("Confirm", (d, w) -> {
                if ("DELETE".equals(input.getText().toString())) {
                    clearAllData();
                } else {
                    Toast.makeText(this, "Confirmation failed", Toast.LENGTH_SHORT).show();
                }
            });
            confirmBuilder.setNegativeButton("Cancel", null);
            confirmBuilder.show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void clearAllData() {
        localDataManager.clearAllData();
        updateStorageInfo();
        Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
    }
    
    private void updateLastBackupTime() {
        // TODO: Get actual last backup time
        lastBackupText.setText("Never backed up");
    }
    
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", 
            size / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}
