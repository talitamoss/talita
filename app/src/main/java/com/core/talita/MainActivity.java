package com.core.talita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.preference.PreferenceManager;

import org.osmdroid.views.MapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationTracker locationTracker;

    private MapView mapView;
    private Polyline polyline; // To track the route
    private final ArrayList<GeoPoint> routePoints = new ArrayList<>(); // To store the locations


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue("com.core.talita");

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(0.0, 0.0);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(startPoint);


        TextView locationTextView = findViewById(R.id.location_info);
        ScrollView locationList = findViewById(R.id.location_display_scrollview);
        mapView = findViewById(R.id.mapView);
        Button toggleButton = findViewById(R.id.show_location_button);

        // Toggle logic
        toggleButton.setOnClickListener(v -> {
            if (locationList.getVisibility() == View.VISIBLE) {
                locationList.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                toggleButton.setText("Show Recorded Locations");
            } else {
                locationList.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                toggleButton.setText("Show Map");
            }
        });


        // Initialize polyline
        polyline = new Polyline();
        mapView.getOverlays().add(polyline); // Add polyline to map overlays

        locationTracker = new LocationTracker(this, locationTextView, mapView, polyline,routePoints);


        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission. ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            locationTracker.startTracking(); // start tracking immediately if permission is already granted
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTracker.stopTracking();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationTracker.startTracking();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
