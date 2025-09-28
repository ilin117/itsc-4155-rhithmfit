package com.example.rhithmfit.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rhithmfit.BuildConfig;
import com.example.rhithmfit.databinding.FragmentMusicBinding;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyViewModel extends AndroidViewModel {
    MutableLiveData<SpotifyAppRemote> spotifyAppRemote;
    MutableLiveData<String> accessToken;

    public SpotifyViewModel(@NonNull Application application) {
        super(application);
        spotifyAppRemote = new MutableLiveData<>();
        accessToken = new MutableLiveData<>();
    }

    public void setSpotifyAppRemote(SpotifyAppRemote remote) {
        spotifyAppRemote.setValue(remote);
    }

    public MutableLiveData<SpotifyAppRemote> getSpotifyAppRemote() {
        return spotifyAppRemote;
    }

    public void setAccessToken(String token) {
        accessToken.setValue(token);
    }

    public MutableLiveData<String> getAccessToken() {
        return accessToken;
    }
}
