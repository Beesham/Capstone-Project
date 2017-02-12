package com.beesham.beerac.ui;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by beesham on 05/02/17.
 */
public class UtilsTest{
    @Test
    public void calculateBAC() throws Exception {
        int numOfBeers = 7;
        double abv = 5;
        double standardDrinkSize = 12;
        String gender = "Male";
        double bodyWeight = 170;
        double timePassed = 4.5; //hours

        double bac = Utils.calculateBAC(numOfBeers,
                abv,
                standardDrinkSize,
                gender,
                bodyWeight,
                timePassed);

        assertEquals(0.106, bac);
    }

}