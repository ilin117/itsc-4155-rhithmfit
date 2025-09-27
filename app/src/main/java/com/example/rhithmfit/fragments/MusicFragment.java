package com.example.rhithmfit.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rhithmfit.R;
import com.example.rhithmfit.viewModels.SpotifyViewModel;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MusicFragment extends Fragment {
    private static final String TAG = "MusicFragment";
    private static final String ME_TRACKS_URL = "https://api.spotify.com/v1/me/tracks";

    private SpotifyViewModel spotifyViewModel;
    private ProgressBar progressBar;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private final List<String> titles = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();
    private final Handler main = new Handler(Looper.getMainLooper());

    public MusicFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        progressBar = v.findViewById(R.id.progress);
        listView = v.findViewById(R.id.listSongs);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        v.findViewById(R.id.buttonFetch).setOnClickListener(view -> startFetch());
        return v;
    }

    private void startFetch() {
        String token = spotifyViewModel.getAccessToken().getValue();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "No Spotify access token. Connect again.", Toast.LENGTH_SHORT).show();
            return;
        }
        titles.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        fetchSavedTracksPaged(token, 0, 50);
    }


    private void fetchSavedTracksPaged(String token, int offset, int limit) {
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
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "HTTP failure", e);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String err = "";
                    try {
                        err = response.peekBody(1024 * 1024).string(); // up to 1MB
                    } catch (Exception ignored) {}
                    final String finalErr = err;
                    main.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Spotify API error: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "API error " + response.code() + " body=" + finalErr);
                    });
                    return;
                }

                // Success: read once, auto-close via try-with-resources
                String json;
                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        main.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Empty response body", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Empty response body");
                        });
                        return;
                    }
                    json = body.string(); // read EXACTLY once
                }

                try {
                    JSONObject root = new JSONObject(json);
                    JSONArray items = root.optJSONArray("items");

                    final List<String> newTitles = new ArrayList<>();
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject savedItem = items.getJSONObject(i);
                            JSONObject track = savedItem.optJSONObject("track");
                            String name = (track != null) ? track.optString("name", "<unknown>") : "<unknown>";
                            newTitles.add(name);
                            Log.d(TAG, (offset + i + 1) + ". " + name);
                        }
                    }

                    final String next = root.optString("next", null);
                    final boolean hasNext = next != null && !"null".equals(next);

                    main.post(() -> {
                        titles.addAll(newTitles);
                        adapter.notifyDataSetChanged();
                        if (hasNext) {
                            fetchSavedTracksPaged(token, offset + limit, limit);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Fetched " + titles.size() + " titles", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    main.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
