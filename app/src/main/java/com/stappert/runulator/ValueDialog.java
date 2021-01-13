package com.stappert.runulator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.List;

public class ValueDialog extends AppCompatDialogFragment {

    /**
     * Text field to set value.
     */
    private ValueType valueType;

    /**
     * Text field to set value.
     */
    private EditText valueEditText;

    /**
     * Spinner to select unit for value.
     */
    private Spinner unitSpinner;

    /**
     * Listener, to transmit value and selected unit.
     */
    private ValueDialogListener listener;

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
    private final Number selectedValue;

    /**
     * Selected unit.
     */
    private final int inputType;

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
    public ValueDialog(ValueType valueType, String title, String message,
                       Number selectedValue, Unit selectedUnit, List<Unit> units, int inputType) {
        this.valueType = valueType;
        this.title = title;
        this.message = message;
        this.selectedValue = selectedValue;
        this.selectedUnit = selectedUnit;
        this.units = units;
        this.inputType = inputType;
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
                            listener.applyValue(valueType, value, unit);
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
     * Attaches listener to dialog.
     *
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ValueDialogListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement "
                    + ValueDialogListener.class.getName());
        }
    }

    /**
     * Defines value dialog listener.
     */
    public interface ValueDialogListener {
        void applyValue(ValueType valueType, Number value, Unit unit);
    }

    public enum ValueType {
        WEIGHT, HEIGHT
    }

    private void initElements(View view) {
        SettingsManager settings = SettingsManager.getInstance();
        // text field to enter value
        valueEditText = view.findViewById(R.id.valueEditTextNumber);
        valueEditText.setText(selectedValue.toString());
        valueEditText.setInputType(inputType);
        // spinner, to select desired unit for value
        unitSpinner = (Spinner) view.findViewById(R.id.unitSpinner);
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(
                this.getContext(), android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(units.indexOf(selectedUnit));
    }
}
