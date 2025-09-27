package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhithmfit.databinding.FragmentWorkoutCreationBinding;

public class WorkoutCreationFragment extends Fragment {
    FragmentWorkoutCreationBinding binding;

    public WorkoutCreationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkoutCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCancelWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.back();
            }
        });

        binding.buttonLowIntensity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendToHome("Low");
            }
        });

        binding.buttonMidIntensity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendToHome("Mid");
            }
        });

        binding.buttonHighIntensity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendToHome("High");
            }
        });
    }

    WorkoutCreationListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutCreationListener) {
            listener = (WorkoutCreationListener) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    public interface WorkoutCreationListener {
        void back();
        void sendToHome(String intensity);
    }
}