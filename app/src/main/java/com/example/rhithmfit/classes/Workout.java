package com.example.rhithmfit.classes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Workout {
    private String intensity;
    private String date;

    public Workout() {} // Needed for Firebase

    public Workout(String intensity, String date) {
        this.intensity = intensity;
        this.date = date;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getDate() {
        return date;
    }

    public String getDisplayName() {
        return intensity + " Intensity " + date;
    }
}