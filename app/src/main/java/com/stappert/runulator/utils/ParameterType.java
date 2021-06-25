package com.stappert.runulator.utils;

import com.stappert.runulator.R;

public enum ParameterType {
    DISTANCE, DURATION, PACE, SPEED, WEIGHT, HEIGHT;

    /**
     * Returns the label regarding to parameter type.
     *
     * @return label
     */
    public int getLabel() {
        switch (this) {
            case DISTANCE:
                return R.string.distance;
            case DURATION:
                return R.string.run_time;
            case PACE:
                return R.string.pace;
            case SPEED:
                return R.string.speed;
            case WEIGHT:
                return R.string.weight;
            case HEIGHT:
                return R.string.height;
            default:
                return R.string.unknown;
        }
    }

    /**
     * Returns the unit regarding to parameter type.
     *
     * @return unit
     */
    public Unit getUnit(SettingsManager settings) {
        switch (this) {
            case DISTANCE:
                return settings.getDistanceUnit();
            case DURATION:
                return settings.getDurationUnit();
            case PACE:
                return settings.getPaceUnit();
            case SPEED:
                return settings.getSpeedUnit();
            case WEIGHT:
                return settings.getWeightUnit();
            case HEIGHT:
                return settings.getHeightUnit();
            default:
                return Unit.DEFAULT;
        }
    }
}

