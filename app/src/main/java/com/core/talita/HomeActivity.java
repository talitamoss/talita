package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // My Data Button
        CardView btnMyData = findViewById(R.id.btn_my_data_card);
        btnMyData.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataViewActivity.class);
            startActivity(intent);
        });

        // Connect Button
        CardView btnConnect = findViewById(R.id.btn_connect_card);
        btnConnect.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        });

        // Settings Button (Bottom Left)
        CardView btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Quick Add Button (Bottom Right)
        CardView btnQuickAdd = findViewById(R.id.btn_quick_add);
        btnQuickAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        });
    }
}
