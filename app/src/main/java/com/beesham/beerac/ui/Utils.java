package com.beesham.beerac.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.widget.BeerACWidgetProvider;

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

import static android.content.Context.MODE_PRIVATE;

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
    static final String KEY_STATUS = "status";
    static final String KEY_ERR_MSG = "errorMessage";



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

        if(beerListJsonObj.has(KEY_STATUS)){
            if(beerListJsonObj.getString(KEY_STATUS).equals("failure")){
                throw new JSONException(beerListJsonObj.getString(KEY_ERR_MSG));
            }
        }

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
        String abv = null;
        int year = 0;
        String imageUrlIcon = null;
        String imageUrlMedium = null;
        String imageUrlLarge = null;

        boolean hasImages = false;

        if(beerJsonObj.has(KEY_STATUS)){
            if(beerJsonObj.getString(KEY_STATUS).equals("failure")){
                throw new JSONException(beerJsonObj.getString(KEY_ERR_MSG));
            }
        }

        JSONObject dataJsonObj = beerJsonObj.getJSONObject(KEY_DATA);

        name = dataJsonObj.getString(KEY_NAME);
        id = dataJsonObj.getString(KEY_BEERID);
        isOrganic = dataJsonObj.getString(KEY_IS_ORGANIC);

        try {
            abv = dataJsonObj.getString(KEY_ABV);
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

    public static double getTimePassed(long startedDrinkingTime){
        Calendar calendar = Calendar.getInstance();

        long curHourInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
        long curMinInMillis =  TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE));

        long elapsedTime = (curHourInMillis + curMinInMillis) - startedDrinkingTime;

        double elapsedHours =  TimeUnit.MILLISECONDS.toHours(elapsedTime);
        double elapsedMins = (TimeUnit.MILLISECONDS.toMinutes(elapsedTime)%60)/60.0;

        Log.v("UTILS", "elapsed time: " + (elapsedHours + elapsedMins));

        return (elapsedHours + elapsedMins);
    }

    public static String getBeerIdFromPrefs(Context context){
        String beerId = null;

        //Get sharedPreferences instance
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.pref_file),
                Context.MODE_PRIVATE);

        //Get preferred beer beerID from prefs
        if(prefs.contains(context.getString(R.string.preferred_beer_key))) {
            beerId = prefs.getString(context.getString(R.string.preferred_beer_key), null);
        }

        return beerId;
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

   /* public static double getBac(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context));

        String gender = preferences.getString(context.getString(R.string.pref_gender_key),
                context.getString(R.string.pref_gender_default));

        double bodyWeight = Double.parseDouble(preferences.getString(context.getString(R.string.pref_body_weight_key),
                context.getString(R.string.pref_default_body_weight)));

        double timePassed = getTimePassed(mStartTime);
        double drinkSize;

        if(preferences.getString(context.getString(R.string.pref_units_key), null).equals("mL")){
            drinkSize = Utils.mLToOz(Integer.parseInt(mVolumeEditText.getText().toString()));
        }else{
            drinkSize = Double.parseDouble(mVolumeEditText.getText().toString());
        }

        return 0;
    }*/

    public static long timeInMillis(int hourOfDay, int minute){
        return (TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute));
    }

    public static int adjustBeerCount(Context context, int incDecFlag, int beerCount){

        switch (incDecFlag){
            case HomeFragment.INC_BEER:
                beerCount++;
                break;

            case HomeFragment.DEC_BEER:
                beerCount--;
                break;
        }

        context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putInt(context.getString(R.string.beer_count_key), beerCount)
                .apply();

        return beerCount;
    }

    public static void updateWidget(Context context){
        int widgetIds[] = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, BeerACWidgetProvider.class));

        Intent intent = new Intent(context, BeerACWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(intent);
    }

    /**
     * checks if app is launched for first time or a consequent
     * launch
     * @param context
     * @return
     */
    public static boolean checkForFirstLaunch(Context context){
        final String PREF_VERSION_CODE_KEY = context.getString(R.string.pref_version_code_key);
        final int NONE_EXIST = -1;
        int currentVersionCode = 0;
        boolean status = true;

        try{
            currentVersionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.pref_file),
                MODE_PRIVATE);
        int savedVersionCode = preferences.getInt(PREF_VERSION_CODE_KEY, NONE_EXIST);

        if(currentVersionCode == savedVersionCode){
            status = false;
        }else if(currentVersionCode == NONE_EXIST){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            editor.apply();
            status = true;    //New install, first launch, user cleared app data
        }else if(currentVersionCode > savedVersionCode){
            //Place upgrade code here
            status = true;
        }

        preferences.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode)
                .commit();
        return status;
    }

    /**
     * Checks for network availability
     * @param context to get ConnectivityManager
     * @returns true is network is available
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
