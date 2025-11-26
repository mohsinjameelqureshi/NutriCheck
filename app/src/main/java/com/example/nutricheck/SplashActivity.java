package com.example.nutricheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_DURATION = 2000; // 2 seconds

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, SignupActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}