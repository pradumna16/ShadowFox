package com.thestart.fitness_gain;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class StepActivity extends AppCompatActivity  {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private boolean isSensorPresent;
    private int stepCount = 0;
    private long startTime, elapsedTime;
    private TextView stepCountText, elapsedTimeText, caloriesText;
    private Button startButton, stopButton;
    private boolean isTracking = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);




        stepCountText = findViewById(R.id.step_count);
        elapsedTimeText = findViewById(R.id.elapsed_time);
        caloriesText = findViewById(R.id.calories);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);


    }
}
