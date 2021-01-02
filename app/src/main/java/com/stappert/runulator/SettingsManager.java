package com.stappert.runulator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages all settings for this application.
 */
public class SettingsManager {

    // Keys to store and load values from shared preferences
    private String KEY_DISTANCE = "distance";
    private String KEY_DURATION = "duration";
    private String KEY_PACE = "pace";
    private String KEY_SPEED = "speed";
    private String KEY_RUNS = "runs";
    private String KEY_WEIGHT = "weight";
    private String KEY_WEIGHT_UNIT = "weight_unit";

    /**
     * Single object of the settings manager.
     */
    private static final SettingsManager SETTINGS = new SettingsManager();

    /**
     * Context of this application.
     */
    private Context context;

    /**
     * Shared preferences, to store data.
     */
    private SharedPreferences sharedPreferences;

    // =============================================================================================
    // create singleton
    // =============================================================================================

    /**
     * Creates the settings object.
     */
    private SettingsManager() {
    }

    /**
     * Returns the single instance of the settings.
     *
     * @return settings
     */
    public static SettingsManager getInstance() {
        return SETTINGS;
    }

    /**
     * Sets the context. Without initialization the settings manager will not work.
     *
     * @param context context
     * @return settings manager
     */
    public SettingsManager init(Context context) {
        this.context = context;
        this.sharedPreferences = Utils.getSharedPreferences(context);
        return SETTINGS;
    }

    // =============================================================================================
    // get and set settings
    // =============================================================================================

    /**
     * Returns the last set distance.
     *
     * @return distance
     */
    public float getDistance() {
        return sharedPreferences.getFloat(KEY_DISTANCE, 10);
    }

    /**
     * Stores the last set distance.
     *
     * @param distance distance
     */
    public void setDistance(String distance) {
        saveValue(KEY_DISTANCE, Float.parseFloat(distance));
    }

    /**
     * Returns the last set duration
     *
     * @return duration
     */
    public int getDuration() {
        return sharedPreferences.getInt(KEY_DURATION, Run.HOUR);
    }

    /**
     * Stores the last set duration.
     *
     * @param duration duration
     */
    public void setDuration(String duration) throws CustomException {
        saveValue(KEY_DURATION, Run.parseTimeInSeconds(duration));
    }

    /**
     * Returns the last set pace.
     *
     * @return pace
     */
    public int getPace() throws CustomException {
        return sharedPreferences.getInt(KEY_PACE,
                Run.createWithDistanceAndDuration(getDistance(), getDuration()).getPaceInSeconds());
    }

    /**
     * Stores the last set pace.
     *
     * @param pace pace
     */
    public void setPace(String pace) throws CustomException {
        saveValue(KEY_PACE, Run.parseTimeInSeconds(pace));
    }

    /**
     * Returns the last set speed.
     *
     * @return speed
     */
    public float getSpeed() throws CustomException {
        return sharedPreferences.getFloat(KEY_SPEED,
                Run.createWithDistanceAndDuration(getDistance(), getDuration()).getSpeedAsNumber());
    }

    /**
     * Stores the last set speed.
     *
     * @param speed speed
     */
    public void setSpeed(String speed) {
        saveValue(KEY_SPEED, speed);
    }

    /**
     * Returns the last calculated run. If no run is available, a default run (10 km, 60 minutes)
     * will be returned.
     *
     * @return run
     * @throws CustomException if run can not be created
     */
    public Run getRun() throws CustomException {
        try {
            return Run.createWithDistanceAndDuration(getDistance(), getDuration());
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
            return Run.createWithDistanceAndDuration(10, 55 * 60);
        }
    }

    /**
     * Returns the favorite runs.
     *
     * @return favorite runs
     */
    public List<Run> getFavoriteRuns() {
        return Run.jsonToRuns(sharedPreferences.getStringSet(KEY_RUNS, new HashSet<String>()));
    }

    /**
     * Stores the favorite runs.
     *
     * @param favoriteRuns favorite runs
     */
    public void setFavoriteRuns(List<Run> favoriteRuns) {
        saveValue(KEY_RUNS, Run.runsToJson(favoriteRuns));
    }

    /**
     * Returns the weight.
     *
     * @return weight
     */
    public int getWeight() {
        return sharedPreferences.getInt(KEY_WEIGHT, 100);
    }

    /**
     * Returns the selected weight unit.
     *
     * @return weight unit
     */
    public WeightUnit getWeightUnit() {
        return WeightUnit.valueOf((sharedPreferences.getString(KEY_WEIGHT_UNIT, WeightUnit.KG.name())));
    }

    /**
     * Stores the weight depending on weight unit.
     *
     * @param weight weight
     * @param unit   unit
     */
    public void setWeight(int weight, WeightUnit unit) {
        saveValue(KEY_WEIGHT, weight);
        saveValue(KEY_WEIGHT_UNIT, unit.name());
    }

    /**
     * Save value in shared preferences.
     *
     * @param key   key
     * @param value value
     */
    private void saveValue(String key, Object value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Set<?>) {
            editor.putStringSet(key, (Set<String>) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.commit();
    }
}
