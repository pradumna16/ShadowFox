package com.thestart.fitness_gain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.ViewModel.SharedViewModel;
import com.thestart.fitness_gain.helper.FirestoreDatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayFragment extends Fragment {
    private SharedViewModel sharedViewModel;
    private LineChart chart;
    private String userId = "userId";
    // Replace with actual user ID


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        chart = view.findViewById(R.id.chart);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        fetchAndPopulateData();
        return view;
    }

    private void fetchAndPopulateData() {
        FirestoreDatabaseHelper dbHelper = new FirestoreDatabaseHelper();
        String userId = getCurrentUserId(); // Get current user ID from Firebase Authentication
        String currentDate = getCurrentDate();
        dbHelper.getUserDataForPeriod(userId, getCurrentDate(), getCurrentDate(), new FirestoreDatabaseHelper.FirestoreCallback<List<UserHealthDataModel>>() {
            @Override
            public void onCallback(List<UserHealthDataModel> result) {
                int totalCalories = 0;
                int totalSteps = 0;

                for (UserHealthDataModel data : result) {
                    totalCalories += data.getCalories();
                    totalSteps += data.getSteps();
                }

                if (result.size() > 0) {
                    int avgCalories = totalCalories / result.size();
                    int avgSteps = totalSteps / result.size();
                    populateChart(avgCalories, avgSteps);
                } else {
                    // Handle the case where there's no data
                    populateChart(0, 0);
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }


        private String getCurrentUserId() {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            return user != null ? user.getUid() : ""; // Return empty string if user is not authenticated
        }


    private void populateChart(int avgCalories, int avgSteps) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, avgCalories));
        entries.add(new Entry(2, avgSteps));

        LineDataSet dataSet = new LineDataSet(entries, "Average Data");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getResources().getColor(R.color.fill_color));
        dataSet.setValueTextSize(12f);
        dataSet.setColor(getResources().getColor(R.color.line_color));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
