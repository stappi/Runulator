package com.stappert.runulator;

import com.stappert.runulator.utils.CustomException;
import com.stappert.runulator.entities.Run;
import com.stappert.runulator.utils.Unit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests class Run and RunUtils (companion object).
 */
public class TestRun {

    /**
     * Seconds in milli seconds
     */
    private final static int SECOND = 1;

    /**
     * Minutes in milli seconds
     */
    private final static int MINUTE = 60 * SECOND;

    /**
     * Hour in milli seconds
     */
    private final static int HOUR = 60 * MINUTE;

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
        assertEquals(MINUTE - SECOND, Run.parseTimeInSeconds("59"));
        assertEquals(MINUTE, Run.parseTimeInSeconds("1:00"));
        assertEquals(MINUTE + SECOND, Run.parseTimeInSeconds("1:01"));
        assertEquals(MINUTE + 9 * SECOND, Run.parseTimeInSeconds("1:09"));
        assertEquals(MINUTE + 10 * SECOND, Run.parseTimeInSeconds("1:10"));
        assertEquals(10 * MINUTE - SECOND, Run.parseTimeInSeconds("9:59"));
        assertEquals(10 * MINUTE, Run.parseTimeInSeconds("10:00"));
        assertEquals(10 * MINUTE + SECOND, Run.parseTimeInSeconds("10:01"));
        assertEquals(HOUR - SECOND, Run.parseTimeInSeconds("59:59"));
        assertEquals(HOUR - SECOND, Run.parseTimeInSeconds("0:59:59"));
        assertEquals(HOUR - SECOND, Run.parseTimeInSeconds("00:59:59"));
        assertEquals(HOUR, Run.parseTimeInSeconds("1:00:00"));
        assertEquals(HOUR + SECOND, Run.parseTimeInSeconds("1:00:01"));
        assertEquals(HOUR + MINUTE + SECOND, Run.parseTimeInSeconds("1:01:01"));
        assertEquals(10 * HOUR - SECOND, Run.parseTimeInSeconds("9:59:59"));
        assertEquals(10 * HOUR, Run.parseTimeInSeconds("10:00:00"));
        assertEquals(10 * HOUR + SECOND, Run.parseTimeInSeconds("10:00:01"));
        assertEquals(101 * HOUR - SECOND, Run.parseTimeInSeconds("100:59:59"));

        // special values
        assertEquals(HOUR + 10 * MINUTE, Run.parseTimeInSeconds("70:00"));
        assertEquals(3 * HOUR, Run.parseTimeInSeconds("1:120:00"));
        assertEquals(HOUR + 10 * MINUTE, Run.parseTimeInSeconds("4200"));
        assertEquals(HOUR + 10 * MINUTE, Run.parseTimeInSeconds("0:0:4200"));
        assertEquals(6 * HOUR, Run.parseTimeInSeconds("1:120:10800"));

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
        Run run_1 = Run.createWithDurationAndSpeed(2 * HOUR, 11);
        assertEquals("22.0", run_1.getDistance(Unit.KM));
        assertEquals("13.6702", run_1.getDistance(Unit.MILE));
        assertEquals(22, run_1.getDistanceAsNumber(Unit.KM), 0.1f);
        assertEquals(13.6702, run_1.getDistanceAsNumber(Unit.MILE), 0.1f);
        assertEquals("2:00:00", run_1.getDuration());
        assertEquals(2 * HOUR, run_1.getDurationAsNumber());
        assertEquals("5:27", run_1.getPace(Unit.MIN_KM));
        assertEquals("8:46", run_1.getPace(Unit.MIN_MILE));
        assertEquals(5 * MINUTE + 27 * SECOND, run_1.getPaceAsNumber(Unit.MIN_KM));
        assertEquals(8 * MINUTE + 46 * SECOND, run_1.getPaceAsNumber(Unit.MIN_MILE));
        assertEquals("11.0", run_1.getSpeed(Unit.KM_H));
        assertEquals("6.84", run_1.getSpeed(Unit.MPH));
        assertEquals(11.0f, run_1.getSpeedAsNumber(Unit.KM_H), 0.0001f);
        assertEquals(6.8351f, run_1.getSpeedAsNumber(Unit.MPH), 0.0001f);

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDurationAndSpeed(0 * HOUR, 12);
        Run.createWithDurationAndSpeed(-1 * HOUR, 12);
        Run.createWithDurationAndSpeed(1 * HOUR, 0);
        Run.createWithDurationAndSpeed(1 * HOUR, -5);
        Run.createWithDurationAndSpeed(0 * HOUR, -5);
    }

    @Test
    public void testCreateWithDurationAndPace() throws CustomException {
        // 1h10min with 5:00 pace
        Run run_1 = Run.createWithDurationAndPace(HOUR + 10 * MINUTE, 5 * MINUTE);
        assertEquals("14.0", run_1.getDistance(Unit.KM));
        assertEquals("1:10:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace(Unit.MIN_KM));
        assertEquals("12.0", run_1.getSpeed(Unit.KM_H));

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDurationAndPace(0 * HOUR, 1 * MINUTE);
        Run.createWithDurationAndPace(-1 * HOUR, 12 * 1 * MINUTE);
        Run.createWithDurationAndPace(1 * HOUR, 0 * MINUTE);
        Run.createWithDurationAndPace(1 * HOUR, -5 * MINUTE);
        Run.createWithDurationAndPace(0 * HOUR, -5 * MINUTE);
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
        Run.createWithDistanceAndPace(0, 5 * MINUTE);
        Run.createWithDistanceAndPace(-1, 6 * MINUTE);
        Run.createWithDistanceAndPace(10, 0 * MINUTE);
        Run.createWithDistanceAndPace(10, -5 * MINUTE);
        Run.createWithDistanceAndPace(0, -5 * MINUTE);
    }

    @Test
    public void testCreateWithDistanceAndDuration() throws CustomException {
        // 10 km in 50 minutes
        Run run_1 = Run.createWithDistanceAndDuration(10, 50 * MINUTE);
        assertEquals("10.0" , run_1.getDistance(Unit.KM));
        assertEquals("50:00", run_1.getDuration());
        assertEquals("5:00", run_1.getPace(Unit.MIN_KM));
        assertEquals("12.0", run_1.getSpeed(Unit.KM_H));

        // expect exceptions with faulty values
        exception.expect(CustomException.class);
        Run.createWithDistanceAndDuration(0, 50 * MINUTE);
        Run.createWithDistanceAndDuration(-1, 60 * MINUTE);
        Run.createWithDistanceAndDuration(10, 0 * MINUTE);
        Run.createWithDistanceAndDuration(10, -50 * MINUTE);
        Run.createWithDistanceAndDuration(0, -50 * MINUTE);
    }

    @Test
    public void testRunsToJson() {
        try {
            Collection<Run> runs = new HashSet<>(Arrays.asList(
                    Run.createWithDurationAndSpeed(2 * HOUR, 11),
                    Run.createWithDurationAndPace(HOUR + 10 * MINUTE, 5 * MINUTE),
                    Run.createWithDistanceAndSpeed(10, 10.91f),
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")),
                    Run.createWithDistanceAndDuration(10, 50 * MINUTE)));
            Set<String> expectedRuns = new HashSet<>(Arrays.asList(
                    "{'DISTANCE':22.0,'DURATION':7200,'PACE':327,'SPEED':11.0}",
                    "{'DISTANCE':14.0,'DURATION':4200,'PACE':300,'SPEED':12.0}",
                    "{'DISTANCE':10.0,'DURATION':3300,'PACE':330,'SPEED':10.91}",
                    "{'DISTANCE':10.0,'DURATION':3600,'PACE':360,'SPEED':10.0}",
                    "{'DISTANCE':10.0,'DURATION':3000,'PACE':300,'SPEED':12.0}"));
            Assert.assertEquals(expectedRuns, Run.runsToJson(runs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJsonToRuns() {
        try {
            Set<String> runs = new HashSet<>(Arrays.asList(
                    "{'DISTANCE':22.0,'DURATION':7200,'PACE':327,'SPEED':11.0}",
                    "{'DISTANCE':14.0,'DURATION':4200,'PACE':300,'SPEED':12.0}",
                    "{'DISTANCE':10.0,'DURATION':3300,'PACE':330,'SPEED':10.91}",
                    "{'DISTANCE':10.0,'DURATION':3600,'PACE':360,'SPEED':10.0}",
                    "{'DISTANCE':10.0,'DURATION':3000,'PACE':300,'SPEED':12.0}"));
            Set<Run> expectedRuns = new HashSet<>(Arrays.asList(
                    Run.createWithDurationAndSpeed(2 * HOUR, 11),
                    Run.createWithDurationAndPace(HOUR + 10 * MINUTE, 5 * MINUTE),
                    Run.createWithDistanceAndSpeed(10, 10.91f),
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")),
                    Run.createWithDistanceAndDuration(10, 50 * MINUTE)));
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
    // test public functions
    // =============================================================================================
    @Test
    public void testToJson() {
        try {
            Assert.assertEquals("{'DISTANCE':22.0,'DURATION':7200,'PACE':327,'SPEED':11.0}",
                    Run.createWithDurationAndSpeed(2 * HOUR, 11).toJson());
            Assert.assertEquals("{'DISTANCE':14.0,'DURATION':4200,'PACE':300,'SPEED':12.0}",
                    Run.createWithDurationAndPace(HOUR + 10 * MINUTE, 5 * MINUTE).toJson());
            Assert.assertEquals("{'DISTANCE':10.0,'DURATION':3300,'PACE':330,'SPEED':10.91}",
                    Run.createWithDistanceAndSpeed(10, 10.91f).toJson());
            Assert.assertEquals("{'DISTANCE':10.0,'DURATION':3600,'PACE':360,'SPEED':10.0}",
                    Run.createWithDistanceAndPace(10, Run.parseTimeInSeconds("6:00")).toJson());
            Assert.assertEquals("{'DISTANCE':10.0,'DURATION':3000,'PACE':300,'SPEED':12.0}",
                    Run.createWithDistanceAndDuration(10, 50 * MINUTE).toJson());
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
                    Run.createWithDistanceAndDuration(10, HOUR + MINUTE + SECOND).getDuration());
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