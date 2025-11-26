package com.example.nutricheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.googleSignupBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to Google login screen
                startActivity(new Intent(SignupActivity.this, PreferencesActivity.class));
            }
        });
    }
}
