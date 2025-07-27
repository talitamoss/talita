package com.core.talita;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PluginManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Plugin Management\n\nCurrently only Water plugin is available.");
        tv.setPadding(32, 32, 32, 32);
        setContentView(tv);
    }
}
