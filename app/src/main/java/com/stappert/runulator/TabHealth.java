package com.stappert.runulator;

import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

/**
 * Organizes the run view of the application.
 */
public class TabHealth extends Fragment {

    // Elements
    private View healthView;
    private TextView heartRateMaxTextView;
    private TextView heartRateFatBurningTextView;
    private TextView heartRateConditionBuildingTextView;
    private TextView heartRateMaxPerformanceTextView;
    private TextView bmiTextView;

    /**
     * Settings manager.
     */
    private SettingsManager settings;

    /**
     * Creates view for tab forecast.
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState saved instance state
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        healthView = inflater.inflate(R.layout.tab_layout_health, container, false);
        settings = SettingsManager.getInstance().init(getContext());
        initElements();
        updateHealthData();
        return healthView;
    }

    /**
     * Update values on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateHealthData();
    }

    /**
     * Updates health data on gui.
     */
    private void updateHealthData() {
        try {
            bmiTextView.setText(String.format(Locale.ENGLISH, "%.2f", Run.calculateBMI(
                    settings.getWeightUnit().toKg(settings.getWeight()),
                    settings.getHeightUnit().toCm(settings.getHeight()))));
            final int age = Utils.calculateAge(settings.getBirthday());
            heartRateMaxTextView.setText("" + Run.calculateMaxHeartRate(age));
            heartRateFatBurningTextView.setText("" + Run.calculateHeartRateFatBurning(age));
            heartRateConditionBuildingTextView.setText("" + Run.calculateHeartRateBuildingCondition(age));
            heartRateMaxPerformanceTextView.setText("" + Run.calculateHeartRateMaxPerformance(age));
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initializes gui elements.
     */
    private void initElements() {
        heartRateMaxTextView = healthView.findViewById(R.id.heartRateMaxTextView);
        heartRateFatBurningTextView = healthView.findViewById(R.id.heartRateFatBurningTextView);
        heartRateConditionBuildingTextView = healthView.findViewById(R.id.heartRateConditionBuildingTextView);
        heartRateMaxPerformanceTextView = healthView.findViewById(R.id.heartRateMaxPerformanceTextView);
        bmiTextView = healthView.findViewById(R.id.bmiTextView);
    }
}
