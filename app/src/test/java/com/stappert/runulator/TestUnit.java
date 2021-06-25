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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestUnit {

    /**
     * One second.
     */
    private final static int SECOND = 1;

    /**
     * One minute.
     */
    private final static int MINUTE = 60 * SECOND;

    /**
     * One hour.
     */
    private final static int HOUR = 60 * MINUTE;

    /**
     * For expected exceptions.
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testToKg()  throws CustomException {
        // kg to kg
        Assert.assertEquals(-10, Unit.KG.toKg(-10));
        Assert.assertEquals(0, Unit.KG.toKg(0));
        Assert.assertEquals(10, Unit.KG.toKg(10));
        // lb to kg
        Assert.assertEquals(-5, Unit.LB.toKg(-10));
        Assert.assertEquals(0, Unit.LB.toKg(0));
        Assert.assertEquals(5, Unit.LB.toKg(10));

        // expect exceptions
        exception.expect(CustomException.class);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Arrays.asList(Unit.KG, Unit.LB));
        for (Unit unit : faultyUnits) {
            unit.toKg(10);
        }
    }

    @Test
    public void testToCm()  throws CustomException {
        // cm to cm
        Assert.assertEquals(-10, Unit.CM.toCm(-10), 0f);
        Assert.assertEquals(0, Unit.CM.toCm(0), 0f);
        Assert.assertEquals(10, Unit.CM.toCm(10), 0f);
        //  feet
        Assert.assertEquals(-304.8f, Unit.FEET.toCm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.FEET.toCm(0), 0.00001f);
        Assert.assertEquals(304.8f, Unit.FEET.toCm(10), 0.00001f);
        // inch
        Assert.assertEquals(-25.4f, Unit.INCH.toCm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.INCH.toCm(0), 0.00001f);
        Assert.assertEquals(25.4f, Unit.INCH.toCm(10), 0.00001f);
        // km
        Assert.assertEquals(-1000000, Unit.KM.toCm(-10), 0f);
        Assert.assertEquals(0, Unit.KM.toCm(0), 0f);
        Assert.assertEquals(1000000, Unit.KM.toCm(10), 0f);
        // mile
        Assert.assertEquals(-160934, Unit.MILE.toCm(-1), 0.00001f);
        Assert.assertEquals(0, Unit.MILE.toCm(0), 0.00001f);
        Assert.assertEquals(160934, Unit.MILE.toCm(1), 0.00001f);

        // expect exceptions
        exception.expect(CustomException.class);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Unit.getDistanceUnits());
        faultyUnits.remove(Unit.getHeightUnits());
        for (Unit unit : faultyUnits) {
            unit.toCm(10);
        }
    }

    @Test
    public void testToKm()  throws CustomException {
        // cm
        Assert.assertEquals(-10, Unit.CM.toKm(-1000000), 0.00001f);
        Assert.assertEquals(-1e-4, Unit.CM.toKm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.CM.toKm(0), 0.00001f);
        Assert.assertEquals(1e-4, Unit.CM.toKm(10), 0.00001f);
        Assert.assertEquals(10, Unit.CM.toKm(1000000), 0.00001f);
        //  feet
        Assert.assertEquals(-0.003048f, Unit.FEET.toKm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.FEET.toKm(0), 0.00001f);
        Assert.assertEquals(0.003048f, Unit.FEET.toKm(10), 0.00001f);
        // inch
        Assert.assertEquals(-0.000254f, Unit.INCH.toKm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.INCH.toKm(0), 0.00001f);
        Assert.assertEquals(0.000254f, Unit.INCH.toKm(10), 0.00001f);
        // km
        Assert.assertEquals(-10, Unit.KM.toKm(-10), 0f);
        Assert.assertEquals(0, Unit.KM.toKm(0), 0f);
        Assert.assertEquals(10, Unit.KM.toKm(10), 0f);
        // mile
        Assert.assertEquals(-16.0934, Unit.MILE.toKm(-10), 0.00001f);
        Assert.assertEquals(0, Unit.MILE.toKm(0), 0.00001f);
        Assert.assertEquals(16.0934, Unit.MILE.toKm(10), 0.00001f);

        // expect exceptions
        exception.expect(CustomException.class);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Unit.getDistanceUnits());
        faultyUnits.remove(Unit.getHeightUnits());
        for (Unit unit : faultyUnits) {
            unit.toKm(10);
        }
    }

    @Test
    public void testKmTo()  throws CustomException {
        // cm
        Assert.assertEquals(-1000000, Unit.CM.kmTo(-10), 0.00001f);
        Assert.assertEquals(0, Unit.CM.kmTo(0), 0.00001f);
        Assert.assertEquals(1000000, Unit.CM.kmTo(10), 0.00001f);
        //  feet
        Assert.assertEquals(-10, Unit.FEET.kmTo(-0.003048f), 0.00001f);
        Assert.assertEquals(0, Unit.FEET.kmTo(0), 0.00001f);
        Assert.assertEquals(10, Unit.FEET.kmTo(0.003048f), 0.00001f);
        // inch
        Assert.assertEquals(-10, Unit.INCH.kmTo(-0.000254f), 0.00001f);
        Assert.assertEquals(0, Unit.INCH.kmTo(0), 0.00001f);
        Assert.assertEquals(10, Unit.INCH.kmTo(0.000254f), 0.00001f);
        // km
        Assert.assertEquals(-10, Unit.KM.kmTo(-10), 0f);
        Assert.assertEquals(0, Unit.KM.kmTo(0), 0f);
        Assert.assertEquals(10, Unit.KM.kmTo(10), 0f);
        // mile
        Assert.assertEquals(-10, Unit.MILE.kmTo(-16.0934f), 0.00001f);
        Assert.assertEquals(0, Unit.MILE.kmTo(0), 0.00001f);
        Assert.assertEquals(10, Unit.MILE.kmTo(16.0934f), 0.00001f);

        // expect exceptions
        exception.expect(CustomException.class);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Unit.getDistanceUnits());
        faultyUnits.remove(Unit.getHeightUnits());
        for (Unit unit : faultyUnits) {
            unit.kmTo(10);
        }
    }

    @Test
    public void testToMinPerKm()  throws CustomException {
        // kmh to minutes per km
        Assert.assertEquals(300, Unit.KM_H.toMinPerKm(12), 0.01f);
        // mph to minutes per km
        Assert.assertEquals(300, Unit.MPH.toMinPerKm(19.31208f), 0.01f);
        // minutes per km to minutes per km
        Assert.assertEquals(300, Unit.MIN_KM.toMinPerKm(300), 0.01f);
        // miles per km to minutes per km
        Assert.assertEquals(300, Unit.MIN_MILE.toMinPerKm(483), 0.01f);

        // expect exceptions
        exception.expect(CustomException.class);
        Assert.assertEquals(-300, Unit.KM_H.toMinPerKm(-12), 0.01f);
        Assert.assertEquals(0, Unit.KM_H.toMinPerKm(0), 0.01f);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Arrays.asList(Unit.KM_H, Unit.MPH, Unit.MIN_KM, Unit.MIN_MILE));
        for (Unit unit : faultyUnits) {
            unit.toKmPerHour(10);
        }
    }

    @Test
    public void testMinPerKmTo()  throws CustomException {
        // kmh to minutes per km
        Assert.assertEquals(12, Unit.KM_H.minPerKmTo(300), 0.01f);
        // mph to minutes per km
        Assert.assertEquals(19.31208f, Unit.MPH.minPerKmTo(300), 0.01f);
        // minutes per km to minutes per km
        Assert.assertEquals(300, Unit.MIN_KM.minPerKmTo(300), 0.01f);
        // miles per km to minutes per km
        Assert.assertEquals(483, Unit.MIN_MILE.minPerKmTo(300), 0.01f);

        // expect exceptions
        exception.expect(CustomException.class);
        Assert.assertEquals(-300, Unit.KM_H.toMinPerKm(-12), 0.01f);
        Assert.assertEquals(0, Unit.KM_H.toMinPerKm(0), 0.01f);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Arrays.asList(Unit.KM_H, Unit.MPH, Unit.MIN_KM, Unit.MIN_MILE));
        for (Unit unit : faultyUnits) {
            unit.minPerKmTo(10);
        }
    }

    @Test
    public void testToKmPerHour()  throws CustomException {
        // kmh to kmh
        Assert.assertEquals(10, Unit.KM_H.toKmPerHour(10), 0.01f);
        // mph to kmh
        Assert.assertEquals(16.09, Unit.MPH.toKmPerHour(10), 0.01f);
        // minutes per km to kmh
        Assert.assertEquals(12, Unit.MIN_KM.toKmPerHour(300), 0.01f);
        // miles per km to kmh
        Assert.assertEquals(19.31208, Unit.MIN_MILE.toKmPerHour(300), 0.01f);

        // expect exceptions
        exception.expect(CustomException.class);
        Assert.assertEquals(-10, Unit.KM_H.toKmPerHour(-10), 0.01f);
        Assert.assertEquals(0, Unit.KM_H.toKmPerHour(0), 0.01f);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Arrays.asList(Unit.KM_H, Unit.MPH, Unit.MIN_KM, Unit.MIN_MILE));
        for (Unit unit : faultyUnits) {
            unit.toKmPerHour(10);
        }
    }

    @Test
    public void testKmPerHourTo()  throws CustomException {
        // kmh to kmh
        Assert.assertEquals(10, Unit.KM_H.kmPerHourTo(10), 0.01f);
        // mph to kmh
        Assert.assertEquals(10, Unit.MPH.kmPerHourTo(16.09f), 0.01f);
        // minutes per km to kmh
        Assert.assertEquals(300, Unit.MIN_KM.kmPerHourTo(12), 0.01f);
        // miles per km to kmh
        Assert.assertEquals(300, Unit.MIN_MILE.kmPerHourTo(19.31208f), 0.01f);

        // expect exceptions
        exception.expect(CustomException.class);
        Assert.assertEquals(-10, Unit.KM_H.toKmPerHour(-10), 0.01f);
        Assert.assertEquals(0, Unit.KM_H.toKmPerHour(0), 0.01f);
        List<Unit> faultyUnits = Arrays.asList(Unit.values());
        faultyUnits.remove(Arrays.asList(Unit.KM_H, Unit.MPH, Unit.MIN_KM, Unit.MIN_MILE));
        for (Unit unit : faultyUnits) {
            unit.kmPerHourTo(10);
        }
    }

    @Test
    public void testFormatSeconds() {
        try {
            // Tests
            assertEquals("0", Unit.formatSeconds(-1));
            assertEquals("0", Unit.formatSeconds(0));
            assertEquals("59", Unit.formatSeconds(MINUTE - SECOND));
            assertEquals("1:00", Unit.formatSeconds(MINUTE));
            assertEquals("1:01", Unit.formatSeconds(MINUTE + SECOND));
            assertEquals("1:09", Unit.formatSeconds(MINUTE + 9 * SECOND));
            assertEquals("1:10", Unit.formatSeconds(MINUTE + 10 * SECOND));
            assertEquals("9:59", Unit.formatSeconds(10 * MINUTE - SECOND));
            assertEquals("10:00", Unit.formatSeconds(10 * MINUTE));
            assertEquals("10:01", Unit.formatSeconds(10 * MINUTE + SECOND));
            assertEquals("59:59", Unit.formatSeconds(HOUR - SECOND));
            assertEquals("1:00:00", Unit.formatSeconds(HOUR));
            assertEquals("1:00:01", Unit.formatSeconds(HOUR + SECOND));
            assertEquals("1:01:01", Unit.formatSeconds(HOUR + MINUTE + SECOND));
            assertEquals("9:59:59", Unit.formatSeconds(10 * HOUR - SECOND));
            assertEquals("10:00:00", Unit.formatSeconds(10 * HOUR));
            assertEquals("10:00:01", Unit.formatSeconds(10 * HOUR + SECOND));
            assertEquals("100:59:59", Unit.formatSeconds(101 * HOUR - SECOND));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
