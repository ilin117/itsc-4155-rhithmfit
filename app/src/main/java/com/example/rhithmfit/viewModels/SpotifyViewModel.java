package com.example.rhithmfit.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.rhithmfit.BuildConfig;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotifyViewModel extends AndroidViewModel {
    MutableLiveData<SpotifyAppRemote> spotifyAppRemote;
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "rhithmfit://callback";

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
