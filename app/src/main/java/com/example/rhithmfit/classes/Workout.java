package com.example.rhithmfit.classes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Workout {
    private String intensity;
    private String date;
    private String id;
    private String name;

    public Workout() {}

    public Workout(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Workout(String intensity, String date, String id) {
        this.intensity = intensity;
        this.date = date;
        this.id = id;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getDate() {
        return date;
    }
    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return name;
    }
}