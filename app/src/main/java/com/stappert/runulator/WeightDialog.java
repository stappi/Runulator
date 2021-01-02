package com.stappert.runulator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;

public class WeightDialog extends AppCompatDialogFragment {

    /**
     * Text field to set weight.
     */
    private EditText weightEditText;

    /**
     * Spinner to select unit for weight.
     */
    private Spinner unitSpinner;

    /**
     * Listener, to transmit weight and selected unit.
     */
    private WeightDialogListener listener;

    /**
     * Creates dialog, to set weight.
     *
     * @param savedInstanceState saved instance state
     * @return dialog
     */
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_weight, null);
        builder.setView(view)
                .setTitle(getContext().getString(R.string.weight))
                .setMessage(getContext().getString(R.string.weight_msg))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int weight = Integer.parseInt(weightEditText.getText().toString());
                            WeightUnit unit = (WeightUnit) unitSpinner.getSelectedItem();
                            listener.applyWeight(weight, unit);
                        } catch (Exception ex) {
                            Log.e("error", ex.getMessage());
                            Toast.makeText(getActivity(), getString(R.string.weight_fail), Toast.LENGTH_LONG);
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
            listener = (WeightDialogListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement "
                    + WeightDialogListener.class.getName());
        }
    }

    /**
     * Defines weight dialog listener.
     */
    public interface WeightDialogListener {
        void applyWeight(int weight, WeightUnit unit);
    }

    private void initElements(View view) {
        SettingsManager settings = SettingsManager.getInstance();
        // text field to enter weihgt
        weightEditText = view.findViewById(R.id.weightEditTextNumber);
        weightEditText.setText("" + settings.getWeight());
        // spinner, to select desired unit for weight
        unitSpinner = (Spinner) view.findViewById(R.id.weightUnitSpinner);
        ArrayAdapter<WeightUnit> weightUnitAdapter = new ArrayAdapter<>(
                this.getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(Arrays.asList(WeightUnit.values())));
        weightUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(weightUnitAdapter);
        unitSpinner.setSelection(settings.getWeightUnit().ordinal());
    }
}
