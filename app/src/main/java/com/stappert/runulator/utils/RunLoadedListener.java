package com.stappert.runulator.utils;

/**
 * Listener to apply entered values.
 */
public interface RunLoadedListener {

    /**
     * Applies entered value.
     *
     * @param run selected run as json string
     */
    public void applyRun(String run);
}
