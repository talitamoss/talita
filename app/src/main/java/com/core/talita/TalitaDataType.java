package com.core.talita;

/**
 * Universal interface for all data types in Talita
 * Implements this interface to get automatic database storage,
 * cloud backup, and sharing capabilities
 */
public interface TalitaDataType {

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

    // Display information
    String getDisplayName();   // Human-readable name for UI
    String getDisplaySummary(); // Brief description for lists
}