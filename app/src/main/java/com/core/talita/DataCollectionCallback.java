package com.core.talita;

/**
 * Callback for when data is collected
 * Brand-agnostic: works with any app name
 */
public interface DataCollectionCallback {
    void onDataCollected(PersonalData data);
    void onCollectionError(String error);
}
