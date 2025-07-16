package com.core.talita;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class AboutSettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("About - Talita v1.0\nPersonal Data Sovereignty Platform");
        tv.setPadding(50, 50, 50, 50);
        setContentView(tv);
    }
}