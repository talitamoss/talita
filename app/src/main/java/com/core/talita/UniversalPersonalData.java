package com.core.talita;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Universal personal data class that can represent ANY data type
 * Brand-agnostic: works with any app name
 */
public class UniversalPersonalData implements PersonalData {
    private final String id;
    private final String type;
    private final long timestamp;
    private final Map<String, Object> data;
    private final Map<String, Object> metadata;

    public UniversalPersonalData(String type, Map<String, Object> data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
        this.metadata = new HashMap<>();
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getType() { return type; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("type", type);
            json.put("timestamp", timestamp);
            json.put("data", new JSONObject(data));
            json.put("metadata", new JSONObject(metadata));
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getDisplayName() {
        return data.getOrDefault("display_name", type).toString();
    }

    @Override
    public String getDisplaySummary() {
        return data.getOrDefault("summary", "Data collected").toString();
    }

    @Override
    public Map<String, Object> getMetadata() { return metadata; }

    public Object getValue(String key) { return data.get(key); }
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
}
