package com.core.talita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Settings Activity with organized navigation
 */
public class SettingsActivity extends AppCompatActivity {

    private RecyclerView settingsSectionsRecycler;
    private SettingsSectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main);

        initializeViews();
        setupSettingsSections();
    }

    private void initializeViews() {
        settingsSectionsRecycler = findViewById(R.id.settings_sections_recycler);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void setupSettingsSections() {
        List<SettingsSection> sections = new ArrayList<>();

        sections.add(new SettingsSection(
                "🎯", "Background Tracking",
                "Location, activity recognition, and continuous monitoring",
                BackgroundTrackingSettingsActivity.class
        ));

        sections.add(new SettingsSection(
                "📊", "Data Collectors",
                "Choose which types of data to collect and configure",
                DataCollectorsSettingsActivity.class
        ));

        sections.add(new SettingsSection(
                "☁️", "Cloud Backup",
                "Secure cloud storage and synchronization options",
                CloudBackupSettingsActivity.class
        ));

        sections.add(new SettingsSection(
                "🔒", "Security & Privacy",
                "Encryption, access controls, and data protection",
                SecurityPrivacySettingsActivity.class
        ));

        sections.add(new SettingsSection(
                "📤", "Data Export",
                "Export your data and manage sharing",
                DataExportSettingsActivity.class
        ));

        sections.add(new SettingsSection(
                "ℹ️", "About",
                "App information, version, and legal notices",
                AboutSettingsActivity.class
        ));

        adapter = new SettingsSectionAdapter(sections, this::onSectionClicked);
        settingsSectionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        settingsSectionsRecycler.setAdapter(adapter);
    }

    private void onSectionClicked(SettingsSection section) {
        Intent intent = new Intent(this, section.activityClass);
        startActivity(intent);
    }

    /**
     * Data class for settings sections
     */
    public static class SettingsSection {
        public final String icon;
        public final String title;
        public final String description;
        public final Class<?> activityClass;

        public SettingsSection(String icon, String title, String description, Class<?> activityClass) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.activityClass = activityClass;
        }
    }

    /**
     * Adapter for settings sections
     */
    public static class SettingsSectionAdapter extends RecyclerView.Adapter<SettingsSectionAdapter.SectionViewHolder> {

        public interface SectionClickListener {
            void onSectionClicked(SettingsSection section);
        }

        private final List<SettingsSection> sections;
        private final SectionClickListener listener;

        public SettingsSectionAdapter(List<SettingsSection> sections, SectionClickListener listener) {
            this.sections = sections;
            this.listener = listener;
        }

        @Override
        public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_settings_section, parent, false);
            return new SectionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SectionViewHolder holder, int position) {
            SettingsSection section = sections.get(position);
            holder.bind(section, listener);
        }

        @Override
        public int getItemCount() {
            return sections.size();
        }

        static class SectionViewHolder extends RecyclerView.ViewHolder {
            private final TextView iconText;
            private final TextView titleText;
            private final TextView descriptionText;
            private final CardView cardView;

            SectionViewHolder(View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.section_icon);
                titleText = itemView.findViewById(R.id.section_title);
                descriptionText = itemView.findViewById(R.id.section_description);
                cardView = itemView.findViewById(R.id.section_card);
            }

            void bind(SettingsSection section, SectionClickListener listener) {
                iconText.setText(section.icon);
                titleText.setText(section.title);
                descriptionText.setText(section.description);

                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSectionClicked(section);
                    }
                });
            }
        }
    }
}