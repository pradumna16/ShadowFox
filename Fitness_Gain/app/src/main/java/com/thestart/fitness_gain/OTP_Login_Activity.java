package com.thestart.fitness_gain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class OTP_Login_Activity extends AppCompatActivity {
    private TextInputLayout textInputLayoutPhoneOtp, textInputLayoutOTP;
    private TextInputEditText editTextPhoneOtp, editTextOTP;
    private Button sendOtpButton, verifyOtpButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textInputLayoutPhoneOtp = findViewById(R.id.textInputLayoutPhoneOtp);
        textInputLayoutOTP = findViewById(R.id.textInputLayoutOTP);
        editTextPhoneOtp = findViewById(R.id.editTextPhoneOtp);
        editTextOTP = findViewById(R.id.editTextOTP);
        sendOtpButton = findViewById(R.id.phoneVerification);
        verifyOtpButton = findViewById(R.id.otpVerification);

        sendOtpButton.setOnClickListener(v -> sendOtp());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }

    private void sendOtp() {
        String phoneNumber = editTextPhoneOtp.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            textInputLayoutPhoneOtp.setError("Phone number is required");
            return;
        } else if (!isValidPhoneNumber(phoneNumber)) {
            textInputLayoutPhoneOtp.setError("Invalid phone number format");
            return;
        } else {
            textInputLayoutPhoneOtp.setError(null);
        }

        // Check if phone number exists in Firestore
        db.collection("users").whereEqualTo("phone", phoneNumber).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                                        // Auto-retrieval or instant verification
                                        signInWithPhoneAuthCredential(credential);
                                    }

                                    @Override
                                    public void onVerificationFailed(FirebaseException e) {
                                        Toast.makeText(OTP_Login_Activity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                        OTP_Login_Activity.this.verificationId = verificationId;
                                       textInputLayoutOTP.setVisibility(View.VISIBLE);
                                        verifyOtpButton.setVisibility(View.VISIBLE);
                                        Toast.makeText(OTP_Login_Activity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    } else {
                        Toast.makeText(this, "Phone number not registered", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyOtp() {
        String otp = editTextOTP.getText().toString().trim();
        if (TextUtils.isEmpty(otp)) {
            textInputLayoutOTP.setError("OTP is required");
            return;
        } else {
            textInputLayoutOTP.setError(null);
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(OTP_Login_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                Toast.makeText(this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // A simple regex for validating international phone numbers
        Pattern pattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
        return pattern.matcher(phoneNumber).matches();
    }
}
