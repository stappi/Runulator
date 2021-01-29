package com.stappert.runulator.activities;

import android.graphics.Color;
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

import com.stappert.runulator.R;
import com.stappert.runulator.utils.SettingsManager;
import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Organizes the forecast view of the application.
 */
public class TabForecast extends Fragment {

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
            List<Float> distances = createDistanceList();
            for (int i = 0; i < distances.size(); i++) {
                TableRow forecast = createForecast(
                        run.getForecastRun(distances.get(i), 1.0759f),
                        i % 2 == 0 ? Color.LTGRAY : Color.TRANSPARENT);
                forecastTable.addView(forecast, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Returns the distances.
     *
     * @return distances
     */
    private List<Float> createDistanceList() {
        SortedSet distances = new TreeSet();
        distances.addAll(Arrays.asList(5f, 10f, 21.0975f, 42.195f, settings.getDistance()));
        try {
            for (int i = 0; i < settings.getFavoriteRuns().size(); i++) {
                distances.add(settings.getFavoriteRuns().get(i).getDistanceAsNumber(Unit.KM));
            }
        } catch (CustomException ex) {
            Log.e("error", ex.getMessage());
        }
        return new ArrayList<>(distances);
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
    private TableRow createForecast(Run forecast, int backgroundColor) throws CustomException {
        TableRow row = new TableRow(getContext());
        row.addView(createCellForecastValue(forecast.getDistance(settings.getDistanceUnit())));
        row.addView(createCellForecastValue(forecast.getDuration()));
        row.addView(createCellForecastValue(forecast.getPace(settings.getPaceUnit())));
        row.addView(createCellForecastValue(forecast.getSpeed(settings.getSpeedUnit())));
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setBackgroundColor(backgroundColor);
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
