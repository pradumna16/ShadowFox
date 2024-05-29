package com.thestart.fitness_gain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_Activity extends AppCompatActivity {
    private TextInputLayout textInputLayoutEmailLog, textInputLayoutPasswordLog;
    private TextInputEditText editTextEmailLog, editTextPasswordLog;
    private Button loginButton, OTPlogin_button;
    private TextView redirectToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        textInputLayoutEmailLog = findViewById(R.id.textInputLayoutEmailLog);
        textInputLayoutPasswordLog = findViewById(R.id.textInputLayoutPasswordLog);
        editTextEmailLog = findViewById(R.id.editTextEmailLog);
        editTextPasswordLog = findViewById(R.id.editTextPasswordLog);
        OTPlogin_button = findViewById(R.id.OTPlogin_button);
        loginButton = findViewById(R.id.login_button);
        redirectToRegister = findViewById(R.id.redirectToregister);

        OTPlogin_button.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, OTP_Login_Activity.class);
            startActivity(intent);
        });

        redirectToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, Registration_Activity.class);
            startActivity(intent);
            finish();
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = editTextEmailLog.getText().toString().trim();
        String password = editTextPasswordLog.getText().toString().trim();

        if (validateInput(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                saveLoginState(currentUser.getUid());
                                checkUserProfile(currentUser);
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmailLog.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmailLog.setError("Invalid email format");
            return false;
        } else {
            textInputLayoutEmailLog.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            textInputLayoutPasswordLog.setError("Password is required");
            return false;
        } else {
            textInputLayoutPasswordLog.setError(null);
        }

        return true;
    }

    private void checkUserProfile(FirebaseUser currentUser) {
        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Intent intent = new Intent(Login_Activity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveLoginState(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.apply();
    }
}
