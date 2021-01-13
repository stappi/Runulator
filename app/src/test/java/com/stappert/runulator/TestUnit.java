package com.stappert.runulator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class TestUnit {

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
        for (Unit unit : Unit.getUnitOfLengths()) {
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
        for (Unit unit : Unit.getWeightUnits()) {
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
        for (Unit unit : Unit.getWeightUnits()) {
            unit.toKm(10);
        }
    }
}
