package com.core.talita;

import android.content.Context;
import java.util.List;

/**
 * Data collector interface - Handles HOW to collect each data type
 */
public interface DataCollector {
    String getDataType();                    // "steps", "water", "exercise", etc.
    String getDisplayName();                 // "Step Counter", "Water Intake", etc.
    String getIcon();                        // "👣", "💧", "💪", etc.

    boolean isAvailable(Context context);    // Can this device collect this data?
    boolean isEnabled(Context context);      // Is user enabled this collector?

    void startCollection(Context context, DataCollectionCallback callback);
    void stopCollection(Context context);

    List<String> getRequiredPermissions();   // What permissions does this need?
    CollectorSettings getSettings();         // How often to collect, thresholds, etc.
}