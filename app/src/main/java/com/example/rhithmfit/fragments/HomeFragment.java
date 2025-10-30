package com.example.rhithmfit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rhithmfit.R;
import com.example.rhithmfit.classes.Workout;
import com.example.rhithmfit.databinding.FragmentHomeBinding;
import com.example.rhithmfit.databinding.ListItemBinding;
import com.example.rhithmfit.viewModels.SpotifyViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String THEME_PREFS = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    List<Workout> savedWorkouts = new ArrayList<>();
    WorkoutListAdapter workoutListAdapter;
    String current_user;
    FragmentHomeBinding binding;
    FirebaseAuth firebase_auth;
    FirebaseFirestore db;

    private String accessToken;
    SpotifyViewModel spotifyViewModel;

    public HomeFragment() { }

    public static HomeFragment newInstance(String workout_intensity) {
        Bundle args = new Bundle();
        args.putString("Intensity", workout_intensity);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);

        // Apply saved theme ASAP (before views inflate if possible)
        boolean isDark = requireContext()
                .getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebase_auth = FirebaseAuth.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                : "User";
        Log.d("Current User", String.valueOf(current_user));
        binding.textViewUserName.setText("Hello " + current_user + "!");

        workoutListAdapter = new WorkoutListAdapter(savedWorkouts);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(workoutListAdapter);

        loadWorkoutSessions();

        binding.buttonLogout.setOnClickListener(v -> {
            firebase_auth.signOut();
            if (listener != null) listener.logout();
        });

        binding.buttonHomeStartNewWorkout.setOnClickListener(v -> {
            if (listener != null) listener.goToWorkoutCreation();
        });

        // ---- Theme toggle ----
        SharedPreferences themePrefs = requireContext().getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean(KEY_DARK_MODE, false);
        binding.switchTheme.setChecked(isDark); // reflect saved state

        binding.switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                themePrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        });

        binding.buttonReminderSettings.setOnClickListener(v -> {
            if (listener != null) listener.openReminderSettings();
        });

        ShowWorkoutReminderPopup();
    }

    private void ShowWorkoutReminderPopup() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(ReminderSettingsFragment.PREFS_NAME, Context.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(ReminderSettingsFragment.KEY_ENABLED, false);
        int targetHour = prefs.getInt(ReminderSettingsFragment.KEY_HOUR, 8);
        int targetMinute = prefs.getInt(ReminderSettingsFragment.KEY_MINUTE, 0);
        int lastShownDay = prefs.getInt(ReminderSettingsFragment.KEY_LAST_SHOWN_DAY, -1);

        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_YEAR);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        boolean pastTargetTime = (currentHour > targetHour) ||
                (currentHour == targetHour && currentMinute >= targetMinute);

        Log.d("RHYTHM_DEBUG",
                "ShowWorkoutReminderPopup() enabled=" + enabled +
                        " pastTargetTime=" + pastTargetTime +
                        " lastShownDay=" + lastShownDay +
                        " currentDay=" + currentDay);

        if (enabled && pastTargetTime && lastShownDay != currentDay) {

            new AlertDialog.Builder(requireContext())
                    .setTitle("RhithmFit Reminder")
                    .setMessage("Time to work out 💪")
                    .setPositiveButton("Let's go", (dialog, which) -> {
                        if (listener != null) listener.goToWorkoutCreation();
                    })
                    .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                    .show();

            prefs.edit()
                    .putInt(ReminderSettingsFragment.KEY_LAST_SHOWN_DAY, currentDay)
                    .apply();
        }
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
                        for (DocumentSnapshot doc : value.getDocuments()) {
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
        private final List<Workout> workouts;

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
                binding.buttonStartWorkout.setOnClickListener(v -> {
                    if (listener != null) {
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
        void openReminderSettings();
    }
}
