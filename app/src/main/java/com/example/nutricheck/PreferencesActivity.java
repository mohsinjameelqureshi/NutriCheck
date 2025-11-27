package com.example.nutricheck;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";

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
                savePreferences();

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

    private void savePreferences() {
        // Collect all preference data mapped to API structure
        Map<String, Object> preferences = new HashMap<>();

        // 1. Diet Type (for ingredients_tags checking)
        int selectedId = radioDiet.getCheckedRadioButtonId();
        String dietType = "None";
        if (selectedId != -1) {
            RadioButton selected = findViewById(selectedId);
            dietType = selected.getText().toString();
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
        nutrientLimits.put("saturated-fat_100g", (float) seekSatFat.getProgress());
        nutrientLimits.put("energy-kcal_100g", (float) ((seekCalories.getProgress() / 10) * 10));
        preferences.put("nutrientLimits", nutrientLimits);

        // 4. Additives to Avoid (Maps to API additives_tags)
        ArrayList<String> avoidAdditives = new ArrayList<>();
        if (chkE150d.isChecked()) avoidAdditives.add("en:e150d");
        if (chkE290.isChecked()) avoidAdditives.add("en:e290");
        if (chkE338.isChecked()) avoidAdditives.add("en:e338");
        if (chkPalmOil.isChecked()) avoidAdditives.add("en:palm-oil");
        preferences.put("avoidAdditives", avoidAdditives);
        preferences.put("avoidAllAdditives", chkAvoidAllAdditives.isChecked());

        // 5. Quality Preferences (Maps to API nova_group and nutriscore_grade)
        preferences.put("avoidNOVA4", chkAvoidNOVA4.isChecked());
        preferences.put("minNutriScore", spinnerNutriScore.getSelectedItem().toString());

        // Display summary
        showPreferencesSummary(preferences);
        Log.d(TAG, "savePreferences: " + preferences);

        // TODO: Save to SharedPreferences
        // SharedPreferences prefs = getSharedPreferences("NutriCheckPrefs", MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // Gson gson = new Gson();
        // String json = gson.toJson(preferences);
        // editor.putString("user_preferences", json);
        // editor.apply();


//        FirebaseFirestore.getInstance()
//                .collection("preferences")
//                .add(preferences)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("FB", "Preferences saved with ID: " + documentReference.getId());
//                    Toast.makeText(this, "Saved to Firebase!", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("FB", "Error saving preferences", e);
//                    Toast.makeText(this, "Error saving to Firebase!", Toast.LENGTH_SHORT).show();
//                });


        // TODO: Save to Firebase Firestore
        // FirebaseFirestore db = FirebaseFirestore.getInstance();
        // String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // db.collection("users").document(userId)
        //   .update("preferences", preferences)
        //   .addOnSuccessListener(aVoid -> {
        //       Toast.makeText(this, "Preferences saved successfully!", Toast.LENGTH_SHORT).show();
        //       navigateToHome();
        //   })
        //   .addOnFailureListener(e -> {
        //       Toast.makeText(this, "Failed to save preferences", Toast.LENGTH_SHORT).show();
        //   });

        // For now, navigate to home after 2 seconds
        btnSavePreferences.postDelayed(this::navigateToHome, 2000);
    }

    private void showPreferencesSummary(Map<String, Object> preferences) {
        StringBuilder summary = new StringBuilder("Preferences Saved Successfully!\n\n");

        summary.append("Diet: ").append(preferences.get("dietType")).append("\n");

        ArrayList<String> allergies = (ArrayList<String>) preferences.get("allergies");
        if (!allergies.isEmpty()) {
            summary.append("Allergies: ").append(allergies.size()).append(" selected\n");
        }

        Map<String, Float> limits = (Map<String, Float>) preferences.get("nutrientLimits");
        summary.append(String.format("Sugar limit: %.0fg\n", limits.get("sugars_100g")));
        summary.append(String.format("Salt limit: %.1fg\n", limits.get("salt_100g")));

        ArrayList<String> additives = (ArrayList<String>) preferences.get("avoidAdditives");
        if (!additives.isEmpty()) {
            summary.append("Avoiding additives: ").append(additives.size()).append(" types\n");
        }

        Toast.makeText(this, summary.toString(), Toast.LENGTH_LONG).show();
    }

    private void navigateToHome() {
        // TODO: Replace with actual HomeActivity
        // Intent intent = new Intent(PreferencesActivity.this, HomeActivity.class);
        // startActivity(intent);
        // finish();

        Toast.makeText(this, "Navigating to Home Screen...", Toast.LENGTH_SHORT).show();
    }
}