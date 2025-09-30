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
    FragmentWorkoutBuilderBinding binding;
    private ArrayAdapter<String> adapter;
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
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, titles);
        binding.listViewSongsList.setAdapter(adapter);
        binding.textViewBuilderIntensity.setText(workout_intensity + " Intensity Workouts");

        binding.buttonBackLogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.back();
            }
        });
        startFetch();

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
        adapter.notifyDataSetChanged();

        // Example: fetch only tracks with tempo between 100–140 BPM
        fetchSavedTracksPaged(token, 0, 5, 100f, 140f);
    }

    private void fetchSavedTracksPaged(String token, int offset, int limit, float minTempo, float maxTempo) {
        HttpUrl url = HttpUrl.parse(ME_TRACKS_URL).newBuilder()
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf(offset))
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
                    String err = "";
                    try {
                        err = response.peekBody(1024 * 1024).string();
                    } catch (Exception ignored) {}
                    final String finalErr = err;
                    main.post(() -> {
                        Toast.makeText(getContext(), "Spotify API error: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "API error " + response.code() + " body=" + finalErr);
                    });
                    return;
                }

                String json;
                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        main.post(() -> Toast.makeText(getContext(), "Empty response body", Toast.LENGTH_LONG).show());
                        return;
                    }
                    json = body.string();
                }

                try {
                    JSONObject root = new JSONObject(json);
                    JSONArray items = root.optJSONArray("items");

                    final List<String> trackIds = new ArrayList<>();
                    final List<String> trackNames = new ArrayList<>();

                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject savedItem = items.getJSONObject(i);
                            JSONObject track = savedItem.optJSONObject("track");
                            if (track != null) {
                                String id = track.optString("id", null);
                                String name = track.optString("name", "<unknown>");
                                if (id != null) {
                                    trackIds.add(id);
                                    trackNames.add(name);
                                }
                            }
                        }
                    }

                    // Call /audio-features to get tempos
                    if (!trackIds.isEmpty()) {
                        HttpUrl featsUrl = HttpUrl.parse("https://api.spotify.com/v1/audio-features")
                                .newBuilder()
                                .addQueryParameter("ids", String.join(",", trackIds))
                                .build();

                        Request featsReq = new Request.Builder()
                                .url(featsUrl)
                                .addHeader("Authorization", "Bearer " + token)
                                .build();

                        http.newCall(featsReq).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e(TAG, "Audio features request failed", e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                                if (!resp.isSuccessful()) {
                                    Log.e(TAG, "Audio features error " + resp.code());
                                    return;
                                }

                                String featsJson = resp.body().string();
                                try {
                                    JSONObject featsRoot = new JSONObject(featsJson);
                                    JSONArray feats = featsRoot.optJSONArray("audio_features");

                                    final List<String> filtered = new ArrayList<>();
                                    if (feats != null) {
                                        for (int i = 0; i < feats.length(); i++) {
                                            JSONObject f = feats.optJSONObject(i);
                                            if (f != null) {
                                                float tempo = (float) f.optDouble("tempo", -1);
                                                String id = f.optString("id");
                                                int idx = trackIds.indexOf(id);
                                                if (tempo >= minTempo && tempo <= maxTempo && idx >= 0) {
                                                    filtered.add(trackNames.get(idx));
                                                    Log.d(TAG, trackNames.get(idx) + " tempo=" + tempo);
                                                }
                                            }
                                        }
                                    }

                                    main.post(() -> {
                                        titles.addAll(filtered);
                                        adapter.notifyDataSetChanged();

                                        final String next = root.optString("next", null);
                                        final boolean hasNext = next != null && !"null".equals(next);
                                        if (hasNext) {
                                            fetchSavedTracksPaged(token, offset + limit, limit, minTempo, maxTempo);
                                        } else {
                                            Toast.makeText(getContext(),
                                                    "Fetched " + titles.size() + " tempo-filtered songs",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } catch (JSONException e) {
                                    Log.e(TAG, "Parse audio features error", e);
                                }
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    main.post(() -> Toast.makeText(getContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show());
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