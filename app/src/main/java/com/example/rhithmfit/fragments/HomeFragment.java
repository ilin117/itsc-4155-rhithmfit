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
import com.example.rhithmfit.databinding.FragmentHomeBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    String current_user;
    FragmentHomeBinding binding;
    FirebaseAuth firebase_auth;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
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
        binding.textViewUserName.setText(current_user);

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase_auth.signOut();
                listener.logout();
            }
        });
    }

    HomeListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeListener) {
            listener = (HomeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeListener");
        }
    }

    public interface HomeListener {
        void logout();
    }
}