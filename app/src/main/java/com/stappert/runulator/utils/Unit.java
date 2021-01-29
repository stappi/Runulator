package com.stappert.runulator.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Denifes all available weight unit in the application.
 */
public enum Unit {

    // ============ weight =========================================================================
    /**
     * Kilogram.
     */
    KG("kg", "kilogram"),
    /**
     * Pound.
     */
    LB("lb", "pound"),

    // ============ length =========================================================================
    /**
     * Centimeter for human height.
     */
    CM("cm", "centimeter"),
    /**
     * Feet for human height.
     */
    FEET("ft", "feet"),
    /**
     * Inch for human height.
     */
    INCH("in", "inch"),
    /**
     * Kilometer for run distances.
     */
    KM("km", "kilometer"),
    /**
     * Mile for run distances.
     */
    MILE("mi", "mile"),
    // ============ pace ===========================================================================
    /**
     * Kilometer for run distances. Note, that values are on seconds level.
     */
    MIN_KM("min:sec/km", "minutes_per_km"),
    /**
     * Mile for run distances. Note, that values are on seconds level.
     */
    MIN_MILE("min:sec/mi", "minutes_per_mile"),
    // ============ speed ==========================================================================
    /**
     * Speed in km per hour.
     */
    KM_H("km/h", "kilometer_per_hour"),
    /**
     * Speed in miles per hour.
     */
    MPH("mph", "miles_per_hour"),
    // ============ time ===========================================================================
    /**
     * Hour in form hh:mm:ss. Note, that values are on seconds level.
     */
    HOUR("h:min:sec", null),
    /**
     * Minutes in form mm:ss. Note, that values are on seconds level.
     */
    MINUTE("min:sec", null),

    // ============ default ========================================================================
    /**
     * Default unit.
     */
    DEFAULT("", null);

    /**
     * International symbol.
     */
    private final String symbol;

    /**
     * String key for label. Null if no label exists.
     */
    private final String label;

    /**
     * Creates a weight unit.
     *
     * @param symbol international symbol
     */
    Unit(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    /**
     * Returns the label.
     *
     * @param context context
     * @return label
     */
    public String getLabel(Context context) {
        return label != null ? Utils.getStringByIdName(context, label) : symbol;
    }


    /**
     * Converts the weight in kilogram. Rounds the value.
     *
     * @param weight kilogram in current unit
     * @return weight in kilogram
     * @throws CustomException if conversion for unit is not supported
     */
    public int toKg(int weight) throws CustomException {
        int parsedWeight = weight;
        switch (this) {
            case KG:
                break;
            case LB:
                parsedWeight = Math.round(weight / 2.20462f);
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to kg is not supported");
        }
        return parsedWeight;
    }

    /**
     * Converts the length in centimeter.
     *
     * @param length length in current unit
     * @return length in centimeter
     * @throws CustomException if conversion for unit is not supported
     */
    public float toCm(float length) throws CustomException {
        float parsedLength = length;
        switch (this) {
            case CM:
                parsedLength = length;
                break;
            case FEET:
                parsedLength = length * 30.48f;
                break;
            case INCH:
                parsedLength = length * 2.54f;
                break;
            case KM:
                parsedLength = length * 1000 * 100;
                break;
            case MILE:
                parsedLength = length * 160934f;
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to cm is not supported");
        }
        return parsedLength;
    }

    /**
     * Converts the length in kilometer.
     *
     * @param length length in current unit
     * @return length in kilometer
     * @throws CustomException if conversion for unit is not supported
     */
    public float toKm(float length) throws CustomException {
        float parsedLength = length;
        switch (this) {
            case CM:
                parsedLength = length / 1000 / 100;
                break;
            case KM:
                break;
            case FEET:
                parsedLength = length / 3280.84f;
                break;
            case INCH:
                parsedLength = length / 39370.1f;
                break;
            case MILE:
                parsedLength = length * 1.60934f;
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to km is not supported");
        }
        return parsedLength;
    }

    /**
     * Converts kilometer length to desired unit.
     *
     * @param km length in kilometer
     * @return length in desired unit
     * @throws CustomException if conversion for unit is not supported
     */
    public float kmTo(float km) throws CustomException {
        float parsedLength = km;
        switch (this) {
            case CM:
                parsedLength = km * 1000 * 100;
                break;
            case KM:
                break;
            case FEET:
                parsedLength = km * 3280.84f;
                break;
            case INCH:
                parsedLength = km * 39370.1f;
                break;
            case MILE:
                parsedLength = km / 1.60934f;
                break;
            default:
                throw new CustomException("error", "conversion from km to " + this.symbol + " is not supported");
        }
        return parsedLength;
    }

    /**
     * Converts the pace in minutes per hour. Note, that pace is on seconds level.
     *
     * @param paceOrSpeed pace or speed in current unit
     * @return pace in kilometer per hour
     * @throws CustomException if conversion for unit is not supported
     */
    public int toMinPerKm(float paceOrSpeed) throws CustomException {
        if (paceOrSpeed < 0) {
            throw new CustomException("error", "speed or pace " + paceOrSpeed + " must be greater than 0");
        }
        float parsedPace = paceOrSpeed;
        switch (this) {
            case KM_H:
                parsedPace = (60 * 60) / paceOrSpeed;
                break;
            case MPH:
                parsedPace = (60 * 60 / paceOrSpeed) * 1.60934f;
            case MIN_KM:
                break;
            case MIN_MILE:
                parsedPace = paceOrSpeed / 1.60934f;
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to minutes per kilometer is not supported");
        }
        return (int) Math.round(parsedPace);
    }

    /**
     * Converts the pace from minutes per hour to desired unit. Note, that pace is on seconds level.
     *
     * @param pace seconds per minute
     * @return pace in desired unit
     * @throws CustomException if conversion for unit is not supported
     */
    public float minPerKmTo(int pace) throws CustomException {
        if (pace < 0) {
            throw new CustomException("error", "speed or pace " + pace + " must be greater than 0");
        }
        float parsedPace = pace;
        switch (this) {
            case KM_H:
                parsedPace = Run.HOUR / pace;
                break;
            case MPH:
                parsedPace = Run.HOUR / pace * 1.60934f;
            case MIN_KM:
                break;
            case MIN_MILE:
                parsedPace = (int) Math.round(pace * 1.60934f);
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to minutes per kilometer is not supported");
        }
        return parsedPace;
    }

    /**
     * Converts the speed in kilometer per hour. Note, that pace is on seconds level.
     *
     * @param speedOrPace speed or pace in current unit
     * @return speed in kilometer per hour
     * @throws CustomException if conversion for unit is not supported
     */
    public float toKmPerHour(float speedOrPace) throws CustomException {
        if (speedOrPace < 0) {
            throw new CustomException("error", "speed or pace " + speedOrPace + " must be greater than 0");
        }
        float parsedSpeed = speedOrPace;
        switch (this) {
            case KM_H:
                break;
            case MPH:
                parsedSpeed = speedOrPace * 1.60934f;
                break;
            case MIN_KM:
                parsedSpeed = Run.HOUR / speedOrPace;
                break;
            case MIN_MILE:
                parsedSpeed = Run.HOUR / speedOrPace * 1.60934f;
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to km/h is not supported");
        }
        return parsedSpeed;
    }

    /**
     * Converts speed from kilometer per hour speed to desired unit.
     *
     * @param speed kilometer per hour
     * @return speed in desired unit
     * @throws CustomException if conversion for unit is not supported
     */
    public float kmPerHourTo(float speed) throws CustomException {
        if (speed < 0) {
            throw new CustomException("error", "speed or pace " + speed + " must be greater than 0");
        }
        float parsedSpeed = speed;
        switch (this) {
            case KM_H:
                break;
            case MPH:
                parsedSpeed = speed / 1.60934f;
                break;
            case MIN_KM:
                parsedSpeed = Run.HOUR / speed;
                break;
            case MIN_MILE:
                parsedSpeed = Run.HOUR / speed * 1.60934f;
                break;
            default:
                throw new CustomException("error", "conversion from " + this.symbol + " to km/h is not supported");
        }
        return parsedSpeed;
    }

    /**
     * Returns the international symbol of the unit.
     *
     * @return unit symbol
     */
    public String toString() {
        return symbol;
    }

    /**
     * Formats the given value with the international symbol of the unit.
     *
     * @return value + symbol
     */
    public String format(int value) {
        return value + " " + symbol;
    }

    /**
     * Returns all weight units.
     *
     * @return list of weight units
     */
    public static List<Unit> getWeightUnits() {
        return new ArrayList<>(Arrays.asList(KG, LB));
    }

    /**
     * Returns all units to set the height.
     *
     * @return list of height units
     */
    public static List<Unit> getHeightUnits() {
        return new ArrayList<>(Arrays.asList(CM, FEET, INCH));
    }

    /**
     * Returns all units to set distances.
     *
     * @return distance units
     */
    public static List<Unit> getDistanceUnits() {
        return new ArrayList<>(Arrays.asList(KM, MILE));
    }
}
