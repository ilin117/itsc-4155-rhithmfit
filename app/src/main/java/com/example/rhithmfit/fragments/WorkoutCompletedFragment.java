package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhithmfit.R;
import com.example.rhithmfit.classes.Song;
import com.example.rhithmfit.databinding.FragmentLoginBinding;
import com.example.rhithmfit.databinding.FragmentWorkoutCompletedBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutCompletedFragment extends Fragment {

    FragmentWorkoutCompletedBinding binding;
    private final List<Song> titles = new ArrayList<>();
    private final List<String> workouts = new ArrayList<>();
    private String workout_intensity;
    FirebaseFirestore db;

    public WorkoutCompletedFragment() {
        // Required empty public constructor
    }

    public static WorkoutCompletedFragment newInstance(List<Song> param1, List<String> param2, String param3) {
        WorkoutCompletedFragment fragment = new WorkoutCompletedFragment();
        Bundle args = new Bundle();
        args.putSerializable("titles", (Serializable) param1);
        args.putStringArrayList("workouts", (ArrayList<String>) param2);
        args.putString("intensity", param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            List<Song> receivedTitles = (List<Song>) getArguments().getSerializable("titles");
            if (receivedTitles != null) {
                titles.addAll(receivedTitles);
            }
            workout_intensity = getArguments().getString("intensity");
            workouts.addAll(getArguments().getStringArrayList("workouts"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWorkoutCompletedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (Song song: titles) {
            Log.d("PASSED_DATA", song.getId());
        }
        Log.d("PASSED_DATA", workouts.toString());

        binding.buttonWorkoutCompleteNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendToHome();
            }
        });

        binding.buttonWorkoutCompleteYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                String intensity = workout_intensity;
                String name = intensity + " " + date;

                Bundle result = new Bundle();
                result.putString("name", name);

                saveWorkoutSession(name);
                listener.sendToHome();
            }
        });
    }

    private void saveWorkoutSession(String name) {
        db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> songMaps = new ArrayList<>();
        for (Song s : titles) {
            Map<String, Object> songMap = new HashMap<>();
            songMap.put("name", s.getTitle());
            songMap.put("artist", s.getArtist());
            songMap.put("id", s.getId());
            songMaps.add(songMap);
        }

        Map<String, Object> workoutSession = new HashMap<>();
        workoutSession.put("name", name);
        workoutSession.put("workouts", workouts);
        workoutSession.put("songs", songMaps);

        db.collection("workout_sessions")
                .add(workoutSession)
                .addOnSuccessListener(docRef -> {
                    Log.d("Firestore", "Workout session saved with ID: " + docRef.getId());
                    listener.sendToHome();
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error saving workout session", e));
    }

    WorkoutCompletedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutCompletedListener) {
            listener = (WorkoutCompletedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginListener");
        }
    }

    public interface WorkoutCompletedListener {
        void sendToHome();
    }
}