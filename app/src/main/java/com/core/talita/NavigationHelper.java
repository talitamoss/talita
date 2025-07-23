package com.core.talita;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.IdRes;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class for consistent navigation across the app
 */
public class NavigationHelper {
    
    /**
     * Setup bottom navigation for an activity
     */
    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav, @IdRes int currentItemId) {
        // Set the current item as selected
        bottomNav.setSelectedItemId(currentItemId);
        
        // Handle navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // Don't navigate if we're already on this screen
            if (itemId == currentItemId) {
                return true;
            }
            
            Intent intent = null;
            int enterAnim = 0;
            int exitAnim = 0;
            
            if (itemId == R.id.nav_today) {
                intent = new Intent(activity, TodayActivity.class);
                // Determine animation based on current position
                if (currentItemId == R.id.nav_patterns || 
                    currentItemId == R.id.nav_vault || 
                    currentItemId == R.id.nav_connect) {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                }
            } else if (itemId == R.id.nav_patterns) {
                intent = new Intent(activity, PatternsActivity.class);
                if (currentItemId == R.id.nav_today) {
                    enterAnim = R.anim.slide_in_right;
                    exitAnim = R.anim.slide_out_left;
                } else {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                }
            } else if (itemId == R.id.nav_vault) {
                intent = new Intent(activity, VaultActivity.class);
                if (currentItemId == R.id.nav_connect) {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                } else {
                    enterAnim = R.anim.slide_in_right;
                    exitAnim = R.anim.slide_out_left;
                }
            } else if (itemId == R.id.nav_connect) {
                // Show coming soon
                showComingSoon(activity, "Connect");
                return false;
            }
            
            if (intent != null) {
                // Clear the task to prevent stack buildup
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.overridePendingTransition(enterAnim, exitAnim);
                activity.finish();
                return true;
            }
            
            return false;
        });
    }
    
    /**
     * Show coming soon screen
     */
    public static void showComingSoon(Context context, String feature) {
        Intent intent = new Intent(context, ComingSoonActivity.class);
        intent.putExtra("feature", feature);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
    
    /**
     * Navigate to Quick Add with context
     */
    public static void openQuickAdd(Context context) {
        Intent intent = new Intent(context, QuickAddActivity.class);
        intent.putExtra("context_aware", true);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.stay);
        }
    }
    
    /**
     * Navigate to Now View
     */
    public static void openNowView(Context context) {
        Intent intent = new Intent(context, NowViewActivity.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
