package com.stappert.runulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.stappert.runulator.R;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.ValueChangeListener;

import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;

public class ValueDialog extends AppCompatDialogFragment {

    /**
     * Text field to set value.
     */
    private ParameterType inputParameterType;

    /**
     * Text field to set value.
     */
    private EditText valueEditText;

    /**
     * Spinner to select unit for value.
     */
    private Spinner unitSpinner;

    /**
     * TextView, to show unit, of only on unit exists.
     */
    private TextView unitTextView;

    /**
     * Listener, to transmit value and selected unit.
     */
    private ValueChangeListener listener;

    /**
     * Title of dialog.
     */
    private final String title;

    /**
     * Selected unit.
     */
    private final Unit selectedUnit;

    /**
     * Message of dialog.
     */
    private final String message;

    /**
     * Selected value.
     */
    private final String selectedValue;

    /**
     * Selected unit.
     */
    private final int textInputType;

    /**
     * Available units.
     */
    private final List<Unit> units;

    /**
     * Creates the number dialog.
     *
     * @param title   title
     * @param message message
     */
    public ValueDialog(ParameterType inputParameterType, String title, String message,
                       String selectedValue, Unit selectedUnit, List<Unit> units, int textInputType,
                       ValueChangeListener listener) {
        this.inputParameterType = inputParameterType;
        this.title = title;
        this.message = message;
        this.selectedValue = selectedValue;
        this.selectedUnit = selectedUnit;
        this.units = units;
        this.textInputType = textInputType;
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
        View view = inflater.inflate(R.layout.dialog_value, null);
        builder.setView(view)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Number value = Float.parseFloat(valueEditText.getText().toString());
                            Unit unit = (Unit) unitSpinner.getSelectedItem();
                            listener.applyValue(inputParameterType, value, unit);
                        } catch (Exception ex) {
                            Log.e("error", ex.getMessage());
                            Toast.makeText(getActivity(), getString(R.string.enter_value_fail), Toast.LENGTH_LONG);
                        }
                    }
                });
        initElements(view);
        return builder.create();
    }

    /**
     * Initializes dialog.
     *
     * @param view view, to access elements
     */
    private void initElements(View view) {
        SettingsManager settings = SettingsManager.getInstance();
        // text field to enter value
        valueEditText = view.findViewById(R.id.valueEditTextNumber);
        valueEditText.setText(selectedValue);
        valueEditText.setInputType(textInputType);
        valueEditText.requestFocus();
        valueEditText.post(new Runnable() {
            public void run() {
                valueEditText.requestFocusFromTouch();
                InputMethodManager inputMethodManager =
                        (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(valueEditText, 0);
            }
        });
        // spinner, to select desired unit for value
        unitSpinner = view.findViewById(R.id.unitSpinner);
        unitTextView = view.findViewById(R.id.unitTextView);
        if (units != null && units.size() > 1) {
            ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(
                    this.getContext(), android.R.layout.simple_spinner_item, units);
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(unitAdapter);
            unitSpinner.setSelection(units.indexOf(selectedUnit));
        } else {
            unitSpinner.setVisibility(View.INVISIBLE);
            unitSpinner.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            unitTextView.setVisibility(View.VISIBLE);
            unitTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            unitTextView.setText(selectedUnit.toString());
        }
    }
}
