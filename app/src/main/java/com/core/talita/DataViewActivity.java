package com.core.talita;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DataViewActivity - Main activity for viewing collected data
 * 
 * Fixed to include all missing inner classes that adapters expect.
 */
public class DataViewActivity extends AppCompatActivity {
    private static final String TAG = "DataViewActivity";
    
    private UniversalDataService dataService;
    private RecyclerView dataTypesRecycler;
    private RecyclerView recentDataRecycler;
    private TextView totalCountText;
    private TextView lastSyncText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        
        dataService = UniversalDataService.getInstance(this);
        
        initializeViews();
        loadDataOverview();
        loadRecentData();
    }
    
    private void initializeViews() {
        dataTypesRecycler = findViewById(R.id.data_types_recycler);
        recentDataRecycler = findViewById(R.id.recent_data_recycler);
        totalCountText = findViewById(R.id.total_count_text);
        lastSyncText = findViewById(R.id.last_sync_text);
        
        // Set up recycler views
        dataTypesRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentDataRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }
    
    private void loadDataOverview() {
        // Get data counts by type
        Map<String, Integer> dataStats = dataService.getDataStats();
        int totalCount = dataService.getTotalDataCount();
        
        // Update UI
        totalCountText.setText(String.format(Locale.getDefault(), 
            "Total Records: %d", totalCount));
        
        // Create overview items
        List<DataTypeOverview> overviews = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dataStats.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            
            DataTypeOverview overview = new DataTypeOverview(
                type,
                getDisplayNameForType(type),
                getEmojiForType(type),
                count,
                getLastUpdateForType(type)
            );
            overviews.add(overview);
        }
        
        // Set adapter
        DataTypeAdapter adapter = new DataTypeAdapter(overviews, this::onDataTypeClicked);
        dataTypesRecycler.setAdapter(adapter);
    }
    
    private void loadRecentData() {
        // Get recent data entries
        List<PersonalData> recentData = dataService.getRecentData(10);
        
        // Convert to display items
        List<RecentDataItem> recentItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        
        for (PersonalData data : recentData) {
            RecentDataItem item = new RecentDataItem(
                data.getId(),
                data.getType(),
                getDisplayNameForType(data.getType()),
                getEmojiForType(data.getType()),
                data.getSummary(),
                dateFormat.format(new Date(data.getTimestamp()))
            );
            recentItems.add(item);
        }
        
        // Set adapter
        RecentDataAdapter adapter = new RecentDataAdapter(recentItems);
        recentDataRecycler.setAdapter(adapter);
    }
    
    private void onDataTypeClicked(DataTypeOverview dataType) {
        Log.d(TAG, "Data type clicked: " + dataType.type);
        // TODO: Open detailed view for this data type
    }
    
    // Helper methods
    
    private String getDisplayNameForType(String type) {
        // Map internal types to display names
        switch (type) {
            case "water": return "Water Intake";
            case "location": return "Location";
            case "mood": return "Mood";
            case "exercise": return "Exercise";
            case "sleep": return "Sleep";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }
    
    private String getEmojiForType(String type) {
        // Map types to emojis
        switch (type) {
            case "water": return "💧";
            case "location": return "📍";
            case "mood": return "😊";
            case "exercise": return "💪";
            case "sleep": return "😴";
            default: return "📊";
        }
    }
    
    private long getLastUpdateForType(String type) {
        List<PersonalData> typeData = dataService.getDataByType(type, 0, System.currentTimeMillis());
        if (!typeData.isEmpty()) {
            return typeData.get(0).getTimestamp();
        }
        return 0;
    }
    
    /**
     * Inner class for data type overview
     */
    public static class DataTypeOverview {
        public final String type;
        public final String displayName;
        public final String emoji;
        public final int count;
        public final long lastUpdate;
        
        public DataTypeOverview(String type, String displayName, String emoji, 
                               int count, long lastUpdate) {
            this.type = type;
            this.displayName = displayName;
            this.emoji = emoji;
            this.count = count;
            this.lastUpdate = lastUpdate;
        }
    }
    
    /**
     * Inner class for recent data items
     */
    public static class RecentDataItem {
        public final String id;
        public final String type;
        public final String displayName;
        public final String emoji;
        public final String summary;
        public final String timeString;
        
        public RecentDataItem(String id, String type, String displayName, 
                             String emoji, String summary, String timeString) {
            this.id = id;
            this.type = type;
            this.displayName = displayName;
            this.emoji = emoji;
            this.summary = summary;
            this.timeString = timeString;
        }
    }
}
