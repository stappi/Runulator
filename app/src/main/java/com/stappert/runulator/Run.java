package com.stappert.runulator;

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
     * One second.
     */
    public final static int SECOND = 1;

    /**
     * One minute in seconds.
     */
    public final static int MINUTE = 60;

    /**
     * One hour in seconds.
     */
    public final static int HOUR = 60 * 60;

    // =============================================================================================
    // json keys
    // =============================================================================================
    /**
     * Key 'distance' to parse in/from json.
     */
    public final static String KEY_DISTANCE = "distance";
    /**
     * Key 'duration' to parse in/from json.
     */
    public final static String KEY_DURATION = "duration";
    /**
     * Key 'pace' to parse in/from json.
     */
    public final static String KEY_PACE = "pace";
    /**
     * Key 'speed' to parse in/from json.
     */
    public final static String KEY_SPEED = "speed";

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
     * Returns in the distance in km as string with maximal 4 decimal places.
     *
     * @return distance in km
     */
    public String getDistance() {
        int noOfDecimalPlaces = Float.toString(distance).split("\\.")[1].length();
        return String.format(Locale.ENGLISH, "%." + (noOfDecimalPlaces < 4 ? noOfDecimalPlaces : 4) + "f", distance);
    }

    /**
     * Returns the distance as number.
     *
     * @return distance
     */
    public float getDistanceAsNumber() {
        return distance;
    }

    /**
     * Returns the duration in hh:mm:ss as string (h = hour, m = minute, s = second).
     *
     * @return duration
     */
    public String getDuration() {
        return secondsToString(duration);
    }

    /**
     * Returns the duration in seconds.
     *
     * @return duration
     */
    public int getDurationInSeconds() {
        return duration;
    }

    /**
     * Returns the pace in hh:mm:ss as string (h = hour, m = minute, s = second).
     *
     * @return pace
     */
    public String getPace() {
        return secondsToString(pace);
    }

    /**
     * Returns the pace.
     *
     * @return pace
     */
    public int getPaceInSeconds() {
        return pace;
    }

    /**
     * Returns in the speed in km/h as string with maximal 4 decimal places.
     *
     * @return speed in km/h
     */
    public String getSpeed() {
        int noOfDecimalPlaces = Float.toString(speed).split("\\.")[1].length();
        return String.format(Locale.ENGLISH, "%." + (noOfDecimalPlaces <= 1 ? 1 : 2) + "f", speed);
    }

    /**
     * Returns in the speed in km/h as string with maximal 4 decimal places.
     *
     * @return speed in km/h
     */
    public float getSpeedAsNumber() {
        return speed;
    }

    /**
     * Calculates the number of calories burned.
     *
     * @param weight
     * @return
     */
    public String getCalories(int weight) {
        return "~" + (int) (distance * weight * 0.9f) + " kcal";
    }

    /**
     * Calculates a forecast for several distances with formula t2 = t1 x ( d2 / d1 )^k by Peter
     * Riegel.
     *
     * @param fatigueCoefficient fatigue coefficient
     * @throws CustomException if forecast gets values not greater than 0
     */
    public String getForecast(float fatigueCoefficient) throws CustomException {
        return getForecast(DISTANCE_5KM, fatigueCoefficient) + "\n\n"
                + getForecast(DISTANCE_10KM, fatigueCoefficient) + "\n\n"
                + getForecast(DISTANCE_HM, fatigueCoefficient) + "\n\n"
                + getForecast(DISTANCE_M, fatigueCoefficient);
    }

    /**
     * Calculates a forecast for a desired distance with formula t2 = t1 x ( d2 / d1 )^k by Peter
     * Riegel.
     *
     * @param forecastDistance   desired distance for forecast
     * @param fatigueCoefficient fatigue coefficient
     * @throws CustomException if forecast gets values not greater than 0
     */
    public String getForecast(float forecastDistance, float fatigueCoefficient) throws CustomException {
        return "~ " + createWithDistanceAndDuration(forecastDistance,
                (int) (duration * Math.pow(forecastDistance / distance, fatigueCoefficient))).toString();
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
        return Float.compare(run.distance, distance) == 0 &&
                duration == run.duration;
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
        return getDistance() + "KM in "
                + getDuration() + (duration >= HOUR ? "h" : duration >= MINUTE ? "min" : "sec") + "\n("
                + getPace() + "min/km; "
                + getSpeed() + "km/h)";
    }

    /**
     * Converts run to json string.
     *
     * @return run as json string
     */
    public String toJson() {
        return "{" + "\'" + KEY_DISTANCE + "\':" + distance + ","
                + "\'" + KEY_DURATION + "\':" + duration + ","
                + "\'" + KEY_PACE + "\':" + pace + ","
                + "\'" + KEY_SPEED + "\':" + speed + "}";
    }

    // =============================================================================================
    // private functions
    // =============================================================================================

    /**
     * Converts the given seconds in hh:mm:ss (h = hour, m = minute, s = second).
     *
     * @param totalSeconds time in seconds
     * @return total seconds in hh:mm:ss
     */
    private String secondsToString(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0";
        } else {
            int hours = totalSeconds / HOUR;
            int minutes = totalSeconds / MINUTE % MINUTE;
            int seconds = totalSeconds % MINUTE % MINUTE;
            return (hours > 0 ? hours + ":" : "")
                    + (hours > 0 && 10 > minutes ? "0" + minutes + ":" : minutes > 0 ? minutes + ":" : "")
                    + (hours + minutes > 0 && 10 > seconds ? "0" + seconds : seconds);
        }
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
        float speed = distance * HOUR / duration;
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
    public static Run createWithDistanceAndPace(float distance, int pace)
            throws CustomException {
        if (distance <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        int duration = Math.round(distance * pace);
        float speed = (float) HOUR / pace;
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
    public static Run createWithDistanceAndSpeed(float distance, float speed)
            throws CustomException {
        if (distance <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        int duration = Math.round(distance / speed * HOUR);
        int pace = Math.round(HOUR / speed);
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
    public static Run createWithDurationAndPace(int duration, int pace)
            throws CustomException {
        if (duration <= 0 || pace <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        float distance = duration / pace;
        float speed = (float) HOUR / pace;
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
    public static Run createWithDurationAndSpeed(int duration, float speed)
            throws CustomException {
        if (duration <= 0 || speed <= 0) {
            throw new CustomException("Error", "values must be greater than 0");
        }
        float distance = duration * speed / HOUR;
        int pace = Math.round(HOUR / speed);
        return new Run(distance, duration, pace, speed);
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
                JSONObject runJson = new JSONObject(runJsonString);
                runs.add(new Run(
                        (float) runJson.getDouble(Run.KEY_DISTANCE),
                        runJson.getInt(Run.KEY_DURATION),
                        runJson.getInt(Run.KEY_PACE),
                        (float) runJson.getDouble(Run.KEY_SPEED)
                ));
            }
        } catch (JSONException ex) {
            Log.e("Error", ex.getMessage());
        }
        return runs;
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
}
