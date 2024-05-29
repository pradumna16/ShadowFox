package com.thestart.fitness_gain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.ViewModel.SharedViewModel;

public class MainActivity extends AppCompatActivity {
    private SharedViewModel sharedViewModel;
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private JournalFragment journalFragment;
    private ProfileFragment profileFragment;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore with offline capabilities
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Check if user is logged in
        checkIfUserLoggedIn();

        // Initialize fragments
        homeFragment = new HomeFragment();
        journalFragment = new JournalFragment();
        profileFragment = new ProfileFragment();

        // Start the StepDetectorService
        Intent stepServiceIntent = new Intent(this, StepDetectorService.class);
        startService(stepServiceIntent);

        // Set up BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigationbar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, homeFragment).commit();
                } else if (menuItem.getItemId() == R.id.journal) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, journalFragment).commit();
                } else if (menuItem.getItemId() == R.id.profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, profileFragment).commit();
                }
                return true;
            }
        });

        // Load home fragment by default
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    private void checkIfUserLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, Login_Activity.class);
            startActivity(intent);
            finish();
        } else {
            loadUserData();
        }
    }

    private void loadUserData() {
        String userId = sharedPreferences.getString("userId", "");
        if (!userId.isEmpty()) {
            firestore.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            UserHealthDataModel userData = document.toObject(UserHealthDataModel.class);
                            if (userData != null) {
                                sharedViewModel.setUserHealthData(userData);
                            }
                        } else {
                            // Handle the error
                            task.getException().printStackTrace();
                        }
                    });
        }
    }

    public void updateHomeFragment() {
        if (homeFragment != null && homeFragment.isVisible()) {
            homeFragment.loadUserData();
        }
    }
}
