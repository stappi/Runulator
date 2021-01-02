package com.stappert.runulator;

import org.junit.Assert;
import org.junit.Test;

public class TestWeightUnit {

    @Test
    public void testToKg() {
        // kg to kg
        Assert.assertEquals(-10, WeightUnit.KG.toKg(-10));
        Assert.assertEquals(0, WeightUnit.KG.toKg(0));
        Assert.assertEquals(10, WeightUnit.KG.toKg(10));
        // lb to kg
        Assert.assertEquals(-5, WeightUnit.LB.toKg(-10));
        Assert.assertEquals(0, WeightUnit.LB.toKg(0));
        Assert.assertEquals(5, WeightUnit.LB.toKg(10));
    }
}
