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
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.Utils;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Organizes the run view of the application.
 */
public class TabRun extends Fragment {

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

    // Elements
    private View runView;
    private TableLayout runInputArea;
    private TextView distanceButton;
    private TextView durationButton;
    private TextView paceButton;
    private TextView speedButton;
    private TextView runParamIntputInfoTextView;
    private TableLayout runResultArea;
    private TextView caloriesTextView;
    private TextView stepFrequencyTextView;

    /**
     * Map, to handle parameter input to right parameter.
     */
    private final Map<RunParameter, EditText> inputParameter = new HashMap<>();

    /**
     * Map, to handle output result to right parameter.
     */
    private final Map<RunParameter, TextView> runResultParameter = new HashMap<>();

    /**
     * Type for run parameter 1, which can be distance or duration.
     */
    private RunParameter runParameter1;

    /**
     * Type for run parameter 2, which can be duration, pace or speed.
     */
    private RunParameter runParameter2;

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
        if (!inputParameter.containsKey(runParameter1) || !inputParameter.containsKey(runParameter2)) {
            // do nothing
        } else if (inputParameter.get(runParameter1).getText().toString().isEmpty()
                || inputParameter.get(runParameter2).getText().toString().isEmpty()) {
            runParamIntputInfoTextView.setVisibility(View.VISIBLE);
        } else {
            runParamIntputInfoTextView.setVisibility(View.INVISIBLE);
            Number runValue1 = getRunParameterValue(runParameter1, inputParameter.get(runParameter1));
            Number runValue2 = getRunParameterValue(runParameter2, inputParameter.get(runParameter2));
            // run parameter 1 is distance or duration
            if (RunParameter.DISTANCE.equals(runParameter1)) {
                // run parameter 2 is duration, pace or speed
                switch (runParameter2) {
                    case DURATION:
                        currentRun = Run.createWithDistanceAndDuration(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(RunParameter.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
                        runResultParameter.get(RunParameter.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case PACE:
                        currentRun = Run.createWithDistanceAndPace(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(RunParameter.DURATION).setText(currentRun.getDuration());
                        runResultParameter.get(RunParameter.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDistanceAndSpeed(runValue1.floatValue(), runValue2.intValue());
                        runResultParameter.get(RunParameter.DURATION).setText(currentRun.getDuration());
                        runResultParameter.get(RunParameter.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
                        break;
                }
            } else {
                // run parameter 2 is pace or speed
                switch (runParameter2) {
                    case PACE:
                        currentRun = Run.createWithDurationAndPace(runValue1.intValue(), runValue2.intValue());
                        runResultParameter.get(RunParameter.DISTANCE).setText(currentRun.getDistance(settings.getDistanceUnit()));
                        runResultParameter.get(RunParameter.SPEED).setText(currentRun.getSpeed(settings.getSpeedUnit()));
                        break;
                    case SPEED:
                        currentRun = Run.createWithDurationAndSpeed(runValue1.intValue(), runValue2.floatValue());
                        runResultParameter.get(RunParameter.DISTANCE).setText(currentRun.getDistance(settings.getDistanceUnit()));
                        runResultParameter.get(RunParameter.PACE).setText(currentRun.getPace(settings.getPaceUnit()));
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
    private Number getRunParameterValue(RunParameter type, EditText textField) throws CustomException {
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
    private boolean applyRunParameter1(RunParameter parameterType) {
        final boolean isSelected = !parameterType.equals(runParameter1);
        if (isSelected) {
            runParameter2 = runParameter1 != null ? runParameter1 : runParameter2;
            runParameter1 = parameterType;
        } else {
            runParameter1 = RunParameter.DURATION.equals(runParameter2) ? RunParameter.DURATION : null;
            runParameter2 = RunParameter.DURATION.equals(runParameter2) ? null : runParameter2;
            inputParameter.remove(parameterType);
        }
        return isSelected;
    }

    /**
     * Applies run parameter 2. Run parameter 2 can be duration, speed or pace.
     *
     * @param parameterType parameter type
     * @return if run parameter is selected or not
     */
    private boolean applyRunParameter2(RunParameter parameterType) {
        final boolean isSelected = !parameterType.equals(runParameter2);
        if (isSelected) {
            runParameter2 = parameterType;
        } else {
            runParameter2 = null;
            inputParameter.remove(parameterType);
        }
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
        distanceButton.setEnabled(RunParameter.DISTANCE.equals(runParameter1) || notAllParamSet);
        durationButton.setEnabled(RunParameter.DURATION.equals(runParameter1) || RunParameter.DURATION.equals(runParameter2) || notAllParamSet);
        paceButton.setEnabled((notAllParamSet || RunParameter.PACE.equals(runParameter2)) && !RunParameter.SPEED.equals(runParameter2));
        speedButton.setEnabled((notAllParamSet || RunParameter.SPEED.equals(runParameter2)) && !RunParameter.PACE.equals(runParameter2));
    }

    /**
     * Updates the input area depending on selected run parameters.
     */
    private void updateInputArea() {
        runInputArea.removeAllViews();
        final int numberOfSetRunParameters = getNumberOfSetRunParameters();
        runParamIntputInfoTextView.setVisibility(View.VISIBLE);
        if (numberOfSetRunParameters == 0) {
            runParamIntputInfoTextView.setText(R.string.input_info_select_please_two);
        } else if (numberOfSetRunParameters == 1) {
            runParamIntputInfoTextView.setText(R.string.input_info_select_please_one);
            addParameterToInputArea(runParameter1 != null ? runParameter1 : runParameter2);
        } else if (numberOfSetRunParameters == 2) {
            runParamIntputInfoTextView.setText(R.string.input_info_enter_values);
            addParameterToInputArea(runParameter1);
            addParameterToInputArea(runParameter2);
        }
    }

    /**
     * Adds a parameter input field to the gui.
     *
     * @param runParameter run parameter
     */
    private void addParameterToInputArea(final RunParameter runParameter) {
        TextView label = new TextView(getContext());
        label.setLayoutParams(LAYOUT_LABEL);
        label.setTypeface(null, Typeface.BOLD);
        final EditText input = new EditText(getContext());
        input.setText(inputParameter.containsKey(runParameter)
                ? inputParameter.get(runParameter).getText().toString() : "");
        input.setLayoutParams(LAYOUT_INPUT_VALUE_UNIT);
        input.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

//        if (RunParameterType.DURATION.equals(runParameterType)) {
//            input.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // TODO Auto-generated method stub
//                    Calendar mcurrentTime = Calendar.getInstance();
//                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//                    int minute = mcurrentTime.get(Calendar.MINUTE);
//                    TimePickerDialog mTimePicker;
//                    mTimePicker = new TimePickerDialog(TabRun.this.getContext(), new TimePickerDialog.OnTimeSetListener() {
//                        @Override
//                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                            inputParameter.get(runParameterType).setText(hourOfDay + ":" + minute);
//                        }
//                    }, hour, minute, true);//Yes 24 hour time
//                    mTimePicker.setTitle("Select Time");
//                    mTimePicker.show();
//                }
//            });
//        }

        TextView emptyCell = new TextView(getContext());
        emptyCell.setLayoutParams(LAYOUT_LABEL);
        TextView unit = new TextView(getContext());
        unit.setLayoutParams(LAYOUT_INPUT_VALUE_UNIT);
        unit.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        inputParameter.put(runParameter, input);
        addTextWatcherToEditText(input);
        switch (runParameter) {
            case DISTANCE:
                label.setText(R.string.distance);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                unit.setText(settings.getDistanceUnit().getLabel(this.getContext()));
                break;
            case DURATION:
                label.setText(R.string.run_time);
                input.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                unit.setText(settings.getDurationUnit().getLabel(this.getContext()));
                break;
            case PACE:
                label.setText(R.string.pace);
                input.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                unit.setText(settings.getPaceUnit().getLabel(this.getContext()));
                break;
            case SPEED:
                label.setText(R.string.speed);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                unit.setText(settings.getSpeedUnit().getLabel(this.getContext()));
                break;
        }
        TableRow inputRow = new TableRow(getContext());
        inputRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        inputRow.addView(label);
        inputRow.addView(input);
        runInputArea.addView(inputRow, LAYOUT_TABLE_ROW);
        TableRow unitRow = new TableRow(getContext());
        unitRow.addView(emptyCell);
        unitRow.addView(unit);
        unitRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        runInputArea.addView(unitRow, LAYOUT_TABLE_ROW);
    }

    /**
     * Returns the number of set run parameters.
     *
     * @return 2 if both parameters set, 1 if one parameter is set, 0 if no parameter is set
     */
    private int getNumberOfSetRunParameters() {
        return (runParameter1 != null ? 1 : 0) + (runParameter2 != null ? 1 : 0);
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
            final RunParameter outputParameter1 = defineOuputRunParameter1();
            final RunParameter outputParameter2 = defineOuputRunParameter2(outputParameter1);
            runResultArea.addView(getOutputParameter(outputParameter1), layoutTableRow);
            runResultArea.addView(getOutputParameter(outputParameter2), layoutTableRow);
        } else if (noOfSetRunParameters == 1 && RunParameter.PACE.equals(runParameter2)) {
            runResultArea.addView(getOutputParameter(RunParameter.SPEED), layoutTableRow);
        } else if (noOfSetRunParameters == 1 && RunParameter.SPEED.equals(runParameter2)) {
            runResultArea.addView(getOutputParameter(RunParameter.PACE), layoutTableRow);
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
    private RunParameter defineOuputRunParameter1() {
        return !RunParameter.DISTANCE.equals(runParameter1)
                ? RunParameter.DISTANCE
                : !RunParameter.DURATION.equals(runParameter1) && !RunParameter.DURATION.equals(runParameter2)
                ? RunParameter.DURATION
                : !RunParameter.PACE.equals(runParameter2)
                ? RunParameter.PACE : RunParameter.SPEED;
    }

    /**
     * Defines the output run parameter 2 depending on run parameter 1. Parameter can be duration,
     * pace or speed.
     *
     * @param outputRunParameter1 run parameter 1 for output
     * @return run parameter 2 for output
     */
    private RunParameter defineOuputRunParameter2(RunParameter outputRunParameter1) {
        return !RunParameter.DURATION.equals(outputRunParameter1)
                && !RunParameter.DURATION.equals(runParameter1) && !RunParameter.DURATION.equals(runParameter2)
                ? RunParameter.DURATION
                : !RunParameter.PACE.equals(outputRunParameter1) && !RunParameter.PACE.equals(runParameter2)
                ? RunParameter.PACE : RunParameter.SPEED;
    }

    /**
     * Creates a output parameter.
     *
     * @param parameter run parameter for output
     * @return row for output parameter
     */
    private TableRow getOutputParameter(RunParameter parameter) {
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
        selectRunParameterButton(distanceButton, RunParameter.DISTANCE.equals(runParameter1));
        selectRunParameterButton(durationButton,
                RunParameter.DURATION.equals(runParameter1) || RunParameter.DURATION.equals(runParameter2));
        selectRunParameterButton(paceButton, RunParameter.PACE.equals(runParameter2));
        selectRunParameterButton(speedButton, RunParameter.SPEED.equals(runParameter2));
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
                        selectRunParameterButton(distanceButton, applyRunParameter1(RunParameter.DISTANCE));
                        break;
                    case R.id.durationButton:
                        selectRunParameterButton(durationButton, RunParameter.DISTANCE.equals(runParameter1)
                                ? applyRunParameter2(RunParameter.DURATION) : applyRunParameter1(RunParameter.DURATION));
                        break;
                    case R.id.paceButton:
                        selectRunParameterButton(paceButton, applyRunParameter2(RunParameter.PACE));
                        break;
                    case R.id.speedButton:
                        selectRunParameterButton(speedButton, applyRunParameter2(RunParameter.SPEED));
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

    /**
     * Run parameter types.
     */
    private enum RunParameter {
        DISTANCE, DURATION, PACE, SPEED;

        /**
         * Returns the label regarding to parameter type.
         *
         * @return label
         */
        private int getLabel() {
            switch (this) {
                case DISTANCE:
                    return R.string.distance;
                case DURATION:
                    return R.string.run_time;
                case PACE:
                    return R.string.pace;
                case SPEED:
                    return R.string.speed;
                default:
                    return R.string.unknown;
            }
        }

        /**
         * Returns the unit regarding to parameter type.
         *
         * @return unit
         */
        private Unit getUnit(SettingsManager settings) {
            switch (this) {
                case DISTANCE:
                    return settings.getDistanceUnit();
                case DURATION:
                    return settings.getDurationUnit();
                case PACE:
                    return settings.getPaceUnit();
                case SPEED:
                    return settings.getSpeedUnit();
                default:
                    return Unit.DEFAULT;
            }
        }
    }
}
