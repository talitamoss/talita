package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Connect Activity - Hub for connecting with other Talita users
 *
 * Future features:
 * - QR code handshake for secure key exchange
 * - P2P mesh networking
 * - Friend management
 * - Secure data sharing
 * - Contact discovery
 */
public class ConnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Views will be initialized here as we add them
    }

    private void setupClickListeners() {
        // Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // QR Handshake - the only implemented feature for now
        CardView qrHandshakeCard = findViewById(R.id.qr_handshake_card);
        qrHandshakeCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 QR Handshake - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        // Placeholder features - to be implemented later
        CardView meshNetworkCard = findViewById(R.id.mesh_network_card);
        meshNetworkCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 P2P Mesh Network - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        CardView friendsCard = findViewById(R.id.friends_card);
        friendsCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 Friends Management - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        CardView discoverCard = findViewById(R.id.discover_card);
        discoverCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 Contact Discovery - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        CardView shareDataCard = findViewById(R.id.share_data_card);
        shareDataCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 Secure Data Sharing - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        CardView solidPodsCard = findViewById(R.id.solid_pods_card);
        solidPodsCard.setOnClickListener(v -> {
            Toast.makeText(this, "🚧 Solid Pod Integration - Coming Soon!", Toast.LENGTH_SHORT).show();
        });
    }
}
