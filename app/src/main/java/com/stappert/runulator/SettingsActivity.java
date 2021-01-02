package com.stappert.runulator;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity implements WeightDialog.WeightDialogListener {

    private SettingsManager settings;

    private MainSettingsFragment settingsFragment;

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
        settingsFragment = new MainSettingsFragment();
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
    public void applyWeight(int weight, WeightUnit unit) {
        settings.setWeight(weight, unit);
        settingsFragment.getWeightButton().setSummary(unit.formatWeight(weight));
    }
}