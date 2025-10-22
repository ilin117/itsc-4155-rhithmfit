package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rhithmfit.R;
import com.example.rhithmfit.databinding.FragmentLoginBinding;
import com.example.rhithmfit.databinding.FragmentWorkoutCompletedBinding;

public class WorkoutCompletedFragment extends Fragment {

    FragmentWorkoutCompletedBinding binding;

    public WorkoutCompletedFragment() {
        // Required empty public constructor
    }

    public static WorkoutCompletedFragment newInstance(String param1, String param2) {
        WorkoutCompletedFragment fragment = new WorkoutCompletedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                String intensity = "";

                Bundle result = new Bundle();
                result.putString("intensity", intensity);
                result.putString("date", date);

                getParentFragmentManager().setFragmentResult("savedWorkout", result);
                listener.sendToHome();
            }
        });
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