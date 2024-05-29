package com.thestart.fitness_gain.helper;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thestart.fitness_gain.Model.UserDataModel;
import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.ViewModel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class FirestoreDatabaseHelper {

    private static final String TAG = "FirestoreDatabaseHelper";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FirestoreDatabaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onCallback(T result);
        void onError(Exception e);
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null; // User is not authenticated
        }
    }
    // Method to retrieve user profile data
    public void getUserData(String userId, FirestoreCallback<UserDataModel> callback) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            UserDataModel userData = document.toObject(UserDataModel.class);
                            SharedViewModel.getInstance().setUserData(userData);
                            callback.onCallback(userData);
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                        Log.e(TAG, "Error getting user data", e);
                    }
                });
    }

    public void getUserData(FirestoreCallback<UserDataModel> callback) {
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                UserDataModel userData = document.toObject(UserDataModel.class);
                                SharedViewModel.getInstance().setUserData(userData);
                                callback.onCallback(userData);
                            } else {
                                callback.onCallback(null);
                            }
                        } else {
                            Exception e = task.getException();
                            callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                            Log.e(TAG, "Error getting user data", e);
                        }
                    });
        } else {
            // Handle the case when the user is not authenticated
            callback.onError(new Exception("User is not authenticated"));
        }
    }


    // Method to update user profile data
    public void updateUserData(String userId, UserDataModel userData, FirestoreCallback<Void> callback) {
        // If userId is null, get the current user's ID
        if (userId == null) {
            userId = getCurrentUserId();
        }
        db.collection("users").document(userId).set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedViewModel.getInstance().setUserData(userData);
                        callback.onCallback(null);
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                        Log.e(TAG, "Error updating user data", e);
                    }
                });
    }


    // Method to update user profile data


    public void getUserDataForPeriod(String userId, String startDate, String endDate, FirestoreCallback<List<UserHealthDataModel>> callback) {
        db.collection("users")
                .document(userId)
                .collection("healthData")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserHealthDataModel> dataList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            UserHealthDataModel userHealthData = document.toObject(UserHealthDataModel.class);
                            if (userHealthData != null) {
                                dataList.add(userHealthData);
                            }
                        }
                        SharedViewModel.getInstance().setUserHealthDataList(dataList);
                        callback.onCallback(dataList);
                    } else {
                        Exception e = task.getException();
                        if (e == null) {
                            e = new Exception("Unknown error occurred");
                        }
                        callback.onError(e);
                    }
                });
    }

    public void getUserHealthData(String userId, FirestoreCallback<UserHealthDataModel> callback) {
        db.collection("user_health_data").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            UserHealthDataModel userHealthData = document.toObject(UserHealthDataModel.class);
                            SharedViewModel.getInstance().setUserHealthData(userHealthData);
                            callback.onCallback(userHealthData);
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                        Log.e(TAG, "Error getting user health data", e);
                    }
                });
    }

    public void updateUserHealthData(String userId, UserHealthDataModel userHealthData, FirestoreCallback<Void> callback) {
        db.collection("user_health_data")
                .document(userId)
                .set(userHealthData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedViewModel.getInstance().setUserHealthData(userHealthData);
                        callback.onCallback(null);
                    } else {
                        Exception e = task.getException();
                        if (e == null) {
                            e = new Exception("Unknown error occurred");
                        }
                        callback.onError(e);
                    }
                });
    }

    public void getUserDailyData(String userId, String date, FirestoreCallback<UserHealthDataModel> callback) {
        db.collection("users").document(userId).collection("dailyData").document(date).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            UserHealthDataModel userHealthData = document.toObject(UserHealthDataModel.class);
                            SharedViewModel.getInstance().setUserHealthData(userHealthData);
                            callback.onCallback(userHealthData);
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                        Log.e(TAG, "Error getting user daily data", e);
                    }
                });
    }

    public void updateUserDailyData(String userId, UserHealthDataModel userHealthData, String date, FirestoreCallback<Void> callback) {
        db.collection("users").document(userId).collection("dailyData").document(date).set(userHealthData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedViewModel.getInstance().setUserHealthData(userHealthData);
                        callback.onCallback(null);
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e : new Exception("Unknown error occurred"));
                        Log.e(TAG, "Error updating user daily data", e);
                    }
                });
    }
}
