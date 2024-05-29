package com.thestart.fitness_gain;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class StepDetectorService extends Service implements SensorEventListener {

    private static final String TAG = "StepDetectorService";
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private SharedPreferences sharedPreferences;
    private long lastStepTime = 0;
    private static final long STEP_THRESHOLD_TIME = 500;
    private int stepCount = 0;
    private long stepActivityDuration = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e(TAG, "Step Detector Sensor Not Available");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - lastStepTime > STEP_THRESHOLD_TIME) {
                stepCount += event.values.length;
                lastStepTime = currentTime;
                updateStepCount();
            }
            if (stepActivityDuration == 0) {
                stepActivityDuration = currentTime;
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

    private void updateStepCount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stepCount", stepCount);
        editor.putLong("stepActivityDuration", stepActivityDuration);
        editor.apply();
    }

    private void updateStepActivityDuration() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("stepActivityDuration", stepActivityDuration);
        editor.apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
