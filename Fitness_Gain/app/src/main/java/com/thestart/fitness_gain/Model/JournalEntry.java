package com.thestart.fitness_gain.Model;

public class JournalEntry {
    private String id;
    private String date;
    private int steps;
    private int calories;
    private long stepActivityDuration;
    private int waterIntake;
    private int sleepDuration;
    private String userId;

    public JournalEntry() { }

    public JournalEntry(String date, int steps, int calories, long stepActivityDuration, int waterIntake, int sleepDuration, String userId) {
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.stepActivityDuration = stepActivityDuration;
        this.waterIntake = waterIntake;
        this.sleepDuration = sleepDuration;
        this.userId = userId;
    }

    // Getters and Setters for all fields including id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public long getStepActivityDuration() {
        return stepActivityDuration;
    }

    public void setStepActivityDuration(long stepActivityDuration) {
        this.stepActivityDuration = stepActivityDuration;
    }

    public int getWaterIntake() {
        return waterIntake;
    }

    public void setWaterIntake(int waterIntake) {
        this.waterIntake = waterIntake;
    }

    public int getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(int sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
