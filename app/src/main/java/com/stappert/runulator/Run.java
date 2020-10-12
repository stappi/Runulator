package com.stappert.runulator;

import java.util.Objects;

/**
 * Defines a run.
 */
public class Run {

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
    public Run(float distance, int duration, int pace, float speed) {
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
        return String.format("%." + (noOfDecimalPlaces < 4 ? noOfDecimalPlaces : 4) + "f", distance);
    }

    /**
     * Returns in the duration in hh:mm:ss as string (h = hour, m = minute, s = second).
     *
     * @return duration
     */
    public String getDuration() {
        return secondsToString(duration);
    }

    /**
     * Returns in the pace in hh:mm:ss as string (h = hour, m = minute, s = second).
     *
     * @return pace
     */
    public String getPace() {
        return secondsToString(pace);
    }

    /**
     * Returns in the speed in km/h as string with maximal 4 decimal places.
     *
     * @return speed in km/h
     */
    public String getSpeed() {
        return String.format("%.2f", speed);
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
     */
    public String getForecast(float fatigueCoefficient) {
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
     */
    public String getForecast(float forecastDistance, float fatigueCoefficient) {
        return "~ " + RunUtils.createWithDistanceAndDuration(forecastDistance,
                (int) (duration * Math.pow(forecastDistance / distance, fatigueCoefficient))).toString();
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
                + getDuration() + (duration >= 60 * 60 ? "h" : duration >= 60 ? "min" : "sec") + "\n("
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
        int hours = totalSeconds / 60 / 60;
        int minutes = totalSeconds / 60 % 60;
        int seconds = totalSeconds % 60 % 60;
        return (hours > 0 ? hours + ":" : "")
                + (minutes >= 10 ? minutes : "0" + minutes)
                + ":" + (seconds >= 10 ? seconds : "0" + seconds);
    }
}
