package com.stappert.runulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.stappert.runulator.R;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.entities.Run;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.ValueChangeListener;

public class TimeDialog extends AppCompatDialogFragment {

    /**
     * Number picker to set hours.
     */
    private ParameterType inputParameterType;

    /**
     * Number picker to set hours.
     */
    private NumberPicker hoursPicker;

    /**
     * Number picker to set minutes.
     */
    private NumberPicker minutesPicker;

    /**
     * Number picker to set seconds.
     */
    private NumberPicker secondsPicker;

    /**
     * True, if with hours, minutes and seconds or false, with minutes and seconds.
     */
    private boolean withHours;

    /**
     * Title of dialog.
     */
    private String title;

    /**
     * Message of dialog.
     */
    private String message;

    /**
     * Listener, to transmit value and selected unit.
     */
    private ValueChangeListener listener;

    /**
     * Selected value.
     */
    private final int seconds;

    /**
     * Creates the time dialog.
     *
     * @param parameterType input parameter type
     * @param withHours     true, if with hours, minutes and seconds or false, with minutes and seconds
     * @param title         title of dialog
     * @param message       message of dialog
     * @param time          time in format hh:min:sec
     * @param listener      listener to apply value
     */
    public TimeDialog(ParameterType parameterType, boolean withHours, String title, String message,
                      String time, ValueChangeListener listener) throws CustomException {
        this.inputParameterType = parameterType;
        this.withHours = withHours;
        this.title = title;
        this.message = message;
        this.seconds = Run.parseTimeInSeconds(time);
        this.listener = listener;
    }

    /**
     * Creates dialog, to set value.
     *
     * @param savedInstanceState saved instance state
     * @return dialog
     */
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_time, null);
        builder.setView(view)
                .setTitle(title)
                .setMessage(message)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        //If the key event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            applyValue();
                            return true;
                        }
                        return false;
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            applyValue();
                        } catch (Exception ex) {
                            Log.e("error", ex.getMessage());
                            Toast.makeText(getActivity(), getString(R.string.enter_distance_fail), Toast.LENGTH_LONG);
                        }
                    }
                });
        initElements(view);
        return builder.create();
    }

    /**
     * Applies the value.
     */
    private void applyValue() {
        listener.applyValue(inputParameterType, getTime(), null);
    }

    /**
     * Converts the set time number pickers to one time.
     *
     * @return time
     */
    private String getTime() {
        final int hours = withHours ? hoursPicker.getValue() : 0;
        final int minutes = minutesPicker.getValue();
        final int seconds = secondsPicker.getValue();
        return (hours == 0 ? "" : hours + ":")
                + (minutes > 9 ? "" : "0") + minutes + ":"
                + (seconds > 9 ? "" : "0") + seconds;
    }

    /**
     * Initializes dialog.
     *
     * @param view view, to access elements
     */
    private void initElements(View view) {
        // hours
        hoursPicker = view.findViewById(R.id.hoursPicker);
        if (withHours) {
            hoursPicker.setMaxValue(23);
            hoursPicker.setValue(seconds / Unit.HOUR_IN_SECONDS);
            hoursPicker.setMinValue(0);
        } else {
            ((LinearLayout) view.findViewById(R.id.timePickerLinearLayout)).removeView(hoursPicker);
        }
        // minutes
        minutesPicker = view.findViewById(R.id.minutesPicker);
        minutesPicker.setMaxValue(59);
        minutesPicker.setValue(seconds % Unit.HOUR_IN_SECONDS / Unit.MINUTE_IN_SECONDS);
        minutesPicker.setMinValue(0);
        if (withHours) {
            minutesPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int value) {
                    return String.format("%02d", value);
                }
            });
        }
        // seconds
        secondsPicker = view.findViewById(R.id.secondsPicker);
        secondsPicker.setMaxValue(59);
        secondsPicker.setValue(seconds % Unit.HOUR_IN_SECONDS % Unit.MINUTE_IN_SECONDS);
        secondsPicker.setMinValue(0);
        secondsPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });
    }
}
