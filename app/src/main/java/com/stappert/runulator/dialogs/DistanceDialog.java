package com.stappert.runulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.stappert.runulator.R;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.Unit;
import com.stappert.runulator.utils.ValueChangeListener;

public class DistanceDialog extends AppCompatDialogFragment {

    /**
     * Text field to set value.
     */
    private EditText distanceEditText;

    /**
     * Text view to show unit.
     */
    private TextView distanceUnitTextView;

    /**
     * Button to set half marathon.
     */
    private Button halfMarathonButton;

    /**
     * Button to set marathon.
     */
    private Button marathonButton;

    /**
     * Listener, to transmit value and selected unit.
     */
    private ValueChangeListener listener;

    /**
     * Selected value.
     */
    private final String distance;

    /**
     * Creates the distance dialog.
     *
     * @param distance distance
     * @param listener listener to apply value
     */
    public DistanceDialog(String distance, ValueChangeListener listener) {
        this.distance = distance;
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
        View view = inflater.inflate(R.layout.dialog_distance, null);
        builder.setView(view)
                .setTitle(getString(R.string.distance))
                .setMessage(getString(R.string.enter_distance))
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
        listener.applyValue(ParameterType.DISTANCE, distanceEditText.getText().toString(), null);
    }

    /**
     * Initializes dialog.
     *
     * @param view view, to access elements
     */
    private void initElements(View view) {
        SettingsManager settings = SettingsManager.getInstance();
        // get view elements
        distanceEditText = view.findViewById(R.id.distanceEditText);
        distanceUnitTextView = view.findViewById(R.id.distanceUnitTextView);
        halfMarathonButton = view.findViewById(R.id.halfMarathonButton);
        marathonButton = view.findViewById(R.id.marathonButton);
        // set values
        distanceEditText.setText(distance);
        distanceUnitTextView.setText(settings.getDistanceUnit().toString());
        // set focus display keyboard
        distanceEditText.requestFocus();
        distanceEditText.post(new Runnable() {
            public void run() {
                distanceEditText.requestFocusFromTouch();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(distanceEditText, 0);
            }
        });
        // set listener
        halfMarathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                distanceEditText.setText("" + Run.HALF_MARATHON);
                applyValue();
                getDialog().dismiss();
            }
        });
        marathonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                distanceEditText.setText("" + Run.MARATHON);
                applyValue();
                getDialog().dismiss();
            }
        });
    }
}
