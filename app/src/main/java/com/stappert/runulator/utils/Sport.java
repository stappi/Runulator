package com.stappert.runulator.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Denifes all sports.
 */
public enum Sport {

    /**
     * Run.
     */
    RUN("kg", "run"),
    /**
     * Cycle.
     */
    CYCLE("lb", "cycle"),
    /**
     * Swim.
     */
    SWIM("lb", "swim");



    // =============================================================================================
    // variables and constructor
    // =============================================================================================
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
    Sport(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    // =============================================================================================
    // methods
    // =============================================================================================

    /**
     * Returns the label.
     *
     * @param context context
     * @return label
     */
    public String getLabel(Context context) {
        return label != null ? Utils.getStringByIdName(context, label) : symbol;
    }


}
