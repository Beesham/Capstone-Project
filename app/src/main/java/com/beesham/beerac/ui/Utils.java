package com.beesham.beerac.ui;

import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.beesham.beerac.data.Columns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Created by beesham on 21/01/17.
 */

public class Utils {

    static final String KEY_CURRENTPAGE = "currentPage";
    static final String KEY_NUMBEROFPAGES = "numberOfPages";
    static final String KEY_TOTALRESULTS = "totalResults";
    static final String KEY_DATA = "data";

    static final String KEY_NAME = "name";
    static final String KEY_BEERID = "id";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_FOOD_PAIRINGS = "foodPairings";
    static final String KEY_ABV = "abv";
    static final String KEY_IS_ORGANIC = "isOrganic";
    static final String KEY_LABELS = "labels";
    static final String KEY_IMAGEURL_ICON = "icon";
    static final String KEY_IMAGEURL_MEDIUM = "medium";
    static final String KEY_IMAGEURL_LARGE = "large";
    static final String KEY_YEAR = "year";


    public static String getCountryName(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses != null && !addresses.isEmpty()) {
            return addresses.get(0).getCountryName();
        }
        return null;
    }

    public static Vector<ContentValues> extractBeers(String jsonResponse) throws JSONException {

        int currentPage;
        int numberOfPages;
        int totalResults;

        JSONObject beerListJsonObj = new JSONObject(jsonResponse);

        currentPage = beerListJsonObj.getInt(KEY_CURRENTPAGE);
        numberOfPages = beerListJsonObj.getInt(KEY_NUMBEROFPAGES);
        totalResults = beerListJsonObj.getInt(KEY_TOTALRESULTS);

        Log.v("Utils", "num of pages: " + numberOfPages);

        JSONArray beerListJsonArray = beerListJsonObj.getJSONArray(KEY_DATA);

        Vector<ContentValues> contentValuesVector = new Vector<>(beerListJsonArray.length());

        for(int j = 0; j < numberOfPages; j++) {
            for (int i = 0; i < beerListJsonArray.length(); i++) {
                String name;
                String id;
                String description = null;
                String labels;
                String imageUrlIcon = null;
                String imageUrlMedium = null;
                String imageUrlLarge = null;

                name = beerListJsonArray.getJSONObject(i).getString(KEY_NAME);
                id = beerListJsonArray.getJSONObject(i).getString(KEY_BEERID);


                if (beerListJsonArray.getJSONObject(i).has(KEY_DESCRIPTION)) {
                    description = beerListJsonArray.getJSONObject(i)
                            .getString(KEY_DESCRIPTION);
                }

                if (beerListJsonArray.getJSONObject(i).has(KEY_LABELS)) {
                    labels = "Y";

                    imageUrlIcon = beerListJsonArray.getJSONObject(i)
                            .getJSONObject(KEY_LABELS)
                            .getString(KEY_IMAGEURL_ICON);

                    imageUrlMedium = beerListJsonArray.getJSONObject(i)
                            .getJSONObject(KEY_LABELS)
                            .getString(KEY_IMAGEURL_MEDIUM);

                    imageUrlLarge = beerListJsonArray.getJSONObject(i)
                            .getJSONObject(KEY_LABELS)
                            .getString(KEY_IMAGEURL_LARGE);
                } else {
                    labels = "N";
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(Columns.SearchedBeerColumns.NAME, name);
                contentValues.put(Columns.SearchedBeerColumns.BEERID, id);
                contentValues.put(Columns.SearchedBeerColumns.DESCRIPTION, description);
                contentValues.put(Columns.SearchedBeerColumns.LABELS, labels);

                if (labels.equals("Y")) {
                    contentValues.put(Columns.SearchedBeerColumns.IMAGEURLICON, imageUrlIcon);
                    contentValues.put(Columns.SearchedBeerColumns.IMAGEURLLARGE, imageUrlMedium);
                    contentValues.put(Columns.SearchedBeerColumns.IMAGEURLMEDIUM, imageUrlLarge);
                }

                contentValuesVector.add(contentValues);
            }
        }

        return contentValuesVector;
    }

    public static Beer extractBeerDetails(String jsonResponse) throws JSONException {
        JSONObject beerJsonObj = new JSONObject(jsonResponse);

        String name;
        String id;
        String description = "";
        String isOrganic;
        String foodPairings = "";
        String abv;
        int year = 0;
        String imageUrlIcon = null;
        String imageUrlMedium = null;
        String imageUrlLarge = null;

        boolean hasImages = false;

        if(!beerJsonObj.getString("status").equals("success")) return null;

        JSONObject dataJsonObj = beerJsonObj.getJSONObject(KEY_DATA);

        name = dataJsonObj.getString(KEY_NAME);
        id = dataJsonObj.getString(KEY_BEERID);
        abv = dataJsonObj.getString(KEY_ABV);
        isOrganic = dataJsonObj.getString(KEY_IS_ORGANIC);

        try {
            foodPairings = dataJsonObj.getString(KEY_FOOD_PAIRINGS);
        }catch(JSONException e){
            if(e.getMessage().equals("No value for foodPairings")){
                foodPairings = "";
            }else if(e.getMessage().equals("No value for abv")){
                abv = "";
            }else{
                throw e;
            }
        }

        if(dataJsonObj.has(KEY_DESCRIPTION)) {
            description = dataJsonObj.getString(KEY_DESCRIPTION);
        }

        if(dataJsonObj.has(KEY_LABELS)){
            hasImages = true;
            JSONObject imagesJsonObj = dataJsonObj.getJSONObject(KEY_LABELS);
            imageUrlIcon = imagesJsonObj.getString(KEY_IMAGEURL_ICON);
            imageUrlMedium = imagesJsonObj.getString(KEY_IMAGEURL_MEDIUM);
            imageUrlLarge = imagesJsonObj.getString(KEY_IMAGEURL_LARGE);
        }

        if(dataJsonObj.has(KEY_YEAR)){
            year = dataJsonObj.getInt(KEY_YEAR);
        }

        Beer beer = new Beer(name, id, description, abv, hasImages, year);
        beer.setIsOrganic(isOrganic);
        beer.setFoodPairings(foodPairings);
        if(hasImages) {
            beer.setUrl_icon(imageUrlIcon);
            beer.setUrl_medium(imageUrlMedium);
            beer.setUrl_large(imageUrlLarge);
        }

        return beer;
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
        final double AVG_ALC_ELIM_RATE = 0.015;
        final double LIQ_OZ_TO_WGHT_OZ = 5.14;   //conversion factor of .823 x 100/16, wherein .823 is used to convert liquid ounces to ounces of weight
        final double ALC_DIST_MALE = 0.73;
        final double ALC_DIST_FEMALE = 0.66;
        double bac = 0.00;

        double a = numOfBeers * drinkSize * (abv/100);

        if(gender.equals("Male")) {
            bac = ((a * LIQ_OZ_TO_WGHT_OZ) / (bodyWeight * ALC_DIST_MALE)) - AVG_ALC_ELIM_RATE * timePassed;
        }else if(gender.equals("Female")){
            bac = ((a * LIQ_OZ_TO_WGHT_OZ) / (bodyWeight * ALC_DIST_FEMALE)) - AVG_ALC_ELIM_RATE * timePassed;
        }

        DecimalFormat threeDForm = new DecimalFormat("#.###");
        return Double.valueOf(threeDForm.format(bac));
    }

    public static long getTimePassed(long startedDrinkingTime){
        Calendar calendar = Calendar.getInstance();

        long curHourInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
        long curMinInMillis =  TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE));

        long elapsedTime = (curHourInMillis + curMinInMillis) - startedDrinkingTime;

        return TimeUnit.MILLISECONDS.toHours(elapsedTime);
    }

    public static double kgToLbs(double bodyWeightInKg){
        return bodyWeightInKg*2.2;  // 1kg = 2.2Lbs
    }

    public static double mLToOz(int mL){
        return mL /  29.5735296875;
    }

    public static String doubleToString(double d){
        return Double.toString(d);
    }

    public static String integerToString(int i){
        return Integer.toString(i);
    }
}
