package com.example.nutricheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 3000; // 3 seconds - increased for Firebase check
    private static final int MIN_SPLASH_DURATION = 2000; // Minimum 2 seconds

    private static final String PREFS_NAME = "NutriCheckPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_HAS_PREFERENCES = "hasPreferences";
    private static final String KEY_USER_ID = "userId";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startTime = System.currentTimeMillis();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Quick check from SharedPreferences (instant)
        checkAuthenticationStatus();
    }

    private void checkAuthenticationStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is logged in
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean hasPreferences = prefs.getBoolean(KEY_HAS_PREFERENCES, false);
        String savedUserId = prefs.getString(KEY_USER_ID, "");

        Log.d(TAG, "Cached - LoggedIn: " + isLoggedIn + ", HasPrefs: " + hasPreferences);

        if (currentUser != null && isLoggedIn && currentUser.getUid().equals(savedUserId)) {
            // User is authenticated and we have cached data
            // Verify with Firestore to ensure data is still valid
            Log.d(TAG, "User authenticated with cache, verifying with Firestore...");
            verifyUserInFirestore(currentUser.getUid(), hasPreferences);
        } else if (currentUser != null) {
            // User is authenticated but no cached data
            Log.d(TAG, "User authenticated but no cache, checking Firestore...");
            verifyUserInFirestore(currentUser.getUid(), false);
        } else {
            // No user authenticated
            Log.d(TAG, "No user authenticated");
            navigateToSignup();
        }
    }

    private void verifyUserInFirestore(String userId, boolean cachedHasPreferences) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // User document exists in Firestore
                            Boolean hasCompletedPreferences = document.getBoolean("hasCompletedPreferences");
                            boolean hasPref = hasCompletedPreferences != null && hasCompletedPreferences;

                            Log.d(TAG, "Firestore - User exists, hasPreferences: " + hasPref);

                            // Update SharedPreferences with latest data
                            updateSharedPreferences(userId, hasPref);

                            // Navigate based on preferences status
                            if (hasPref) {
                                navigateToHome();
                            } else {
                                navigateToPreferences();
                            }
                        } else {
                            // User document doesn't exist in Firestore
                            Log.d(TAG, "User document doesn't exist in Firestore");

                            // Clear invalid cache
                            clearSharedPreferences();

                            // Navigate to signup to create user document
                            navigateToSignup();
                        }
                    } else {
                        // Error checking Firestore
                        Log.e(TAG, "Error checking Firestore", task.getException());

                        // Fallback to cached data if available
                        if (cachedHasPreferences) {
                            Log.d(TAG, "Firestore error, using cached data");
                            navigateToHome();
                        } else {
                            navigateToSignup();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to verify user in Firestore", e);

                    // Fallback to cached data if available
                    if (cachedHasPreferences) {
                        Log.d(TAG, "Firestore failure, using cached data");
                        navigateToHome();
                    } else {
                        navigateToSignup();
                    }
                });
    }

    private void updateSharedPreferences(String userId, boolean hasPreferences) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_HAS_PREFERENCES, hasPreferences);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
        Log.d(TAG, "SharedPreferences updated");
    }

    private void clearSharedPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "SharedPreferences cleared");
    }

    /**
     * Calculate remaining time to show splash screen
     * Ensures minimum splash duration is met
     */
    private long getRemainingDelay() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = MIN_SPLASH_DURATION - elapsedTime;
        return Math.max(remainingTime, 0);
    }

    private void navigateToHome() {
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to Home");
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, getRemainingDelay());
    }

    private void navigateToPreferences() {
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to Preferences");
            Intent intent = new Intent(SplashActivity.this, PreferencesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, getRemainingDelay());
    }

    private void navigateToSignup() {
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to Signup");
            Intent intent = new Intent(SplashActivity.this, SignupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, getRemainingDelay());
    }
}