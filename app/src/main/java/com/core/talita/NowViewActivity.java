package com.core.talita;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Now View Activity - Real-time tracking visualization
 * TODO: Show live data streams
 */
public class NowViewActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple placeholder layout for now
        TextView textView = new TextView(this);
        textView.setText("Now View - Coming Soon\n\nReal-time data streams");
        textView.setTextSize(20);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextColor(0xFFFFFFFF);
        setContentView(textView);
        
        // Set dark background
        getWindow().getDecorView().setBackgroundColor(0xFF0A0A0A);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Fade animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
