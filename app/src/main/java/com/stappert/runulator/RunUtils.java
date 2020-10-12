package com.stappert.runulator;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for create and manage runs.
 */
public class RunUtils {

    // =============================================================================================
    // create runs depending on parameters
    // =============================================================================================

    /**
     * Creates a run depending on distance in km and duration in seconds.
     *
     * @param distance in km
     * @param duration in seconds
     * @return run
     */
    public static Run createWithDistanceAndDuration(float distance, int duration) {
        int pace = duration / (int) distance;
        float speed = distance * 60 * 60 / duration;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on distance in km and pace in seconds.
     *
     * @param distance in km
     * @param pace     in seconds
     * @return run
     */
    public static Run createWithDistanceAndPace(float distance, int pace) {
        int duration = (int) distance * pace;
        float speed = 60f * 60 / pace;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on distance in km and speed in km/h.
     *
     * @param distance in km
     * @param speed    in km/h
     * @return run
     */
    public static Run createWithDistanceAndSpeed(float distance, float speed) {
        int duration = (int) (distance / speed * 60 * 60);
        int pace = 60 * 60 / (int) speed;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on duration in seconds and pace in seconds.
     *
     * @param duration in seconds
     * @param pace     in seconds
     * @return run
     */
    public static Run createWithDurationAndPace(int duration, int pace) {
        float distance = duration / pace;
        float speed = 60f * 60 / pace;
        return new Run(distance, duration, pace, speed);
    }

    /**
     * Creates a run depending on duration in seconds and speed in km/h.
     *
     * @param duration in seconds
     * @param speed    in km/h
     * @return run
     */
    public static Run createWithDurationAndSpeed(int duration, float speed) {
        float distance = duration * speed;
        int pace = 60 * 60 / (int) speed;
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
    public static float parseToFloat(String number) throws Exception {
        try {
            return Float.parseFloat(number.replace(",", "."));
        } catch (Exception ex) {
            throw new Exception(number + " is not a number. Reset to default value.");
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
    public static int parseTimeInSeconds(String time) {
        int duration = 0;
        String[] timeSegments = time.split(":");
        int maxTimeSegments = timeSegments.length >= 3 ? 2 : timeSegments.length - 1; // max until hours
        for (int i = maxTimeSegments; i >= 0; i--) {
            // seconds timeSegments[0] * 60^0 + minutes + timeSegments[1] * 60^1 + ...
            duration += Integer.parseInt(timeSegments[i]) * Math.pow(60, maxTimeSegments - i);
        }
        return duration;
    }
}