package com.core.talita.plugins.all;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.core.talita.api.*;
import com.core.talita.plugins.base.BaseDataCollector;
import com.core.talita.plugins.DataCollectorPlugin;
import java.util.*;

/**
 * Location tracking plugin - tracks user location with privacy in mind
 */
public class LocationPlugin extends DataCollectorPlugin {
    
    @Override
    public String getPluginId() {
        return "core.location";
    }
    
    @Override
    public String getPluginName() {
        return "Location Tracker";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Core Team";
    }
    
    @Override
    public String getCategory() {
        return "all"; // Environmental category
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
        return Color.parseColor("#EC4899"); // Pink
    }
    
    @Override
    public int getIconResource() {
        return 0; // Use emoji
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
        return true; // Can manually log locations
    }
    
    @Override
    public boolean supportsScheduling() {
        return false;
    }
    
    @Override
    public DataCollector createCollector(Context context) {
        return new LocationDataCollector();
    }
    
    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public void openSettings(Context context) {
        // TODO: Open location settings (frequency, accuracy, etc.)
    }
    
    @Override
    public QuickAddConfig getQuickAddConfig() {
        return new QuickAddConfig(
            "Location",
            "Log current location",
            QuickAddConfig.QuickAddStyle.CARD,
            false // Not in main grid
        );
    }
    
    /**
     * Custom location collector
     */
    private static class LocationDataCollector extends BaseDataCollector implements LocationListener {
        private static final String TYPE = "location";
        private LocationManager locationManager;
        private Location lastLocation;
        
        @Override
        public void initialize(Context context) {
            super.initialize(context);
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        
        @Override
        public CollectorResult collect() {
            // Check permissions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                return CollectorResult.failure(TYPE, "Location permission not granted");
            }
            
            // Get last known location
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                
                if (location != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("latitude", location.getLatitude());
                    data.put("longitude", location.getLongitude());
                    data.put("accuracy", location.getAccuracy());
                    data.put("provider", location.getProvider());
                    data.put("timestamp", location.getTime());
                    
                    return collectQuick(data);
                } else {
                    return CollectorResult.failure(TYPE, "No location available");
                }
            } catch (SecurityException e) {
                return CollectorResult.failure(TYPE, "Security exception: " + e.getMessage());
            }
        }
        
        @Override
        protected void onAutomatedCollectionStarted() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    // Request location updates
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        60000, // 1 minute
                        100,   // 100 meters
                        this
                    );
                } catch (SecurityException e) {
                    // Handle error
                }
            }
        }
        
        @Override
        protected void onAutomatedCollectionStopped() {
            locationManager.removeUpdates(this);
        }
        
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            
            Map<String, Object> data = new HashMap<>();
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data.put("accuracy", location.getAccuracy());
            data.put("speed", location.getSpeed());
            data.put("bearing", location.getBearing());
            data.put("altitude", location.getAltitude());
            data.put("provider", location.getProvider());
            data.put("timestamp", location.getTime());
            
            // Save automatically
            saveData(data);
        }
        
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        
        @Override
        public void onProviderEnabled(String provider) {}
        
        @Override
        public void onProviderDisabled(String provider) {}
        
        @Override
        public String getType() {
            return TYPE;
        }
        
        @Override
        public String getDisplayName() {
            return "Location Tracker";
        }
        
        @Override
        public String getDescription() {
            return "Track your location privately and securely";
        }
        
        @Override
        public String getEmoji() {
            return "📍";
        }
        
        @Override
        public String getCategory() {
            return "all";
        }
        
        @Override
        public List<String> getRequiredPermissions() {
            return Arrays.asList(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            );
        }
        
        @Override
        protected CollectorSettings getDefaultSettings() {
            return new CollectorSettings.Builder()
                .setEnabled(false) // Disabled by default for privacy
                .setAutomatedCollection(true)
                .setCollectionFrequency(5) // Every 5 minutes
                .setBatteryOptimized(true)
                .setRequiresLocation(true)
                .build();
        }
    }
}
