package com.stappert.runulator.activities;

import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.stappert.runulator.R;
import com.stappert.runulator.dialogs.DistanceDialog;
import com.stappert.runulator.dialogs.TimeDialog;
import com.stappert.runulator.dialogs.ValueDialog;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.RunLoadedListener;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.Utils;
import com.stappert.runulator.utils.ValueChangeListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizes the run view of the application.
 */
public class TabRun extends Fragment implements ValueChangeListener, RunLoadedListener {

    /**
     * Layout for labels in gui.
     */
    private static TableRow.LayoutParams LAYOUT_LABEL = new TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, .45f);

    /**
     * Layout for values and units in gui.
     */
    private static TableRow.LayoutParams LAYOUT_INPUT_VALUE_UNIT = new TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, .55f);

    /**
     * Layout for values and units in gui.
     */
    private static TableRow.LayoutParams LAYOUT_OUTPUT_VALUE = new TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, .20f);

    /**
     * Layout for values and units in gui.
     */
    private static TableRow.LayoutParams LAYOUT_OUTPUT_UNIT = new TableRow.LayoutParams(
            0, TableRow.LayoutParams.WRAP_CONTENT, .35f);

    /**
     * Layout for rows.
     */
    private static TableLayout.LayoutParams LAYOUT_TABLE_ROW = new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

    private final static int INPUT_TYPE_NUMBER = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    private final static int INPUT_TYPE_TIME = InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME;

    // Input elements
    private View runView;
    private TextView distanceButton;
    private TextView durationButton;
    private TextView paceButton;
    private TextView speedButton;
    private TextView inputParamInfoTextView;
    private TextView inputParameter1LabelTextView;
    private EditText inputParameter1EditText;
    private TextView inputParameter1UnitTextView;
    private TextView inputParameter2LabelTextView;
    private EditText inputParameter2EditText;
    private TextView inputParameter2UnitTextView;
    // Result elements
    private TextView resultParameter1LabelTextView;
    private TextView resultParameter1ValueTextView;
    private TextView resultParameter1UnitTextView;
    private TextView resultParameter2LabelTextView;
    private TextView resultParameter2ValueTextView;
    private TextView resultParameter2UnitTextView;
    private TextView caloriesLabelTextView;
    private TextView caloriesValueTextView;
    private TextView caloriesUnitTextView;
    private TextView cadenceCountLabelTextView;
    private TextView cadenceCountValueTextView;
    private TextView cadenceCountUnitTextView;

    /**
     * Favorite button.
     */
    private ImageButton favoriteButton;

    /**
     * Type for run parameter 1, which can be distance or duration.
     */
    private ParameterType inputParameter1;

    /**
     * Type for run parameter 2, which can be duration, pace or speed.
     */
    private ParameterType inputParameter2;

    /**
     * Current run.
     */
    private Run currentRun;

    /**
     * Current run as json string.
     */
    private String currentRunJson;

    /**
     * Favorite runs as json string.
     */
    private List<String> favoriteRuns;
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
        runView = inflater.inflate(R.layout.tab_layout_run, container, false);
        try {
            settings = SettingsManager.getInstance().init(getContext());
            initElements();
            initListener();
            currentRun = settings.getRun();
            updateInputArea();
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return runView;
    }

    /**
     * Update values on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        try {
            updatePillButtons();
            updateInputArea();
            updateResultArea();
            calculateAndUpdateRun();
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =============================================================================================
    // private utility functions
    // =============================================================================================

    /**
     * Calculates and updates run depending on input.
     */
    private void calculateAndUpdateRun() throws CustomException {
        // both input parameters must be set
        if (!inputParameter1EditText.isEnabled() || !inputParameter2EditText.isEnabled()) {
            deactivateFavoriteButton();
        } else if (inputParameter1EditText.getText().toString().isEmpty()
                || inputParameter2EditText.getText().toString().isEmpty()) {
            inputParamInfoTextView.setText(getString(R.string.input_info_enter_values));
            resultParameter1ValueTextView.setText("-");
            resultParameter2ValueTextView.setText("-");
            deactivateFavoriteButton();
        } else {
            inputParamInfoTextView.setText("");
            Number runValue1 = getRunParameterValue(inputParameter1, inputParameter1EditText);
            Number runValue2 = getRunParameterValue(inputParameter2, inputParameter2EditText);
            // run parameter 1 is distance or duration
            if (ParameterType.DISTANCE.equals(inputParameter1)) {
                // run parameter 2 is duration, pace or speed
                if (ParameterType.DURATION.equals(inputParameter2)) {
                    currentRun = Run.createWithDistanceAndDuration(runValue1.floatValue(), runValue2.intValue());
                    currentRunJson = Run.jsonWithDistanceAndDuration(runValue1.floatValue(), runValue2.intValue());
                    resultParameter1ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                    resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                } else if (ParameterType.PACE.equals(inputParameter2)) {
                    currentRun = Run.createWithDistanceAndPace(runValue1.floatValue(), runValue2.intValue());
                    currentRunJson = Run.jsonWithDistanceAndPace(runValue1.floatValue(), runValue2.intValue());
                    resultParameter1ValueTextView.setText(currentRun.getDuration());
                    resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                } else if (ParameterType.SPEED.equals(inputParameter2)) {
                    currentRun = Run.createWithDistanceAndSpeed(runValue1.floatValue(), runValue2.intValue());
                    currentRunJson = Run.jsonWithDistanceAndSpeed(runValue1.floatValue(), runValue2.intValue());
                    resultParameter1ValueTextView.setText(currentRun.getDuration());
                    resultParameter2ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                }
            } else {
                // run parameter 2 is pace or speed
                if (ParameterType.PACE.equals(inputParameter2)) {
                    currentRun = Run.createWithDurationAndPace(runValue1.intValue(), runValue2.intValue());
                    currentRunJson = Run.jsonWithDurationAndPace(runValue1.intValue(), runValue2.intValue());
                    resultParameter1ValueTextView.setText(currentRun.getDistance(settings.getDistanceUnit()));
                    resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                } else if (ParameterType.SPEED.equals(inputParameter2)) {
                    currentRun = Run.createWithDurationAndSpeed(runValue1.intValue(), runValue2.floatValue());
                    currentRunJson = Run.jsonWithDurationAndSpeed(runValue1.intValue(), runValue2.floatValue());
                    resultParameter1ValueTextView.setText(currentRun.getDistance(settings.getDistanceUnit()));
                    resultParameter2ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                }
            }
            caloriesValueTextView.setText(currentRun.calculateCalories(settings.getWeightInKg()));
            cadenceCountValueTextView.setText("" + currentRun.calculateCadenceCount(settings.getHeightInCm()));
            settings.setRun(currentRun);
            updateActiveFavoriteButton();
        }
    }

    private void updateActiveFavoriteButton() {
        favoriteButton.setEnabled(true);
        favoriteButton.setClickable(true);
        if (favoriteRuns.contains(currentRunJson)) {
            favoriteButton.setImageIcon(Icon.createWithBitmap(
                    Utils.drawableToBitmap(getContext().getDrawable(R.drawable.ic_favorite))));
        } else {
            favoriteButton.setImageIcon(Icon.createWithBitmap(
                    Utils.drawableToBitmap(getContext().getDrawable(R.drawable.ic_favorite_add))));
        }
    }

    private void deactivateFavoriteButton() {
        favoriteButton.setEnabled(false);
        favoriteButton.setClickable(false);
        favoriteButton.setImageIcon(Icon.createWithBitmap(
                Utils.drawableToBitmap(getContext().getDrawable(R.drawable.ic_favorite_add))));
    }

    /**
     * Returns distance from text field parsed to kilometer.
     *
     * @return distance
     * @throws CustomException if conversion failed
     */
    private Number getRunParameterValue(ParameterType type, EditText textField) throws CustomException {
        if (ParameterType.DISTANCE.equals(type)) {
            return settings.getDistanceUnit().toKm(Run.parseToFloat(textField.getText().toString()));
        } else if (ParameterType.DURATION.equals(type)) {
            return Run.parseTimeInSeconds(textField.getText().toString());
        } else if (ParameterType.PACE.equals(type)) {
            return settings.getPaceUnit().toMinPerKm(Run.parseTimeInSeconds(textField.getText().toString()));
        } else if (ParameterType.SPEED.equals(type)) {
            return settings.getSpeedUnit().toKmPerHour(Run.parseToFloat(textField.getText().toString()));
        } else {
            return Float.NaN;
        }
    }

    // =============================================================================================
    // Input area
    // =============================================================================================

    /**
     * Selects or disable
     */
    private void updatePillButtons() {
        selectRunParameterButton(distanceButton, ParameterType.DISTANCE.equals(inputParameter1));
        selectRunParameterButton(durationButton,
                ParameterType.DURATION.equals(inputParameter1) || ParameterType.DURATION.equals(inputParameter2));
        selectRunParameterButton(paceButton, ParameterType.PACE.equals(inputParameter2));
        selectRunParameterButton(speedButton, ParameterType.SPEED.equals(inputParameter2));
        enableButtons();
    }


    /**
     * Selects or deselects a run parameter pill button.
     *
     * @param button     button
     * @param isSelected select (true) or deselect (false)
     */
    private void selectRunParameterButton(TextView button, boolean isSelected) {
        button.setBackground(getResources().getDrawable(isSelected ? R.drawable.shape_pill_button_selected : R.drawable.shape_pill_button, getContext().getTheme()));
        button.setCompoundDrawablesWithIntrinsicBounds(isSelected ? R.drawable.ic_cancel : R.drawable.ic_empty, 0, 0, 0);
    }

    /**
     * Disable or enable  pill buttons for setting run parameters.
     */
    private void enableButtons() {
        final boolean notAllParamSet = getNumberOfSetInputParameters() < 2;
        distanceButton.setEnabled(ParameterType.DISTANCE.equals(inputParameter1) || notAllParamSet);
        durationButton.setEnabled(ParameterType.DURATION.equals(inputParameter1) || ParameterType.DURATION.equals(inputParameter2) || notAllParamSet);
        paceButton.setEnabled((notAllParamSet || ParameterType.PACE.equals(inputParameter2)) && !ParameterType.SPEED.equals(inputParameter2));
        speedButton.setEnabled((notAllParamSet || ParameterType.SPEED.equals(inputParameter2)) && !ParameterType.PACE.equals(inputParameter2));
    }

    /**
     * Updates the input area depending on selected run parameters.
     */
    private void updateInputArea() {
        final int numberOfSetRunParameters = getNumberOfSetInputParameters();
        inputParamInfoTextView.setText(
                numberOfSetRunParameters == 0 ? R.string.input_info_select_please_two
                        : numberOfSetRunParameters == 1 ? R.string.input_info_select_please_one
                        : numberOfSetRunParameters == 2 ? R.string.input_info_enter_values
                        : R.string.input_info_enter_values);
        updateInputParameter();
    }

    /**
     * Applies run parameter 1. Run parameter 1 can be distance or duration.
     *
     * @param parameterType parameter type
     * @return if run parameter is selected or not
     */
    private boolean applyRunParameter1(ParameterType parameterType) {
        final boolean isSelected = !parameterType.equals(inputParameter1);
        if (isSelected) {
            inputParameter2 = inputParameter1 != null ? inputParameter1 : inputParameter2;
            // switch value for duration to parameter 2 if set
            if (inputParameter1 != null) {
                inputParameter2 = inputParameter1;
                inputParameter2EditText.setText(inputParameter1EditText.getText());
                inputParameter1EditText.setText("");
            }
            inputParameter1 = parameterType;
        } else {
            inputParameter1 = ParameterType.DURATION.equals(inputParameter2) ? ParameterType.DURATION : null;
            inputParameter2 = ParameterType.DURATION.equals(inputParameter2) ? null : inputParameter2;
            // switch value for duration to parameter 1 if set
            if (ParameterType.DURATION.equals(inputParameter1)) {
                inputParameter1EditText.setText(inputParameter2EditText.getText());
                inputParameter2EditText.setText("");
            }
        }
        return isSelected;
    }

    /**
     * Applies run parameter 2. Run parameter 2 can be duration, speed or pace.
     *
     * @param parameterType parameter type
     * @return if run parameter is selected or not
     */
    private boolean applyRunParameter2(ParameterType parameterType) {
        final boolean isSelected = !parameterType.equals(inputParameter2);
        inputParameter2 = isSelected ? parameterType : null;
        return isSelected;
    }

    /**
     * Updates both input parameters depending on selected parameter types.
     */
    private void updateInputParameter() {
        updateInputParameter1();
        updateInputParameter2();
    }

    /**
     * Updates the first input parameter depending on selected parameter input type.
     * The first input parameter can be distance or duration.
     */
    private void updateInputParameter1() {
        if (ParameterType.DISTANCE.equals(inputParameter1)) {
            updateInputParameter1(true, getString(R.string.distance), INPUT_TYPE_NUMBER,
                    settings.getDistanceUnit().getLabel(getContext()));
        } else if (ParameterType.DURATION.equals(inputParameter1)) {
            updateInputParameter1(true, getString(R.string.run_time), INPUT_TYPE_TIME,
                    settings.getDurationUnit().getLabel(getContext()));
        } else {
            updateInputParameter1(false, getString(R.string.run_parameter_1), InputType.TYPE_CLASS_TEXT, getString(R.string.unit));
        }
    }

    /**
     * Updates the second input parameter depending on selected parameter input type.
     * The second input parameter can be duration, pace or speed.
     */
    private void updateInputParameter2() {
        if (ParameterType.DURATION.equals(inputParameter2)) {
            updateInputParameter2(true, getString(R.string.run_time), INPUT_TYPE_TIME,
                    settings.getDurationUnit().getLabel(getContext()));
        } else if (ParameterType.PACE.equals(inputParameter2)) {
            updateInputParameter2(true, getString(R.string.pace), INPUT_TYPE_TIME,
                    settings.getPaceUnit().getLabel(getContext()));
        } else if (ParameterType.SPEED.equals(inputParameter2)) {
            updateInputParameter2(true, getString(R.string.speed), INPUT_TYPE_NUMBER,
                    settings.getSpeedUnit().getLabel(getContext()));
        } else {
            updateInputParameter2(false, getString(R.string.run_parameter_2), InputType.TYPE_CLASS_TEXT, getString(R.string.unit));
        }
    }

    /**
     * Updates the first input parameter depending on selected parameter input type.
     * The first input parameter can be distance or duration.
     *
     * @param isEnabled if a parameter type is selected
     * @param label     label of parameter
     * @param inputType input type (for example: decimal or time)
     * @param unit      unit of parameter
     */
    private void updateInputParameter1(boolean isEnabled, String label, int inputType, String unit) {
        updateInputParameter(inputParameter1LabelTextView, inputParameter1EditText, inputParameter1UnitTextView,
                isEnabled, label, inputType, unit);
        // add dialogs
        if (settings.isDialogInput()) {
            createInputParameterListeners(inputParameter1EditText, inputParameter1);
        } else {
            inputParameter1EditText.setFocusable(true);
        }
    }

    /**
     * Updates the first input parameter depending on selected parameter input type.
     * The first input parameter can be distance or duration.
     *
     * @param isEnabled if a parameter type is selected
     * @param label     label of parameter
     * @param inputType input type (for example: decimal or time)
     * @param unit      unit of parameter
     */
    private void updateInputParameter2(boolean isEnabled, String label, int inputType, String unit) {
        updateInputParameter(inputParameter2LabelTextView, inputParameter2EditText, inputParameter2UnitTextView,
                isEnabled, label, inputType, unit);
        // add dialogs
        if (settings.isDialogInput()) {
            createInputParameterListeners(inputParameter2EditText, inputParameter2);
        } else {
            inputParameter2EditText.setFocusable(true);
        }
    }

    /**
     * Updates the desired input parameter.
     *
     * @param label     text view for label
     * @param input     edit text field for input
     * @param unit      text view for unit
     * @param isEnabled if a parameter type is selected
     * @param label     label of parameter
     * @param inputType input type (for example: decimal or time)
     * @param unit      unit of parameter
     */
    private void updateInputParameter(TextView label, EditText input, TextView unit,
                                      boolean isEnabled, String labelText, int inputType,
                                      String unitText) {
        label.setText(labelText);
        label.setEnabled(isEnabled);
        if (!isEnabled) {
            input.setText("");
        }
        input.setInputType(inputType);
        input.setEnabled(isEnabled);
        unit.setText(unitText);
        unit.setEnabled(isEnabled);
    }

    /**
     * Creates a listener, to add a parameter value.
     *
     * @param input     edit text field
     * @param parameter run parameter type
     */
    public void createInputParameterListeners(final EditText input, final ParameterType parameter) {
        input.setFocusable(false);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String value = input.getText().toString();
                if (ParameterType.DISTANCE.equals(parameter)) {
                    new DistanceDialog(value, TabRun.this)
                            .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.distance));
                } else if (ParameterType.DURATION.equals(parameter)) {
                    try {
                        new TimeDialog(ParameterType.DURATION, true, getString(R.string.run_time),
                                getString(R.string.enter_run_time), value, TabRun.this)
                                .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.run_time));
                    } catch (CustomException ex) {
                        Log.e("error", ex.getMessage());
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (ParameterType.PACE.equals(parameter)) {
                    try {
                        new TimeDialog(ParameterType.PACE, false, getString(R.string.pace),
                                getString(R.string.enter_pace), value, TabRun.this)
                                .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.pace));
                    } catch (CustomException ex) {
                        Log.e("error", ex.getMessage());
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (ParameterType.SPEED.equals(parameter)) {
                    new ValueDialog(ParameterType.SPEED, getContext().getString(R.string.speed),
                            getContext().getString(R.string.enter_speed),
                            value, settings.getSpeedUnit(), null,
                            android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
                            TabRun.this
                    ).show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.speed));
                }
            }
        });
    }

    /**
     * Returns the number of set run parameters.
     *
     * @return 2 if both parameters set, 1 if one parameter is set, 0 if no parameter is set
     */
    private int getNumberOfSetInputParameters() {
        return (inputParameter1 != null ? 1 : 0) + (inputParameter2 != null ? 1 : 0);
    }

    /**
     * Applys the set run parameter value.
     *
     * @param value value
     */
    @Override
    public void applyValue(ParameterType parameter, Object value, Unit unit) {
        if (parameter.equals(inputParameter1)) {
            inputParameter1EditText.setText("" + value);
        } else if (parameter.equals(inputParameter2)) {
            inputParameter2EditText.setText("" + value);
        }
    }


    /**
     * Applies entered value.
     *
     * @param runJson selected run as json string
     */
    @Override
    public void applyRun(String runJson) {
        try {
            Run run = Run.jsonToRun(runJson);
            if (runJson.contains(ParameterType.DISTANCE.name())) {
                inputParameter1 = ParameterType.DISTANCE;
                inputParameter1EditText.setText(run.getDistance(settings.getDistanceUnit()));
                selectRunParameterButton(distanceButton, true);
                if (runJson.contains(ParameterType.DURATION.name())) {
                    inputParameter2 = ParameterType.DURATION;
                    inputParameter2EditText.setText(run.getDuration());
                    selectRunParameterButton(durationButton, true);
                    selectRunParameterButton(paceButton, false);
                    selectRunParameterButton(speedButton, false);
                } else if (runJson.contains(ParameterType.PACE.name())) {
                    inputParameter2 = ParameterType.PACE;
                    inputParameter2EditText.setText(run.getPace(settings.getPaceUnit()));
                    selectRunParameterButton(durationButton, false);
                    selectRunParameterButton(paceButton, true);
                    selectRunParameterButton(speedButton, false);
                } else if (runJson.contains(ParameterType.SPEED.name())) {
                    inputParameter2 = ParameterType.SPEED;
                    inputParameter2EditText.setText(run.getSpeed(settings.getSpeedUnit()));
                    selectRunParameterButton(durationButton, false);
                    selectRunParameterButton(paceButton, false);
                    selectRunParameterButton(speedButton, true);
                }
            } else if (runJson.contains(ParameterType.DURATION.name())) {
                inputParameter1 = ParameterType.DURATION;
                inputParameter1EditText.setText(run.getDuration());
                selectRunParameterButton(durationButton, true);
                selectRunParameterButton(distanceButton, false);
                if (runJson.contains(ParameterType.PACE.name())) {
                    inputParameter2 = ParameterType.PACE;
                    inputParameter2EditText.setText(run.getPace(settings.getPaceUnit()));
                    selectRunParameterButton(paceButton, true);
                    selectRunParameterButton(speedButton, false);
                } else if (runJson.contains(ParameterType.SPEED.name())) {
                    inputParameter2 = ParameterType.SPEED;
                    inputParameter2EditText.setText(run.getSpeed(settings.getSpeedUnit()));
                    selectRunParameterButton(paceButton, false);
                    selectRunParameterButton(speedButton, true);
                }
            }
            currentRun = run;
            currentRunJson = run.toJson();
            enableButtons();
            updateInputArea();
            updateResultArea();
            calculateAndUpdateRun();
        } catch (JSONException | CustomException ex) {
            Log.e("error", ex.getMessage());
        }
    }

    // =============================================================================================
    // Result area
    // =============================================================================================

    /**
     * Initializes the run result.
     */
    private void updateResultArea() {
        // get number of set input parameters
        final int noOfSetRunParameters = getNumberOfSetInputParameters();

        // reset calories and cadence count
        resetStaticResultParameters(noOfSetRunParameters == 2);

        // update temporary result parameters
        if (noOfSetRunParameters == 2) {
            final ParameterType outputParameter1 = defineOuputRunParameter1();
            final ParameterType outputParameter2 = defineOuputRunParameter2(outputParameter1);
            updateResultParameter1(outputParameter1);
            updateResultParameter2(outputParameter2);
        } else if (noOfSetRunParameters == 1 && ParameterType.PACE.equals(inputParameter2)) {
            updateResultParameter1(false, getString(R.string.speed), settings.getSpeedUnit().toString());
            updateResultParameter2(null);
        } else if (noOfSetRunParameters == 1 && ParameterType.SPEED.equals(inputParameter2)) {
            updateResultParameter1(false, getString(R.string.pace), settings.getPaceUnit().toString());
            updateResultParameter2(null);
        } else {
            updateResultParameter1(null);
            updateResultParameter2(null);
        }
    }

    /**
     * Defines the output run parameter 1, which can be distance, duration or pace.
     *
     * @return run parameter 1 for output
     */
    private ParameterType defineOuputRunParameter1() {
        return !ParameterType.DISTANCE.equals(inputParameter1)
                ? ParameterType.DISTANCE
                : !ParameterType.DURATION.equals(inputParameter1) && !ParameterType.DURATION.equals(inputParameter2)
                ? ParameterType.DURATION
                : !ParameterType.PACE.equals(inputParameter2)
                ? ParameterType.PACE : null;
    }

    /**
     * Defines the output run parameter 2 depending on run parameter 1. Parameter can be duration,
     * pace or speed.
     *
     * @param outputParameterType1 run parameter 1 for output
     * @return run parameter 2 for output
     */
    private ParameterType defineOuputRunParameter2(ParameterType outputParameterType1) {
        return !ParameterType.DURATION.equals(outputParameterType1)
                && !ParameterType.DURATION.equals(inputParameter1) && !ParameterType.DURATION.equals(inputParameter2)
                ? ParameterType.DURATION
                : !ParameterType.PACE.equals(outputParameterType1) && !ParameterType.PACE.equals(inputParameter2)
                ? ParameterType.PACE : ParameterType.SPEED;
    }

    /**
     * Resets calories and cadence count fields.
     *
     * @param isEnabled is enabled of both input parameters are selected.
     */
    private void resetStaticResultParameters(boolean isEnabled) {
        // calories row
        caloriesLabelTextView.setEnabled(isEnabled);
        caloriesValueTextView.setText("-");
        caloriesValueTextView.setEnabled(isEnabled);
        caloriesUnitTextView.setEnabled(isEnabled);
        // cadence count row
        cadenceCountLabelTextView.setEnabled(isEnabled);
        cadenceCountValueTextView.setText("-");
        cadenceCountValueTextView.setEnabled(isEnabled);
        cadenceCountUnitTextView.setEnabled(isEnabled);
    }

    /**
     * Updates first result parameter.
     *
     * @param resultParameter result parameter
     */
    private void updateResultParameter1(ParameterType resultParameter) {
        if (ParameterType.DISTANCE.equals(resultParameter)) {
            updateResultParameter1(true, getString(R.string.distance), settings.getDistanceUnit().toString());
        } else if (ParameterType.DURATION.equals(resultParameter)) {
            updateResultParameter1(true, getString(R.string.run_time), settings.getDurationUnit().toString());
        } else if (ParameterType.PACE.equals(resultParameter)) {
            updateResultParameter1(true, getString(R.string.pace), settings.getPaceUnit().toString());
        } else if (ParameterType.SPEED.equals(resultParameter)) {
            updateResultParameter1(true, getString(R.string.speed), settings.getSpeedUnit().toString());
        } else {
            updateResultParameter1(false, getString(R.string.run_result_param_1), "");
        }
    }

    /**
     * Updates first result parameter.
     *
     * @param resultParameter result parameter
     */
    private void updateResultParameter2(ParameterType resultParameter) {
        if (ParameterType.DISTANCE.equals(resultParameter)) {
            updateResultParameter2(true, getString(R.string.distance), settings.getDistanceUnit().toString());
        } else if (ParameterType.DURATION.equals(resultParameter)) {
            updateResultParameter2(true, getString(R.string.run_time), settings.getDurationUnit().toString());
        } else if (ParameterType.PACE.equals(resultParameter)) {
            updateResultParameter2(true, getString(R.string.pace), settings.getPaceUnit().toString());
        } else if (ParameterType.SPEED.equals(resultParameter)) {
            updateResultParameter2(true, getString(R.string.speed), settings.getSpeedUnit().toString());
        } else {
            updateResultParameter2(false, getString(R.string.run_result_param_2), "");
        }
    }

    /**
     * Updates first result parameter.
     *
     * @param isEnabled if value is enabled
     * @param label     label
     * @param unit      unit
     */
    private void updateResultParameter1(boolean isEnabled, String label, String unit) {
        updateResultParameter(resultParameter1LabelTextView, resultParameter1ValueTextView,
                resultParameter1UnitTextView, isEnabled, label, unit);
    }

    /**
     * Updates second result parameter.
     *
     * @param isEnabled if value is enabled
     * @param label     label
     * @param unit      unit
     */
    private void updateResultParameter2(boolean isEnabled, String label, String unit) {
        updateResultParameter(resultParameter2LabelTextView, resultParameter2ValueTextView,
                resultParameter2UnitTextView, isEnabled, label, unit);
    }

    /**
     * Updates the a result paremter.
     *
     * @param label     text view for label
     * @param value     text view for result
     * @param unit      text view for unit
     * @param isEnabled true, if elements are enabled
     * @param labelText text for label
     * @param unitText  text for unit
     */
    private void updateResultParameter(TextView label, TextView value, TextView unit,
                                       boolean isEnabled, String labelText, String unitText) {
        label.setText(labelText);
        label.setEnabled(isEnabled);
        value.setText("-");
        value.setEnabled(isEnabled);
        unit.setText(unitText);
        unit.setEnabled(isEnabled);
    }
    // =============================================================================================
    // Initialize
    // =============================================================================================

    /**
     * Initializes gui elements.
     */
    private void initElements() {
        // input parameters
        distanceButton = runView.findViewById(R.id.distanceButton);
        durationButton = runView.findViewById(R.id.durationButton);
        paceButton = runView.findViewById(R.id.paceButton);
        speedButton = runView.findViewById(R.id.speedButton);
        inputParamInfoTextView = runView.findViewById(R.id.inputParamInfoTextView);
        inputParameter1LabelTextView = runView.findViewById(R.id.inputParameter1LabelTextView);
        inputParameter1EditText = runView.findViewById(R.id.inputParameter1EditText);
        inputParameter1UnitTextView = runView.findViewById(R.id.inputParameter1UnitTextView);
        inputParameter2LabelTextView = runView.findViewById(R.id.inputParameter2LabelTextView);
        inputParameter2EditText = runView.findViewById(R.id.inputParameter2EditText);
        inputParameter2UnitTextView = runView.findViewById(R.id.inputParameter2UnitTextView);
        // result parameters
        resultParameter1LabelTextView = runView.findViewById(R.id.resultParameter1LabelTextView);
        resultParameter1ValueTextView = runView.findViewById(R.id.resultParameter1ValueTextView);
        resultParameter1UnitTextView = runView.findViewById(R.id.resultParameter1UnitTextView);
        resultParameter2LabelTextView = runView.findViewById(R.id.resultParameter2LabelTextView);
        resultParameter2ValueTextView = runView.findViewById(R.id.resultParameter2ValueTextView);
        resultParameter2UnitTextView = runView.findViewById(R.id.resultParameter2UnitTextView);
        caloriesLabelTextView = runView.findViewById(R.id.caloriesLabelTextView);
        caloriesValueTextView = runView.findViewById(R.id.caloriesValueTextView);
        caloriesUnitTextView = runView.findViewById(R.id.caloriesUnitTextView);
        cadenceCountLabelTextView = runView.findViewById(R.id.cadenceCountLabelTextView);
        cadenceCountValueTextView = runView.findViewById(R.id.cadenceCountValueTextView);
        cadenceCountUnitTextView = runView.findViewById(R.id.cadenceCountUnitTextView);
        // favorites
        favoriteRuns = new ArrayList<>(SettingsManager.getInstance().getFavoriteRunsJson());
        favoriteButton = runView.findViewById(R.id.favoriteButton);
        favoriteButton.setEnabled(false);
    }

    /**
     * Initializes listeners for elements.
     */
    private void initListener() {
        addOnClickListenerToRunParameterButtons(distanceButton);
        addOnClickListenerToRunParameterButtons(durationButton);
        addOnClickListenerToRunParameterButtons(paceButton);
        addOnClickListenerToRunParameterButtons(speedButton);
        addTextWatcherToEditText(inputParameter1EditText);
        addTextWatcherToEditText(inputParameter2EditText);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                if (favoriteRuns.contains(currentRunJson)) {
                    favoriteRuns.remove(currentRunJson);
                    Toast.makeText(getContext(), getString(R.string.run_removed), Toast.LENGTH_LONG).show();
                } else {
                    favoriteRuns.add(currentRunJson);
                    Toast.makeText(getContext(), getString(R.string.run_added), Toast.LENGTH_LONG).show();
                }
                // save favorite runs
                settings.setFavoriteRuns(favoriteRuns);
                // update active favorite button
                updateActiveFavoriteButton();
            }
        });
    }

    /**
     * Add text watcher to input fields.
     *
     * @param inputField input field
     */
    private void addTextWatcherToEditText(EditText inputField) {
        inputField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) { /* do nothing */ }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { /* do nothing */ }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                try {
                    calculateAndUpdateRun();
                } catch (Exception ex) {
                    Log.e("error", ex.getMessage());
                    Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Add listener to buttons for select run parameter.
     *
     * @param runParameterButton run parameter button
     */
    private void addOnClickListenerToRunParameterButtons(TextView runParameterButton) {
        runParameterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                switch (button.getId()) {
                    case R.id.distanceButton:
                        selectRunParameterButton(distanceButton, applyRunParameter1(ParameterType.DISTANCE));
                        break;
                    case R.id.durationButton:
                        selectRunParameterButton(durationButton, ParameterType.DISTANCE.equals(inputParameter1)
                                ? applyRunParameter2(ParameterType.DURATION) : applyRunParameter1(ParameterType.DURATION));
                        break;
                    case R.id.paceButton:
                        selectRunParameterButton(paceButton, applyRunParameter2(ParameterType.PACE));
                        break;
                    case R.id.speedButton:
                        selectRunParameterButton(speedButton, applyRunParameter2(ParameterType.SPEED));
                        break;
                }
                enableButtons();
                updateInputArea();
                updateResultArea();
            }
        });
    }
}
