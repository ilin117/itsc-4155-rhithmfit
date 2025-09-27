package com.example.rhithmfit.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyViewModel extends AndroidViewModel {
    MutableLiveData<SpotifyAppRemote> spotifyAppRemote;
<<<<<<< HEAD
=======
    private final MutableLiveData<String> accessToken = new MutableLiveData<>(null);
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "rhithmfit://callback";
>>>>>>> 8795486 (Added Spotify Fetch)

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
    public void setAccessToken(String token) { accessToken.setValue(token); }
    public LiveData<String> getAccessToken() { return accessToken; }
}
