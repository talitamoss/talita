package com.core.talita;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Today Screen - The new home screen for Talita
 * Shows a breathing visualization of today's data
 */
public class TodayActivity extends AppCompatActivity {
    private static final String TAG = "TodayActivity";
    
    // UI Components
    private TextView dateText;
    private TextView trackingStatusText;
    private DataVisualizationView dataViz;
    private BottomNavigationView bottomNav;
    private FloatingActionButton quickAddFab;
    private FloatingActionButton nowViewFab;
    private FloatingActionButton moreOptionsFab;
    private View fabOverlay;
    
    // Managers
    private TrackingManager trackingManager;
    private DataCollectorManager collectorManager;
    private UniversalDataService dataService;
    
    // Animation
    private Handler animationHandler = new Handler();
    private boolean isBreathing = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        
        // Initialize services
        trackingManager = new TrackingManager(this);
        collectorManager = new DataCollectorManager(this);
        dataService = new UniversalDataService(this);
        
        setupViews();
        setupBottomNavigation();
        setupFloatingActions();
        updateVisualization();
        startBreathingAnimation();
    }
    
    private void setupViews() {
        dateText = findViewById(R.id.date_text);
        trackingStatusText = findViewById(R.id.tracking_status);
        dataViz = findViewById(R.id.data_visualization);
        bottomNav = findViewById(R.id.bottom_navigation);
        quickAddFab = findViewById(R.id.fab_quick_add);
        nowViewFab = findViewById(R.id.fab_now_view);
        moreOptionsFab = findViewById(R.id.fab_more);
        fabOverlay = findViewById(R.id.fab_overlay);
        
        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date()));
        
        // Update tracking status
        updateTrackingStatus();
    }
    
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_today) {
                // Already on Today screen
                return true;
            } else if (itemId == R.id.nav_capture) {
                startActivity(new Intent(this, LogActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_my_data) {
                startActivity(new Intent(this, MyDataActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_connect) {
                startActivity(new Intent(this, ConnectActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            
            return false;
        });
        
        // Set Today as selected
        bottomNav.setSelectedItemId(R.id.nav_today);
    }
    
    private void setupFloatingActions() {
        quickAddFab.setOnClickListener(v -> {
            toggleFabMenu();
        });
        
        nowViewFab.setOnClickListener(v -> {
            // Show current location/activity
            showComingSoon("now view");
        });
        
        moreOptionsFab.setOnClickListener(v -> {
            showMoreOptions();
        });
        
        fabOverlay.setOnClickListener(v -> {
            if (fabOverlay.getVisibility() == View.VISIBLE) {
                toggleFabMenu();
            }
        });
    }
    
    private void toggleFabMenu() {
        if (nowViewFab.getVisibility() == View.GONE) {
            // Show menu
            fabOverlay.setVisibility(View.VISIBLE);
            fabOverlay.animate().alpha(1f).setDuration(200);
            
            nowViewFab.setVisibility(View.VISIBLE);
            nowViewFab.animate().scaleX(1f).scaleY(1f).setDuration(200);
            
            moreOptionsFab.setVisibility(View.VISIBLE);
            moreOptionsFab.animate().scaleX(1f).scaleY(1f).setDuration(200).setStartDelay(50);
            
            // Rotate main FAB
            quickAddFab.animate().rotation(45f).setDuration(200);
        } else {
            // Hide menu
            fabOverlay.animate().alpha(0f).setDuration(200).withEndAction(() -> 
                fabOverlay.setVisibility(View.GONE));
            
            nowViewFab.animate().scaleX(0f).scaleY(0f).setDuration(200).withEndAction(() ->
                nowViewFab.setVisibility(View.GONE));
            
            moreOptionsFab.animate().scaleX(0f).scaleY(0f).setDuration(200).withEndAction(() ->
                moreOptionsFab.setVisibility(View.GONE));
            
            // Reset rotation
            quickAddFab.animate().rotation(0f).setDuration(200);
        }
    }
    
    private void updateTrackingStatus() {
        if (trackingManager != null && trackingManager.isTrackingEnabled()) {
            trackingStatusText.setText("📍 Tracking Active");
        } else {
            trackingStatusText.setText("📍 Tracking Paused");
        }
    }
    
    private void updateVisualization() {
        // Get today's data
        List<PersonalData> todaysData = dataService.getTodaysData();
        
        // Update the visualization
        dataViz.setData(todaysData);
        
        // Update breathing animation based on data density
        if (todaysData.size() > 10) {
            dataViz.setBreathingSpeed(3000); // Slower when more data
        } else {
            dataViz.setBreathingSpeed(2000); // Faster when less data
        }
    }
    
    private void startBreathingAnimation() {
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBreathing) {
                    dataViz.breathe();
                    animationHandler.postDelayed(this, 2000);
                }
            }
        }, 500);
    }
    
    private void showMoreOptions() {
        // Create bottom sheet with more options
        MoreOptionsBottomSheet bottomSheet = new MoreOptionsBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "more_options");
    }
    
    private void showComingSoon(String feature) {
        Intent intent = new Intent(this, ComingSoonActivity.class);
        intent.putExtra("feature", feature);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateVisualization();
        updateTrackingStatus();
        isBreathing = true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isBreathing = false;
    }
}
