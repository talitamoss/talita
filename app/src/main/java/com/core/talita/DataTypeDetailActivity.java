package com.core.talita;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DataTypeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("Data Type Detail - Coming Soon!");
        textView.setPadding(50, 50, 50, 50);
        setContentView(textView);
    }
}