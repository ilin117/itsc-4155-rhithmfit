// team 1: Issac, Brittany Avalos-Ortiz, Raj Dalsaniya

package com.example.rhithmfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.rhithmfit.fragments.HomeFragment;
import com.example.rhithmfit.fragments.LandingFragment;
import com.example.rhithmfit.fragments.LoginFragment;
import com.example.rhithmfit.fragments.PasswordResetFragment;
import com.example.rhithmfit.fragments.SignupFragment;
import com.example.rhithmfit.fragments.SpotifyCheckFragment;
import com.example.rhithmfit.fragments.WorkoutCreationFragment;
import com.example.rhithmfit.viewModels.SpotifyViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity implements WorkoutCreationFragment.WorkoutCreationListener, SpotifyCheckFragment.SpotifyCheckListener, LandingFragment.LandingListener, LoginFragment.LoginListener, PasswordResetFragment.PasswordResetListener, HomeFragment.HomeListener, SignupFragment.SignupListener {

    // spotify
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "rhithmfit://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private SpotifyViewModel spotifyViewModel;

    // firebase
    FirebaseAuth firebase_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spotifyViewModel = new ViewModelProvider(this).get(SpotifyViewModel.class);

        // remembers current user
        firebase_auth = FirebaseAuth.getInstance();

        if (firebase_auth.getCurrentUser() == null && mSpotifyAppRemote == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new LandingFragment()).commit();
        }
        else if (mSpotifyAppRemote == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new SpotifyCheckFragment()).commit();
        }
        else getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new HomeFragment()).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpotifyAppRemote.getPlayerApi().pause();
        Log.d("NNN", "onStop:" + mSpotifyAppRemote.toString());
        if (mSpotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            Log.d("NNN", "onStop:" + mSpotifyAppRemote.toString());
        }
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoSignup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginSuccessful() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SpotifyCheckFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRegisterSuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SpotifyCheckFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void sendToHome(String intensity) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, HomeFragment.newInstance(intensity))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToPasswordReset() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new PasswordResetFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new LandingFragment())
                .commit();
    }

    @Override
    public void goToWorkoutCreation() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new WorkoutCreationFragment())
                .addToBackStack(null)
                .commit();
    }

    // spotify auth flow. these two methods are connected
    @Override
    public void connectSpotify() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                        .setScopes(new String[]{"app-remote-control", "user-modify-playback-state", "user-library-read", "playlist-read-private"})
                        .setShowDialog(true);

        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    String accessToken = response.getAccessToken();
                    spotifyViewModel.setAccessToken(accessToken);
                    ConnectionParams connectionParams =
                            new ConnectionParams.Builder(CLIENT_ID)
                                    .setRedirectUri(REDIRECT_URI)
                                    .showAuthView(true)
                                    .build();

                    SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            spotifyViewModel.setSpotifyAppRemote(mSpotifyAppRemote);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e("MainActivity", "Failed to connect", throwable);
                        }
                    });
                    break;
                case ERROR:
                    Log.e("MainActivity", "Auth error: " + response.getError());
                    break;
                default:
                    Log.d("MainActivity", "Auth cancelled or unknown");
            }
        }
    }
    // end

    @Override
    public void onSpotifyConnection() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .commit();
    }
    @Override
    public void openMusic() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new com.example.rhithmfit.fragments.MusicFragment())
                .addToBackStack(null)
                .commit();
    }
}