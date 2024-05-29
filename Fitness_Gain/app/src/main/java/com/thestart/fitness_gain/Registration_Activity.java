package com.thestart.fitness_gain;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Registration_Activity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout textInputLayoutName, textInputLayoutPhone, textInputLayoutEmail, textInputLayoutPassword, textInputLayoutRePassword;
    private TextInputEditText editTextName, editTextPhone, editTextEmail, editTextPassword, editTextRePassword;
    private ImageView profileImage;
    private Button registerButton, buttonSelectImage;
    private TextView redirectToLogin;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        textInputLayoutName = findViewById(R.id.textInputLayoutName);
        textInputLayoutPhone = findViewById(R.id.textInputLayoutPhone);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutRePassword = findViewById(R.id.textInputLayoutrePassword);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextRePassword = findViewById(R.id.editTextRePassword);

        profileImage = findViewById(R.id.profileImage);
        buttonSelectImage = findViewById(R.id.button_select_image);
        registerButton = findViewById(R.id.register_button);
        redirectToLogin = findViewById(R.id.redirectTologin);

        registerButton.setOnClickListener(v -> registerUser());
        buttonSelectImage.setOnClickListener(v -> openFileChooser());
        redirectToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Registration_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> storeUserInfoInFirestore(uri.toString())))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        storeUserInfoInFirestore(null);
                    });
        } else {
            storeUserInfoInFirestore(null);
        }
    }

    private void registerUser() {
        if (allChecked()) {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            uploadImageToFirebaseStorage();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Registration has Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void storeUserInfoInFirestore(String profileImageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String fullName = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Map<String, Object> user = new HashMap<>();
            user.put("fullname", fullName);
            user.put("phone", phone);
            user.put("email", email);
            user.put("passwd", password);
            user.put("profileImageURL", profileImageUrl);

            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(Registration_Activity.this, Login_Activity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error storing user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allChecked() {
        String username = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rePassword = editTextRePassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            textInputLayoutName.setError("Username is required");
            return false;
        } else {
            textInputLayoutName.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            textInputLayoutPhone.setError("Phone number is required");
            return false;
        } else if (!isValidPhoneNumber(phone)) {
            textInputLayoutPhone.setError("Invalid phone number format");
            return false;
        } else {
            textInputLayoutPhone.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Invalid email format");
            return false;
        } else {
            textInputLayoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setError("Password is required");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        if (!password.equals(rePassword)) {
            textInputLayoutRePassword.setError("Passwords do not match");
            return false;
        } else {
            textInputLayoutRePassword.setError(null);
        }
        return true;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // A simple regex for validating international phone numbers
        Pattern pattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
        return pattern.matcher(phoneNumber).matches();
    }
}
