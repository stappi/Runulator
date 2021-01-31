package com.stappert.runulator.activities;

import android.content.DialogInterface;
import android.graphics.Typeface;
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
import com.stappert.runulator.dialogs.ValueDialog;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.Utils;
import com.stappert.runulator.utils.ValueChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Elements
    private View runView;
    private TableLayout runInputArea;
    private TextView distanceButton;
    private TextView durationButton;
    private TextView paceButton;
    private TextView speedButton;
    private TextView runParamIntputInfoTextView;
    private TextView runParameter1LabelTextView;
    private EditText runParameter1EditText;
    private TextView runParameter1UnitTextView;
    private TextView runParameter2LabelTextView;
    private EditText runParameter2EditText;
    private TextView runParameter2UnitTextView;
    private TableLayout runResultArea;
    private TextView caloriesTextView;
    private TextView stepFrequencyTextView;

//    /**
//     * Map, to handle parameter input to right parameter.
//     */
//    private final Map<ParameterType, EditText> inputParameter = new HashMap<>();

    /**
     * Map, to handle output result to right parameter.
     */
    private final Map<ParameterType, TextView> runResultParameter = new HashMap<>();

    /**
     * Type for run parameter 1, which can be distance or duration.
     */
    private ParameterType parameterType1;

    /**
     * Type for run parameter 2, which can be duration, pace or speed.
     */
    private ParameterType parameterType2;

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
            calculateAndUpdateRun();
            updatePillButtons();
            updateInputArea();
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
        if (!runParameter1EditText.isEnabled() || !runParameter2EditText.isEnabled()) {
            // do nothing
        } else if (runParameter1EditText.getText().toString().isEmpty()
                || runParameter2EditText.getText().toString().isEmpty()) {
            runParamIntputInfoTextView.setText(getString(R.string.input_info_enter_values));
        } else {
            runParamIntputInfoTextView.setText("");
            Number runValue1 = getRunParameterValue(parameterType1, runParameter1EditText);
            Number runValue2 = getRunParameterValue(parameterType2, runParameter2EditText);
            // run parameter 1 is distance or duration
            if (ParameterType.DISTANCE.equals(parameterType1)) {
                // run parameter 2 is duration, pace or speed
                switch (parameterType2) {
                    case DURATION:
                        currentRun = Run.createWithDistanceAndDuration(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(ParameterType.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
                        runResultParameter.get(ParameterType.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case PACE:
                        currentRun = Run.createWithDistanceAndPace(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(ParameterType.DURATION).setText(currentRun.getDuration());
                        runResultParameter.get(ParameterType.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDistanceAndSpeed(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(ParameterType.DURATION).setText(currentRun.getDuration());
                        runResultParameter.get(ParameterType.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
                        break;
                }
            } else {
                // run parameter 2 is pace or speed
                switch (parameterType2) {
                    case PACE:
                        currentRun = Run.createWithDurationAndPace(runValue1.intValue(), runValue2.intValue());
                        runResultParameter.get(ParameterType.DISTANCE).setText(currentRun.getDistance(settings.getDistanceUnit()));
                        runResultParameter.get(ParameterType.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDurationAndSpeed(runValue1.intValue(), runValue2.floatValue());
                        runResultParameter.get(ParameterType.DISTANCE).setText(currentRun.getDistance(settings.getDistanceUnit()));
                        runResultParameter.get(ParameterType.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
                        break;
                }
            }
            caloriesTextView.setText(currentRun.calculateCalories(settings.getWeightInKg()));
            stepFrequencyTextView.setText("" + currentRun.calculateStepFrequency(settings.getHeightInCm()));
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
     * Applies run parameter 1. Run parameter 1 can be distance or duration.
     *
     * @param parameterType parameter type
     * @return if run parameter is selected or not
     */
    private boolean applyRunParameter1(ParameterType parameterType) {
        final boolean isSelected = !parameterType.equals(parameterType1);
        if (isSelected) {
            parameterType2 = parameterType1 != null ? parameterType1 : parameterType2;
            parameterType1 = parameterType;
        } else {
            parameterType1 = ParameterType.DURATION.equals(parameterType2) ? ParameterType.DURATION : null;
            parameterType2 = ParameterType.DURATION.equals(parameterType2) ? null : parameterType2;
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
        final boolean isSelected = !parameterType.equals(parameterType2);
        parameterType2 = isSelected ? parameterType : null;
        return isSelected;
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
        final boolean notAllParamSet = getNumberOfSetRunParameters() < 2;
        distanceButton.setEnabled(ParameterType.DISTANCE.equals(parameterType1) || notAllParamSet);
        durationButton.setEnabled(ParameterType.DURATION.equals(parameterType1) || ParameterType.DURATION.equals(parameterType2) || notAllParamSet);
        paceButton.setEnabled((notAllParamSet || ParameterType.PACE.equals(parameterType2)) && !ParameterType.SPEED.equals(parameterType2));
        speedButton.setEnabled((notAllParamSet || ParameterType.SPEED.equals(parameterType2)) && !ParameterType.PACE.equals(parameterType2));
    }

    /**
     * Updates the input area depending on selected run parameters.
     */
    private void updateInputArea() {
        final int numberOfSetRunParameters = getNumberOfSetRunParameters();
        runParamIntputInfoTextView.setText(
                numberOfSetRunParameters == 0 ? R.string.input_info_select_please_two
                        : numberOfSetRunParameters == 1 ? R.string.input_info_select_please_one
                        : numberOfSetRunParameters == 2 ? R.string.input_info_enter_values
                        : R.string.input_info_enter_values);
        updateInputParameter();
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
        if (ParameterType.DISTANCE.equals(parameterType1)) {
            updateInputParameter1(true, getString(R.string.distance), INPUT_TYPE_NUMBER,
                    settings.getDistanceUnit().getLabel(getContext()));
        } else if (ParameterType.DURATION.equals(parameterType1)) {
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
        if (ParameterType.DURATION.equals(parameterType2)) {
            updateInputParameter2(true, getString(R.string.run_time), INPUT_TYPE_TIME,
                    settings.getDurationUnit().getLabel(getContext()));
        } else if (ParameterType.PACE.equals(parameterType2)) {
            updateInputParameter2(true, getString(R.string.pace), INPUT_TYPE_TIME,
                    settings.getPaceUnit().getLabel(getContext()));
        } else if (ParameterType.SPEED.equals(parameterType2)) {
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
        updateInputParameter(runParameter1LabelTextView, runParameter1EditText, runParameter1UnitTextView,
                isEnabled, label, inputType, unit);
        // add dialogs
        if ((ParameterType.DISTANCE.equals(parameterType1) || ParameterType.SPEED.equals(parameterType1)) && settings.isDialogInput()) {
            createInputParameterListeners(runParameter1EditText, parameterType1);
        } else {
            runParameter1EditText.setFocusable(true);
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
        updateInputParameter(runParameter2LabelTextView, runParameter2EditText, runParameter2UnitTextView,
                isEnabled, label, inputType, unit);
        // add dialogs
        if ((ParameterType.DISTANCE.equals(parameterType2) || ParameterType.SPEED.equals(parameterType2)) && settings.isDialogInput()) {
            createInputParameterListeners(runParameter2EditText, parameterType2);
        } else {
            runParameter2EditText.setFocusable(true);
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
     * Applys the set run parameter value.
     *
     * @param value value
     */
    @Override
    public void applyValue(ParameterType parameter, Object value, Unit unit) {
        if (parameter.equals(parameterType1)) {
            runParameter1EditText.setText("" + value);
        } else if (parameter.equals(parameterType2)) {
            runParameter2EditText.setText("" + value);
        }
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
                    case SPEED:
                        new ValueDialog(ParameterType.SPEED, getContext().getString(R.string.speed),
                                getContext().getString(R.string.weight_msg),
                                value, settings.getSpeedUnit(), null,
                                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
                                TabRun.this
                        ).show(TabRun.this.getChildFragmentManager(), getContext().getString(R.string.weight));
                }
            }
        });
    }

    /**
     * Returns the number of set run parameters.
     *
     * @return 2 if both parameters set, 1 if one parameter is set, 0 if no parameter is set
     */
    private int getNumberOfSetRunParameters() {
        return (parameterType1 != null ? 1 : 0) + (parameterType2 != null ? 1 : 0);
    }

    /**
     * Initializes the run result.
     */
    private void initRunResult() {

        // clear temporary elements
        clearRunResult();

        // reset calories and step frequency
        caloriesTextView.setText("-");
        stepFrequencyTextView.setText("-");

        // show temporary input elements
        final int noOfSetRunParameters = getNumberOfSetRunParameters();

        TableLayout.LayoutParams layoutTableRow = new TableLayout.LayoutParams(LAYOUT_TABLE_ROW);
        layoutTableRow.setMargins(0, Utils.dp2Pixel(getContext(), 5), 0, 0);

        if (noOfSetRunParameters == 2) {
            final ParameterType outputParameter1 = defineOuputRunParameter1();
            final ParameterType outputParameter2 = defineOuputRunParameter2(outputParameter1);
            runResultArea.addView(getOutputParameter(outputParameter1), layoutTableRow);
            runResultArea.addView(getOutputParameter(outputParameter2), layoutTableRow);
        } else if (noOfSetRunParameters == 1 && ParameterType.PACE.equals(parameterType2)) {
            runResultArea.addView(getOutputParameter(ParameterType.SPEED), layoutTableRow);
        } else if (noOfSetRunParameters == 1 && ParameterType.SPEED.equals(parameterType2)) {
            runResultArea.addView(getOutputParameter(ParameterType.PACE), layoutTableRow);
        }
    }

    /**
     * Removes temporary run result fields.
     */
    private void clearRunResult() {
        runResultParameter.clear();
        final int noOfOutputAreaElements = runResultArea.getChildCount();
        for (int i = noOfOutputAreaElements - 1; i >= 0; i--) {
            View element = runResultArea.getChildAt(i);
            if (element != null && element.getId() != R.id.caloriesTableRow && element.getId() != R.id.stepFrequencyTableRow) {
                runResultArea.removeView(element);
            }
        }
    }

    /**
     * Defines the output run parameter 1, which can be distance, duration or pace.
     *
     * @return run parameter 1 for output
     */
    private ParameterType defineOuputRunParameter1() {
        return !ParameterType.DISTANCE.equals(parameterType1)
                ? ParameterType.DISTANCE
                : !ParameterType.DURATION.equals(parameterType1) && !ParameterType.DURATION.equals(parameterType2)
                ? ParameterType.DURATION
                : !ParameterType.PACE.equals(parameterType2)
                ? ParameterType.PACE : ParameterType.SPEED;
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
                && !ParameterType.DURATION.equals(parameterType1) && !ParameterType.DURATION.equals(parameterType2)
                ? ParameterType.DURATION
                : !ParameterType.PACE.equals(outputParameterType1) && !ParameterType.PACE.equals(parameterType2)
                ? ParameterType.PACE : ParameterType.SPEED;
    }

    /**
     * Creates a output parameter.
     *
     * @param parameter run parameter for output
     * @return row for output parameter
     */
    private TableRow getOutputParameter(ParameterType parameter) {
        // create label
        TextView labelTextView = new TextView(getContext());
        labelTextView.setText(parameter.getLabel());
        labelTextView.setLayoutParams(LAYOUT_LABEL);
        labelTextView.setTypeface(null, Typeface.BOLD);
        // create result
        TextView outputTextView = new TextView(getContext());
        outputTextView.setText("-");
        outputTextView.setLayoutParams(LAYOUT_OUTPUT_VALUE);
        outputTextView.setPadding(0, 0, Utils.dp2Pixel(getContext(), 5), 0);
        outputTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        runResultParameter.put(parameter, outputTextView);
        // create unit
        TextView unitTextView = new TextView(getContext());
        unitTextView.setText(parameter.getUnit(settings).toString());
        unitTextView.setLayoutParams(LAYOUT_OUTPUT_UNIT);
        // create row
        TableRow outputRow = new TableRow(getContext());
        outputRow.addView(labelTextView);
        outputRow.addView(outputTextView);
        outputRow.addView(unitTextView);
        return outputRow;
    }

    /**
     * Selects or disable
     */
    private void updatePillButtons() {
        selectRunParameterButton(distanceButton, ParameterType.DISTANCE.equals(parameterType1));
        selectRunParameterButton(durationButton,
                ParameterType.DURATION.equals(parameterType1) || ParameterType.DURATION.equals(parameterType2));
        selectRunParameterButton(paceButton, ParameterType.PACE.equals(parameterType2));
        selectRunParameterButton(speedButton, ParameterType.SPEED.equals(parameterType2));
        enableButtons();
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
    // Initialize
    // =============================================================================================

    /**
     * Initializes gui elements.
     */
    private void initElements() {
        // input area with pill buttons
        runInputArea = runView.findViewById(R.id.runParamIntputTableLayout);
        distanceButton = runView.findViewById(R.id.distanceButton);
        durationButton = runView.findViewById(R.id.durationButton);
        paceButton = runView.findViewById(R.id.paceButton);
        speedButton = runView.findViewById(R.id.speedButton);
        runParamIntputInfoTextView = runView.findViewById(R.id.runParamIntputInfoTextView);
        runParameter1LabelTextView = runView.findViewById(R.id.runParameter1LabelTextView);
        runParameter1EditText = runView.findViewById(R.id.runParameter1EditText);
        runParameter1UnitTextView = runView.findViewById(R.id.runParameter1UnitTextView);
        runParameter2LabelTextView = runView.findViewById(R.id.runParameter2LabelTextView);
        runParameter2EditText = runView.findViewById(R.id.runParameter2EditText);
        runParameter2UnitTextView = runView.findViewById(R.id.runParameter2UnitTextView);
        // output parameters
        runResultArea = runView.findViewById(R.id.runOutputTableLayout);
        caloriesTextView = runView.findViewById(R.id.caloriesValueTextView);
        stepFrequencyTextView = runView.findViewById(R.id.stepFrequencyValueTextView);
    }

    /**
     * Initializes listeners for elements.
     */
    private void initListener() {
        addOnClickListenerToRunParameterButtons(distanceButton);
        addOnClickListenerToRunParameterButtons(durationButton);
        addOnClickListenerToRunParameterButtons(paceButton);
        addOnClickListenerToRunParameterButtons(speedButton);
        addTextWatcherToEditText(runParameter1EditText);
        addTextWatcherToEditText(runParameter2EditText);
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
                        selectRunParameterButton(durationButton, ParameterType.DISTANCE.equals(parameterType1)
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
                initRunResult();
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
