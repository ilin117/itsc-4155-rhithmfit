package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rhithmfit.BuildConfig;
import com.example.rhithmfit.R;
import com.example.rhithmfit.databinding.FragmentLandingBinding;
import com.example.rhithmfit.databinding.FragmentSpotifyCheckBinding;
import com.example.rhithmfit.viewModels.SpotifyViewModel;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyCheckFragment extends Fragment {

    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "rhithmfit://callback";
    private static final int REQUEST_CODE = 1337;
    FragmentSpotifyCheckBinding binding;
    SpotifyViewModel spotifyViewModel;

    public SpotifyCheckFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSpotifyCheckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSpotifyCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.connectSpotify();
                spotifyViewModel.getSpotifyAppRemote().observe(getViewLifecycleOwner(), new Observer<SpotifyAppRemote>() {
                    @Override
                    public void onChanged(SpotifyAppRemote spotifyAppRemote) {
                        listener.onSpotifyConnection();
                        Toast.makeText(getActivity(), "Spotify Connected!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    SpotifyCheckListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SpotifyCheckListener) {
            listener = (SpotifyCheckListener) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    public interface SpotifyCheckListener {
        void connectSpotify();
        void onSpotifyConnection();
    }
}