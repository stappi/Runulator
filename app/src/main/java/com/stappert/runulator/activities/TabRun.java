package com.stappert.runulator.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.stappert.runulator.R;
import com.stappert.runulator.dialogs.DistanceDialog;
import com.stappert.runulator.dialogs.TimeDialog;
import com.stappert.runulator.dialogs.ValueDialog;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.ValueChangeListener;

import java.util.List;

/**
 * Organizes the run view of the application.
 */
public class TabRun extends Fragment implements ValueChangeListener {

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
    private TextView stepFrequencyLabelTextView;
    private TextView stepFrequencyValueTextView;
    private TextView stepFrequencyUnitTextView;

//    /**
//     * Map, to handle parameter input to right parameter.
//     */
//    private final Map<ParameterType, EditText> inputParameter = new HashMap<>();

//    /**
//     * Map, to handle output result to right parameter.
//     */
//    private final Map<ParameterType, TextView> runResultParameter = new HashMap<>();

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
     * Favorite runs.
     */
    private List<Run> favoriteRuns;
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
            updateValues();
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
            // do nothing
        } else if (inputParameter1EditText.getText().toString().isEmpty()
                || inputParameter2EditText.getText().toString().isEmpty()) {
            inputParamInfoTextView.setText(getString(R.string.input_info_enter_values));
        } else {
            inputParamInfoTextView.setText("");
            Number runValue1 = getRunParameterValue(inputParameter1, inputParameter1EditText);
            Number runValue2 = getRunParameterValue(inputParameter2, inputParameter2EditText);
            // run parameter 1 is distance or duration
            if (ParameterType.DISTANCE.equals(inputParameter1)) {
                // run parameter 2 is duration, pace or speed
                switch (inputParameter2) {
                    case DURATION:
                        currentRun = Run.createWithDistanceAndDuration(runValue1.floatValue(), runValue2.intValue());
                        resultParameter1ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                        resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case PACE:
                        currentRun = Run.createWithDistanceAndPace(runValue1.floatValue(), runValue2.intValue());
                        resultParameter1ValueTextView.setText(currentRun.getDuration());
                        resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDistanceAndSpeed(runValue1.floatValue(), runValue2.intValue());
                        resultParameter1ValueTextView.setText(currentRun.getDuration());
                        resultParameter2ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                        break;
                }
            } else {
                // run parameter 2 is pace or speed
                switch (inputParameter2) {
                    case PACE:
                        currentRun = Run.createWithDurationAndPace(runValue1.intValue(), runValue2.intValue());
                        resultParameter1ValueTextView.setText(currentRun.getDistance(settings.getDistanceUnit()));
                        resultParameter2ValueTextView.setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDurationAndSpeed(runValue1.intValue(), runValue2.floatValue());
                        resultParameter1ValueTextView.setText(currentRun.getDistance(settings.getDistanceUnit()));
                        resultParameter2ValueTextView.setText(currentRun.getPace(settings.getPaceUnit()));
                        break;
                }
            }
            caloriesValueTextView.setText(currentRun.calculateCalories(settings.getWeightInKg()));
            stepFrequencyValueTextView.setText("" + currentRun.calculateStepFrequency(settings.getHeightInCm()));
            settings.setRun(currentRun);
        }
    }

    /**
     * Add or removes the run to favorite list.
     */
    private void addRemoveRunInFavorites() {
        if (favoriteRuns.contains(currentRun)) {
            favoriteRuns.remove(currentRun);
        } else {
            favoriteRuns.add(currentRun);
        }

        // save favorite runs
        settings.setFavoriteRuns(favoriteRuns);
    }

    /**
     * Returns distance from text field parsed to kilometer.
     *
     * @return distance
     * @throws CustomException if conversion failed
     */
    private Number getRunParameterValue(ParameterType type, EditText textField) throws CustomException {
        switch (type) {
            case DISTANCE:
                return settings.getDistanceUnit().toKm(Run.parseToFloat(textField.getText().toString()));
            case DURATION:
                return Run.parseTimeInSeconds(textField.getText().toString());
            case PACE:
                return settings.getPaceUnit().toMinPerKm(Run.parseTimeInSeconds(textField.getText().toString()));
            case SPEED:
                return settings.getSpeedUnit().toKmPerHour(Run.parseToFloat(textField.getText().toString()));
            default:
                return Float.NaN;
        }
    }

    /**
     * Load values from shared preferences.
     *
     * @throws CustomException if initialization failed
     */
    private void updateValues() throws CustomException {
        favoriteRuns = settings.getFavoriteRuns();
        currentRun = settings.getRun();
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
            inputParameter1 = parameterType;
        } else {
            inputParameter1 = ParameterType.DURATION.equals(inputParameter2) ? ParameterType.DURATION : null;
            inputParameter2 = ParameterType.DURATION.equals(inputParameter2) ? null : inputParameter2;
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
                switch (parameter) {
                    case DISTANCE:
                        new DistanceDialog(value, TabRun.this)
                                .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.distance));
                        break;
                    case DURATION:
                        try {
                            new TimeDialog(ParameterType.DURATION, true, getString(R.string.run_time),
                                    getString(R.string.enter_run_time), value, TabRun.this)
                                    .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.run_time));
                        } catch (CustomException ex) {
                            Log.e("error", ex.getMessage());
                            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case PACE:
                        try {
                            new TimeDialog(ParameterType.PACE, false, getString(R.string.pace),
                                    getString(R.string.enter_pace), value, TabRun.this)
                                    .show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.pace));
                        } catch (CustomException ex) {
                            Log.e("error", ex.getMessage());
                            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case SPEED:
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

    // =============================================================================================
    // Result area
    // =============================================================================================

    /**
     * Initializes the run result.
     */
    private void updateResultArea() {
        // get number of set input parameters
        final int noOfSetRunParameters = getNumberOfSetInputParameters();

        // reset calories and step frequency
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
     * Resets calories and step frequency fields.
     * @param isEnabled is enabled of both input parameters are selected.
     */
    private void resetStaticResultParameters(boolean isEnabled) {
        // calories row
        caloriesLabelTextView.setEnabled(isEnabled);
        caloriesValueTextView.setText("-");
        caloriesValueTextView.setEnabled(isEnabled);
        caloriesUnitTextView.setEnabled(isEnabled);
        // step frequency row
        stepFrequencyLabelTextView.setEnabled(isEnabled);
        stepFrequencyValueTextView.setText("-");
        stepFrequencyValueTextView.setEnabled(isEnabled);
        stepFrequencyUnitTextView.setEnabled(isEnabled);
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
        stepFrequencyLabelTextView = runView.findViewById(R.id.stepFrequencyLabelTextView);
        stepFrequencyValueTextView = runView.findViewById(R.id.stepFrequencyValueTextView);
        stepFrequencyUnitTextView = runView.findViewById(R.id.stepFrequencyUnitTextView);
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

    /**
     * Shows dialog, to load run from favorite list.
     */
    private void showOpenFavoriteDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Favorit laden");
        ArrayAdapter arrayAdapter = new ArrayAdapter<Run>(
                getContext(),
                android.R.layout.select_dialog_item, // Layout
                favoriteRuns
        );
        builder.setSingleChoiceItems(arrayAdapter,
                favoriteRuns.indexOf(currentRun),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        try {
                            // Get the alert dialog selected item's text
                            currentRun = favoriteRuns.get(i);
//                            updateRunOnGui();
//                            checkFavoriteButton();
                            settings.setRun(currentRun);
                            dialog.cancel();
                        } catch (CustomException ex) {
                            Log.e(ex.getTitle(), ex.getMessage());
                            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }
}
