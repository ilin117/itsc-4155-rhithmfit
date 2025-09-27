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

import com.example.rhithmfit.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    FragmentLoginBinding binding;
    private FirebaseAuth firebase_auth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase_auth = FirebaseAuth.getInstance();

        // forget password
        binding.textViewPasswordResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.goToPasswordReset();
            }
        });

        // back button
        binding.buttonBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.back();
            }
        });

        // register button functionality
        binding.textViewRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.goToRegister();
            }
        });

        // login button functionality
        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter all required information.", Toast.LENGTH_SHORT).show();
            }
            else {
                firebase_auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            listener.onLoginSuccessful();
                        }
                        else {
                            Toast.makeText(getActivity(), "Incorrect email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    LoginListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LoginListener) {
            listener = (LoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginListener");
        }
    }

    public interface LoginListener {
        void goToRegister();
        void onLoginSuccessful();
        void back();
        void goToPasswordReset();
    }
}