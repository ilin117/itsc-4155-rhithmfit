// team 1: Issac, Brittany Avalos-Ortiz, Raj Dalsaniya

package com.example.rhithmfit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rhithmfit.fragments.HomepageFragment;
import com.example.rhithmfit.fragments.LoginFragment;
import com.example.rhithmfit.fragments.SignupFragment;

public class MainActivity extends AppCompatActivity implements HomepageFragment.HomepageListener {

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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new HomepageFragment()).commit();
    }

    @Override
    public void gotoLogin() {
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
}