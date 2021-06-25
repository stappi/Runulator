package com.stappert.runulator;

import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.utils.Run;
import com.stappert.runulator.utils.Unit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
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
        assertEquals("22.0", run_1.getDistance(Unit.KM));
        assertEquals("13.6702", run_1.getDistance(Unit.MILE));
        assertEquals(22, run_1.getDistanceAsNumber(Unit.KM), 0.1f);
        assertEquals(13.6702, run_1.getDistanceAsNumber(Unit.MILE), 0.1f);
        assertEquals("2:00:00", run_1.getDuration());
        assertEquals(2 * Run.HOUR, run_1.getDurationAsNumber());
        assertEquals("5:27", run_1.getPace(Unit.MIN_KM));
        assertEquals("8:46", run_1.getPace(Unit.MIN_MILE));
        assertEquals(5 * Run.MINUTE + 27 * Run.SECOND, run_1.getPaceAsNumber(Unit.MIN_KM));
        assertEquals(8 * Run.MINUTE + 46 * Run.SECOND, run_1.getPaceAsNumber(Unit.MIN_MILE));
        assertEquals("11.0", run_1.getSpeed(Unit.KM_H));
        assertEquals("6.84", run_1.getSpeed(Unit.MPH));
        assertEquals(11.0f, run_1.getSpeedAsNumber(Unit.KM_H), 0.0001f);
        assertEquals(6.8351f, run_1.getSpeedAsNumber(Unit.MPH), 0.0001f);

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
        assertEquals("14.0", run_1.getDistance(Unit.KM));
        assertEquals("1:10:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace(Unit.MIN_KM));
        assertEquals("12.0", run_1.getSpeed(Unit.KM_H));

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
        assertEquals("10.0", run_1.getDistance(Unit.KM));
        assertEquals("55:00", run_1.getDuration());
        assertEquals("5:30", run_1.getPace(Unit.MIN_KM));
        assertEquals("10.91", run_1.getSpeed(Unit.KM_H));

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
        assertEquals("10.0", run_1.getDistance(Unit.KM));
        assertEquals("1:00:00", run_1.getDuration());
        assertEquals("6:00", run_1.getPace(Unit.MIN_KM));
        assertEquals("10.0", run_1.getSpeed(Unit.KM_H));

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
        assertEquals("10.0" , run_1.getDistance(Unit.KM));
        assertEquals("50:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace(Unit.MIN_KM));
        assertEquals("12.0", run_1.getSpeed(Unit.KM_H));

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

    @Test
    public void testCalculateBMI() {
        assertEquals(29.91, Run.calculateBMI(108, 190), 0.1f);
        assertEquals(0, Run.calculateBMI(0, 190), 0.1f);
        assertEquals(Float.POSITIVE_INFINITY, Run.calculateBMI(108, 0), 0.1f);
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
                    Run.createWithDistanceAndSpeed(10, 10).getDistance(Unit.KM));
            Assert.assertEquals("10.0",
                    Run.createWithDistanceAndSpeed(10.0f, 10).getDistance(Unit.KM));
            Assert.assertEquals("10.75",
                    Run.createWithDistanceAndSpeed(10.75f, 10).getDistance(Unit.KM));
            Assert.assertEquals("21.0975",
                    Run.createWithDistanceAndSpeed(21.0975f, 10).getDistance(Unit.KM));
            Assert.assertEquals("34.3434",
                    Run.createWithDistanceAndSpeed(34.34344334f, 10).getDistance(Unit.KM));
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

    @Test
    public void testCalculateCadenceCount() throws CustomException {

        // height 190 cm
        Assert.assertEquals(153, Run.createWithDistanceAndSpeed(10, 7).calculateCadenceCount(190));
        Assert.assertEquals(155, Run.createWithDistanceAndSpeed(10, 8).calculateCadenceCount(190));
        Assert.assertEquals(158, Run.createWithDistanceAndSpeed(10, 9).calculateCadenceCount(190));
        Assert.assertEquals(160, Run.createWithDistanceAndSpeed(10, 10).calculateCadenceCount(190));
        Assert.assertEquals(163, Run.createWithDistanceAndSpeed(10, 11).calculateCadenceCount(190));
        Assert.assertEquals(165, Run.createWithDistanceAndSpeed(10, 12).calculateCadenceCount(190));
        Assert.assertEquals(168, Run.createWithDistanceAndSpeed(10, 13).calculateCadenceCount(190));
        Assert.assertEquals(170, Run.createWithDistanceAndSpeed(10, 14).calculateCadenceCount(190));
        Assert.assertEquals(173, Run.createWithDistanceAndSpeed(10, 15).calculateCadenceCount(190));
        Assert.assertEquals(175, Run.createWithDistanceAndSpeed(10, 16).calculateCadenceCount(190));
        Assert.assertEquals(178, Run.createWithDistanceAndSpeed(10, 17).calculateCadenceCount(190));
        Assert.assertEquals(180, Run.createWithDistanceAndSpeed(10, 18).calculateCadenceCount(190));
        Assert.assertEquals(183, Run.createWithDistanceAndSpeed(10, 19).calculateCadenceCount(190));
        Assert.assertEquals(185, Run.createWithDistanceAndSpeed(10, 20).calculateCadenceCount(190));

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Assert.assertEquals(153, Run.createWithDistanceAndSpeed(10, 7).calculateCadenceCount(-1));
        Assert.assertEquals(153, Run.createWithDistanceAndSpeed(10, 7).calculateCadenceCount(0));
        Assert.assertEquals(153, Run.createWithDistanceAndSpeed(10, 7).calculateCadenceCount(272));
    }
}