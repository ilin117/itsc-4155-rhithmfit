// team 1: Issac, Brittany Avalos-Ortiz, Raj Dalsaniya

package com.example.rhithmfit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rhithmfit.fragments.HomeFragment;
import com.example.rhithmfit.fragments.LandingFragment;
import com.example.rhithmfit.fragments.LoginFragment;
import com.example.rhithmfit.fragments.PasswordResetFragment;
import com.example.rhithmfit.fragments.SignupFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements LandingFragment.LandingListener, LoginFragment.LoginListener, PasswordResetFragment.PasswordResetListener, HomeFragment.HomeListener, SignupFragment.SignupListener {

    FirebaseAuth firebase_auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // remembers current user
        firebase_auth = FirebaseAuth.getInstance();

        if (firebase_auth.getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new LandingFragment()).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new HomeFragment()).commit();
        }
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoSignup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginSuccessful() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRegisterSuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void goToPasswordReset() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new PasswordResetFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new LandingFragment())
                .commit();
    }
}