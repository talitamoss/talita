package com.core.talita;

public interface TalitaData {
    String getId();
    String getType();
    long getTimestamp();
    String getValue();
    String getMetadata();
    String getDisplayName();
    boolean isEncrypted();
    boolean isBackedUp();
}