package com.core.talita;

/**
 * Base interface for all personal data types
 */
public interface PersonalData {
    String getDataType();
    long getTimestamp();
    String getDisplaySummary();
    Object getValue();
}
