package com.stappert.runulator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
     * Button, to set birthday.
     */
    private Preference birthdayButton;

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
        runSettings.addPreference(createUnitOfLength());
        runSettings.addPreference(createWeightButton());
        runSettings.addPreference(createHeightButton());
    }

    /**
     * Creates the preference to set the unit of length (km or mile).
     *
     * @return preference to set unit of length
     */
    private Preference createUnitOfLength() {
        String[] units = new String[Unit.getDistanceUnits().size()];
        String[] unitLabels = new String[Unit.getDistanceUnits().size()];
        for (int i = 0; i < Unit.getDistanceUnits().size(); i++) {
            units[i] = Unit.getDistanceUnits().get(i).name();
            unitLabels[i] = Unit.getDistanceUnits().get(i).getLabel(context);
        }
        ListPreference selectedUnitOfLength = new ListPreference(context);
        selectedUnitOfLength.setKey(SettingsManager.KEY_DISTANCE_UNIT);
        selectedUnitOfLength.setTitle(context.getString(R.string.unit_of_length));
        selectedUnitOfLength.setIcon(R.drawable.ic_unit_of_length);
        selectedUnitOfLength.setEntryValues(units);
        selectedUnitOfLength.setEntries(unitLabels);
        selectedUnitOfLength.setDefaultValue(Unit.KM.name());
        selectedUnitOfLength.setValue(settings.getDistanceUnit().name());
        selectedUnitOfLength.setSummary(settings.getDistanceUnit().getLabel(context));
        selectedUnitOfLength.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object unit) {
                SettingsManager settings = SettingsManager.getInstance();
                switch (Unit.valueOf(unit.toString())) {
                    case MILE:
                        settings.setDistanceUnit(Unit.MILE);
                        settings.setPaceUnit(Unit.MIN_MILE);
                        settings.setSpeedUnit(Unit.MPH);
                        break;
                    default:
                        settings.setDistanceUnit(Unit.KM);
                        settings.setPaceUnit(Unit.MIN_KM);
                        settings.setSpeedUnit(Unit.KM_H);
                }
                preference.setSummary(settings.getDistanceUnit().getLabel(context));
                return true;
            }
        });
        return selectedUnitOfLength;
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
        birthdayButton = new Preference(context);
        birthdayButton.setTitle(context.getString(R.string.birthday));
        updateBirthday();
        birthdayButton.setIcon(context.getDrawable(R.drawable.ic_birthday));
        birthdayButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // create listener, to apply birthday
                DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        settings.setBirthday(calendar.getTimeInMillis());
                        updateBirthday();
                    }
                };
                // create date dialog
                Calendar birthday = Calendar.getInstance();
                birthday.setTimeInMillis(settings.getBirthday());
                DatePickerDialog dateDialog = new DatePickerDialog(getContext(), datePickerListener,
                        birthday.get(Calendar.YEAR), birthday.get(Calendar.MONTH), birthday.get(Calendar.DAY_OF_MONTH));
                dateDialog.getDatePicker().setMaxDate(new Date().getTime());
                dateDialog.show();
                return true;
            }
        });
        return birthdayButton;
    }

    /**
     * Updates birthday.
     */
    private void updateBirthday() {
        long birthday = settings.getBirthday();
        birthdayButton.setSummary(new SimpleDateFormat(context.getString(R.string.date_format)).format(new Date(birthday))
                + " (" + Utils.calculateAge(birthday) + " " + getString(R.string.years) + ")");
    }

    /**
     * creates the weight button.
     *
     * @return weight button
     */
    private Preference createAgeButton() {
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