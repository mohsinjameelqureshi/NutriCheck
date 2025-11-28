package com.example.nutricheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";

    private static final String PREFS_NAME = "NutriCheckPrefs";
    private static final String KEY_HAS_PREFERENCES = "hasPreferences";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    // UI Elements
    RadioGroup radioDiet;

    // Allergies (Based on allergens_tags from API)
    CheckBox chkPeanuts, chkTreeNuts, chkMilk, chkGluten, chkEggs, chkSoy, chkFish, chkShellfish, chkSesame;

    // Nutrient Limits (Based on nutriments from API)
    SeekBar seekSugar, seekSalt, seekSatFat, seekCalories;
    TextView tvSugarValue, tvSaltValue, tvSatFatValue, tvCaloriesValue;

    // Additives (Based on additives_tags from API)
    CheckBox chkE150d, chkE290, chkE338, chkPalmOil, chkAvoidAllAdditives;

    // Quality Preferences (Based on nova_group and nutriscore_grade from API)
    CheckBox chkAvoidNOVA4;
    Spinner spinnerNutriScore;

    Button btnSavePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_activity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to signup
            startActivity(new Intent(this, SignupActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupSeekBars();
        setupSaveButton();
    }

    private void initializeViews() {
        // Diet Type
        radioDiet = findViewById(R.id.radioDiet);

        // Allergies
        chkPeanuts = findViewById(R.id.chkPeanuts);
        chkTreeNuts = findViewById(R.id.chkTreeNuts);
        chkMilk = findViewById(R.id.chkMilk);
        chkGluten = findViewById(R.id.chkGluten);
        chkEggs = findViewById(R.id.chkEggs);
        chkSoy = findViewById(R.id.chkSoy);
        chkFish = findViewById(R.id.chkFish);
        chkShellfish = findViewById(R.id.chkShellfish);
        chkSesame = findViewById(R.id.chkSesame);

        // Nutrient Seekbars
        seekSugar = findViewById(R.id.seekSugar);
        seekSalt = findViewById(R.id.seekSalt);
        seekSatFat = findViewById(R.id.seekSatFat);
        seekCalories = findViewById(R.id.seekCalories);

        // Nutrient Value TextViews
        tvSugarValue = findViewById(R.id.tvSugarValue);
        tvSaltValue = findViewById(R.id.tvSaltValue);
        tvSatFatValue = findViewById(R.id.tvSatFatValue);
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue);

        // Additives
        chkE150d = findViewById(R.id.chkE150d);
        chkE290 = findViewById(R.id.chkE290);
        chkE338 = findViewById(R.id.chkE338);
        chkPalmOil = findViewById(R.id.chkPalmOil);
        chkAvoidAllAdditives = findViewById(R.id.chkAvoidAllAdditives);

        // Quality Preferences
        chkAvoidNOVA4 = findViewById(R.id.chkAvoidNOVA4);
        spinnerNutriScore = findViewById(R.id.spinnerNutriScore);

        // Save Button
        btnSavePreferences = findViewById(R.id.btnSavePreferences);
    }

    private void setupSeekBars() {
        // Sugar SeekBar (0-50g)
        seekSugar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSugarValue.setText(progress + "g");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Salt SeekBar (0-5g with 0.1g precision)
        seekSalt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float saltValue = progress / 10.0f;
                tvSaltValue.setText(String.format("%.1fg", saltValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Saturated Fat SeekBar (0-40g)
        seekSatFat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSatFatValue.setText(progress + "g");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Calories SeekBar (0-1000 kcal)
        seekCalories.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int calories = (progress / 10) * 10;
                tvCaloriesValue.setText(calories + " kcal");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSaveButton() {
        btnSavePreferences.setOnClickListener(v -> {
            if (validatePreferences()) {
                savePreferencesToFirebase();
            }
        });
    }

    private boolean validatePreferences() {
        // Check if at least one preference is selected
        int selectedDietId = radioDiet.getCheckedRadioButtonId();
        boolean hasAllergies = chkPeanuts.isChecked() || chkTreeNuts.isChecked() ||
                chkMilk.isChecked() || chkGluten.isChecked() ||
                chkEggs.isChecked() || chkSoy.isChecked() ||
                chkFish.isChecked() || chkShellfish.isChecked() ||
                chkSesame.isChecked();

        if (selectedDietId == -1 && !hasAllergies) {
            Toast.makeText(this, "Please select at least one preference", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Remove all emojis and special characters, keep only text
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "none";
        }

        // Remove emojis and special unicode characters
        String cleaned = text.replaceAll("[^\\p{L}\\p{N}\\s-]", "");

        // Trim whitespace and convert to lowercase
        cleaned = cleaned.trim().toLowerCase();

        // Replace multiple spaces with single space
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.isEmpty() ? "none" : cleaned;
    }

    private void savePreferencesToFirebase() {
        // Show loading state
        btnSavePreferences.setEnabled(false);
        btnSavePreferences.setText("Saving...");

        // Collect all preference data mapped to API structure
        Map<String, Object> preferences = new HashMap<>();

        // 1. Diet Type (for ingredients_tags checking) - CLEAN TEXT ONLY
        int selectedId = radioDiet.getCheckedRadioButtonId();
        String dietType = "none";
        if (selectedId != -1) {
            RadioButton selected = findViewById(selectedId);
            String rawText = selected.getText().toString();
            dietType = cleanText(rawText); // Remove emojis
        }
        preferences.put("dietType", dietType);

        // 2. Allergies (Maps to API allergens_tags)
        ArrayList<String> allergies = new ArrayList<>();
        if (chkPeanuts.isChecked()) allergies.add("en:peanuts");
        if (chkTreeNuts.isChecked()) allergies.add("en:nuts");
        if (chkMilk.isChecked()) allergies.add("en:milk");
        if (chkGluten.isChecked()) allergies.add("en:gluten");
        if (chkEggs.isChecked()) allergies.add("en:eggs");
        if (chkSoy.isChecked()) allergies.add("en:soybeans");
        if (chkFish.isChecked()) allergies.add("en:fish");
        if (chkShellfish.isChecked()) allergies.add("en:crustaceans");
        if (chkSesame.isChecked()) allergies.add("en:sesame-seeds");
        preferences.put("allergies", allergies);

        // 3. Nutrient Limits (Maps to API nutriments values)
        Map<String, Float> nutrientLimits = new HashMap<>();
        nutrientLimits.put("sugars_100g", (float) seekSugar.getProgress());
        nutrientLimits.put("salt_100g", seekSalt.getProgress() / 10.0f);
        nutrientLimits.put("saturated_fat_100g", (float) seekSatFat.getProgress());
        nutrientLimits.put("energy_kcal_100g", (float) ((seekCalories.getProgress() / 10) * 10));
        preferences.put("nutrientLimits", nutrientLimits);

        // 4. Additives to Avoid (Maps to API additives_tags)
        ArrayList<String> avoidAdditives = new ArrayList<>();
        if (chkE150d.isChecked()) avoidAdditives.add("en:e150d");
        if (chkE290.isChecked()) avoidAdditives.add("en:e290");
        if (chkE338.isChecked()) avoidAdditives.add("en:e338");
        if (chkPalmOil.isChecked()) avoidAdditives.add("en:palm-oil");
        preferences.put("avoidAdditives", avoidAdditives);
        preferences.put("avoidAllAdditives", chkAvoidAllAdditives.isChecked());

        // 5. Quality Preferences (Maps to API nova_group and nutriscore_grade) - CLEAN TEXT
        preferences.put("avoidNOVA4", chkAvoidNOVA4.isChecked());
        String minNutriScore = cleanText(spinnerNutriScore.getSelectedItem().toString());
        preferences.put("minNutriScore", minNutriScore);

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            btnSavePreferences.setEnabled(true);
            btnSavePreferences.setText("Save Preferences");
            return;
        }

        String userId = currentUser.getUid();

        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("preferences", preferences);
        updates.put("hasCompletedPreferences", true);
        updates.put("preferencesUpdatedAt", System.currentTimeMillis());

        // Log the clean data
        Log.d(TAG, "Saving preferences (NO EMOJIS): " + preferences);

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Preferences saved successfully");

                    // Save to SharedPreferences for instant access
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_HAS_PREFERENCES, true);
                    editor.apply();

                    Toast.makeText(PreferencesActivity.this,
                            "Preferences saved successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to home
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving preferences", e);

                    // If update fails, try setting the data (in case document doesn't exist)
                    db.collection("users")
                            .document(userId)
                            .set(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Preferences created successfully");

                                // Save to SharedPreferences
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(KEY_HAS_PREFERENCES, true);
                                editor.apply();

                                Toast.makeText(PreferencesActivity.this,
                                        "Preferences saved successfully!", Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error creating preferences", e2);
                                Toast.makeText(PreferencesActivity.this,
                                        "Failed to save preferences. Please try again.",
                                        Toast.LENGTH_SHORT).show();

                                // Reset button state
                                btnSavePreferences.setEnabled(true);
                                btnSavePreferences.setText("Save Preferences");
                            });
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(PreferencesActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}