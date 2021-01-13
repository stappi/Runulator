package com.stappert.runulator;

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
    KG("kg"),
    /**
     * Pound.
     */
    LB("lb"),

    // ============ height =========================================================================
    /**
     * Centimeter for human height.
     */
    CM("cm"),
    /**
     * Feet for human height.
     */
    FEET("ft"),
    /**
     * Inch for human height.
     */
    INCH("in"),
    /**
     * Kilometer for run distances.
     */
    KM("km"),
    /**
     * Mile for run distances.
     */
    MILE("mi");

    // =============================================================================================

    /**
     * International symbol.
     */
    private final String symbol;

    /**
     * Creates a weight unit.
     *
     * @param symbol international symbol
     */
    Unit(String symbol) {
        this.symbol = symbol;
    }

    public static List<Unit> getWeightUnits() {
        return new ArrayList<>(Arrays.asList(KG, LB));
    }

    public static List<Unit> getHeightUnits() {
        return new ArrayList<>(Arrays.asList(CM, FEET, INCH));
    }

    public static List<Unit> getUnitOfLengths() {
        return new ArrayList<>(Arrays.asList(CM, FEET, INCH, KM, MILE));
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
                throw new CustomException("error", "conversion from " + this.symbol + " to km is not supported");
        }
        return parsedWeight;
    }

    /**
     * Converts the length in kilometer.
     *
     * @param length kilometer in current unit
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
     * Converts the length in centimeter.
     *
     * @param length centimeter in current unit
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
                throw new CustomException("error", "conversion from " + this.symbol + " to km is not supported");
        }
        return parsedLength;
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
}
