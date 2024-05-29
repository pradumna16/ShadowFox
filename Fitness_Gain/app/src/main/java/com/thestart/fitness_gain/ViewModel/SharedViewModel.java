package com.thestart.fitness_gain.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thestart.fitness_gain.Model.UserHealthDataModel;
import com.thestart.fitness_gain.Model.UserDataModel;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private static SharedViewModel instance;

    private final MutableLiveData<UserHealthDataModel> userHealthData = new MutableLiveData<>();
    private final MutableLiveData<UserDataModel> userData = new MutableLiveData<>();
    private final MutableLiveData<List<UserHealthDataModel>> userHealthDataList = new MutableLiveData<>();

    private SharedViewModel() {}

    public static synchronized SharedViewModel getInstance() {
        if (instance == null) {
            instance = new SharedViewModel();
        }
        return instance;
    }

    public void setUserHealthData(UserHealthDataModel data) {
        userHealthData.setValue(data);
    }

    public LiveData<UserHealthDataModel> getUserHealthData() {
        return userHealthData;
    }

    public void setUserData(UserDataModel data) {
        userData.setValue(data);
    }

    public LiveData<UserDataModel> getUserData() {
        return userData;
    }

    public void setUserHealthDataList(List<UserHealthDataModel> data) {
        userHealthDataList.setValue(data);
    }

    public LiveData<List<UserHealthDataModel>> getUserHealthDataList() {
        return userHealthDataList;
    }
}
