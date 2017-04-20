package com.beesham.beerac.ui;

import com.beesham.beerac.utils.MathUtils;
import com.beesham.beerac.utils.Utils;

import org.junit.Test;

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

        double bac = MathUtils.calculateBAC(numOfBeers,
                abv,
                standardDrinkSize,
                gender,
                bodyWeight,
                timePassed);

        assertEquals(0.106, bac);
    }

    @Test
    public void convertMillisToHourAndMins() throws Exception {
        long time =  MathUtils.timeInMillis(13, 15); // 13 hours (1pm), 15 mins
        int[] timeArray;

        timeArray = MathUtils.convertTimeMillisToHourAndMins(time);

        assertEquals(13, timeArray[0]);
        assertEquals(15, timeArray[1]);
    }

}