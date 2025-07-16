package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnMyData = findViewById(R.id.btn_my_data);
        Button btnRecord = findViewById(R.id.btn_record);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnSettings = findViewById(R.id.btn_settings);

        btnMyData.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataSummaryActivity.class);
            startActivity(intent);
        });

        btnRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        });

        btnConnect.setOnClickListener(v -> {
            Toast.makeText(this, "🔗 Connect - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}