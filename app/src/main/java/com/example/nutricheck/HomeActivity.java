package com.example.nutricheck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    TextView tvUserName;
    Button btnGoToPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUserName = findViewById(R.id.tvUserName);
        btnGoToPreferences = findViewById(R.id.btnGoToPreferences);

        // Set user name
        tvUserName.setText("Welcome, User!");

        // Navigate to Preferences
        btnGoToPreferences.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PreferencesActivity.class);
            startActivity(intent);
        });
    }
}