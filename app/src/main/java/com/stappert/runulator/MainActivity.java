package com.stappert.runulator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.List;

/**
 * Main activity of application.
 */
public class MainActivity extends AppCompatActivity {

    // Elements
    private EditText distanceEditText;
    private EditText durationEditText;
    private EditText paceEditText;
    private EditText speedEditText;
    private Button calculateButton;
    private ImageButton resetDistanceButton;
    private ImageButton resetDurationButton;
    private ImageButton resetPaceButton;
    private ImageButton resetSpeedButton;
    private ImageButton favoriteButton;
    private ImageButton openFavoritesButton;
    private TextView caloriesTextView;
    private TextView forecastTextView;

    // variables
    private Run currentRun;
    private List<Run> favoriteRuns;
    private SettingsManager settings;

    /**
     * Initializes activity.
     *
     * @param savedInstanceState save instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            settings = SettingsManager.getInstance().init(this);
            initElements();
            initListener();
            initValues();
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates main menu.
     *
     * @param menu menu
     * @return true, if menu is successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Called, when menu item of main menu is selected.
     *
     * @param item selected menu item
     * @return true, if process was successfully
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                Toast.makeText(this, this.getText(R.string.info_text), Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called on resume of activity.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            initValues();
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =============================================================================================
    // private utility functions
    // =============================================================================================

    /**
     * Calculates run depending on input.
     */
    private void calculateRun() {
        boolean isDistanceSet = !distanceEditText.getText().toString().isEmpty();
        boolean isDurationSet = !durationEditText.getText().toString().isEmpty();
        boolean isPaceSet = !paceEditText.getText().toString().isEmpty();
        boolean isSpeedSet = !speedEditText.getText().toString().isEmpty();
        // calculate depending on set values
        try {
            if (isDistanceSet && isDurationSet) {
                currentRun = Run.createWithDistanceAndDuration(
                        Run.parseToFloat(distanceEditText.getText().toString()),
                        Run.parseTimeInSeconds(durationEditText.getText().toString()));
            } else if (isDistanceSet && isPaceSet) {
                currentRun = Run.createWithDistanceAndPace(
                        Run.parseToFloat(distanceEditText.getText().toString()),
                        Run.parseTimeInSeconds(paceEditText.getText().toString()));
            } else if (isDistanceSet && isSpeedSet) {
                currentRun = Run.createWithDistanceAndSpeed(
                        Run.parseToFloat(distanceEditText.getText().toString()),
                        Run.parseToFloat(speedEditText.getText().toString()));
            } else if (isDurationSet && isPaceSet) {
                currentRun = Run.createWithDurationAndPace(
                        Run.parseTimeInSeconds(durationEditText.getText().toString()),
                        Run.parseTimeInSeconds(paceEditText.getText().toString()));
            } else if (isDurationSet && isSpeedSet) {
                currentRun = Run.createWithDurationAndSpeed(
                        Run.parseTimeInSeconds(durationEditText.getText().toString()),
                        Run.parseToFloat(speedEditText.getText().toString()));
            } else if (isPaceSet && isSpeedSet) {
                // this case can not be calculated
                Toast.makeText(this, "this case can not be calculated", Toast.LENGTH_LONG).show();
            }
            // check if run can add to favorite list
            updateRunOnGui();
            checkFavoriteButton();
            // save values to shared preferences
            settings.setDistance(distanceEditText.getText().toString());
            settings.setDuration(durationEditText.getText().toString());
            settings.setPace(paceEditText.getText().toString());
            settings.setSpeed(speedEditText.getText().toString());
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates run on gui.
     */
    private void updateRunOnGui() {
        try {
            // set run
            distanceEditText.setText(currentRun.getDistance());
            durationEditText.setText(currentRun.getDuration());
            paceEditText.setText(currentRun.getPace());
            speedEditText.setText(currentRun.getSpeed());
            // set calories and forecast
            SharedPreferences sharedPref = getSharedPreferences("runulator", Context.MODE_PRIVATE);
            caloriesTextView.setText(currentRun.getCalories(settings.getWeightUnit().toKg(settings.getWeight())));
            forecastTextView.setText(currentRun.getForecast(1.0759f));
        } catch (CustomException ex) {
            Log.e(ex.getTitle(), ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
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

        // update gui
        checkFavoriteButton();
        checkOpenFavoritesButton();
    }

    /**
     * Sets icon to favorite button, depending on existence run in favorite list.
     */
    private void checkFavoriteButton() {
        if (favoriteRuns.contains(currentRun)) {
            favoriteButton.setImageIcon(Icon.createWithResource(this, R.drawable.ic_favorite));
        } else {
            favoriteButton.setImageIcon(Icon.createWithResource(this, R.drawable.ic_favorite_add));
        }
    }

    /**
     * Enables or disables open favorite list button depending on number of favorite runs.
     */
    private void checkOpenFavoritesButton() {
        openFavoritesButton.setEnabled(!favoriteRuns.isEmpty());
    }

    /**
     * Enables or disables calculation button, depending on
     */
    private void updateCalculationButton() {
        calculateButton.setEnabled(getNumberOfFilledFields() == 2 && isPossibleCase());
    }

    /**
     * Return number of filled fields.
     *
     * @return number of filled fields
     */
    private int getNumberOfFilledFields() {
        return (distanceEditText.getText().toString().isEmpty() ? 0 : 1)
                + (durationEditText.getText().toString().isEmpty() ? 0 : 1)
                + (paceEditText.getText().toString().isEmpty() ? 0 : 1)
                + (speedEditText.getText().toString().isEmpty() ? 0 : 1);
    }

    /**
     * All cases for calculation are possible, except if speed and pace are set.
     *
     * @return true, except speed and pace are set are not empty
     */
    private boolean isPossibleCase() {
        return paceEditText.getText().toString().isEmpty()
                || speedEditText.getText().toString().isEmpty();
    }

    // =============================================================================================
    // Initialize
    // =============================================================================================

    /**
     * Initializes gui elements.
     */
    private void initElements() {
        distanceEditText = findViewById(R.id.distanceEditTextNumber);
        durationEditText = findViewById(R.id.durationEditTextNumber);
        paceEditText = findViewById(R.id.paceEditTextNumber);
        speedEditText = findViewById(R.id.speedEditTextNumber);
        calculateButton = findViewById(R.id.calculateButton);
        resetDistanceButton = findViewById(R.id.resetDistanceButton);
        resetDurationButton = findViewById(R.id.resetDurationButton);
        resetPaceButton = findViewById(R.id.resetPaceButton);
        resetSpeedButton = findViewById(R.id.resetSpeedButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        openFavoritesButton = findViewById(R.id.openButton);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        forecastTextView = findViewById(R.id.forecastTextView);
    }

    /**
     * Initializes listeners for elements.
     */
    private void initListener() {
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateRun();
            }
        });
        addTextWatcherToEditText(distanceEditText);
        addTextWatcherToEditText(durationEditText);
        addTextWatcherToEditText(paceEditText);
        addTextWatcherToEditText(speedEditText);
        addOnClickListenerToResetButtons(resetDistanceButton);
        addOnClickListenerToResetButtons(resetDurationButton);
        addOnClickListenerToResetButtons(resetPaceButton);
        addOnClickListenerToResetButtons(resetSpeedButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRemoveRunInFavorites();
            }
        });
        openFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenFavoriteDialog();
            }
        });
    }

    /**
     * Add text watcher to input fields.
     *
     * @param inputField input field
     */
    private void addTextWatcherToEditText(EditText inputField) {
        // attach change lister to enable or disable button if calculation is possible or not
        inputField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) { /* do nothing */ }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { /* do nothing */ }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updateCalculationButton();
            }
        });
    }

    /**
     * Add listener to buttons for resets.
     *
     * @param resetButton reset button
     */
    private void addOnClickListenerToResetButtons(ImageButton resetButton) {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View resetButton) {
                switch (resetButton.getId()) {
                    case R.id.resetDistanceButton:
                        distanceEditText.setText("");
                        break;
                    case R.id.resetDurationButton:
                        durationEditText.setText("");
                        break;
                    case R.id.resetPaceButton:
                        paceEditText.setText("");
                        break;
                    case R.id.resetSpeedButton:
                        speedEditText.setText("");
                        break;
                }
                updateCalculationButton();
            }
        });
    }

    /**
     * Shows dialog, to load run from favorite list.
     */
    private void showOpenFavoriteDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Favorit laden");
        ArrayAdapter arrayAdapter = new ArrayAdapter<Run>(
                this,
                android.R.layout.select_dialog_item, // Layout
                favoriteRuns
        );
        builder.setSingleChoiceItems(arrayAdapter,
                favoriteRuns.indexOf(currentRun),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        // Get the alert dialog selected item's text
                        currentRun = favoriteRuns.get(i);
                        updateRunOnGui();
                        checkFavoriteButton();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    /**
     * Load values from shared preferences.
     *
     * @throws CustomException if initialization failed
     */
    private void initValues() throws CustomException {
        favoriteRuns = settings.getFavoriteRuns();
        currentRun = settings.getRun();
        updateRunOnGui();
        checkFavoriteButton();
        checkOpenFavoritesButton();
    }
}