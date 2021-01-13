package com.stappert.runulator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

/**
 * Creates the main settings.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {

    /**
     * Current context of application.
     */
    private Context context;

    /**
     * Manages the settings.
     */
    private SettingsManager settings;

    /**
     * Button, to set weight.
     */
    private Preference weightButton;

    /**
     * Button, to set weight.
     */
    private Preference heightButton;

    /**
     * Creates the preferences.
     *
     * @param savedInstanceState saved instance state
     * @param rootKey            root key
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.context = getPreferenceManager().getContext();
        this.settings = SettingsManager.getInstance();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        addRunSettings(screen);
        addGeneralSettings(screen);
        super.setPreferenceScreen(screen);
    }

    /**
     * Returns the weight button.
     *
     * @return weight button
     */
    public Preference getWeightButton() {
        return weightButton;
    }

    /**
     * Returns the weight button.
     *
     * @return weight button
     */
    public Preference getHeightButton() {
        return heightButton;
    }

    /**
     * Adds the run settings.
     *
     * @param screen screen of settings
     */
    private void addRunSettings(PreferenceScreen screen) {
        PreferenceCategory runSettings = new PreferenceCategory(context);
        runSettings.setTitle(context.getString(R.string.run_settings));
        screen.addPreference(runSettings);
        runSettings.addPreference(createWeightButton());
        runSettings.addPreference(createHeightButton());
    }

    /**
     * creates the weight button.
     *
     * @return weight button
     */
    private Preference createWeightButton() {
        weightButton = new Preference(context);
        weightButton.setTitle(context.getString(R.string.weight));
        weightButton.setSummary(settings.getWeightUnit().format(settings.getWeight()));
        weightButton.setIcon(context.getDrawable(R.drawable.ic_weight));
        weightButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ValueDialog(ValueDialog.ValueType.WEIGHT,
                        getContext().getString(R.string.weight), getContext().getString(R.string.weight_msg),
                        settings.getWeight(), settings.getWeightUnit(), Unit.getWeightUnits(), InputType.TYPE_CLASS_NUMBER
                ).show(MainSettingsFragment.this.getChildFragmentManager(), context.getString(R.string.weight));
                return true;
            }
        });
        return weightButton;
    }

    /**
     * creates the weight button.
     *
     * @return weight button
     */
    private Preference createHeightButton() {
        heightButton = new Preference(context);
        heightButton.setTitle(context.getString(R.string.height));
        heightButton.setSummary(settings.getHeightUnit().format(settings.getHeight()));
        heightButton.setIcon(context.getDrawable(R.drawable.ic_height));
        heightButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ValueDialog(ValueDialog.ValueType.HEIGHT,
                        getContext().getString(R.string.height), getContext().getString(R.string.height_msg),
                        settings.getHeight(), settings.getHeightUnit(), Unit.getHeightUnits(), InputType.TYPE_CLASS_NUMBER
                ).show(MainSettingsFragment.this.getChildFragmentManager(), context.getString(R.string.weight));
                return true;
            }
        });
        return heightButton;
    }

    /**
     * Adds general settings.
     *
     * @param screen screen of settings
     */
    private void addGeneralSettings(PreferenceScreen screen) {
        PreferenceCategory generalSettings = new PreferenceCategory(context);
        generalSettings.setTitle(context.getString(R.string.general_settings));
        screen.addPreference(generalSettings);
        generalSettings.addPreference(createResetAppButton());
    }

    /**
     * Creates the button, to reset the app settings including dialog to confirm the reset.
     *
     * @return reset button
     */
    private Preference createResetAppButton() {
        Preference resetButton = new Preference(context);
        resetButton.setTitle(context.getString(R.string.reset_app));
        resetButton.setSummary(context.getString(R.string.reset_app_msg));
        resetButton.setIcon(context.getDrawable(R.drawable.ic_settings_backup_restore));
        resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(MainSettingsFragment.this.context)
                        .setTitle(MainSettingsFragment.this.context.getString(R.string.reset_app))
                        .setMessage(MainSettingsFragment.this.context.getString(R.string.reset_app_confirm))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Context context = MainSettingsFragment.this.context;
                                context.getSharedPreferences("runulator", Context.MODE_PRIVATE).edit().clear().commit();
                                weightButton.setSummary(settings.getWeightUnit().format(settings.getWeight()));
                                Toast.makeText(context, "App wurde zurückgesetzt", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
        return resetButton;
    }
}