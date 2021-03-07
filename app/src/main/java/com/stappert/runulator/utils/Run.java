package com.stappert.runulator.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Defines a run. Contains static functions, to create and manage runs. (Companion object)
 */
public class Run {

    // =============================================================================================
    // time units
    // =============================================================================================
    /**
     * Distance for half marathon.
     */
    public final static float HALF_MARATHON = 21.0975f;

    /**
     * Distance for marathon.
     */
    public final static float MARATHON = 42.195f;

    // =============================================================================================
    // class variables
    // =============================================================================================
    /**
     * Distance of run in km.
     */
    private float distance;
    /**
     * Duration of run in seconds.
     */
    private int duration;
    /**
     * Pace of run in seconds.
     */
    private int pace;
    /**
     * Speed of run in km/h.
     */
    private float speed;

    // =============================================================================================
    // constructors
    // =============================================================================================

    /**
     * Creates a run.
     *
     * @param distance in km
     * @param duration in seconds
     * @param pace     in seconds
     * @param speed    in km/h
     */
    private Run(float distance, int duration, int pace, float speed) {
        this.distance = distance;
        this.duration = duration;
        this.pace = pace;
        this.speed = speed;
    }

    // =============================================================================================
    // getter
    // =============================================================================================

    /**
     * Returns in the distance as string with maximal 4 decimal places.
     *
     * @param unit unit to convert
     * @return distance
     * @throws CustomException if conversion to desired unit is not possible
     */
    public String getDistance(Unit unit) throws CustomException {
        float parsedDistance = getDistanceAsNumber(unit);
        int noOfDecimalPlaces = Float.toString(parsedDistance).split("\\.")[1].length();
        return String.format(Locale.ENGLISH, "%." + (noOfDecimalPlaces < 4 ? noOfDecimalPlaces : 4) + "f", parsedDistance);
    }

    /**
     * Returns the distance as number.
     *
     * @param unit unit to convert
     * @return distance
     * @throws CustomException if conversion to desired unit is not possible
     */
    public float getDistanceAsNumber(Unit unit) throws CustomException {
        return unit.kmTo(distance);
    }

    /**
     * Returns the duration in hh:mm:ss as string (h = hour, m = minute, s = second).
     *
     * @return duration
     */
    public String getDuration() {
        return Unit.formatSeconds(duration);
    }

    /**
     * Returns the duration in seconds.
     *
     * @return duration
     */
    public int getDurationAsNumber() {
        return duration;
    }

    /**
     * Returns the pace in hh:mm:ss as string (h = hour, m = minute, s = second) per desired length
     * (km, mi).
     *
     * @param unit convert to
     * @return pace
     * @throws CustomException if conversion to desired unit is not possible
     */
    public String getPace(Unit unit) throws CustomException {
        return Unit.formatSeconds(getPaceAsNumber(unit));
    }

    /**
     * Returns the pace.
     *
     * @param unit convert to
     * @return pace
     * @throws CustomException if conversion to desired unit is not possible
     */
    public int getPaceAsNumber(Unit unit) throws CustomException {
        return (int) Math.round(unit.minPerKmTo(pace));
    }

    /**
     * Returns in the speed as string with maximal 2 decimal places.
     *
     * @param unit convert to
     * @return speed
     * @throws CustomException if conversion to desired unit is not possible
     */
    public String getSpeed(Unit unit) throws CustomException {
        float parsedSpeed = getSpeedAsNumber(unit);
        int noOfDecimalPlaces = Float.toString(parsedSpeed).split("\\.")[1].length();
        return String.format(Locale.ENGLISH, "%." + (noOfDecimalPlaces <= 1 ? 1 : 2) + "f", parsedSpeed);
    }


    /**
     * Returns in the speed as number.
     *
     * @param unit convert to
     * @return speed
     * @throws CustomException if conversion to desired unit is not possible
     */
    public float getSpeedAsNumber(Unit unit) throws CustomException {
        return unit.kmPerHourTo(speed);
    }

    /**
     * Calculates the number of calories burned.
     *
     * @param weight in kg
     * @return
     */
    public String calculateCalories(int weight) {
        return "~" + (int) (distance * weight * 0.9f);
    }

    /**
     * Creates a forecast run for the desired distance depending on the fatigue coefficient.
     *
     * @param forecastDistance   distance for forecast
     * @param fatigueCoefficient fatigue coefficient
     * @return forecast run
     * @throws CustomException if forecast is not possible
     */
    public Run getForecastRun(float forecastDistance, float fatigueCoefficient) throws CustomException {
        return createWithDistanceAndDuration(forecastDistance,
                (int) (duration * Math.pow(forecastDistance / distance, fatigueCoefficient)));
    }

    /**
     * Calculates the recommended step frequency per minute depending on height and speed.
     * Formula is from https://www.matthias-marquardt.com/rechner/schrittfrequenz/.
     *
     * @param height height in cm
     * @return recommended step frequency per minute
     */
    public int calculateStepFrequency(int height) throws CustomException {
        if (100 < height && height < 272) {
            return (int) Math.ceil(160 + (speed - 6) * 2.5 - (height - 170) / 2);
        } else {
            throw new CustomException("error", "You're are not " + height + "cm tall.");
        }
    }

    // =============================================================================================
    // utility functions
    // =============================================================================================

    /**
     * Compares this run with an object. This run and the object are equals, if object is a run and
     * distance and duration are equals.
     *
     * @param object object to compare
     * @return true if distance and duration are equals
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Run run = (Run) object;
        return run.distance == distance && duration == run.duration;
    }

    /**
     * Generates hashcode depending on distance and duration.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(distance, duration);
    }

    /**
     * Converts run to string with info distance, duration, pace and speed.
     *
     * @return run
     */
    public String toString() {
        try {
            return getDistance(Unit.KM) + "KM in "
                    + Unit.HOUR.format(duration) + "\n("
                    + getPace(Unit.MIN_KM) + "min/km; "
                    + getSpeed(Unit.KM_H) + "km/h)";
        } catch (CustomException ex) {
            return "error";
        }
    }

    /**
     * Converts run to json string.
     *
     * @return run as json string
     */
    public String toJson() {
        return "{" + "\'" + ParameterType.DISTANCE.name() + "\':" + distance + ","
                + "\'" + ParameterType.DURATION.name() + "\':" + duration + ","
                + "\'" + ParameterType.PACE.name() + "\':" + pace + ","
                + "\'" + ParameterType.SPEED.name() + "\':" + speed + "}";
    }
    // =============================================================================================
    // create runs depending on parameters
    // =============================================================================================

    /**
     * Creates a run depending on distance in km and duration in seconds.
     *
     * @param distance in km
     * @param duration in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static Run createWithDistanceAndDuration(float distance, int duration)
            throws CustomException {
        if (distance <= 0 || duration <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        int pace = Math.round(duration / distance);
        float speed = distance * Unit.HOUR_IN_SECONDS / duration;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on distance in km and duration in seconds.
     *
     * @param distance in km
     * @param duration in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static String jsonWithDistanceAndDuration(float distance, int duration)
            throws CustomException {
        if (distance <= 0 || duration <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        return "{" + "\'" + ParameterType.DISTANCE.name() + "\':" + distance + ","
                + "\'" + ParameterType.DURATION.name() + "\':" + duration + "}";
    }

    /**
     * Creates a run depending on distance in km and pace in seconds.
     *
     * @param distance in km
     * @param pace     in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static Run createWithDistanceAndPace(float distance, int pace)
            throws CustomException {
        if (distance <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        int duration = Math.round(distance * pace);
        float speed = (float) Unit.HOUR_IN_SECONDS / pace;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on distance in km and pace in seconds.
     *
     * @param distance in km
     * @param pace     in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static String jsonWithDistanceAndPace(float distance, int pace)
            throws CustomException {
        if (distance <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        return "{" + "\'" + ParameterType.DISTANCE.name() + "\':" + distance + ","
                + "\'" + ParameterType.PACE.name() + "\':" + pace + "}";
    }

    /**
     * Creates a run depending on distance in km and speed in km/h.
     *
     * @param distance in km
     * @param speed    in km/h
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static Run createWithDistanceAndSpeed(float distance, float speed)
            throws CustomException {
        if (distance <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        int duration = Math.round(distance / speed * Unit.HOUR_IN_SECONDS);
        int pace = Math.round(Unit.HOUR_IN_SECONDS / speed);
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on distance in km and speed in km/h.
     *
     * @param distance in km
     * @param speed    in km/h
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static String jsonWithDistanceAndSpeed(float distance, float speed)
            throws CustomException {
        if (distance <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        return "{" + "\'" + ParameterType.DISTANCE.name() + "\':" + distance + ","
                + "\'" + ParameterType.SPEED.name() + "\':" + speed + "}";
    }

    /**
     * Creates a run depending on duration in seconds and pace in seconds.
     *
     * @param duration in seconds
     * @param pace     in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static Run createWithDurationAndPace(int duration, int pace)
            throws CustomException {
        if (duration <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        float distance = 1.0f * duration / pace;
        float speed = (float) Unit.HOUR_IN_SECONDS / pace;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on duration in seconds and pace in seconds.
     *
     * @param duration in seconds
     * @param pace     in seconds
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static String jsonWithDurationAndPace(int duration, int pace)
            throws CustomException {
        if (duration <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        return "{" + "\'" + ParameterType.DURATION.name() + "\':" + duration + ","
                + "\'" + ParameterType.PACE.name() + "\':" + pace + "}";
    }

    /**
     * Creates a run depending on duration in seconds and speed in km/h.
     *
     * @param duration in seconds
     * @param speed    in km/h
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static Run createWithDurationAndSpeed(int duration, float speed)
            throws CustomException {
        if (duration <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        float distance = duration * speed / Unit.HOUR_IN_SECONDS;
        int pace = Math.round(Unit.HOUR_IN_SECONDS / speed);
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on duration in seconds and speed in km/h.
     *
     * @param duration in seconds
     * @param speed    in km/h
     * @return run
     * @throws CustomException if parameters are not greater than 0
     */
    public static String jsonWithDurationAndSpeed(int duration, float speed)
            throws CustomException {
        if (duration <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        return "{" + "\'" + ParameterType.DURATION.name() + "\':" + duration + ","
                + "\'" + ParameterType.SPEED.name() + "\':" + speed + "}";
    }

    /**
     * Converts a list of run as json strings to run objects.
     *
     * @param runsJson list of runs as json strings
     * @return list of run objects
     */
    public static List<Run> jsonToRuns(Set<String> runsJson) {
        List<Run> runs = new ArrayList<>();
        try {
            for (String runJsonString : runsJson) {
                runs.add(jsonToRun(runJsonString));
            }
        } catch (JSONException | CustomException ex) {
            Log.e("Error", ex.getMessage());
        }
        return runs;
    }

    /**
     * Converts a list of run as json strings to run objects.
     *
     * @param runJsonString run as json string
     * @return list of run objects
     */
    public static Run jsonToRun(String runJsonString) throws JSONException, CustomException {
        JSONObject runJson = new JSONObject(runJsonString);
        if (runJson.has(ParameterType.DISTANCE.name())) {
            final float distance = (float) runJson.getDouble(ParameterType.DISTANCE.name());
            if (runJson.has(ParameterType.DURATION.name())) {
                return Run.createWithDistanceAndDuration(distance, runJson.getInt(ParameterType.DURATION.name()));
            } else if (runJson.has(ParameterType.PACE.name())) {
                return Run.createWithDistanceAndPace(distance, runJson.getInt(ParameterType.PACE.name()));
            } else if (runJson.has(ParameterType.SPEED.name())) {
                return Run.createWithDistanceAndSpeed(distance, (float) runJson.getDouble(ParameterType.SPEED.name()));
            }
        } else if (runJson.has(ParameterType.DURATION.name())) {
            final int duration = runJson.getInt(ParameterType.DURATION.name());
            if (runJson.has(ParameterType.PACE.name())) {
                return Run.createWithDurationAndPace(duration, runJson.getInt(ParameterType.PACE.name()));
            } else if (runJson.has(ParameterType.SPEED.name())) {
                return Run.createWithDurationAndSpeed(duration, (float) runJson.getDouble(ParameterType.SPEED.name()));
            }
        }
        return null;
    }


    /**
     * Converts a collection of run objects to a set of runs as json strings.
     *
     * @param runs collection of run objects
     * @return runs as json strings
     */
    public static Set<String> runsToJson(Collection<Run> runs) {
        Set<String> runsJson = new HashSet<>();
        for (Run run : runs) {
            runsJson.add(run.toJson());
        }
        return runsJson;
    }

    // =============================================================================================
    // utility functions
    // =============================================================================================

    /**
     * Parses string value (input) to float (distance or speed).
     *
     * @param number user input
     * @return distance or speed
     * @throws Exception if input cannot convert to number
     */
    public static float parseToFloat(String number) throws CustomException {
        try {
            return Float.parseFloat(number.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new CustomException("Error", number + " is not a number. Reset to default value.");
        }
    }

    /**
     * Parses string value (input) to float. Minimal format is ss, middle format is mm:ss,
     * full format is hh:mm:ss (h = hour, m = minute, s = second).
     *
     * @param time user input
     * @return distance or speed
     * @throws Exception if input cannot convert to number
     */
    public static int parseTimeInSeconds(String time) throws CustomException {
        time = time == null || time.isEmpty() ? "0" : time;
        try {
            int duration = 0;
            String[] timeSegments = time.split(":");
            int maxTimeSegments = timeSegments.length >= 3 ? 2 : timeSegments.length - 1; // max until hours
            for (int i = maxTimeSegments; i >= 0; i--) {
                // seconds timeSegments[0] * 60^0 + minutes + timeSegments[1] * 60^1 + ...
                duration += Integer.parseInt(timeSegments[i]) * Math.pow(60, maxTimeSegments - i);
            }
            return duration;
        } catch (NumberFormatException ex) {
            throw new CustomException("Error", "Can not parse time.");
        }
    }

    /**
     * Calculates bmi.
     *
     * @param weight weight in kg
     * @param height height in cm
     * @return bmi
     */
    public static float calculateBMI(float weight, float height) {
        return weight / (float) Math.pow(height / 100, 2);
    }

    /**
     * Calculates the maximal heart rate.
     * https://www.runtastic.com/blog/de/berechne-deine-maximale-herzfrequenz-und-ziel-herzfrequenz/
     *
     * @param age age
     * @return maximal heart rate
     */
    public static int calculateMaxHeartRate(int age) {
        return 220 - age;
    }

    /**
     * Calculates the heart rate for burning fat.
     * https://www.runtastic.com/blog/de/berechne-deine-maximale-herzfrequenz-und-ziel-herzfrequenz/
     *
     * @param age age
     * @return heart rate for fat burning
     */
    public static int calculateHeartRateFatBurning(int age) {
        return (int) Math.round(calculateMaxHeartRate(age) * 0.65);
    }

    /**
     * Calculates the heart rate for building condition.
     * https://www.runtastic.com/blog/de/berechne-deine-maximale-herzfrequenz-und-ziel-herzfrequenz/
     *
     * @param age age
     * @return heart rate for condition building
     */
    public static int calculateHeartRateBuildingCondition(int age) {
        return (int) Math.round(calculateMaxHeartRate(age) * 0.75);
    }

    /**
     * Calculates the heart rate for maximal performance.
     * https://www.runtastic.com/blog/de/berechne-deine-maximale-herzfrequenz-und-ziel-herzfrequenz/
     *
     * @param age age
     * @return heart rate for maximal performance
     */
    public static int calculateHeartRateMaxPerformance(int age) {
        return (int) Math.round(calculateMaxHeartRate(age) * 0.85);
    }
}
