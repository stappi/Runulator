package com.stappert.runulator;

import android.util.Log;
import android.widget.Toast;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests class Run and RunUtils (companion object).
 */
public class TestRun {


    /**
     * For expected exceptions.
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    // =============================================================================================
    // test static functions
    // =============================================================================================
    @Test
    public void testParseTimeInSeconds() throws CustomException {
        // test correct values
        assertEquals(0, Run.parseTimeInSeconds("0"));
        assertEquals(0, Run.parseTimeInSeconds("00:00"));
        assertEquals(Run.MINUTE - Run.SECOND, Run.parseTimeInSeconds("59"));
        assertEquals(Run.MINUTE, Run.parseTimeInSeconds("1:00"));
        assertEquals(Run.MINUTE + Run.SECOND, Run.parseTimeInSeconds("1:01"));
        assertEquals(Run.MINUTE + 9 * Run.SECOND, Run.parseTimeInSeconds("1:09"));
        assertEquals(Run.MINUTE + 10 * Run.SECOND, Run.parseTimeInSeconds("1:10"));
        assertEquals(10 * Run.MINUTE - Run.SECOND, Run.parseTimeInSeconds("9:59"));
        assertEquals(10 * Run.MINUTE, Run.parseTimeInSeconds("10:00"));
        assertEquals(10 * Run.MINUTE + Run.SECOND, Run.parseTimeInSeconds("10:01"));
        assertEquals(Run.HOUR - Run.SECOND, Run.parseTimeInSeconds("59:59"));
        assertEquals(Run.HOUR - Run.SECOND, Run.parseTimeInSeconds("0:59:59"));
        assertEquals(Run.HOUR - Run.SECOND, Run.parseTimeInSeconds("00:59:59"));
        assertEquals(Run.HOUR, Run.parseTimeInSeconds("1:00:00"));
        assertEquals(Run.HOUR + Run.SECOND, Run.parseTimeInSeconds("1:00:01"));
        assertEquals(Run.HOUR + Run.MINUTE + Run.SECOND, Run.parseTimeInSeconds("1:01:01"));
        assertEquals(10 * Run.HOUR - Run.SECOND, Run.parseTimeInSeconds("9:59:59"));
        assertEquals(10 * Run.HOUR, Run.parseTimeInSeconds("10:00:00"));
        assertEquals(10 * Run.HOUR + Run.SECOND, Run.parseTimeInSeconds("10:00:01"));
        assertEquals(101 * Run.HOUR - Run.SECOND, Run.parseTimeInSeconds("100:59:59"));

        // special values
        assertEquals(Run.HOUR + 10 * Run.MINUTE, Run.parseTimeInSeconds("70:00"));
        assertEquals(3 * Run.HOUR, Run.parseTimeInSeconds("1:120:00"));
        assertEquals(Run.HOUR + 10 * Run.MINUTE, Run.parseTimeInSeconds("4200"));
        assertEquals(Run.HOUR + 10 * Run.MINUTE, Run.parseTimeInSeconds("0:0:4200"));
        assertEquals(6 * Run.HOUR, Run.parseTimeInSeconds("1:120:10800"));

        // expect exceptions
        exception.expect(CustomException.class);
        Run.parseTimeInSeconds("::00");
        Run.parseTimeInSeconds(":::");
        Run.parseTimeInSeconds("");
        Run.parseTimeInSeconds("weer24");
        Run.parseTimeInSeconds("1,45,11");
        Run.parseTimeInSeconds(null);
    }

    @Test
    public void testParseToFloat() throws CustomException {
        // test correct values
        assertEquals(-1f, Run.parseToFloat("-1"), 0f);
        assertEquals(0f, Run.parseToFloat("0"), 0f);
        assertEquals(0.00001f, Run.parseToFloat("0.00001"), 0f);
        assertEquals(5, Run.parseToFloat("5"), 0f);
        assertEquals(10f, Run.parseToFloat("10"), 0f);
        assertEquals(21.0975f, Run.parseToFloat("21.0975"), 0f);
        assertEquals(42.195f, Run.parseToFloat("42.195"), 0f);

        // expect exceptions
        exception.expect(CustomException.class);
        Run.parseToFloat("::00");
        Run.parseToFloat("0..0");
        Run.parseToFloat("0,0");
        Run.parseToFloat("");
        Run.parseToFloat("weer24");
        Run.parseToFloat("1,45,11");
        Run.parseToFloat("1.45.31");
        Run.parseToFloat(null);
    }

    @Test
    public void testCreateWithDurationAndSpeed() throws CustomException {
        // 2h with 11 km/h
        Run run_1 = Run.createWithDurationAndSpeed(2 * Run.HOUR, 11);
        assertEquals("22.0", run_1.getDistance());
        assertEquals("2:00:00", run_1.getDuration());
        assertEquals("5:27", run_1.getPace());
        assertEquals("11.0", run_1.getSpeed());

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDurationAndSpeed(0 * Run.HOUR, 12);
        Run.createWithDurationAndSpeed(-1 * Run.HOUR, 12);
        Run.createWithDurationAndSpeed(1 * Run.HOUR, 0);
        Run.createWithDurationAndSpeed(1 * Run.HOUR, -5);
        Run.createWithDurationAndSpeed(0 * Run.HOUR, -5);
    }

    @Test
    public void testCreateWithDurationAndPace() throws CustomException {
        // 1h10min with 5:00 pace
        Run run_1 = Run.createWithDurationAndPace(Run.HOUR + 10 * Run.MINUTE, 5 * Run.MINUTE);
        assertEquals("14.0", run_1.getDistance());
        assertEquals("1:10:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace());
        assertEquals("12.0", run_1.getSpeed());

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDurationAndPace(0 * Run.HOUR, 1 * Run.MINUTE);
        Run.createWithDurationAndPace(-1 * Run.HOUR, 12 * 1 * Run.MINUTE);
        Run.createWithDurationAndPace(1 * Run.HOUR, 0 * Run.MINUTE);
        Run.createWithDurationAndPace(1 * Run.HOUR, -5 * Run.MINUTE);
        Run.createWithDurationAndPace(0 * Run.HOUR, -5 * Run.MINUTE);
    }

    @Test
    public void testCreateWithDistanceAndSpeed() throws CustomException {
        // 10 km with 10.91 km/h
        Run run_1 = Run.createWithDistanceAndSpeed(10, 10.91f);
        assertEquals("10.0", run_1.getDistance());
        assertEquals("55:00", run_1.getDuration());
        assertEquals("5:30", run_1.getPace());
        assertEquals("10.91", run_1.getSpeed());

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDistanceAndSpeed(0, 10);
        Run.createWithDistanceAndSpeed(-1, 12);
        Run.createWithDistanceAndSpeed(10, 0);
        Run.createWithDistanceAndSpeed(10, -5);
        Run.createWithDistanceAndSpeed(0, -5);
    }

    @Test
    public void testCreateWithDistanceAndPace() throws CustomException {
        // 10 km with 5:00 pace
        Run run_1 = Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00"));
        assertEquals("10.0", run_1.getDistance());
        assertEquals("1:00:00", run_1.getDuration());
        assertEquals("6:00", run_1.getPace());
        assertEquals("10.0", run_1.getSpeed());

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDistanceAndPace(0, 5 * Run.MINUTE);
        Run.createWithDistanceAndPace(-1, 6 * Run.MINUTE);
        Run.createWithDistanceAndPace(10, 0 * Run.MINUTE);
        Run.createWithDistanceAndPace(10, -5 * Run.MINUTE);
        Run.createWithDistanceAndPace(0, -5 * Run.MINUTE);
    }

    @Test
    public void testCreateWithDistanceAndDuration() throws CustomException {
        // 10 km in 50 minutes
        Run run_1 = Run.createWithDistanceAndDuration(10, 50 * Run.MINUTE);
        assertEquals("10.0", run_1.getDistance());
        assertEquals("50:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace());
        assertEquals("12.0", run_1.getSpeed());

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDistanceAndDuration(0, 50 * Run.MINUTE);
        Run.createWithDistanceAndDuration(-1, 60 * Run.MINUTE);
        Run.createWithDistanceAndDuration(10, 0 * Run.MINUTE);
        Run.createWithDistanceAndDuration(10, -50 * Run.MINUTE);
        Run.createWithDistanceAndDuration(0, -50 * Run.MINUTE);
    }

    @Test
    public void testRunsToJson() {
        try {
            Collection<Run> runs = new HashSet<>(Arrays.asList(
                    Run.createWithDurationAndSpeed(2 * Run.HOUR, 11),
                    Run.createWithDurationAndPace(Run.HOUR + 10 * Run.MINUTE, 5 * Run.MINUTE),
                    Run.createWithDistanceAndSpeed(10, 10.91f),
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")),
                    Run.createWithDistanceAndDuration(10, 50 * Run.MINUTE)));
            Set<String> expectedRuns = new HashSet<>(Arrays.asList(
                    "{'distance':22.0,'duration':7200,'pace':327,'speed':11.0}",
                    "{'distance':14.0,'duration':4200,'pace':300,'speed':12.0}",
                    "{'distance':10.0,'duration':3300,'pace':330,'speed':10.91}",
                    "{'distance':10.0,'duration':3600,'pace':360,'speed':10.0}",
                    "{'distance':10.0,'duration':3000,'pace':300,'speed':12.0}"));
            Assert.assertEquals(expectedRuns, Run.runsToJson(runs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJsonToRuns() {
        try {
            Set<String> runs = new HashSet<>(Arrays.asList(
                    "{'distance':22.0,'duration':7200,'pace':327,'speed':11.0}",
                    "{'distance':14.0,'duration':4200,'pace':300,'speed':12.0}",
                    "{'distance':10.0,'duration':3300,'pace':330,'speed':10.91}",
                    "{'distance':10.0,'duration':3600,'pace':360,'speed':10.0}",
                    "{'distance':10.0,'duration':3000,'pace':300,'speed':12.0}"));
            Set<Run> expectedRuns = new HashSet<>(Arrays.asList(
                    Run.createWithDurationAndSpeed(2 * Run.HOUR, 11),
                    Run.createWithDurationAndPace(Run.HOUR + 10 * Run.MINUTE, 5 * Run.MINUTE),
                    Run.createWithDistanceAndSpeed(10, 10.91f),
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")),
                    Run.createWithDistanceAndDuration(10, 50 * Run.MINUTE)));
            Assert.assertEquals(expectedRuns, Run.jsonToRuns(runs).stream().collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================================================================
    // test private functions
    // =============================================================================================
    @Test
    public void testSecondsToString() {
        try {
            Run defaultRun = Run.createWithDistanceAndDuration(10, 50 * Run.MINUTE);
            // make method secondsToString public
            Method secondsToString =
                    Run.class.getDeclaredMethod("secondsToString", int.class);
            secondsToString.setAccessible(true);

            // Tests
            assertEquals("0", secondsToString.invoke(defaultRun, -1));
            assertEquals("0", secondsToString.invoke(defaultRun, 0));
            assertEquals("59", secondsToString.invoke(defaultRun, Run.MINUTE - Run.SECOND));
            assertEquals("1:00", secondsToString.invoke(defaultRun, Run.MINUTE));
            assertEquals("1:01", secondsToString.invoke(defaultRun, Run.MINUTE + Run.SECOND));
            assertEquals("1:09", secondsToString.invoke(defaultRun, Run.MINUTE + 9 * Run.SECOND));
            assertEquals("1:10", secondsToString.invoke(defaultRun, Run.MINUTE + 10 * Run.SECOND));
            assertEquals("9:59", secondsToString.invoke(defaultRun, 10 * Run.MINUTE - Run.SECOND));
            assertEquals("10:00", secondsToString.invoke(defaultRun, 10 * Run.MINUTE));
            assertEquals("10:01", secondsToString.invoke(defaultRun, 10 * Run.MINUTE + Run.SECOND));
            assertEquals("59:59", secondsToString.invoke(defaultRun, Run.HOUR - Run.SECOND));
            assertEquals("1:00:00", secondsToString.invoke(defaultRun, Run.HOUR));
            assertEquals("1:00:01", secondsToString.invoke(defaultRun, Run.HOUR + Run.SECOND));
            assertEquals("1:01:01", secondsToString.invoke(defaultRun, Run.HOUR + Run.MINUTE + Run.SECOND));
            assertEquals("9:59:59", secondsToString.invoke(defaultRun, 10 * Run.HOUR - Run.SECOND));
            assertEquals("10:00:00", secondsToString.invoke(defaultRun, 10 * Run.HOUR));
            assertEquals("10:00:01", secondsToString.invoke(defaultRun, 10 * Run.HOUR + Run.SECOND));
            assertEquals("100:59:59", secondsToString.invoke(defaultRun, 101 * Run.HOUR - Run.SECOND));

            // make method secondsToString private
            secondsToString.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================================================================
    // test public functions
    // =============================================================================================
    @Test
    public void testToJson() {
        try {
            Assert.assertEquals("{'distance':22.0,'duration':7200,'pace':327,'speed':11.0}",
                    Run.createWithDurationAndSpeed(2 * Run.HOUR, 11).toJson());
            Assert.assertEquals("{'distance':14.0,'duration':4200,'pace':300,'speed':12.0}",
                    Run.createWithDurationAndPace(Run.HOUR + 10 * Run.MINUTE, 5 * Run.MINUTE).toJson());
            Assert.assertEquals("{'distance':10.0,'duration':3300,'pace':330,'speed':10.91}",
                    Run.createWithDistanceAndSpeed(10, 10.91f).toJson());
            Assert.assertEquals("{'distance':10.0,'duration':3600,'pace':360,'speed':10.0}",
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")).toJson());
            Assert.assertEquals("{'distance':10.0,'duration':3000,'pace':300,'speed':12.0}",
                    Run.createWithDistanceAndDuration(10, 50 * Run.MINUTE).toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDistance() {
        try {
            Assert.assertEquals("10.0",
                    Run.createWithDistanceAndSpeed(10, 10).getDistance());
            Assert.assertEquals("10.0",
                    Run.createWithDistanceAndSpeed(10.0f, 10).getDistance());
            Assert.assertEquals("10.75",
                    Run.createWithDistanceAndSpeed(10.75f, 10).getDistance());
            Assert.assertEquals("21.0975",
                    Run.createWithDistanceAndSpeed(21.0975f, 10).getDistance());
            Assert.assertEquals("34.3434",
                    Run.createWithDistanceAndSpeed(34.34344334f, 10).getDistance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDuration() {
        try {
            Assert.assertEquals("59:59",
                    Run.createWithDistanceAndDuration(10, Run.parseTimeInSeconds("0:59:59")).getDuration());
            Assert.assertEquals("1:00:00",
                    Run.createWithDistanceAndDuration(10, Run.parseTimeInSeconds("1:00:00")).getDuration());
            Assert.assertEquals("1:01:01",
                    Run.createWithDistanceAndDuration(10, Run.HOUR + Run.MINUTE + Run.SECOND).getDuration());
            // further test are tested with testSecondsToString()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}