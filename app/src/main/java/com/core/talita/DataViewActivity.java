package com.core.talita;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DataViewActivity - Display collected data by type
 * 
 * Location: app/src/main/java/com/core/talita/DataViewActivity.java
 */
public class DataViewActivity extends AppCompatActivity {
    private static final String TAG = "DataViewActivity";
    
    private UniversalDataService dataService;
    private String dataType;
    
    private TextView titleText;
    private TextView countText;
    private RecyclerView dataRecycler;
    private ProgressBar loadingProgress;
    private LinearLayout emptyView;
    
    private DataAdapter adapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        
        // Get data type from intent
        dataType = getIntent().getStringExtra("data_type");
        if (dataType == null) {
            finish();
            return;
        }
        
        // Initialize service - FIXED: Using getInstance()
        dataService = UniversalDataService.getInstance(this);
        
        initializeViews();
        loadData();
    }
    
    private void initializeViews() {
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        titleText = findViewById(R.id.title_text);
        countText = findViewById(R.id.count_text);
        dataRecycler = findViewById(R.id.data_recycler);
        loadingProgress = findViewById(R.id.loading_progress);
        emptyView = findViewById(R.id.empty_view);
        
        titleText.setText(formatDataType(dataType));
        
        // Setup recycler
        adapter = new DataAdapter();
        dataRecycler.setLayoutManager(new LinearLayoutManager(this));
        dataRecycler.setAdapter(adapter);
        
        // Export button
        findViewById(R.id.export_button).setOnClickListener(v -> exportData());
        
        // Delete all button
        findViewById(R.id.delete_all_button).setOnClickListener(v -> confirmDeleteAll());
    }
    
    private void loadData() {
        showLoading(true);
        
        new Thread(() -> {
            try {
                // FIXED: Using DecryptedDataItem without UniversalDataService prefix
                List<DecryptedDataItem> items = dataService.getDecryptedDataByType(dataType);
                
                runOnUiThread(() -> {
                    adapter.setItems(items);
                    countText.setText(items.size() + " entries");
                    emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    showLoading(false);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading data", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }
    
    private void exportData() {
        showLoading(true);
        
        new Thread(() -> {
            try {
                // Get decrypted data
                List<DecryptedDataItem> items = dataService.getDecryptedDataByType(dataType);
                
                // Create CSV content
                StringBuilder csv = new StringBuilder();
                csv.append("Timestamp,Data\n");
                
                for (DecryptedDataItem item : items) {
                    csv.append(dateFormat.format(new Date(item.getTimestamp())));
                    csv.append(",");
                    csv.append(createSummaryFromData(item));
                    csv.append("\n");
                }
                
                runOnUiThread(() -> {
                    // TODO: Save to file and share
                    Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error exporting data", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }
    
    private void confirmDeleteAll() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete All " + formatDataType(dataType) + "?")
            .setMessage("This cannot be undone!")
            .setPositiveButton("Delete", (dialog, which) -> deleteAllData())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteAllData() {
        // TODO: Implement batch delete
        Toast.makeText(this, "Delete feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private String createSummaryFromData(DecryptedDataItem item) {
        // TODO: Decrypt and parse data for summary
        return "Data entry";
    }
    
    private String formatDataType(String type) {
        // Convert data type to display name
        switch (type) {
            case "water": return "Water Intake";
            case "mood": return "Mood";
            case "exercise": return "Exercise";
            case "sleep": return "Sleep";
            case "location": return "Location";
            case "audio": return "Audio Notes";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }
    
    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        dataRecycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Adapter for data items
     */
    private class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
        private List<DecryptedDataItem> items = new ArrayList<>();
        
        void setItems(List<DecryptedDataItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_data_entry, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DecryptedDataItem item = items.get(position);
            holder.bind(item);
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            TextView summaryText;
            ImageButton deleteButton;
            
            ViewHolder(View itemView) {
                super(itemView);
                timeText = itemView.findViewById(R.id.time_text);
                summaryText = itemView.findViewById(R.id.summary_text);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
            
            void bind(DecryptedDataItem item) {
                timeText.setText(dateFormat.format(new Date(item.getTimestamp())));
                summaryText.setText(createSummaryFromData(item));
                
                deleteButton.setOnClickListener(v -> {
                    // TODO: Implement single item delete
                    Toast.makeText(DataViewActivity.this, 
                        "Delete feature coming soon", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
