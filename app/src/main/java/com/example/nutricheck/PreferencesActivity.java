package com.example.nutricheck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    // UI Elements
    RadioGroup radioDiet;

    // Allergies
    CheckBox chkPeanuts, chkTreeNuts, chkMilk, chkGluten, chkEggs, chkSoy, chkFish, chkShellfish, chkSesame;

    // Nutrient Limits
    SeekBar seekSugar, seekSalt, seekSatFat, seekCalories;
    TextView tvSugarValue, tvSaltValue, tvSatFatValue, tvCaloriesValue;

    // Ingredient Blacklist
    CheckBox chkMSG, chkAspartame, chkHighFructose, chkArtificialColors, chkPalmOil;

    // Quality Preferences
    CheckBox chkAvoidAdditives, chkAvoidNOVA4;
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

        // Ingredient Blacklist
        chkMSG = findViewById(R.id.chkMSG);
        chkAspartame = findViewById(R.id.chkAspartame);
        chkHighFructose = findViewById(R.id.chkHighFructose);
        chkArtificialColors = findViewById(R.id.chkArtificialColors);
        chkPalmOil = findViewById(R.id.chkPalmOil);

        // Quality Preferences
        chkAvoidAdditives = findViewById(R.id.chkAvoidAdditives);
        chkAvoidNOVA4 = findViewById(R.id.chkAvoidNOVA4);
        spinnerNutriScore = findViewById(R.id.spinnerNutriScore);

        // Save Button
        btnSavePreferences = findViewById(R.id.btnSavePreferences);
    }

    private void setupSeekBars() {
        // Sugar SeekBar (0-50g, step 1g)
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

        // Salt SeekBar (0-5g, step 0.1g)
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

        // Saturated Fat SeekBar (0-40g, step 1g)
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

        // Calories SeekBar (0-1000 kcal, step 10 kcal)
        seekCalories.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int calories = (progress / 10) * 10; // Round to nearest 10
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
        // Collect all preference data
        Map<String, Object> preferences = new HashMap<>();

        // 1. Diet Type
        int selectedId = radioDiet.getCheckedRadioButtonId();
        String dietType = "None";
        if (selectedId != -1) {
            RadioButton selected = findViewById(selectedId);
            dietType = selected.getText().toString();
        }
        preferences.put("dietType", dietType);

        // 2. Allergies
        ArrayList<String> allergies = new ArrayList<>();
        if (chkPeanuts.isChecked()) allergies.add("peanuts");
        if (chkTreeNuts.isChecked()) allergies.add("tree-nuts");
        if (chkMilk.isChecked()) allergies.add("milk");
        if (chkGluten.isChecked()) allergies.add("gluten");
        if (chkEggs.isChecked()) allergies.add("eggs");
        if (chkSoy.isChecked()) allergies.add("soy");
        if (chkFish.isChecked()) allergies.add("fish");
        if (chkShellfish.isChecked()) allergies.add("shellfish");
        if (chkSesame.isChecked()) allergies.add("sesame");
        preferences.put("allergies", allergies);

        // 3. Nutrient Limits
        Map<String, Float> nutrientLimits = new HashMap<>();
        nutrientLimits.put("maxSugar", (float) seekSugar.getProgress());
        nutrientLimits.put("maxSalt", seekSalt.getProgress() / 10.0f);
        nutrientLimits.put("maxSaturatedFat", (float) seekSatFat.getProgress());
        nutrientLimits.put("maxCalories", (float) ((seekCalories.getProgress() / 10) * 10));
        preferences.put("nutrientLimits", nutrientLimits);

        // 4. Ingredient Blacklist
        ArrayList<String> blacklist = new ArrayList<>();
        if (chkMSG.isChecked()) blacklist.add("msg");
        if (chkAspartame.isChecked()) blacklist.add("aspartame");
        if (chkHighFructose.isChecked()) blacklist.add("high-fructose-corn-syrup");
        if (chkArtificialColors.isChecked()) blacklist.add("artificial-colors");
        if (chkPalmOil.isChecked()) blacklist.add("palm-oil");
        preferences.put("ingredientBlacklist", blacklist);

        // 5. Quality Preferences
        preferences.put("avoidAdditives", chkAvoidAdditives.isChecked());
        preferences.put("avoidNOVA4", chkAvoidNOVA4.isChecked());
        preferences.put("minNutriScore", spinnerNutriScore.getSelectedItem().toString());

        // Display summary
        showPreferencesSummary(preferences);

        // TODO: Save to Firestore
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
        StringBuilder summary = new StringBuilder("✅ Preferences Saved!\n\n");

        summary.append("Diet: ").append(preferences.get("dietType")).append("\n");

        ArrayList<String> allergies = (ArrayList<String>) preferences.get("allergies");
        if (!allergies.isEmpty()) {
            summary.append("Allergies: ").append(allergies.size()).append(" selected\n");
        }

        Map<String, Float> limits = (Map<String, Float>) preferences.get("nutrientLimits");
        summary.append(String.format("Sugar limit: %.0fg\n", limits.get("maxSugar")));
        summary.append(String.format("Salt limit: %.1fg\n", limits.get("maxSalt")));

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