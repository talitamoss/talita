package com.core.talita;

/**
 * DataItem - Universal interface for all data stored in the app
 */
public interface DataItem {
    String getId();
    String getType();
    long getTimestamp();
    String getValue();
    String getMetadata();
    String getDisplayName();
    boolean isEncrypted();
    boolean isBackedUp();
}
