package com.thestart.fitness_gain.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.thestart.fitness_gain.Model.JournalEntry;
import com.thestart.fitness_gain.R;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {
    private List<JournalEntry> journalEntries;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class JournalViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, stepsTextView, caloriesTextView, stepDurationTexxtView ,waterIntakeTextView, sleepDurationTextView;
        public Button editButton, deleteButton;

        public JournalViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            stepsTextView = itemView.findViewById(R.id.steps_text_view);
            caloriesTextView = itemView.findViewById(R.id.calories_text_view);
            stepDurationTexxtView = itemView.findViewById(R.id.stepduration_text_view);
            waterIntakeTextView = itemView.findViewById(R.id.water_intake_text_view);
            sleepDurationTextView = itemView.findViewById(R.id.sleep_duration_text_view);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(position);
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }

    public JournalAdapter(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal_entry, parent, false);
        return new JournalViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        JournalEntry currentEntry = journalEntries.get(position);
        holder.dateTextView.setText(currentEntry.getDate());
        holder.stepsTextView.setText("Steps: " + currentEntry.getSteps());
        holder.caloriesTextView.setText("Calories: " + currentEntry.getCalories());
        holder.stepDurationTexxtView.setText("Duration: " + currentEntry.getStepActivityDuration());
        holder.waterIntakeTextView.setText("Water Intake: " + currentEntry.getWaterIntake() + " ml");
        holder.sleepDurationTextView.setText("Sleep Duration: " + currentEntry.getSleepDuration() + " hrs");
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }
}
