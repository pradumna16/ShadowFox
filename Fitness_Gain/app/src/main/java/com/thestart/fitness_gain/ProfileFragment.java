package com.thestart.fitness_gain;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.ViewModel.SharedViewModel;
import com.thestart.fitness_gain.helper.FirestoreDatabaseHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ProfileFragment extends Fragment {

    private EditText profileSteps, profileCalories, profileWeight, profileHeight, profileStepGoal, profileCaloriesGoal, profileAge;
    private TextView profileBirthday, profileBMI, profileBMICategory ;
    private ImageView profile_imageinfo;
    private Spinner profileGender;
    private Button btnSelectBirthday, btnSave;
    private ProgressBar bmiMeter;
    private FirestoreDatabaseHelper dbHelper;
    private String userId;
    private SharedViewModel sharedViewModel;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements
        profile_imageinfo = view.findViewById(R.id.profile_imageinfo);
        profileSteps = view.findViewById(R.id.profile_steps);
        profileCalories = view.findViewById(R.id.profile_calories);
        profileStepGoal = view.findViewById(R.id.profile_stepgoal);
        profileCaloriesGoal = view.findViewById(R.id.profile_caloriesgoal);
        profileWeight = view.findViewById(R.id.profile_weight);
        profileHeight = view.findViewById(R.id.profile_height);
        profileGender = view.findViewById(R.id.profile_gender);
        profileAge = view.findViewById(R.id.AgeView);
        profileBirthday = view.findViewById(R.id.profile_birthday);
        profileBMI = view.findViewById(R.id.profile_bmi);
        bmiMeter = view.findViewById(R.id.bmi_meter);
        btnSelectBirthday = view.findViewById(R.id.btn_select_birthday);
        btnSave = view.findViewById(R.id.btn_save);
        profileBMICategory = view.findViewById(R.id.profile_bmi_category);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileGender.setAdapter(adapter);

        // Initialize helpers and ViewModel
        dbHelper = new FirestoreDatabaseHelper();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Observe shared ViewModel
        sharedViewModel.getUserHealthData().observe(getViewLifecycleOwner(), this::populateUserData);
        sharedViewModel.getUserData().observe(getViewLifecycleOwner(), userData -> {
            if (userData != null) {
                userId = currentUser.getUid();
                fetchUserProfileImage(userId);
                fetchUserDailyData(userId, getCurrentDate());
            }
        });

        btnSelectBirthday.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> saveUserData());
        profile_imageinfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserInfo.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchUserProfileImage(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String profileImageUrl = document.getString("profileImageURL");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(profile_imageinfo);
                            }
                        } else {
                            Toast.makeText(getContext(), "No user information found. Please register first.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve user information. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateUserData(UserHealthDataModel userHealthData) {
        if (userHealthData != null) {
            profileAge.setText(String.valueOf(userHealthData.getAge()));
            profileSteps.setText(String.valueOf(userHealthData.getSteps()));
            profileCalories.setText(String.valueOf(userHealthData.getCalories()));
            profileStepGoal.setText(String.valueOf(userHealthData.getStepGoal()));
            profileCaloriesGoal.setText(String.valueOf(userHealthData.getCalorieGoal()));
            profileWeight.setText(String.valueOf(userHealthData.getWeight()));
            profileHeight.setText(String.valueOf(userHealthData.getHeight()));
            profileBirthday.setText(userHealthData.getDate());
            setSpinnerSelection(profileGender, userHealthData.getGender());
            calculateAndDisplayBMI(userHealthData.getWeight(), userHealthData.getHeight());
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(value)) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    Calendar dob = Calendar.getInstance();
                    dob.set(year1, monthOfYear, dayOfMonth);
                    Calendar today = Calendar.getInstance();
                    int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    profileBirthday.setText(selectedDate);
                    profileAge.setText(String.valueOf(age));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateAndDisplayBMI(double weight, double height) {
        if (height > 0) {
            double bmi = weight / ((height / 100) * (height / 100));
            profileBMI.setText(String.format("%.2f", bmi));
            displayBMICategory(bmi);
            updateBMIMeter(bmi);
        } else {
            profileBMI.setText("Invalid height");
        }
    }

    private void updateBMIMeter(double bmi) {
        bmiMeter.setProgress((int) bmi);
    }

    private void displayBMICategory(double bmi) {
        String category;
        if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            category = "Normal weight";
        } else if (bmi >= 25 && bmi < 29.9) {
            category = "Overweight";
        } else {
            category = "Obese";
        }
        profileBMICategory.setText(category);
    }

    private void saveUserData() {
        try {
            double weight = Double.parseDouble(profileWeight.getText().toString());
            double height = Double.parseDouble(profileHeight.getText().toString());
            String gender = profileGender.getSelectedItem().toString();
            String birthday = profileBirthday.getText().toString();
            int age = Integer.parseInt(profileAge.getText().toString());
            double steps = Double.parseDouble(profileSteps.getText().toString());
            int calories = Integer.parseInt(profileCalories.getText().toString());
            int stepGoal = Integer.parseInt(profileStepGoal.getText().toString());
            int calorieGoal = Integer.parseInt(profileCaloriesGoal.getText().toString());
            List<Integer> weeklyProgress = Arrays.asList(0, 0, 0, 0, 0, 0, 0); // Placeholder for weekly progress

            UserHealthDataModel userHealthData = new UserHealthDataModel(userId, (int) steps, calories, calculateDistance(steps), gender, weight, height, age, stepGoal, calorieGoal, birthday, weeklyProgress, 0.0);

            dbHelper.updateUserHealthData(userId, userHealthData, new FirestoreDatabaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onCallback(Void result) {
                    Toast.makeText(getContext(), "User health data updated successfully!", Toast.LENGTH_LONG).show();
                    sharedViewModel.setUserHealthData(userHealthData); // Update SharedViewModel
                    ((MainActivity) getActivity()).updateHomeFragment();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to update user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            String today = getCurrentDate();
            dbHelper.updateUserDailyData(userId, userHealthData, today, new FirestoreDatabaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onCallback(Void result) {
                    Toast.makeText(getContext(), "Daily progress updated successfully!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to update daily progress: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers for weight, height, steps, and calories.", Toast.LENGTH_LONG).show();
        }
    }

    private double calculateDistance(double steps) {
        return (steps * 0.7) / 1000; //we are displaying distance in km
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    private void fetchUserDailyData(String userId, String date) {
        dbHelper.getUserDailyData(userId, date, new FirestoreDatabaseHelper.FirestoreCallback<UserHealthDataModel>() {
            @Override
            public void onCallback(UserHealthDataModel result) {
                if (result != null) {
                    profileSteps.setText(String.valueOf(result.getSteps()));
                    profileCalories.setText(String.valueOf(result.getCalories()));
                    sharedViewModel.setUserHealthData(result); // Update SharedViewModel
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to fetch daily data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
