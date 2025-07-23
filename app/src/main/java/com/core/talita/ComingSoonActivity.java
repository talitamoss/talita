package com.core.talita;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Coming Soon Activity - Beautiful teaser for locked features
 * Creates anticipation while keeping users informed
 */
public class ComingSoonActivity extends AppCompatActivity {
    
    private ImageView featureIcon;
    private TextView featureTitle;
    private TextView featureDescription;
    private TextView teaserText;
    private Button notifyButton;
    private Button backButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);
        
        setupViews();
        
        // Get which feature we're showing
        String feature = getIntent().getStringExtra("feature");
        if (feature != null) {
            displayFeature(feature);
        }
        
        startAnimations();
    }
    
    private void setupViews() {
        featureIcon = findViewById(R.id.feature_icon);
        featureTitle = findViewById(R.id.feature_title);
        featureDescription = findViewById(R.id.feature_description);
        teaserText = findViewById(R.id.teaser_text);
        notifyButton = findViewById(R.id.notify_button);
        backButton = findViewById(R.id.back_button);
        
        notifyButton.setOnClickListener(v -> {
            // TODO: Save preference to notify user when feature launches
            notifyButton.setText("✓ We'll let you know!");
            notifyButton.setEnabled(false);
        });
        
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }
    
    private void displayFeature(String feature) {
        switch (feature.toLowerCase()) {
            case "connect":
                featureIcon.setImageResource(R.drawable.ic_users);
                featureTitle.setText("Connect");
                featureDescription.setText("Share without revealing");
                teaserText.setText("Soon you'll be able to share insights with trusted friends while keeping your data encrypted. They store it, but can't read it - true privacy-preserving connection.");
                break;
                
            case "ai guide":
                featureIcon.setImageResource(R.drawable.ic_activity); // Placeholder
                featureTitle.setText("AI Guide");
                featureDescription.setText("Your patterns will unlock insights");
                teaserText.setText("An intelligent companion that learns from your patterns to provide personalized insights. No data leaves your device - AI that respects your privacy.");
                break;
                
            case "calendar":
                featureIcon.setImageResource(R.drawable.ic_trending_up); // Placeholder
                featureTitle.setText("Calendar");
                featureDescription.setText("Plan with your rhythms");
                teaserText.setText("Schedule your life in harmony with your natural patterns. See when you're most energetic, focused, or creative based on your historical data.");
                break;
                
            case "cloud sync":
                featureIcon.setImageResource(R.drawable.ic_shield);
                featureTitle.setText("Cloud Sync");
                featureDescription.setText("Your vault, everywhere");
                teaserText.setText("Seamlessly sync your encrypted data across all your devices. You hold the keys - not even we can read your data.");
                break;
                
            default:
                featureTitle.setText("Coming Soon");
                featureDescription.setText("Something amazing is in the works");
                teaserText.setText("We're building features that put you in control of your digital life. Stay tuned!");
        }
    }
    
    private void startAnimations() {
        // Gentle floating animation for the icon
        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(
            featureIcon, 
            "translationY", 
            0f, -20f, 0f
        );
        floatAnimator.setDuration(3000);
        floatAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        floatAnimator.start();
        
        // Fade in the content
        View[] views = {featureTitle, featureDescription, teaserText, notifyButton};
        for (int i = 0; i < views.length; i++) {
            views[i].setAlpha(0f);
            views[i].animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(i * 100)
                .start();
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
