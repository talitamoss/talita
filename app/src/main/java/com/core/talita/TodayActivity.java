package com.core.talita;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.util.AttributeSet;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
                // Already here
                return true;
            } else if (itemId == R.id.nav_patterns) {
                startActivity(new Intent(this, PatternsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_vault) {
                startActivity(new Intent(this, VaultActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_connect) {
                showComingSoon("Connect");
                return false;
            }
            
            return false;
        });
        
        // Set Today as selected
        bottomNav.setSelectedItemId(R.id.nav_today);
    }
    
    private void setupFloatingActions() {
        // Main FAB (Quick Add)
        quickAddFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuickAddActivity.class);
            intent.putExtra("context_aware", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);
        });
        
        // Now View FAB (Real-time data)
        nowViewFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NowViewActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        
        // More Options FAB
        moreOptionsFab.setOnClickListener(v -> {
            showMoreOptions();
        });
        
        // Initially hide secondary FABs
        nowViewFab.setVisibility(View.GONE);
        moreOptionsFab.setVisibility(View.GONE);
        
        // Long press on main FAB reveals all options
        quickAddFab.setOnLongClickListener(v -> {
            toggleFabMenu();
            return true;
        });
    }
    
    private void toggleFabMenu() {
        if (nowViewFab.getVisibility() == View.VISIBLE) {
            // Hide menu
            hideFabMenu();
        } else {
            // Show menu
            showFabMenu();
        }
    }
    
    private void showFabMenu() {
        // Show overlay
        fabOverlay.setVisibility(View.VISIBLE);
        fabOverlay.animate().alpha(1f).setDuration(200);
        
        // Animate secondary FABs
        nowViewFab.setVisibility(View.VISIBLE);
        moreOptionsFab.setVisibility(View.VISIBLE);
        
        nowViewFab.setTranslationY(100f);
        moreOptionsFab.setTranslationY(100f);
        
        nowViewFab.animate()
            .translationY(0f)
            .translationX(-120f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator());
            
        moreOptionsFab.animate()
            .translationY(0f)
            .translationX(-240f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator());
        
        // Close on overlay tap
        fabOverlay.setOnClickListener(v -> hideFabMenu());
    }
    
    private void hideFabMenu() {
        fabOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction(() -> fabOverlay.setVisibility(View.GONE));
        
        nowViewFab.animate()
            .translationY(100f)
            .translationX(0f)
            .setDuration(200)
            .withEndAction(() -> nowViewFab.setVisibility(View.GONE));
            
        moreOptionsFab.animate()
            .translationY(100f)
            .translationX(0f)
            .setDuration(200)
            .withEndAction(() -> moreOptionsFab.setVisibility(View.GONE));
    }
    
    private void updateTrackingStatus() {
        boolean isTracking = trackingManager.isTrackingEnabled();
        
        if (isTracking) {
            trackingStatusText.setText("◉ Tracking Active");
            trackingStatusText.setTextColor(getColor(R.color.accent_flow));
        } else {
            trackingStatusText.setText("◐ Tracking Paused");
            trackingStatusText.setTextColor(getColor(R.color.text_secondary));
        }
        
        // Tap to toggle
        trackingStatusText.setOnClickListener(v -> {
            trackingManager.setTrackingEnabled(!isTracking);
            updateTrackingStatus();
            updateVisualization();
        });
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
    
    /**
     * Custom view for data visualization
     * Creates generative art based on user's data
     */
    public static class DataVisualizationView extends View {
        private Paint paint;
        private List<PersonalData> data;
        private float breathingScale = 1.0f;
        private int breathingSpeed = 2000;
        private ValueAnimator breathAnimator;
        
        // Particle system for data points
        private List<DataParticle> particles;
        private Random random = new Random();
        
        public DataVisualizationView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }
        
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            particles = new ArrayList<>();
            data = new ArrayList<>();
        }
        
        public void setData(List<PersonalData> newData) {
            this.data = newData;
            updateParticles();
            invalidate();
        }
        
        public void setBreathingSpeed(int speed) {
            this.breathingSpeed = speed;
        }
        
        public void breathe() {
            if (breathAnimator != null) {
                breathAnimator.cancel();
            }
            
            breathAnimator = ValueAnimator.ofFloat(1.0f, 1.1f, 1.0f);
            breathAnimator.setDuration(breathingSpeed);
            breathAnimator.setInterpolator(new DecelerateInterpolator());
            breathAnimator.addUpdateListener(animation -> {
                breathingScale = (float) animation.getAnimatedValue();
                invalidate();
            });
            breathAnimator.start();
        }
        
        private void updateParticles() {
            particles.clear();
            
            // Create particles based on data
            for (PersonalData item : data) {
                DataParticle particle = new DataParticle();
                particle.x = random.nextFloat() * getWidth();
                particle.y = random.nextFloat() * getHeight();
                particle.size = 5 + random.nextFloat() * 20;
                particle.color = getColorForDataType(item.getDataType());
                particle.alpha = 0.3f + random.nextFloat() * 0.7f;
                particles.add(particle);
            }
        }
        
        private int getColorForDataType(String type) {
            // Map data types to colors
            switch (type) {
                case "water": return Color.parseColor("#3B82F6");
                case "exercise": return Color.parseColor("#10B981");
                case "mood": return Color.parseColor("#8B5CF6");
                case "sleep": return Color.parseColor("#6366F1");
                case "location": return Color.parseColor("#EC4899");
                default: return Color.parseColor("#FAFAFA");
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Draw breathing circle
            paint.setColor(Color.parseColor("#6366F1"));
            paint.setAlpha(20);
            float radius = Math.min(centerX, centerY) * 0.8f * breathingScale;
            canvas.drawCircle(centerX, centerY, radius, paint);
            
            // Draw data particles
            for (DataParticle particle : particles) {
                paint.setColor(particle.color);
                paint.setAlpha((int) (particle.alpha * 255));
                
                // Apply breathing scale to particle positions
                float px = centerX + (particle.x - centerX) * breathingScale;
                float py = centerY + (particle.y - centerY) * breathingScale;
                
                canvas.drawCircle(px, py, particle.size * breathingScale, paint);
            }
            
            // Draw connection lines between nearby particles
            paint.setStrokeWidth(1f);
            paint.setAlpha(30);
            
            for (int i = 0; i < particles.size(); i++) {
                DataParticle p1 = particles.get(i);
                for (int j = i + 1; j < particles.size(); j++) {
                    DataParticle p2 = particles.get(j);
                    
                    float distance = (float) Math.sqrt(
                        Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)
                    );
                    
                    if (distance < 150) {
                        float p1x = centerX + (p1.x - centerX) * breathingScale;
                        float p1y = centerY + (p1.y - centerY) * breathingScale;
                        float p2x = centerX + (p2.x - centerX) * breathingScale;
                        float p2y = centerY + (p2.y - centerY) * breathingScale;
                        
                        canvas.drawLine(p1x, p1y, p2x, p2y, paint);
                    }
                }
            }
        }
        
        private static class DataParticle {
            float x, y, size, alpha;
            int color;
        }
    }
}
