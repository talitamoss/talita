package com.core.talita;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * CloudBackupSettingsActivity - Placeholder for future cloud backup feature
 * 
 * Currently just shows "Coming Soon" to avoid build errors.
 * Cloud backup can be properly implemented when needed.
 */
public class CloudBackupSettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple layout showing coming soon
        setContentView(createSimpleLayout());
    }
    
    private android.view.View createSimpleLayout() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(0xFFF5F5F5);
        
        // Back button
        Button backButton = new Button(this);
        backButton.setText("← Back");
        backButton.setOnClickListener(v -> finish());
        layout.addView(backButton);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Cloud Backup");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFF000000);
        titleText.setPadding(0, 32, 0, 16);
        layout.addView(titleText);
        
        // Coming soon message
        TextView messageText = new TextView(this);
        messageText.setText("Cloud backup feature coming soon!\n\n" +
                           "Your data is currently stored securely on your device.\n\n" +
                           "Cloud backup will allow you to:\n" +
                           "• Sync across devices\n" +
                           "• Restore after device loss\n" +
                           "• Choose your preferred cloud provider");
        messageText.setTextSize(16);
        messageText.setTextColor(0xFF666666);
        messageText.setLineSpacing(8, 1);
        layout.addView(messageText);
        
        return layout;
    }
}
