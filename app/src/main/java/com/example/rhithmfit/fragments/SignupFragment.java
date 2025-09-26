package com.example.rhithmfit.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rhithmfit.R;
import com.example.rhithmfit.databinding.FragmentSignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupFragment extends Fragment {

    FragmentSignupBinding binding;
    FirebaseAuth firebase_auth;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase_auth = FirebaseAuth.getInstance();

        // back button
        binding.buttonBackSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.back();
            }
        });
        
        // login button
        binding.textViewRegisterLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.goToLogin();
            }
        });

        // register button
        binding.buttonRegisterSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = binding.editTextRegisterUsername.getText().toString().trim();
                String email = binding.editTextRegisterEmail.getText().toString().trim();
                String password = binding.editTextRegisterPassword.getText().toString().trim();
                String confirm_password = binding.editTextRegisterConfirmPassword.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please be sure that all fields are entered.", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(confirm_password)) {
                    Toast.makeText(getActivity(), "Passwords doesn't match", Toast.LENGTH_SHORT).show();
                }
                else {

                    firebase_auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebase_auth.getCurrentUser();

                                if (user != null) {
                                    UserProfileChangeRequest profile_updates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();

                                    user.updateProfile((profile_updates))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        listener.onRegisterSuccess();
                                                    }
                                                }
                                            });
                                }
                            }
                            else {
                                Toast.makeText(getActivity(), "Profile creation failed. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    SignupListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SignupListener) {
            listener = (SignupListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SignupListener");
        }
    }

    public interface SignupListener {
        void onRegisterSuccess();
        void back();
        void goToLogin();
    }
}