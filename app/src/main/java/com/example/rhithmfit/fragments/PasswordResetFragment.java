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
import android.widget.Toast;

import com.example.rhithmfit.R;
import com.example.rhithmfit.databinding.FragmentPasswordResetBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetFragment extends Fragment {
    private FirebaseAuth firebase_auth;
    FragmentPasswordResetBinding binding;

    public PasswordResetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordResetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase_auth = FirebaseAuth.getInstance();

        // back button
        binding.buttonBackPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.back();
            }
        });

        binding.buttonSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editTextPasswordResetEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter all required information.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("EMAIL", email);
                    firebase_auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                listener.back();
                                Toast.makeText(getActivity(), "Password reset email sent. Check spam.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT);
                            }
                        }
                    });
                }
            }
        });
    }

    PasswordResetListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PasswordResetListener) {
            listener = (PasswordResetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PasswordResetListener");
        }
    }

    public interface PasswordResetListener {
        void back();
    }
}