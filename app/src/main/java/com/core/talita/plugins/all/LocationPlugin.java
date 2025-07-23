package com.core.talita.plugins.all;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import com.core.talita.collectors.DataCollector;
import com.core.talita.collectors.LocationCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import com.core.talita.plugins.PluginCategories;

/**
 * Location Plugin - "All" category
 * Tracks your place in the world
 */
public class LocationPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "all.location";
    }
    
    @Override
    public String getPluginName() {
        return "Location";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Talita Core Team";
    }
    
    @Override
    public String getCategory() {
        return PluginCategories.ALL;
    }
    
    @Override
    public int getPriority() {
        return 70;
    }
    
    @Override
    public String getEmoji() {
        return "📍";
    }
    
    @Override
    public int getAccentColor() {
        return Color.parseColor("#4CAF50");
    }
    
    @Override
    public int getIconResource() {
        return 0;
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }
    
    @Override
    public boolean requiresBackgroundTracking() {
        return true;
    }
    
    @Override
    public boolean supportsQuickAdd() {
        return true;
    }
    
    @Override
    public boolean supportsScheduling() {
        return false;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new LocationCollector();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Location settings
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Location",
            "Mark this place",
            QuickAddStyle.LOCATION_MARK,
            false
        );
    }
}
