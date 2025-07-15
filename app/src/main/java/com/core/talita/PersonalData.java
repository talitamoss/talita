package com.core.talita;

import java.util.Map;

/**
 * Universal personal data interface - ALL data types implement this
 * Brand-agnostic: works with any app name
 */
public interface PersonalData {
    String getId();
    String getType();
    long getTimestamp();
    String toJson();
    String getDisplayName();
    String getDisplaySummary();
    Map<String, Object> getMetadata();
}