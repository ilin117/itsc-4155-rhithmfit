package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.rhithmfit.R;
import com.example.rhithmfit.databinding.FragmentMusicBinding;
import com.example.rhithmfit.databinding.FragmentWorkoutBuilderBinding;
import com.example.rhithmfit.viewModels.SpotifyViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class WorkoutBuilderFragment extends Fragment {

    SpotifyViewModel spotifyViewModel;
    private static final String ME_TRACKS_URL = "https://api.spotify.com/v1/me/tracks";
    private final OkHttpClient http = new OkHttpClient();
    private final List<String> titles = new ArrayList<>();
    private final List<String> workouts = new ArrayList<>();
    FragmentWorkoutBuilderBinding binding;
    private ArrayAdapter<String> songAdapter;
    private ArrayAdapter<String> workoutAdapter;
    private static final String TAG = "MusicFragment";
    private final Handler main = new Handler(Looper.getMainLooper());
    String workout_intensity;

    public WorkoutBuilderFragment() {
        // Required empty public constructor
    }

    public static WorkoutBuilderFragment newInstance(String workout_intensity) {

        Bundle args = new Bundle();
        args.putString("Intensity", workout_intensity);
        WorkoutBuilderFragment fragment = new WorkoutBuilderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workout_intensity = getArguments().getString("Intensity");
        }
        spotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWorkoutBuilderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, titles);
        workoutAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, workouts);
        binding.listViewSongsList.setAdapter(songAdapter);
        binding.listViewWorkoutsList.setAdapter(workoutAdapter);
        binding.textViewBuilderIntensity.setText(workout_intensity + " Intensity Workouts");

        binding.buttonBackLogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.back();
            }
        });
        startFetch();
        fetchWorkouts();

        binding.buttonCompletedWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendToWorkoutCompleted();
            }
        });
    }

    private void startFetch() {
        String token = spotifyViewModel.getAccessToken().getValue();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "No Spotify access token. Connect again.", Toast.LENGTH_SHORT).show();
            return;
        }
        titles.clear();
        songAdapter.notifyDataSetChanged();

        fetchSavedTracksPaged(token, 5);
    }

    private void fetchWorkouts() {
        Request request = new Request.Builder()
                .url("https://exercisedb.p.rapidapi.com/exercises")
                .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "00fb2da46cmsh8e9f4b0be71d476p106048jsn81388490ab95")
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONArray exercises = new JSONArray(responseBody);
                        List<JSONObject> workoutList = new ArrayList<>();

                        for (int i = 0; i < exercises.length(); i++) {
                            workoutList.add(exercises.getJSONObject(i));
                        }

                        Collections.shuffle(workoutList);
                        List<JSONObject> randomSeven = workoutList.subList(0, 7);

                        workouts.clear();
                        for (JSONObject workout : randomSeven) {
                            workouts.add(workout.getString("name"));
                        }

                        main.post(() -> {
                            workoutAdapter.notifyDataSetChanged();
                            Log.d("WORKOUTSS", "Displayed: " + workouts);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchSavedTracksPaged(String token, int limit) {

        int randomOffset = new java.util.Random().nextInt(20);

        HttpUrl url = HttpUrl.parse(ME_TRACKS_URL).newBuilder()
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf(randomOffset))
                .build();

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                main.post(() -> {
                    Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "HTTP failure", e);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Spotify API error: " + response.code() + " " + response.message());
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    Log.e(TAG, "Empty response body");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody.string());
                    JSONArray items = json.getJSONArray("items");

                    List<String> fetchedTitles = new ArrayList<>();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject trackObj = items.getJSONObject(i).getJSONObject("track");
                        String name = trackObj.getString("name");
                        String artist = trackObj.getJSONArray("artists").getJSONObject(0).getString("name");
                        fetchedTitles.add(name + " – " + artist);
                    }

                    Collections.shuffle(fetchedTitles);

                    List<String> randomTracks = fetchedTitles.size() > 7
                            ? fetchedTitles.subList(0, 7)
                            : fetchedTitles;

                    main.post(() -> {
                        titles.clear();
                        titles.addAll(randomTracks);
                        songAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Randomized " + titles.size() + " Spotify tracks (offset=" + randomOffset + ")");
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                }
            }
        });
    }


    WorkoutBuilderListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutBuilderListener) {
            listener = (WorkoutBuilderListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginListener");
        }
    }

    public interface WorkoutBuilderListener {
        void back();
        void sendToWorkoutCompleted();
    }
}