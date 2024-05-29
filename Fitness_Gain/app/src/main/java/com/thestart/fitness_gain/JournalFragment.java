package com.thestart.fitness_gain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thestart.fitness_gain.Adapter.JournalAdapter;
import com.thestart.fitness_gain.Model.JournalEntry;
import java.util.ArrayList;
import java.util.List;

public class JournalFragment extends Fragment {
    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalEntries;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        recyclerView = view.findViewById(R.id.journal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        journalEntries = new ArrayList<>();
        adapter = new JournalAdapter(journalEntries);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        view.findViewById(R.id.add_entry_button).setOnClickListener(v -> showAddEntryDialog());

        // Get data from HomeFragment
        Bundle bundle = getArguments();
        if (bundle != null){
            /*String date = bundle.getString("date", "");*/ // Example, get date string from the bundle
            int steps = bundle.getInt("steps");
            int calories = bundle.getInt("calories");
            long stepActivityDuration = bundle.getLong("stepActivityDuration");

            JournalEntry entry = new JournalEntry("0000-00-00", steps, calories, stepActivityDuration, 0, 0, userId);
            addEntry(entry);
        }

        adapter.setOnItemClickListener(new JournalAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                showEditEntryDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteEntry(position);
            }
        });

        loadEntries();

        return view;
    }

    private void loadEntries() {
        db.collection("journal_entries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    journalEntries.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        JournalEntry entry = snapshot.toObject(JournalEntry.class);
                        if (entry != null) {
                            entry.setId(snapshot.getId());
                            journalEntries.add(entry);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to load entries", Toast.LENGTH_SHORT).show());
    }

    private void showAddEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_entry, null);
        EditText dateInput = view.findViewById(R.id.date_input);
        EditText stepsInput = view.findViewById(R.id.steps_input);
        EditText caloriesInput = view.findViewById(R.id.calories_input);
        EditText stepdurationInput = view.findViewById(R.id.stepduration_input);
        EditText waterIntakeInput = view.findViewById(R.id.water_intake_input);
        EditText sleepDurationInput = view.findViewById(R.id.sleep_duration_input);

        builder.setView(view)
                .setTitle("Add Entry")
                .setPositiveButton("Add", (dialog, which) -> {
                    String date = dateInput.getText().toString();
                    int steps = Integer.parseInt(stepsInput.getText().toString());
                    int calories = Integer.parseInt(caloriesInput.getText().toString());
                    long stepActivityDuration = Long.parseLong(stepdurationInput.getText().toString());
                    int waterIntake = Integer.parseInt(waterIntakeInput.getText().toString());
                    int sleepDuration = Integer.parseInt(sleepDurationInput.getText().toString());

                    JournalEntry entry = new JournalEntry(date, steps, calories, stepActivityDuration, waterIntake, sleepDuration, userId);
                    addEntry(entry);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showEditEntryDialog(int position) {
        JournalEntry entry = journalEntries.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_entry, null);
        EditText dateInput = view.findViewById(R.id.date_input);
        EditText stepsInput = view.findViewById(R.id.steps_input);
        EditText caloriesInput = view.findViewById(R.id.calories_input);
        EditText stepdurationInput = view.findViewById(R.id.stepduration_input);
        EditText waterIntakeInput = view.findViewById(R.id.water_intake_input);
        EditText sleepDurationInput = view.findViewById(R.id.sleep_duration_input);

        dateInput.setText(entry.getDate());
        stepsInput.setText(String.valueOf(entry.getSteps()));
        caloriesInput.setText(String.valueOf(entry.getCalories()));
        stepdurationInput.setText(String.valueOf(entry.getStepActivityDuration()));
        waterIntakeInput.setText(String.valueOf(entry.getWaterIntake()));
        sleepDurationInput.setText(String.valueOf(entry.getSleepDuration()));

        builder.setView(view)
                .setTitle("Edit Entry")
                .setPositiveButton("Update", (dialog, which) -> {
                    String date = dateInput.getText().toString();
                    int steps = Integer.parseInt(stepsInput.getText().toString());
                    int calories = Integer.parseInt(caloriesInput.getText().toString());
                    long stepActivityDuration = Long.parseLong(stepdurationInput.getText().toString());
                    int waterIntake = Integer.parseInt(waterIntakeInput.getText().toString());
                    int sleepDuration = Integer.parseInt(sleepDurationInput.getText().toString());

                    entry.setDate(date);
                    entry.setSteps(steps);
                    entry.setCalories(calories);
                    entry.setStepActivityDuration(stepActivityDuration);
                    entry.setWaterIntake(waterIntake);
                    entry.setSleepDuration(sleepDuration);
                    updateEntry(position, entry);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void addEntry(JournalEntry entry) {
        db.collection("journal_entries").add(entry)
                .addOnSuccessListener(documentReference -> {
                    entry.setId(documentReference.getId());
                    journalEntries.add(entry);
                    adapter.notifyItemInserted(journalEntries.size() - 1);
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to add entry", Toast.LENGTH_SHORT).show());
    }

    private void updateEntry(int position, JournalEntry entry) {
        db.collection("journal_entries").document(entry.getId())
                .set(entry)
                .addOnSuccessListener(aVoid -> adapter.notifyItemChanged(position))
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to update entry", Toast.LENGTH_SHORT).show());
    }

    private void deleteEntry(int position) {
        JournalEntry entry = journalEntries.get(position);
        db.collection("journal_entries").document(entry.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    journalEntries.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to delete entry", Toast.LENGTH_SHORT).show());
    }
}
