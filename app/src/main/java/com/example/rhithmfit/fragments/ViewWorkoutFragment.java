package com.example.rhithmfit.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.rhithmfit.R;
import com.example.rhithmfit.classes.Song;
import com.example.rhithmfit.databinding.FragmentViewWorkoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ViewWorkoutFragment extends Fragment {

    FragmentViewWorkoutBinding binding;
    FirebaseFirestore db;
    private String workoutId;
    private ArrayAdapter<Song> songAdapter;
    private ArrayAdapter<String> workoutAdapter;
    private List<Song> titles = new ArrayList<>();
    private List<String> workouts = new ArrayList<>();
    private String workout_name;

    public ViewWorkoutFragment() {
        // Required empty public constructor
    }

    public static ViewWorkoutFragment newInstance(String param1, String param2) {
        ViewWorkoutFragment fragment = new ViewWorkoutFragment();
        Bundle args = new Bundle();
        args.putString("workout_id", param1);
        args.putString("workout_name", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.workout_name = getArguments().getString("workout_name");
            this.workoutId = getArguments().getString("workout_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewWorkoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editTextSessionName.setText(workout_name);
        songAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, titles);
        workoutAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, workouts);
        binding.listViewViewerSongsList.setAdapter(songAdapter);
        Log.d("WORKOUT_ID", workoutId);
        binding.ListViewViewerExercises.setAdapter(workoutAdapter);
        loadWorkout();
        binding.buttonViewerDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = binding.editTextSessionName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    updateWorkoutName(newName);
                }
                mListener.sendToHome();
            }
        });

        binding.buttonViewerDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkout();
                mListener.sendToHome();
            }
        });
    }

    private void deleteWorkout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Workout");
        builder.setMessage("Are you sure you want to delete this workout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db = FirebaseFirestore.getInstance();
                db.collection("workout_sessions").document(workoutId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getContext(), "Workout deleted successfully", Toast.LENGTH_SHORT).show();
                                songAdapter.notifyDataSetChanged();
                                workoutAdapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void loadWorkout() {
        db = FirebaseFirestore.getInstance();

        db.collection("workout_sessions").document(workoutId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error fetching workout", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        titles.clear();
                        workouts.clear();

                        List<String> workoutList = (List<String>) value.get("workouts");
                        if (workoutList != null) {
                            workouts.addAll(workoutList);
                        }

                        List<Map<String, Object>> songList = (List<Map<String, Object>>) value.get("songs");
                        if (songList != null) {
                            for (Map<String, Object> songMap : songList) {
                                String title = (String) songMap.get("name");
                                String artist = (String) songMap.get("artist");
                                String id = (String) songMap.get("id");
                                titles.add(new Song(title, artist, id));
                            }
                        }
                        Log.d("HELLO_WORLDS", titles.toString());

                        String name = value.getString("name");
                        binding.editTextSessionName.setText(name);

                        songAdapter.notifyDataSetChanged();
                        workoutAdapter.notifyDataSetChanged();

                        Log.d("FIRESTORE", "Loaded " + titles.size() + " songs and " + workouts.size() + " exercises.");
                    }
                });
    }


    private void updateWorkoutName(String newName) {
        db = FirebaseFirestore.getInstance();
        db.collection("workout_sessions").document(workoutId)
                .update("name", newName);
    }

    ViewWorkoutListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ViewWorkoutListener) {
            mListener = (ViewWorkoutListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ViewWorkoutListener");
        }
    }

    public interface ViewWorkoutListener {
        void sendToHome();
    }
}