package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet for additional quick actions
 */
public class MoreOptionsBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_more_options, container, false);
        
        setupOptions(view);
        
        return view;
    }
    
    private void setupOptions(View view) {
        // Quick Add
        LinearLayout quickAddOption = view.findViewById(R.id.option_quick_add);
        quickAddOption.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), QuickAddActivity.class));
            dismiss();
        });
        
        // View History
        LinearLayout historyOption = view.findViewById(R.id.option_history);
        historyOption.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), DataSummaryActivity.class));
            dismiss();
        });
        
        // Export Data
        LinearLayout exportOption = view.findViewById(R.id.option_export);
        exportOption.setOnClickListener(v -> {
            // TODO: Implement export
            dismiss();
        });
        
        // Settings
        LinearLayout settingsOption = view.findViewById(R.id.option_settings);
        settingsOption.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SettingsActivity.class));
            dismiss();
        });
    }
}
