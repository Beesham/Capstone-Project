package com.beesham.beerac.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.model.BacConstants.ALC_DIST_FEMALE;
import static com.beesham.beerac.model.BacConstants.ALC_DIST_MALE;
import static com.beesham.beerac.model.BacConstants.AVG_ALC_ELIM_RATE;
import static com.beesham.beerac.model.BacConstants.LIQ_OZ_TO_WGHT_OZ;
import static com.beesham.beerac.utils.Utils.getTimePassed;

/**
 * Created by Beesham on 4/19/2017.
 */

public class MathUtils {

    public static long timeInMillis(int hourOfDay, int minute){
        return (TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute));
    }

    public static int[] convertTimeMillisToHourAndMins(long millis){
        int[] timeArray = new int[2];
        timeArray[0] = (int) TimeUnit.MILLISECONDS.toHours(millis);
        timeArray[1] = (int) TimeUnit.MILLISECONDS.toMinutes(millis)%60;

        return timeArray;
    }

    public static double kgToLbs(double bodyWeightInKg){
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(bodyWeightInKg*2.20462)); // 1kg = 2.2Lbs
    }

    public static double lbsToKg(double bodyWeightInLbs){
        DecimalFormat df = new DecimalFormat("#.#");
        return  Double.parseDouble(df.format(bodyWeightInLbs*0.45359237));  // 1Lbs = 0.45359237Kgs
    }

    public static double mLToOz(int mL){
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(mL /  29.5735296875));
    }

    /**
     * Calculates the blood alcohol content
     * @param context
     * @return the BAC calculated
     */
    public static double getBac(Context context){
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String gender = defaultPreferences.getString(context.getString(R.string.pref_gender_key),
                context.getString(R.string.pref_gender_default));

        double bodyWeight = Double.parseDouble(defaultPreferences.getString(context.getString(R.string.pref_body_weight_key),
                context.getString(R.string.pref_default_body_weight)));

        if(defaultPreferences.getString(context.getString(R.string.pref_units_key), null)
                .equals(context.getString(R.string.unit_system_metric))) {
            bodyWeight = kgToLbs(bodyWeight);
        }

        double timePassed = getTimePassed(context);
        double drinkSize;
        double abv = 0;

        int numOfBeers = context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .getInt(context.getString(R.string.beer_count_key),
                        0);

        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
                Columns.SavedBeerColumns.ABV
        };

        Cursor c = context.getContentResolver().query(
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{context.getSharedPreferences(context.getString(R.string.pref_file), MODE_PRIVATE)
                        .getString(context.getString(R.string.preferred_beer_key),
                        context.getString(R.string.default_preferred_beer))},
                null);

        if(c.moveToFirst()) {
            abv = Double.parseDouble(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.ABV)));
        }else{
            abv = Double.parseDouble(context.getString(R.string.stock_beer_abv));
        }


        drinkSize = mLToOz(context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .getInt(context.getString(R.string.beer_volume_key),
                        Integer.parseInt(context.getString(R.string.default_volume))));


        defaultPreferences.edit()
                .putString(context.getString(R.string.bac_calculation_string_key),
                        getCalculationsAsString(numOfBeers, abv, drinkSize, gender, bodyWeight, timePassed))
                .apply();
        return calculateBAC(numOfBeers, abv, drinkSize, gender, bodyWeight, timePassed);
    }


    /**
     * BAC is calculated using Widmark Formula
     * % BAC = (A x 5.14 / W x r) – .015 x H
     * A = liquid ounces of alcohol consumed
     * W = a person’s weight in pounds
     * r = a gender constant of alcohol distribution (.73 for men and .66 for women)
     * H = hours elapsed since drinking commenced
     * @param numOfBeers
     * @param abv
     * @param gender
     * @param bodyWeight
     */
    public static double calculateBAC(int numOfBeers,
                                      double abv,
                                      double drinkSize,
                                      String gender,
                                      double bodyWeight,
                                      double timePassed){
        double bac = 0.00;

        double a = numOfBeers * drinkSize * (abv/100);

        if (gender.equals("Male")) {
            bac = ((a * LIQ_OZ_TO_WGHT_OZ) / (bodyWeight * ALC_DIST_MALE)) - AVG_ALC_ELIM_RATE * timePassed;
        } else if (gender.equals("Female")) {
            bac = ((a * LIQ_OZ_TO_WGHT_OZ) / (bodyWeight * ALC_DIST_FEMALE)) - AVG_ALC_ELIM_RATE * timePassed;
        }

        DecimalFormat threeDForm = new DecimalFormat("#.###");
        if(bac < 0){
            return 0;
        }
        return Double.valueOf(threeDForm.format(bac));
    }

    private static String getCalculationsAsString(int numOfBeers,
                                                 double abv,
                                                 double drinkSize,
                                                 String gender,
                                                 double bodyWeight,
                                                 double timePassed){

        double a = numOfBeers * drinkSize * (abv/100);

        StringBuilder calcAsString = new StringBuilder();
        calcAsString.append("A = liquid ounces of alcohol consumed\n");
        calcAsString.append("W = a person’s weight in pounds (converted if using Kg)\n");
        calcAsString.append("r = a gender constant of alcohol distribution (.73 for men and .66 for women)\n");
        calcAsString.append("H = hours elapsed since drinking commenced\n\n");

        calcAsString.append("% BAC = (A x 5.14 / W x r) – .015 x H\n\n");

        calcAsString.append("% BAC = ");
        calcAsString.append("((" + Double.toString(a));
        calcAsString.append(" * ")
                .append(LIQ_OZ_TO_WGHT_OZ + ")")
                .append(" / ")
                .append("(" + bodyWeight)
                .append(" * ");

        if(gender.equals("Male")){
            calcAsString.append(ALC_DIST_MALE + "))");
        }else{
            calcAsString.append(ALC_DIST_FEMALE + "))");
        }

        calcAsString.append(" - ")
                .append(AVG_ALC_ELIM_RATE)
                .append(" * ")
                .append(String.format("%.2f", timePassed));

        return calcAsString.toString();
    }
}
