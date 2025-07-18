// InsightsAdapter.java - This file should ONLY contain this class
package com.core.talita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.InsightViewHolder> {

    private List<Insight> insights = new ArrayList<>();

    public void setInsights(List<Insight> insights) {
        this.insights = insights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InsightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insight, parent, false);
        return new InsightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsightViewHolder holder, int position) {
        Insight insight = insights.get(position);
        holder.bind(insight);
    }

    @Override
    public int getItemCount() {
        return insights.size();
    }

    static class InsightViewHolder extends RecyclerView.ViewHolder {
        private final TextView insightText;
        private final TextView correlationText;
        private final ImageView strengthIndicator;

        public InsightViewHolder(@NonNull View itemView) {
            super(itemView);
            insightText = itemView.findViewById(R.id.insight_text);
            correlationText = itemView.findViewById(R.id.correlation_text);
            strengthIndicator = itemView.findViewById(R.id.strength_indicator);
        }

        public void bind(Insight insight) {
            insightText.setText(insight.getDescription());
            correlationText.setText(String.format("%d%% correlation",
                    (int)(insight.getCorrelationStrength() * 100)));

            // Set indicator color based on strength
            int color;
            if (insight.getCorrelationStrength() > 0.8) {
                color = itemView.getContext().getColor(R.color.success_green);
            } else if (insight.getCorrelationStrength() > 0.6) {
                color = itemView.getContext().getColor(R.color.warning_yellow);
            } else {
                color = itemView.getContext().getColor(R.color.text_secondary);
            }
            strengthIndicator.setColorFilter(color);
        }
    }
}