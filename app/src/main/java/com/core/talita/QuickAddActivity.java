package com.core.talita;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Quick Add Activity - Context-aware data entry
 * TODO: Implement smart suggestions based on time of day
 */
public class QuickAddActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple placeholder layout for now
        TextView textView = new TextView(this);
        textView.setText("Quick Add - Coming Soon\n\nContext-aware data entry");
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
        // Slide down animation
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
    }
}
