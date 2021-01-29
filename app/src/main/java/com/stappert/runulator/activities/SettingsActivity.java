package com.stappert.runulator.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.stappert.runulator.R;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.dialogs.ValueDialog;
import com.stappert.runulator.utils.Unit;

public class SettingsActivity extends AppCompatActivity implements ValueDialog.ValueDialogListener {

    private SettingsManager settings;

    private com.stappert.runulator.activities.SettingsFragment settingsFragment;

    /**
     * Creates the settings activity.
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        settings = SettingsManager.getInstance();
        settingsFragment = new com.stappert.runulator.activities.SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, settingsFragment).commit();
    }

    /**
     * Fragement of settings.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // set layout
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void applyValue(ValueDialog.ValueType valueType, Number value, Unit unit) {
        switch (valueType) {
            case WEIGHT:
                settings.setWeight(value.intValue(), unit);
                settingsFragment.getWeightButton().setSummary(unit.format(value.intValue()));
                break;
            case HEIGHT:
                settings.setHeight(value.intValue(), unit);
                settingsFragment.getHeightButton().setSummary(unit.format(value.intValue()));
                break;
        }
    }
}