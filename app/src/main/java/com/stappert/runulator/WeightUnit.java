package com.stappert.runulator;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Denifes all available weight unit in the application.
 */
public enum WeightUnit {

    /**
     * Kilogram.
     */
    KG("kg"),
    /**
     * Pound.
     */
    LB("lb");

    /**
     * International symbol.
     */
    private final String symbol;

    /**
     * Creates a weight unit.
     *
     * @param symbol international symbol
     */
    WeightUnit(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Converts the weight in kilogram. Rounds the value.
     *
     * @param weight kilogram in current unit
     * @return weight in kilogram
     */
    public int toKg(int weight) {
        return KG.equals(this) ? weight : Math.round(weight / 2.20462f);
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
     * Formats the given weight with the international symbol of the unit.
     *
     * @return weight + symbol
     */
    public String formatWeight(int weight) {
        return weight + " " + symbol;
    }
}
