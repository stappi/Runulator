package com.stappert.runulator;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Organizes the forecast view of the application.
 */
public class TabForecast extends Fragment {

    // =============================================================================================
    // default run distances
    // =============================================================================================
    /**
     * Default distance 5 kilometers.
     */
    private final static float DISTANCE_5KM = 5;
    /**
     * Default distance 10 kilometers.
     */
    private final static float DISTANCE_10KM = 10;
    /**
     * Default distance half marathon.
     */
    private final static float DISTANCE_HM = 21.0975f;
    /**
     * Default distance marathon.
     */
    private final static float DISTANCE_M = 42.195f;

    // ==============

    /**
     * Settings manager.
     */
    private SettingsManager settings;
    /**
     * Table, to set forecast.
     */
    private TableLayout forecastTable;

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
        View view = inflater.inflate(R.layout.tab_layout_forecast, container, false);
        settings = SettingsManager.getInstance();
        forecastTable = view.findViewById(R.id.forecastTable);
        updateForecastTable();
        return view;
    }

    /**
     * Update table on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateForecastTable();
    }

    /**
     * Updates the forecast depending on the current run.
     */
    public void updateForecastTable() {
        forecastTable.removeAllViews();
        try {
            Run run = SettingsManager.getInstance().getRun();
            // add header
            forecastTable.addView(createHeader(), new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            // add header
            forecastTable.addView(createUnits(), new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            // add runs
            for (float distance : new float[]{5, 10, 21.0975f, 42.195f}) {
                forecastTable.addView(createForecast(run.getForecastRun(distance, 1.0759f)),
                        new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates the header row for the table including column titles.
     *
     * @return header of table
     */
    private TableRow createHeader() {
        TableRow row = new TableRow(getContext());
        row.addView(createColumnTitle(getContext().getString(R.string.distance)));
        row.addView(createColumnTitle(getContext().getString(R.string.run_time)));
        row.addView(createColumnTitle(getContext().getString(R.string.pace)));
        row.addView(createColumnTitle(getContext().getString(R.string.speed)));
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return row;
    }

    /**
     * Creates the row for the table to define the unit for the column.
     *
     * @return unit row
     */
    private TableRow createUnits() {
        TableRow row = new TableRow(getContext());
        row.addView(createColumnUnit("[" + settings.getDistanceUnit().toString() + "]"));
        row.addView(createColumnUnit("[" + settings.getDurationUnit().toString() + "]"));
        row.addView(createColumnUnit("[" + settings.getPaceUnit().toString() + "]"));
        row.addView(createColumnUnit("[" + settings.getSpeedUnit().toString() + "]"));
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return row;
    }

    /**
     * Creates a row for the table with a forecast.
     *
     * @param forecast
     * @return forecast row
     */
    private TableRow createForecast(Run forecast) throws CustomException {
        TableRow row = new TableRow(getContext());
        row.addView(createCellForecastValue(forecast.getDistance(settings.getDistanceUnit())));
        row.addView(createCellForecastValue(forecast.getDuration()));
        row.addView(createCellForecastValue(forecast.getPace(settings.getPaceUnit())));
        row.addView(createCellForecastValue(forecast.getSpeed(settings.getSpeedUnit())));
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return row;
    }

    /**
     * Creates a cell for the column title.
     *
     * @param columnTitle column title
     * @return cell
     */
    private TextView createColumnTitle(String columnTitle) {
        TextView columnTextView = new TextView(getContext());
        columnTextView.setText(columnTitle);
        columnTextView.setTypeface(null, Typeface.BOLD);
        columnTextView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return columnTextView;
    }

    /**
     * Creates a cell for the column unit.
     *
     * @param unit unit
     * @return cell
     */
    private TextView createColumnUnit(String unit) {
        TextView unitTextView = new TextView(getContext());
        unitTextView.setText(unit);
        unitTextView.setTypeface(null, Typeface.ITALIC);
        unitTextView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return unitTextView;
    }

    /**
     * Creates a cell with a forecast value.
     *
     * @param value forecast value
     * @return cell
     */
    private TextView createCellForecastValue(String value) {
        TextView valueTextView = new TextView(getContext());
        valueTextView.setText(value);
        valueTextView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return valueTextView;
    }
}
