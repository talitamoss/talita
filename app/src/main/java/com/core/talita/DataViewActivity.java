package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Data View Activity - "My Data" section
 * Shows user's collected data in an organized, accessible format
 */
public class DataViewActivity extends AppCompatActivity {

    private RecyclerView dataTypesRecycler;
    private RecyclerView recentDataRecycler;
    private TextView totalDataPointsText;
    private TextView todayDataPointsText;
    private TextView storageUsedText;

    private UniversalDataService dataService;
    private DataTypeAdapter dataTypeAdapter;
    private RecentDataAdapter recentDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        dataService = new UniversalDataService(this);

        initializeViews();
        setupDataTypes();
        setupRecentData();
        updateStatistics();
    }

    private void initializeViews() {
        dataTypesRecycler = findViewById(R.id.data_types_recycler);
        recentDataRecycler = findViewById(R.id.recent_data_recycler);
        totalDataPointsText = findViewById(R.id.total_data_points);
        todayDataPointsText = findViewById(R.id.today_data_points);
        storageUsedText = findViewById(R.id.storage_used);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void setupDataTypes() {
        List<DataTypeOverview> dataTypes = createDataTypeOverviews();

        dataTypeAdapter = new DataTypeAdapter(dataTypes, this::onDataTypeClicked);
        dataTypesRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        dataTypesRecycler.setAdapter(dataTypeAdapter);
    }

    private void setupRecentData() {
        List<RecentDataItem> recentItems = getRecentDataItems();

        recentDataAdapter = new RecentDataAdapter(recentItems);
        recentDataRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentDataRecycler.setAdapter(recentDataAdapter);
    }

    private List<DataTypeOverview> createDataTypeOverviews() {
        List<DataTypeOverview> overviews = new ArrayList<>();

        // Get actual data counts from the universal service
        overviews.add(new DataTypeOverview("📍", "Location", "location", getDataCount("location")));
        overviews.add(new DataTypeOverview("🎤", "Audio", "audio", getDataCount("audio")));
        overviews.add(new DataTypeOverview("💧", "Hydration", "water", getDataCount("water")));
        overviews.add(new DataTypeOverview("💤", "Sleep", "sleep", getDataCount("sleep")));
        overviews.add(new DataTypeOverview("🏃", "Exercise", "exercise", getDataCount("exercise")));
        overviews.add(new DataTypeOverview("😊", "Mood", "mood", getDataCount("mood")));
        overviews.add(new DataTypeOverview("🍽️", "Nutrition", "nutrition", getDataCount("nutrition")));
        overviews.add(new DataTypeOverview("🚬", "Substances", "substance", getDataCount("substance")));

        return overviews;
    }

    private int getDataCount(String dataType) {
        try {
            List<UniversalDataService.DecryptedDataItem> items =
                    dataService.getDecryptedDataByType(dataType);
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private List<RecentDataItem> getRecentDataItems() {
        List<RecentDataItem> recentItems = new ArrayList<>();

        // Get recent data from all types
        String[] dataTypes = {"location", "audio", "water", "sleep", "exercise", "mood", "nutrition", "substance"};

        for (String dataType : dataTypes) {
            try {
                List<UniversalDataService.DecryptedDataItem> items =
                        dataService.getDecryptedDataByType(dataType);

                // Add most recent items (limit to prevent overwhelming the list)
                for (int i = 0; i < Math.min(items.size(), 3); i++) {
                    UniversalDataService.DecryptedDataItem item = items.get(i);

                    String icon = getIconForDataType(dataType);
                    String summary = createSummaryFromData(item);
                    String timeAgo = getTimeAgo(item.timestamp);

                    recentItems.add(new RecentDataItem(icon, summary, timeAgo, item.timestamp));
                }
            } catch (Exception e) {
                // Continue with other data types
            }
        }

        // Sort by timestamp (most recent first)
        recentItems.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        // Limit to 20 most recent items
        return recentItems.subList(0, Math.min(recentItems.size(), 20));
    }

    private String getIconForDataType(String dataType) {
        switch (dataType) {
            case "location": return "📍";
            case "audio": return "🎤";
            case "water": return "💧";
            case "sleep": return "💤";
            case "exercise": return "🏃";
            case "mood": return "😊";
            case "nutrition": return "🍽️";
            case "substance": return "🚬";
            default: return "📊";
        }
    }

    private String createSummaryFromData(UniversalDataService.DecryptedDataItem item) {
        try {
            switch (item.type) {
                case "water":
                    int volume = item.decryptedData.optInt("volume_ml", 0);
                    return volume + "ml water logged";
                case "sleep":
                    double hours = item.decryptedData.optDouble("hours_slept", 0);
                    return String.format("%.1f hours sleep", hours);
                case "exercise":
                    String exerciseType = item.decryptedData.optString("exercise_type", "Exercise");
                    String duration = item.decryptedData.optString("duration", "");
                    return exerciseType + " for " + duration;
                case "mood":
                    int rating = item.decryptedData.optInt("mood_rating", 3);
                    return "Mood: " + rating + "/5";
                case "location":
                    return "Location recorded";
                case "audio":
                    long durationMs = item.decryptedData.optLong("duration_ms", 0);
                    int seconds = (int) (durationMs / 1000);
                    return String.format("Audio recording (%ds)", seconds);
                default:
                    return item.type + " data recorded";
            }
        } catch (Exception e) {
            return item.type + " data";
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        if (diff < 604800000) return (diff / 86400000) + "d ago";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void updateStatistics() {
        // Calculate statistics
        int totalDataPoints = 0;
        int todayDataPoints = 0;

        String[] dataTypes = {"location", "audio", "water", "sleep", "exercise", "mood", "nutrition", "substance"};
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long todayStart = today.getTimeInMillis();

        for (String dataType : dataTypes) {
            try {
                List<UniversalDataService.DecryptedDataItem> items =
                        dataService.getDecryptedDataByType(dataType);

                totalDataPoints += items.size();

                for (UniversalDataService.DecryptedDataItem item : items) {
                    if (item.timestamp >= todayStart) {
                        todayDataPoints++;
                    }
                }
            } catch (Exception e) {
                // Continue with other types
            }
        }

        // Update UI
        totalDataPointsText.setText(String.valueOf(totalDataPoints));
        todayDataPointsText.setText(String.valueOf(todayDataPoints));

        // Estimate storage (rough calculation)
        long estimatedStorage = totalDataPoints * 1024; // ~1KB per data point average
        storageUsedText.setText(formatBytes(estimatedStorage));
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void onDataTypeClicked(DataTypeOverview dataType) {
        Intent intent;

        switch (dataType.type) {
            case "location":
                intent = new Intent(this, LocationActivity.class);
                break;
            case "audio":
                intent = new Intent(this, AudioActivity.class);
                break;
            default:
                intent = new Intent(this, DataTypeDetailActivity.class);
                intent.putExtra("data_type", dataType.type);
                intent.putExtra("data_type_name", dataType.name);
                intent.putExtra("data_type_icon", dataType.icon);
                break;
        }

        startActivity(intent);
    }

    /**
     * Data classes
     */
    public static class DataTypeOverview {
        public final String icon;
        public final String name;
        public final String type;
        public final int count;

        public DataTypeOverview(String icon, String name, String type, int count) {
            this.icon = icon;
            this.name = name;
            this.type = type;
            this.count = count;
        }
    }

    public static class RecentDataItem {
        public final String icon;
        public final String summary;
        public final String timeAgo;
        public final long timestamp;

        public RecentDataItem(String icon, String summary, String timeAgo, long timestamp) {
            this.icon = icon;
            this.summary = summary;
            this.timeAgo = timeAgo;
            this.timestamp = timestamp;
        }
    }

    // Adapters would be implemented similar to previous examples...
}