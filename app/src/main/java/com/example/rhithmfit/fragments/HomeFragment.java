package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.example.rhithmfit.R;
import com.example.rhithmfit.classes.Workout;
import java.util.List;
import java.util.ArrayList;

import com.example.rhithmfit.databinding.ListItemBinding;
import com.example.rhithmfit.viewModels.SpotifyViewModel;
//import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.example.rhithmfit.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    List<Workout> savedWorkouts = new ArrayList<>();
    WorkoutListAdapter workoutListAdapter;
    String current_user;
    FragmentHomeBinding binding;
    FirebaseAuth firebase_auth;
    FirebaseFirestore db;

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

        workoutListAdapter = new WorkoutListAdapter(savedWorkouts);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(workoutListAdapter);

        loadWorkoutSessions();

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

        binding.switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

    private void loadWorkoutSessions() {
        db = FirebaseFirestore.getInstance();
        db.collection("workout_sessions")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE", "Error loading workouts", error);
                        return;
                    }

                    if (value != null) {
                        savedWorkouts.clear();
                        for (var doc : value.getDocuments()) {
                            String date = doc.getString("date");
                            String name = doc.getString("name");
                            String id = doc.getId();
                            Workout workout = new Workout(name, id);
                            savedWorkouts.add(workout);
                        }

                        workoutListAdapter.notifyDataSetChanged();
                        Log.d("FIRESTORE", "Loaded " + savedWorkouts.size() + " workouts");
                    }
                });
    }

    class WorkoutListAdapter extends RecyclerView.Adapter<WorkoutListAdapter.WorkoutViewHolder> {
        private List<Workout> workouts;

        public WorkoutListAdapter(List<Workout> workouts) {
            this.workouts = workouts;
        }

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new WorkoutViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            Workout workout = workouts.get(position);
            holder.bind(workout);
        }

        @Override
        public int getItemCount() {
            return workouts.size();
        }

        class WorkoutViewHolder extends RecyclerView.ViewHolder {
            ListItemBinding binding;

            public WorkoutViewHolder(ListItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(Workout workout) {
                binding.textViewWorkoutName.setText(workout.getDisplayName());
                binding.buttonStartWorkout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.openWorkoutDetails(workout.getId(), workout.getDisplayName());
                    }
                });
            }
        }
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
        void openWorkoutDetails(String workoutId, String name);

    }
}