package com.thestart.fitness_gain;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.Model.UserDataModel;
import com.thestart.fitness_gain.ViewModel.SharedViewModel;
import com.thestart.fitness_gain.helper.FirestoreDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "HomeFragment";

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private boolean isSensorPresent;
    private TextView stepsCompleted, calorieCount, kmCount, moveCount;
    private ProgressBar stepProgressBar, calorieProgressBar;
    private Button startStepActivityButton;
    private int stepCount = 0;
    private double distanceCovered = 0.0;
    private int caloriesBurned = 0;

    private CircularProgressView circularProgressView;
    private TextView stepProgressText, calorieProgressText;

    private UserHealthDataModel userHealthData;
    private UserDataModel userData;
    private String userId;
    private FirestoreDatabaseHelper dbHelper;

    private long lastStepTime = 0;
    private static final long STEP_THRESHOLD_TIME = 500;
    private List<Integer> stepCounts = new ArrayList<>();
    private static final int WINDOW_SIZE = 5;

    private RelativeLayout weekActivity;
    private ImageView profileImageInfo;
    private SharedViewModel sharedViewModel;
    private long stepActivityStartTime = 0;
    private long stepActivityDuration = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        stepsCompleted = view.findViewById(R.id.steps_completed);
        calorieCount = view.findViewById(R.id.calorie_count);
        kmCount = view.findViewById(R.id.km_count);
        moveCount = view.findViewById(R.id.move_count);
        stepProgressBar = view.findViewById(R.id.step_progress_bar);
        calorieProgressBar = view.findViewById(R.id.calorie_progress_bar);
        stepProgressText = view.findViewById(R.id.step_progress_text);
        calorieProgressText = view.findViewById(R.id.calorie_progress_text);
        weekActivity = view.findViewById(R.id.week_activity);
        profileImageInfo = view.findViewById(R.id.profile_imagedinfo);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe UserHealthDataModel
        sharedViewModel.getUserHealthData().observe(getViewLifecycleOwner(), new Observer<UserHealthDataModel>() {
            @Override
            public void onChanged(UserHealthDataModel userHealthDataModel) {
                updateUIWithUserHealthData(userHealthDataModel);
            }
        });

        // Observe UserDataModel
        sharedViewModel.getUserData().observe(getViewLifecycleOwner(), new Observer<UserDataModel>() {
            @Override
            public void onChanged(UserDataModel userDataModel) {
                userData = userDataModel;
                // Use userData to update UI or perform actions
            }
        });

        dbHelper = new FirestoreDatabaseHelper();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchUserData(userId);
        }

        // Create bundle and pass data to JournalFragment
        Bundle bundle = new Bundle();
        bundle.putInt("steps", stepCount);
        bundle.putInt("calories", caloriesBurned);
        bundle.putLong("stepActivityDuration", stepActivityDuration);

        JournalFragment journalFragment = new JournalFragment();
        journalFragment.setArguments(bundle);


        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isSensorPresent = true;
        } else {
            stepsCompleted.setText("Step Detector Sensor Not Available");
            isSensorPresent = false;
        }

        profileImageInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserInfo.class);
            startActivity(intent);
        });

        weekActivity.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AnalyticsActivity.class);
            startActivity(intent);
        });

        startStepActivityButton = view.findViewById(R.id.start_step_activity_button);
        startStepActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StepActivity.class);
            startActivity(intent);
        });

        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        loadProfileImage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }
    }

    private void loadProfileImage(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                String profileImageUrl = document.getString("profileImageURL");
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Picasso.get().load(profileImageUrl).into(profileImageInfo);
                                }
                            }else {
                                Toast.makeText(getContext(), "No profile image found.", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(getContext(), "Failed to load profile image.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - lastStepTime > STEP_THRESHOLD_TIME) {
                stepCount += sensorEvent.values.length;
                lastStepTime = currentTime;
                updateStepCount();
            }
            if (stepActivityStartTime == 0) {
                stepActivityStartTime = currentTime;
            } else {
                stepActivityDuration += currentTime - lastStepTime;
            }
            lastStepTime = currentTime;
            updateStepActivityDuration();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not implemented
    }

    public void loadUserData() {
        if (userId != null) {
            dbHelper.getUserHealthData(userId, new FirestoreDatabaseHelper.FirestoreCallback<UserHealthDataModel>() {
                @Override
                public void onCallback(UserHealthDataModel result) {
                    userHealthData = result;
                    if (userHealthData != null) {
                        updateUIWithUserHealthData(userHealthData);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error fetching user data", e);
                }
            });
        }
    }

    private void fetchUserData(String userId) {
        dbHelper.getUserHealthData(userId, new FirestoreDatabaseHelper.FirestoreCallback<UserHealthDataModel>() {
            @Override
            public void onCallback(UserHealthDataModel result) {
                userHealthData = result;
                if (userHealthData != null) {
                    updateUIWithUserHealthData(userHealthData);
                    // Initialize stepCount with the value from Firestore
                    stepCount = userHealthData.getSteps();
                    distanceCovered = userHealthData.getDistance();
                    caloriesBurned = userHealthData.getCalories();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error fetching user health data", e);
            }
        });

        dbHelper.getUserData(userId, new FirestoreDatabaseHelper.FirestoreCallback<UserDataModel>() {
            @Override
            public void onCallback(UserDataModel result) {
                userData = result;
                if (userData != null) {
                    sharedViewModel.setUserData(userData);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error fetching user data", e);
            }
        });
    }

    private void updateUIWithUserHealthData(UserHealthDataModel userHealthDataModel) {
        if (userHealthDataModel != null) {
            stepsCompleted.setText("Steps Completed: " + userHealthDataModel.getSteps());
            kmCount.setText(String.format(Locale.getDefault(), "%.2f KM", userHealthDataModel.getDistance()));
            calorieCount.setText(String.format(Locale.getDefault(), "%d", userHealthDataModel.getCalories()));
            moveCount.setText(formatDuration(stepActivityDuration));

            // Update progress bars and their text views
            stepProgressBar.setMax(userHealthDataModel.getStepGoal());
            stepProgressBar.setProgress(userHealthDataModel.getSteps());
            stepProgressText.setText("Steps: " + userHealthDataModel.getSteps() + "/" + userHealthDataModel.getStepGoal());

            calorieProgressBar.setMax(userHealthDataModel.getCalorieGoal());
            calorieProgressBar.setProgress(userHealthDataModel.getCalories());
            calorieProgressText.setText("Calories: " + userHealthDataModel.getCalories() + "/" + userHealthDataModel.getCalorieGoal());
        }
    }

    private void updateStepCount() {
        stepCounts.add(stepCount);
        if (stepCounts.size() > WINDOW_SIZE) {
            stepCounts.remove(0);
        }

        int sum = 0;
        for (int count : stepCounts) {
            sum += count;
        }
        int smoothedStepCount = sum / stepCounts.size();

        stepsCompleted.setText("Steps Completed: " + smoothedStepCount);
        calculateMetrics();
        updateUserHealthDataInDatabase(smoothedStepCount); // Update the method to take smoothedStepCount as parameter
        /*circularProgressView.setProgress(smoothedStepCount);*/ //i wiil use this later

        // Update progress bars and their text views
        if (userHealthData != null) {
            stepProgressBar.setProgress(smoothedStepCount);
            stepProgressText.setText("Steps: " + smoothedStepCount + "/" + userHealthData.getStepGoal());

            calorieProgressBar.setProgress(caloriesBurned);
            calorieProgressText.setText("Calories: " + caloriesBurned + "/" + userHealthData.getCalorieGoal());
        }
    }

    private void updateUserHealthDataInDatabase(int smoothedStepCount) {
        if (userHealthData != null) {
            userHealthData.setSteps(smoothedStepCount);
            userHealthData.setDistance(distanceCovered);
            userHealthData.setCalories(caloriesBurned);
            String today = getCurrentDate();
            sharedViewModel.setUserHealthData(userHealthData); // Update ViewModel

            dbHelper.updateUserHealthData(userId, userHealthData, new FirestoreDatabaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onCallback(Void result) {
                    // Handle success case if needed
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to update daily data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error updating daily data", e);
                }
            });
        }
    }

    private void calculateMetrics() {
        double strideLength = 0.78;
        distanceCovered = (stepCount * strideLength) / 1000;
        caloriesBurned = (int) (stepCount * 0.04);

        kmCount.setText(String.format(Locale.getDefault(), "%.2f KM", distanceCovered));
        calorieCount.setText(String.format(Locale.getDefault(), "%d", caloriesBurned));
        moveCount.setText(String.valueOf(stepCount));
    }

    private void updateStepActivityDuration() {
        // Update the UI to reflect the duration
        moveCount.setText(formatDuration(stepActivityDuration));
    }

    private String formatDuration(long durationInMillis) {
        int seconds = (int) (durationInMillis / 1000) % 60;
        int minutes = (int) ((durationInMillis / (1000 * 60)) % 60);
        int hours = (int) ((durationInMillis / (1000 * 60 * 60)) % 24);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getDateDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }
}
