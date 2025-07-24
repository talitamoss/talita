package com.core.talita.plugins.base;

import android.content.Context;
import android.util.Log;
import com.core.talita.api.*;
import com.core.talita.PersonalData;
import com.core.talita.UniversalDataService;
import java.util.*;

/**
 * BaseDataCollector - Abstract base class for data collectors
 * 
 * Provides common functionality that most collectors need.
 * Plugins can extend this instead of implementing DataCollector directly.
 */
public abstract class BaseDataCollector implements DataCollector {
    protected static final String TAG = "BaseDataCollector";
    
    protected Context context;
    protected CollectorSettings settings;
    protected boolean isAutomatedCollectionActive = false;
    private UniversalDataService dataService;

    public BaseDataCollector() {
        this.settings = getDefaultSettings();
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        this.dataService = UniversalDataService.getInstance(context);
        Log.d(TAG, "Initialized collector: " + getDisplayName());
    }

    @Override
    public CollectorResult collectQuick(Map<String, Object> data) {
        if (!validateData(data)) {
            return CollectorResult.failure(getType(), "Invalid data");
        }

        try {
            // Save the data
            PersonalData personalData = PersonalData.create(getType());
            personalData.setData(data);
            dataService.saveData(personalData);

            Log.d(TAG, "Quick collected: " + getType());
            return CollectorResult.success(getType(), data);
        } catch (Exception e) {
            Log.e(TAG, "Quick collection failed", e);
            return CollectorResult.failure(getType(), e.getMessage());
        }
    }

    @Override
    public boolean startAutomatedCollection() {
        if (!settings.isAutomatedCollection()) {
            Log.w(TAG, "Automated collection not enabled for: " + getType());
            return false;
        }

        isAutomatedCollectionActive = true;
        Log.d(TAG, "Started automated collection: " + getType());
        onAutomatedCollectionStarted();
        return true;
    }

    @Override
    public void stopAutomatedCollection() {
        isAutomatedCollectionActive = false;
        Log.d(TAG, "Stopped automated collection: " + getType());
        onAutomatedCollectionStopped();
    }

    @Override
    public boolean isCollectingAutomatically() {
        return isAutomatedCollectionActive;
    }

    @Override
    public CollectorSettings getSettings() {
        return settings;
    }

    @Override
    public void updateSettings(CollectorSettings newSettings) {
        this.settings = newSettings;
        onSettingsUpdated(newSettings);
    }

    @Override
    public boolean validateData(Map<String, Object> data) {
        return data != null && !data.isEmpty();
    }

    @Override
    public void onDestroy() {
        stopAutomatedCollection();
        Log.d(TAG, "Destroyed collector: " + getType());
    }

    // Protected helper methods for subclasses

    protected void saveData(Map<String, Object> data) {
        PersonalData personalData = PersonalData.create(getType());
        personalData.setData(data);
        dataService.saveData(personalData);
    }

    protected Context getContext() {
        return context;
    }

    protected UniversalDataService getDataService() {
        return dataService;
    }

    // Abstract methods that subclasses can override

    /**
     * Get default settings for this collector
     */
    protected abstract CollectorSettings getDefaultSettings();

    /**
     * Called when automated collection is started
     */
    protected void onAutomatedCollectionStarted() {
        // Override if needed
    }

    /**
     * Called when automated collection is stopped
     */
    protected void onAutomatedCollectionStopped() {
        // Override if needed
    }

    /**
     * Called when settings are updated
     */
    protected void onSettingsUpdated(CollectorSettings newSettings) {
        // Override if needed
    }
}
