package com.core.talita;

import java.util.Map;

/**
 * Implements this interface to get automatic database storage,
 * cloud backup, and sharing capabilities
 */
public interface UniversalDataType {

    // Basic identification
    String getType();          // "location", "audio", "steps", "expenses", etc.
    String getId();            // Unique identifier for this data item

    // Data serialization
    String toJson();           // Convert data to JSON for database storage

    // File handling (optional - return null if no file)
    String getFilePath();      // Path to associated file (for audio, photos, etc.)

    // Metadata
    long getTimestamp();       // When this data was created
    double getLatitude();      // Location context (return 0.0 if not applicable)
    double getLongitude();     // Location context (return 0.0 if not applicable)
    Map<String, Object> getMetadata(); // Additional metadata

    // Display information
    String getDisplayName();   // Human-readable name for UI
    String getDisplaySummary(); // Brief description for lists - ADD THIS IF MISSING
}
