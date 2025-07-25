package com.core.talita.api;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.util.*;

/**
 * SimpleDataCollector - Ready-to-use collector for simple data types
 * 
 * Perfect for plugins that just need basic data collection with a simple input dialog.
 * Handles text, numbers, and basic selections.
 * 
 * Examples: Water intake, mood tracking, weight, simple notes
 */
public class SimpleDataCollector extends BaseDataCollector {
    private static final String TAG = "SimpleDataCollector";
    
    private final String type;
    private final String displayName;
    private final String description;
    private final String emoji;
    private final String category;
    private final String inputHint;
    private final InputType inputType;
    private final String unit;
    private final List<String> quickOptions;
    
    public enum InputType {
        TEXT,
        NUMBER,
        DECIMAL,
        CHOICE,
        EMOJI_PICKER
    }

    /**
     * Private constructor - use Builder
     */
    private SimpleDataCollector(Builder builder) {
        this.type = builder.type;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.emoji = builder.emoji;
        this.category = builder.category;
        this.inputHint = builder.inputHint;
        this.inputType = builder.inputType;
        this.unit = builder.unit;
        this.quickOptions = builder.quickOptions;
    }

    @Override
    public String getDataType() {
        return type;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getEmoji() {
        return emoji;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    protected boolean checkDeviceCapabilities() {
        // Simple collectors work on all devices
        return true;
    }

    @Override
    protected void onStartCollection() {
        // Simple collectors don't need automated collection
        Log.w(TAG, "Simple collectors don't support automated collection");
    }

    @Override
    protected void onStopCollection() {
        // Nothing to stop
    }

    @Override
    protected CollectorResult performCollection() {
        // Show input dialog
        showInputDialog();
        return CollectorResult.pending(type);
    }

    @Override
    protected CollectorResult performQuickCollection(Map<String, Object> data) {
        try {
            // Validate input based on type
            Object value = data.get("value");
            if (value == null) {
                return CollectorResult.failure(type, "No value provided");
            }

            // Validate based on input type
            switch (inputType) {
                case NUMBER:
                    if (!(value instanceof Number)) {
                        value = Integer.parseInt(value.toString());
                    }
                    break;
                case DECIMAL:
                    if (!(value instanceof Number)) {
                        value = Double.parseDouble(value.toString());
                    }
                    break;
                case CHOICE:
                    if (quickOptions != null && !quickOptions.contains(value.toString())) {
                        return CollectorResult.failure(type, "Invalid choice: " + value);
                    }
                    break;
            }

            // Create data map
            Map<String, Object> collectedData = new HashMap<>();
            collectedData.put("value", value);
            collectedData.put("unit", unit);
            collectedData.put("timestamp", System.currentTimeMillis());

            // Store the data
            storeData(collectedData);

            return CollectorResult.success(type, collectedData);

        } catch (Exception e) {
            return CollectorResult.failure(type, "Failed to collect: " + e.getMessage());
        }
    }

    /**
     * Show input dialog for manual collection
     */
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(emoji + " " + displayName);
        builder.setMessage(description);

        // Create input layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Add quick options if available
        if (quickOptions != null && !quickOptions.isEmpty()) {
            for (String option : quickOptions) {
                android.widget.Button quickButton = new android.widget.Button(context);
                quickButton.setText(option + (unit != null ? " " + unit : ""));
                quickButton.setOnClickListener(v -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("value", option);
                    performQuickCollection(data);
                    // Dismiss dialog
                });
                layout.addView(quickButton);
            }
        }

        // Add custom input
        EditText input = new EditText(context);
        input.setHint(inputHint);
        
        // Set input type
        switch (inputType) {
            case NUMBER:
                input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                break;
            case DECIMAL:
                input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                 android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case TEXT:
            default:
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }
        
        layout.addView(input);

        builder.setView(layout);

        // Buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString();
            if (!value.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("value", value);
                performQuickCollection(data);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Builder for SimpleDataCollector
     */
    public static class Builder {
        private String type;
        private String displayName;
        private String description;
        private String emoji = "📊";
        private String category = "i";
        private String inputHint = "Enter value";
        private InputType inputType = InputType.TEXT;
        private String unit;
        private List<String> quickOptions;

        public Builder(String type, String displayName) {
            this.type = type;
            this.displayName = displayName;
            this.description = "Track your " + displayName.toLowerCase();
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder inputHint(String hint) {
            this.inputHint = hint;
            return this;
        }

        public Builder inputType(InputType type) {
            this.inputType = type;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder quickOptions(String... options) {
            this.quickOptions = Arrays.asList(options);
            return this;
        }

        public SimpleDataCollector build() {
            return new SimpleDataCollector(this);
        }
    }
}
