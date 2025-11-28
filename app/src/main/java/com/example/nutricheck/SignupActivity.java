package com.example.nutricheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private static final String PREFS_NAME = "NutriCheckPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_HAS_PREFERENCES = "hasPreferences";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private View googleSignupBtn;
    private SharedPreferences prefs;

    // ActivityResultLauncher for Google Sign In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserPreferencesAndNavigate(currentUser.getUid());
            return;
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }
        );

        // Setup Google Sign In Button
        googleSignupBtn = findViewById(R.id.googleSignupBtn);
        googleSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Save basic login status to SharedPreferences
                            saveLoginStatus(user);

                            // Check if this is a new user
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                            if (isNewUser) {
                                // Create user document in Firestore
                                createUserDocument(user);
                            } else {
                                // Check if user has completed preferences
                                checkUserPreferencesAndNavigate(user.getUid());
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveLoginStatus(FirebaseUser user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUid());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getDisplayName());
        editor.apply();
        Log.d(TAG, "Login status saved to SharedPreferences");
    }

    private void savePreferencesStatus(boolean hasPreferences) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_HAS_PREFERENCES, hasPreferences);
        editor.apply();
        Log.d(TAG, "Preferences status saved: " + hasPreferences);
    }

    private void createUserDocument(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("hasCompletedPreferences", false);

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created successfully");
                    // Save to SharedPreferences
                    savePreferencesStatus(false);
                    // Navigate to preferences for new user
                    navigateToPreferences();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating user document", e);
                    Toast.makeText(SignupActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserPreferencesAndNavigate(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean hasCompletedPreferences = document.getBoolean("hasCompletedPreferences");

                                // Save to SharedPreferences for faster future checks
                                boolean hasPref = hasCompletedPreferences != null && hasCompletedPreferences;
                                savePreferencesStatus(hasPref);

                                if (hasPref) {
                                    // User has completed preferences, go to home
                                    navigateToHome();
                                } else {
                                    // User needs to set preferences
                                    navigateToPreferences();
                                }
                            } else {
                                // Document doesn't exist, create it
                                createUserDocument(mAuth.getCurrentUser());
                            }
                        } else {
                            Log.w(TAG, "Error checking user preferences", task.getException());
                            Toast.makeText(SignupActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToPreferences() {
        Intent intent = new Intent(SignupActivity.this, PreferencesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}