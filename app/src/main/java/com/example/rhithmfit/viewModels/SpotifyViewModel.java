package com.example.rhithmfit.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyViewModel extends AndroidViewModel {
    MutableLiveData<SpotifyAppRemote> spotifyAppRemote;

    public SpotifyViewModel(@NonNull Application application) {
        super(application);
        spotifyAppRemote = new MutableLiveData<>();
    }

    public void setSpotifyAppRemote(SpotifyAppRemote remote) {
        spotifyAppRemote.setValue(remote);
    }

    public MutableLiveData<SpotifyAppRemote> getSpotifyAppRemote() {
        return spotifyAppRemote;
    }

}
