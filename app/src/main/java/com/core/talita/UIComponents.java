package com.core.talita;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

/**
 * UIComponents - Reusable UI component helper
 * 
 * Provides consistent buttons and UI elements throughout the app
 */
public class UIComponents {
    
    /**
     * Add a plus button to any ViewGroup
     */
    public static View addPlusButton(ViewGroup parent, View.OnClickListener listener) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        View plusButton = inflater.inflate(R.layout.component_plus_button, parent, false);
        plusButton.setOnClickListener(listener);
        parent.addView(plusButton);
        
        return plusButton;
    }
    
    /**
     * Create a plus button (not added to parent)
     */
    public static View createPlusButton(Context context, View.OnClickListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View plusButton = inflater.inflate(R.layout.component_plus_button, null);
        plusButton.setOnClickListener(listener);
        return plusButton;
    }
    
    /**
     * Setup standard back button behavior
     */
    public static View setupBackButton(Activity activity, int buttonId) {
        View backButton = activity.findViewById(buttonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> activity.finish());
        }
        return backButton;
    }
    
    /**
     * Create a standard back button
     */
    public static View createBackButton(Context context, View.OnClickListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View backButton = inflater.inflate(R.layout.component_back_button, null);
        backButton.setOnClickListener(listener);
        return backButton;
    }
    
    /**
     * Create a FAB (Floating Action Button) with icon and label
     */
    public static View createFAB(Context context, int iconRes, String label, View.OnClickListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View fab = inflater.inflate(R.layout.component_fab_button, null);
        
        ImageView icon = fab.findViewById(R.id.fab_icon);
        TextView labelText = fab.findViewById(R.id.fab_label);
        
        if (iconRes != 0) {
            icon.setImageResource(iconRes);
        }
        if (label != null) {
            labelText.setText(label);
        } else {
            labelText.setVisibility(View.GONE);
        }
        
        fab.setOnClickListener(listener);
        return fab;
    }
    
    /**
     * Create an icon button
     */
    public static View createIconButton(Context context, int iconRes, View.OnClickListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View button = inflater.inflate(R.layout.component_icon_button, null);
        
        ImageView icon = button.findViewById(R.id.icon_image);
        icon.setImageResource(iconRes);
        
        button.setOnClickListener(listener);
        return button;
    }
    
    /**
     * Style any existing button to match app theme
     */
    public static void styleAsBackButton(View button) {
        if (button instanceof TextView) {
            TextView textView = (TextView) button;
            textView.setText("←");
            textView.setTextSize(20);
        }
        // Apply standard back button styling
        button.setBackgroundResource(R.drawable.circle_button);
        ViewGroup.LayoutParams params = button.getLayoutParams();
        if (params != null) {
            params.width = dpToPx(button.getContext(), 48);
            params.height = dpToPx(button.getContext(), 48);
            button.setLayoutParams(params);
        }
    }
    
    /**
     * Apply consistent styling to any card
     */
    public static void styleCard(CardView card) {
        card.setRadius(dpToPx(card.getContext(), 12));
        card.setCardElevation(dpToPx(card.getContext(), 2));
        card.setCardBackgroundColor(card.getContext().getColor(R.color.card_background));
    }
    
    // Utility method
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
