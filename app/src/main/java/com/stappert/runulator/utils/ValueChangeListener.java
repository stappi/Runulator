package com.stappert.runulator.utils;

/**
 * Listener to apply entered values.
 */
public interface ValueChangeListener {

    /**
     * Applies entered value.
     *
     * @param parameter run parameter type or null
     * @param value     value
     */
    void applyValue(ParameterType parameter, Object value, Unit unit);
}
