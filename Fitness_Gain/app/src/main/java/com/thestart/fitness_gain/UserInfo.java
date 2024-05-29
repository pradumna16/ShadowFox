package com.thestart.fitness_gain;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import androidx.constraintlayout.utils.widget.MotionButton;

public class UserInfo extends AppCompatActivity {

    private TextInputLayout textInputLayoutInfoName, textInputLayoutInfoPhone, textInputLayoutInfoEmail, textInputLayoutInfoPassword, textInputLayoutReInfoPassword;
    private TextInputEditText editTextInfoName, editTextInfoPhone, editTextInfoEmail, editTextInfoPassword, editTextReInfoPassword;
    private MotionButton  btnLogout, btnDeleteAccount;
    private Button saveInfo;
    private ImageView profile_imageInfo;
    private Uri imageUri;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        textInputLayoutInfoName = findViewById(R.id.textInputLayoutInfoName);
        textInputLayoutInfoPhone = findViewById(R.id.textInputLayoutInfoPhone);
        textInputLayoutInfoEmail = findViewById(R.id.textInputLayoutInfoEmail);
        textInputLayoutInfoPassword = findViewById(R.id.textInputLayoutInfoPassword);
        textInputLayoutReInfoPassword = findViewById(R.id.textInputLayoutreInfoPassword);

        editTextInfoName = findViewById(R.id.editTexInfotName);
        editTextInfoPhone = findViewById(R.id.editTextInfoPhone);
        editTextInfoEmail = findViewById(R.id.editTextInfoEmail);
        editTextInfoPassword = findViewById(R.id.editTextInfoPassword);
        editTextReInfoPassword = findViewById(R.id.editTextReInfoPassword);

        profile_imageInfo = findViewById(R.id.profile_imageInfo);
        saveInfo = findViewById(R.id.saveInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images/" + currentUser.getUid() + ".jpg");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    editTextInfoName.setText(document.getString("fullname"));
                                    editTextInfoPhone.setText(document.getString("phone"));
                                    editTextInfoEmail.setText(document.getString("email"));
                                    String profileImageUrl = document.getString("profileImageURL");
                                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                        Picasso.get().load(profileImageUrl).into(profile_imageInfo);
                                    }
                                } else {
                                    Toast.makeText(UserInfo.this, "No user information found. Please register first.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(UserInfo.this, "Failed to retrieve user information. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        profile_imageInfo.setOnClickListener(v -> openFileChooser());
        saveInfo.setOnClickListener(v -> saveUserInfo());
        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
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
            profile_imageInfo.setImageURI(imageUri);
        }
    }

    private void saveUserInfo() {
        if (allChecked()) {
            String fullName = editTextInfoName.getText().toString().trim();
            String phone = editTextInfoPhone.getText().toString().trim();
            String email = editTextInfoEmail.getText().toString().trim();
            String password = editTextInfoPassword.getText().toString().trim();
            String rePassword = editTextReInfoPassword.getText().toString().trim();

            if (!password.equals(rePassword)) {
                textInputLayoutReInfoPassword.setError("Passwords do not match");
                return;
            }

            if (imageUri != null) {
                uploadImageToFirebaseStorage(fullName, phone, email, password);
            } else {
                updateUserInfo(null, fullName, phone, email, password);
            }
        }
    }

    private void uploadImageToFirebaseStorage(String fullName, String phone, String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    updateUserInfo(uri.toString(), fullName, phone, email, password);
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UserInfo.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUserInfo(null, fullName, phone, email, password);
                });
    }

    private void updateUserInfo(String profileImageUrl, String fullName, String phone, String email, String password) {
        String userId = currentUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("fullname", fullName);
        user.put("phone", phone);
        user.put("email", email);
        if (profileImageUrl != null) {
            user.put("profileImageURL", profileImageUrl);
        }

        db.collection("users").document(userId).update(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserInfo.this, "User Info Updated", Toast.LENGTH_SHORT).show();
                    if (!TextUtils.isEmpty(password)) {
                        currentUser.updatePassword(password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(UserInfo.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UserInfo.this, "Password Update Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfo.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean allChecked() {
        String username = editTextInfoName.getText().toString().trim();
        String phone = editTextInfoPhone.getText().toString().trim();
        String email = editTextInfoEmail.getText().toString().trim();
        String password = editTextInfoPassword.getText().toString().trim();
        String rePassword = editTextReInfoPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            textInputLayoutInfoName.setError("Username is required");
            return false;
        } else {
            textInputLayoutInfoName.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            textInputLayoutInfoPhone.setError("Phone number is required");
            return false;
        } else {
            textInputLayoutInfoPhone.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            textInputLayoutInfoEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutInfoEmail.setError("Invalid email format");
            return false;
        } else {
            textInputLayoutInfoEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            textInputLayoutInfoPassword.setError("Password is required");
            return false;
        } else {
            textInputLayoutInfoPassword.setError(null);
        }

        if (!password.equals(rePassword)) {
            textInputLayoutReInfoPassword.setError("Passwords do not match");
            return false;
        } else {
            textInputLayoutReInfoPassword.setError(null);
        }
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(UserInfo.this, Login_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void deleteAccount() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Account...");
        progressDialog.show();

        String userId = currentUser.getUid();

        // Delete user data from Firestore
        db.collection("users").document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Delete user from Firebase Auth
                        currentUser.delete()
                                .addOnCompleteListener(task1 -> {
                                    progressDialog.dismiss();
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(UserInfo.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(UserInfo.this, Login_Activity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(UserInfo.this, "Failed to Delete Account: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(UserInfo.this, "Failed to Delete User Data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
