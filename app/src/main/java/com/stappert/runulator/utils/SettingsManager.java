package com.stappert.runulator.utils;

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
    private static String KEY_DISTANCE = "distance";
    public static String KEY_DISTANCE_UNIT = "distance_unit";
    private static String KEY_DURATION = "duration";
    private static String KEY_PACE_UNIT = "pace_unit";
    private static String KEY_PACE = "pace";
    private static String KEY_SPEED_UNIT = "speed_unit";
    private static String KEY_SPEED = "speed";
    private static String KEY_RUNS = "runs";
    private static String KEY_WEIGHT = "weight";
    private static String KEY_WEIGHT_UNIT = "weight_unit";
    private static String KEY_HEIGHT = "height";
    private static String KEY_HEIGHT_UNIT = "height_unit";
    private static String KEY_BIRTHDAY = "birthday";

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
     * Stores the last set distance in km.
     *
     * @param distance distance
     * @throws CustomException if conversion for unit is not supported
     */
    public void setDistance(String distance) throws CustomException {
        setDistance(Float.parseFloat(distance));
    }

    /**
     * Stores the last set distance.
     *
     * @param distance distance
     * @throws CustomException if conversion for unit is not supported
     */
    public void setDistance(float distance) throws CustomException {
        saveValue(KEY_DISTANCE, getDistanceUnit().toKm(distance));
    }

    /**
     * Returns the distance unit.
     *
     * @return distance unit.
     */
    public Unit getDistanceUnit() {
        return Unit.valueOf((sharedPreferences.getString(KEY_DISTANCE_UNIT, Unit.KM.name())));
    }

    /**
     * Sets the distance unit.
     *
     * @param unit distance unit.
     */
    public void setDistanceUnit(Unit unit) {
        saveValue(KEY_DISTANCE_UNIT, unit.name());
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
        setDuration(Run.parseTimeInSeconds(duration));
    }

    /**
     * Stores the last set duration.
     *
     * @param duration duration
     */
    public void setDuration(int duration) {
        saveValue(KEY_DURATION, duration);
    }

    /**
     * Returns the duration unit.
     *
     * @return duration unit.
     */
    public Unit getDurationUnit() {
        return Unit.HOUR;
    }

    /**
     * Stores the last set pace.
     *
     * @param pace pace
     * @throws CustomException if conversion for unit is not supported
     */
    public void setPace(String pace) throws CustomException {
        setPace(Run.parseTimeInSeconds(pace));
    }

    /**
     * Stores the last set pace in seconds per minute.
     *
     * @param pace pace
     * @throws CustomException if conversion for unit is not supported
     */
    public void setPace(int pace) throws CustomException {
        saveValue(KEY_PACE, getPaceUnit().toMinPerKm(pace));
    }

    /**
     * Returns the pace unit.
     *
     * @return speed unit.
     */
    public Unit getPaceUnit() {
        return Unit.valueOf((sharedPreferences.getString(KEY_PACE_UNIT, Unit.MIN_KM.name())));
    }

    /**
     * Sets the pace unit.
     *
     * @param unit pace unit.
     */
    public void setPaceUnit(Unit unit) {
        saveValue(KEY_PACE_UNIT, unit.name());
    }

    /**
     * Stores the last set speed.
     *
     * @param speed speed
     * @throws CustomException if conversion for unit is not supported
     */
    public void setSpeed(String speed) throws CustomException {
        setSpeed(Float.parseFloat(speed));
    }

    /**
     * Stores the last set speed in km/h.
     *
     * @param speed speed
     * @throws CustomException if conversion for unit is not supported
     */
    public void setSpeed(float speed) throws CustomException {
        saveValue(KEY_SPEED, getSpeedUnit().toKmPerHour(speed));
    }

    /**
     * Returns the speed unit.
     *
     * @return speed unit.
     */
    public Unit getSpeedUnit() {
        return Unit.valueOf((sharedPreferences.getString(KEY_SPEED_UNIT, Unit.KM_H.name())));
    }

    /**
     * Sets the speed unit.
     *
     * @param unit speed unit.
     */
    public void setSpeedUnit(Unit unit) {
        saveValue(KEY_SPEED_UNIT, unit.name());
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
     * Returns the last calculated run. If no run is available, a default run (10 km, 60 minutes)
     * will be returned.
     *
     * @return run
     * @throws CustomException if run can not be created
     */
    public void setRun(Run run) throws CustomException {
        setDistance(run.getDistanceAsNumber(Unit.KM));
        setDuration(run.getDurationAsNumber());
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
     * Returns the weight in kg.
     *
     * @return weight in kg
     * @throws CustomException if conversion for unit is not supported
     */
    public int getWeightInKg() throws CustomException {
        return getWeightUnit().toKg(getWeight());
    }

    /**
     * Returns the selected weight unit.
     *
     * @return weight unit
     */
    public Unit getWeightUnit() {
        return Unit.valueOf((sharedPreferences.getString(KEY_WEIGHT_UNIT, Unit.KG.name())));
    }

    /**
     * Stores the weight depending on weight unit.
     *
     * @param weight weight
     * @param unit   unit
     */
    public void setWeight(int weight, Unit unit) {
        saveValue(KEY_WEIGHT, weight);
        saveValue(KEY_WEIGHT_UNIT, unit.name());
    }

    /**
     * Returns the height. Default value are 190 cm.
     *
     * @return height
     */
    public int getHeight() {
        return sharedPreferences.getInt(KEY_HEIGHT, 190);
    }

    /**
     * Returns the weight in kg.
     *
     * @return weight in kg
     * @throws CustomException if conversion for unit is not supported
     */
    public int getHeightInCm() throws CustomException {
        return (int) getHeightUnit().toCm(getHeight());
    }

    /**
     * Returns the selected height unit. Default unit is cm.
     *
     * @return height unit
     */
    public Unit getHeightUnit() {
        return Unit.valueOf((sharedPreferences.getString(KEY_HEIGHT_UNIT, Unit.CM.name())));
    }

    /**
     * Stores the birthday.
     *
     * @param birthday birthday
     */
    public void setBirthday(long birthday) {
        saveValue(KEY_BIRTHDAY, birthday);
    }

    /**
     * Returns the birthday.
     * @return birthday
     */
    public long getBirthday() {
        return sharedPreferences.getLong(KEY_BIRTHDAY, 0);
    }

    /**
     * Stores the height depending on weight unit.
     *
     * @param height height
     * @param unit   unit
     */
    public void setHeight(int height, Unit unit) {
        saveValue(KEY_HEIGHT, height);
        saveValue(KEY_HEIGHT_UNIT, unit.name());
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
