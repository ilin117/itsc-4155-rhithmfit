package com.example.rhithmfit.classes;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private String id;

    public Song() {

    }

    public Song(String title, String artist, String id) {
        this.title = title;
        this.artist = artist;
        this.id = id;
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getId() { return id; }


    @Override
    public String toString() {
        return title + " – " + artist;
    }

}
