package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Button btnMe = findViewById(R.id.btn_me);
        Button btnOthers = findViewById(R.id.btn_others);

        btnMe.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        btnOthers.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrHandshakeActivity.class);
            startActivity(intent);
        });
    }

}
