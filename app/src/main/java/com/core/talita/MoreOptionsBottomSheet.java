package com.core.talita;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet for additional options
 */
public class MoreOptionsBottomSheet extends BottomSheetDialogFragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // For now, just return a simple view
        View view = new View(getContext());
        view.setMinimumHeight(200);
        return view;
    }
}
