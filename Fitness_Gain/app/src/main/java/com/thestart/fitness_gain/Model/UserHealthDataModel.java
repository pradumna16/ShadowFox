package com.thestart.fitness_gain.Model;

import java.util.ArrayList;
import java.util.List;

public class UserHealthDataModel {
    private String userId;
    private int steps;
    private int calories;
    private double distance;
    private double bmi;
    private String gender;
    private double weight;
    private double height;
    private int age;
    private int stepGoal;
    private int calorieGoal;
    private String date;
    private List<Integer> weeklyProgress;

    // Default constructor for Firestore serialization
    public UserHealthDataModel() {
        this.weeklyProgress = new ArrayList<>();
    }

    // Parameterized constructor
    public UserHealthDataModel(String userId, int steps, int calories, double bmi, String gender, double weight, double height, int age, int stepGoal, int calorieGoal, String date, List<Integer> weeklyProgress, double distance) {
        this.userId = userId;
        this.steps = steps >= 0 ? steps : 0;
        this.calories = calories >= 0 ? calories : 0;
        this.distance = distance >= 0 ? distance : 0.0;
        this.bmi = bmi >= 0 ? bmi : 0.0;
        this.gender = gender;
        this.weight = weight >= 0 ? weight : 0.0;
        this.height = height >= 0 ? height : 0.0;
        this.age = age >= 0 ? age : 0;
        this.stepGoal = stepGoal >= 0 ? stepGoal : 0;
        this.calorieGoal = calorieGoal >= 0 ? calorieGoal : 0;
        this.date = date;
        this.weeklyProgress = (weeklyProgress != null) ? weeklyProgress : new ArrayList<>();
    }

    // Getters and Setters with validation
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps >= 0 ? steps : this.steps; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories >= 0 ? calories : this.calories; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance >= 0 ? distance : this.distance; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi >= 0 ? bmi : this.bmi; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight >= 0 ? weight : this.weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height >= 0 ? height : this.height; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age >= 0 ? age : this.age; }

    public int getStepGoal() { return stepGoal; }
    public void setStepGoal(int stepGoal) { this.stepGoal = stepGoal >= 0 ? stepGoal : this.stepGoal; }

    public int getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(int calorieGoal) { this.calorieGoal = calorieGoal >= 0 ? calorieGoal : this.calorieGoal; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<Integer> getWeeklyProgress() { return weeklyProgress; }
    public void setWeeklyProgress(List<Integer> weeklyProgress) { this.weeklyProgress = (weeklyProgress != null) ? weeklyProgress : new ArrayList<>(); }

    // Utility methods
    public void addDailyProgress(int steps) {
        if (this.weeklyProgress.size() >= 7) {
            this.weeklyProgress.remove(0); // remove oldest entry to keep the list size to 7
        }
        this.weeklyProgress.add(steps);
    }

    @Override
    public String toString() {
        return "UserHealthDataModel{" +
                "userId='" + userId + '\'' +
                ", steps=" + steps +
                ", calories=" + calories +
                ", distance=" + distance +
                ", bmi=" + bmi +
                ", gender='" + gender + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                ", stepGoal=" + stepGoal +
                ", calorieGoal=" + calorieGoal +
                ", date='" + date + '\'' +
                ", weeklyProgress=" + weeklyProgress +
                '}';
    }
}
