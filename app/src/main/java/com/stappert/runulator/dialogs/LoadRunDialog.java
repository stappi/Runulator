package com.stappert.runulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.stappert.runulator.R;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.ParameterType;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.RunLoadedListener;
import com.stappert.runulator.utils.SettingsManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoadRunDialog extends AppCompatDialogFragment {

    /**
     * Runs, contained in hash map.
     */
    private final Map<TextView, String> runs = new HashMap<>();

    /**
     * Listener, to transmit value and selected unit.
     */
    private RunLoadedListener listener;

    /**
     * Settings manager.
     */
    private SettingsManager settings;

    /**
     * Creates the distance dialog.
     *
     * @param listener to transmit value and selected unit
     */
    public LoadRunDialog(RunLoadedListener listener) {
        this.listener = listener;
        this.settings = SettingsManager.getInstance();
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
        View view = inflater.inflate(R.layout.dialog_load_run, null);
        builder.setView(view).setTitle(getString(R.string.load_run))/*.setMessage(getString(R.string.load_run_msg))*/;
        createList(view);
        return builder.create();
    }

    /**
     * Initializes dialog.
     *
     * @param view view, to access elements
     */
    private void createList(View view) {
        LinearLayout layout = view.findViewById(R.id.loadRunLayout);
        layout.addView(createSeparator());
        // create and assign text views to according runs in map
        for (String runJson : settings.getFavoriteRunsJson()) {
            final TextView runTextView = createRunOption(runJson);
            layout.addView(runTextView);
            layout.addView(createSeparator());
            runs.put(runTextView, runJson);
        }
    }

    /**
     * Creates run option.
     *
     * @param run run as json string
     * @return run option
     */
    private TextView createRunOption(String run) {
        final TextView runTextView = new TextView(getContext());
        runTextView.setText(convertRunJsonToLabel(run));
        LinearLayout.LayoutParams layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout.setMargins(75, 25, 25, 15);
        runTextView.setLayoutParams(layout);
        runTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View run) {
                if (runs.containsKey(run)) {
                    listener.applyRun(runs.get(run));
                    LoadRunDialog.this.dismiss();
                }
            }
        });
        return runTextView;
    }

    /**
     * Creates a separator.
     *
     * @return separator
     */
    private View createSeparator() {
        View separator = new View(getContext());
        int dividerHeight = (int) getResources().getDisplayMetrics().density * 1;
        separator.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));
        separator.setBackgroundColor(getResources().getColor(R.color.colorSeparator, getContext().getTheme()));
        return separator;
    }

    /**
     * Converts run json string to label.
     *
     * @param runJsonString run json string
     * @return run
     */
    private String convertRunJsonToLabel(String runJsonString) {
        String parameter1 = "";
        String preposition = "";
        String parameter2 = "";
        try {
            JSONObject runJson = new JSONObject(runJsonString);
            if (runJson.has(ParameterType.DISTANCE.name())) {
                parameter1 = settings.getDistanceUnit().format(runJson.getDouble(ParameterType.DISTANCE.name()));
                if (runJson.has(ParameterType.DURATION.name())) {
                    preposition = getString(R.string.in);
                    parameter2 = settings.getDurationUnit().format(runJson.getInt(ParameterType.DURATION.name()));
                } else if (runJson.has(ParameterType.PACE.name())) {
                    preposition = getString(R.string.with);
                    parameter2 = settings.getPaceUnit().format(runJson.getInt(ParameterType.PACE.name()));
                } else if (runJson.has(ParameterType.SPEED.name())) {
                    preposition = getString(R.string.with);
                    parameter2 = settings.getSpeedUnit().format(runJson.getInt(ParameterType.SPEED.name()));
                }
            } else if (runJson.has(ParameterType.DURATION.name())) {
                parameter1 = settings.getDurationUnit().format(runJson.getInt(ParameterType.DURATION.name()));
                preposition = getString(R.string.with);
                if (runJson.has(ParameterType.PACE.name())) {
                    parameter2 = settings.getPaceUnit().format(runJson.getInt(ParameterType.PACE.name()));
                } else if (runJson.has(ParameterType.SPEED.name())) {
                    parameter2 = settings.getSpeedUnit().format(runJson.getInt(ParameterType.SPEED.name()));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parameter1 + " " + preposition + " " + parameter2;
    }
}
