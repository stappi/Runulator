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

    /**
     * Distance for half marathon.
     */
    public final static float HALF_MARATHON = 21.0975f;

    /**
     * Distance for marathon.
     */
    public final static float MARATHON = 42.195f;

    // =============================================================================================
    // json keys
    // =============================================================================================
    /**
     * Key 'distance' to parse in/from json.
     */
    public final static String JSON_KEY_DISTANCE = "distance";
    /**
     * Key 'duration' to parse in/from json.
     */
    public final static String JSON_KEY_DURATION = "duration";
    /**
     * Key 'pace' to parse in/from json.
     */
    public final static String JSON_KEY_PACE = "pace";
    /**
     * Key 'speed' to parse in/from json.
     */
    public final static String JSON_KEY_SPEED = "speed";

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
        return secondsToString(duration);
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
        return secondsToString(getPaceAsNumber(unit));
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
                    + getDuration() + (duration >= HOUR ? "h" : duration >= MINUTE ? "min" : "sec") + "\n("
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
        return "{" + "\'" + JSON_KEY_DISTANCE + "\':" + distance + ","
                + "\'" + JSON_KEY_DURATION + "\':" + duration + ","
                + "\'" + JSON_KEY_PACE + "\':" + pace + ","
                + "\'" + JSON_KEY_SPEED + "\':" + speed + "}";
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
        return "{" + "\'" + JSON_KEY_DISTANCE + "\':" + distance + ","
                + "\'" + JSON_KEY_DURATION + "\':" + duration + "}";
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
        return "{" + "\'" + JSON_KEY_DISTANCE + "\':" + distance + ","
                + "\'" + JSON_KEY_PACE + "\':" + pace + "}";
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
        return "{" + "\'" + JSON_KEY_DISTANCE + "\':" + distance + ","
                + "\'" + JSON_KEY_SPEED + "\':" + speed + "}";
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
        float speed = (float) HOUR / pace;
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
        return "{" + "\'" + JSON_KEY_DURATION + "\':" + duration + ","
                + "\'" + JSON_KEY_PACE + "\':" + pace + "}";
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
        return "{" + "\'" + JSON_KEY_DURATION + "\':" + duration + ","
                + "\'" + JSON_KEY_SPEED + "\':" + speed + "}";
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
        if (runJson.has(Run.JSON_KEY_DISTANCE)) {
            if (runJson.has(Run.JSON_KEY_DURATION)) {
                return Run.createWithDistanceAndDuration((float) runJson.getDouble(Run.JSON_KEY_DISTANCE),
                        runJson.getInt(Run.JSON_KEY_DURATION));
            } else if (runJson.has(Run.JSON_KEY_PACE)) {
                return Run.createWithDistanceAndPace((float) runJson.getDouble(Run.JSON_KEY_DISTANCE),
                        runJson.getInt(Run.JSON_KEY_PACE));
            } else if (runJson.has(Run.JSON_KEY_SPEED)) {
                return Run.createWithDistanceAndSpeed((float) runJson.getDouble(Run.JSON_KEY_DISTANCE),
                        (float) runJson.getDouble(Run.JSON_KEY_SPEED));
            }
        } else if (runJson.has(Run.JSON_KEY_DURATION)) {
            if (runJson.has(Run.JSON_KEY_PACE)) {
                return Run.createWithDurationAndPace(runJson.getInt(Run.JSON_KEY_DURATION),
                        runJson.getInt(Run.JSON_KEY_PACE));
            } else if (runJson.has(Run.JSON_KEY_SPEED)) {
                return Run.createWithDurationAndSpeed(runJson.getInt(Run.JSON_KEY_DURATION),
                        (float) runJson.getDouble(Run.JSON_KEY_SPEED));
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
     * Converts the given seconds in hh:mm:ss (h = hour, m = minute, s = second).
     *
     * @param totalSeconds time in seconds
     * @return total seconds in hh:mm:ss
     */
    public static String secondsToString(int totalSeconds) {
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
