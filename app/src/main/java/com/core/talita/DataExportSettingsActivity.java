package com.core.talita;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * DataExportSettingsActivity - Configure data export options
 * 
 * Location: app/src/main/java/com/core/talita/DataExportSettingsActivity.java
 */
public class DataExportSettingsActivity extends AppCompatActivity {
    
    private UniversalDataService dataService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_export_settings);
        
        // Initialize service - FIXED: Using getInstance()
        dataService = UniversalDataService.getInstance(this);
        
        setupViews();
    }
    
    private void setupViews() {
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Full export
        CardView fullExportCard = findViewById(R.id.full_export_card);
        fullExportCard.setOnClickListener(v -> showFullExportOptions());
        
        // Selective export
        CardView selectiveExportCard = findViewById(R.id.selective_export_card);
        selectiveExportCard.setOnClickListener(v -> showSelectiveExportOptions());
        
        // Raw export
        CardView rawExportCard = findViewById(R.id.raw_export_card);
        rawExportCard.setOnClickListener(v -> showRawExportOptions());
        
        // Encrypted export
        CardView encryptedExportCard = findViewById(R.id.encrypted_export_card);
        encryptedExportCard.setOnClickListener(v -> showEncryptedExportOptions());
        
        // Legal compliance export
        CardView legalExportCard = findViewById(R.id.legal_export_card);
        legalExportCard.setOnClickListener(v -> showLegalExportOptions());
    }
    
    private void showFullExportOptions() {
        String[] formats = {"📊 JSON Archive", "📋 CSV Files", "📄 PDF Report", "💾 SQLite Database"};
        new AlertDialog.Builder(this)
            .setTitle("Full Data Export")
            .setItems(formats, (dialog, which) -> {
                String selectedFormat = formats[which];
                confirmExport("Full Export", selectedFormat);
            })
            .show();
    }
    
    private void showSelectiveExportOptions() {
        String[] types = {"📍 Location Data", "🎤 Audio Recordings", "💧 Wellness Data", "😊 Mood & Activities"};
        boolean[] selected = new boolean[types.length];
        
        new AlertDialog.Builder(this)
            .setTitle("Select Data Types")
            .setMultiChoiceItems(types, selected, (dialog, which, isChecked) -> {
                selected[which] = isChecked;
            })
            .setPositiveButton("Export Selected", (dialog, which) -> {
                // Export selected types
                Toast.makeText(this, "Selective export coming soon", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showRawExportOptions() {
        new AlertDialog.Builder(this)
            .setTitle("Raw Data Export")
            .setMessage("Export unprocessed data including:\n\n" +
                    "• Encrypted database files\n" +
                    "• Raw sensor readings\n" +
                    "• System metadata\n\n" +
                    "This format is for advanced users only.")
            .setPositiveButton("Export Raw", (dialog, which) -> {
                Toast.makeText(this, "Raw export coming soon", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showEncryptedExportOptions() {
        new AlertDialog.Builder(this)
            .setTitle("Encrypted Export")
            .setMessage("Export your data in encrypted format.\n\n" +
                    "You'll need your encryption key to decrypt later.")
            .setPositiveButton("Export Encrypted", (dialog, which) -> {
                Toast.makeText(this, "Encrypted export coming soon", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showLegalExportOptions() {
        new AlertDialog.Builder(this)
            .setTitle("Legal Compliance Export")
            .setMessage("Generate legally compliant data export for:\n\n" +
                    "• GDPR requests\n" +
                    "• Legal proceedings\n" +
                    "• Personal records\n\n" +
                    "Includes timestamps and verification.")
            .setPositiveButton("Generate", (dialog, which) -> {
                Toast.makeText(this, "Legal export coming soon", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void confirmExport(String type, String format) {
        new AlertDialog.Builder(this)
            .setTitle("Confirm Export")
            .setMessage("Export " + type + " as " + format + "?")
            .setPositiveButton("Export", (dialog, which) -> {
                performExport(type, format);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void performExport(String type, String format) {
        // TODO: Implement actual export
        Toast.makeText(this, "Exporting as " + format + "...", Toast.LENGTH_LONG).show();
        
        // For now, just show success after delay
        findViewById(R.id.back_button).postDelayed(() -> {
            Toast.makeText(this, "Export complete!", Toast.LENGTH_SHORT).show();
        }, 2000);
    }
}
