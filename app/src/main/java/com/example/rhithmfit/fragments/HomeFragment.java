package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rhithmfit.viewModels.SpotifyViewModel;
//import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.example.rhithmfit.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    String current_user;
    FragmentHomeBinding binding;
    FirebaseAuth firebase_auth;

//    private SpotifyAppRemote mSpotifyAppRemote;
    private String accessToken;
    SpotifyViewModel spotifyViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance(String workout_intensity) {

        Bundle args = new Bundle();
        args.putString("Intensity", workout_intensity);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase_auth = FirebaseAuth.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Log.d("Current User", current_user);
        binding.textViewUserName.setText("Hello "+ current_user + "!");

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase_auth.signOut();
                listener.logout();
            }
        });

        binding.buttonHomeStartNewWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.goToWorkoutCreation();
            }
        });

//        binding.buttonLLLLL.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mSpotifyAppRemote = spotifyViewModel.getSpotifyAppRemote().getValue();
//                if (mSpotifyAppRemote == null) {
//                    Log.d("PPP", "Spotify app not connected");
//                }
//                else {
//                    accessToken = spotifyViewModel.getAccessToken().getValue();
//                    Log.d("PPP", accessToken);
//                    mSpotifyAppRemote.getPlayerApi().play("spotify:track:4R5bSS8yoCl2czeWLr61aO");
//                }
//            }
//        });
//        binding.buttonOpenMusic.setOnClickListener(v ->{
//            listener.openMusic();
//        });
    }


    HomeListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeListener) {
            listener = (HomeListener) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    public interface HomeListener {
        void logout();
        void goToWorkoutCreation();
        void openMusic();
    }
}