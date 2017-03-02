package com.beesham.beerac.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.widget.BeerACWidgetProvider;
import com.squareup.picasso.Picasso;

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
import static com.beesham.beerac.service.BeerACIntentService.RESPONSE_HAS_LABELS;
import static com.beesham.beerac.service.BeerACIntentService.RESPONSE_NO_LABELS;

/**
 * Created by beesham on 21/01/17.
 */

public class Utils {

    static final String STATUS_SUCCESS = "success";
    static final String STATUS_FAILURE = "failure";

    static final String KEY_CURRENTPAGE = "currentPage";
    static final String KEY_NUMBEROFPAGES = "numberOfPages";
    static final String KEY_TOTALRESULTS = "totalResults";
    static final String KEY_DATA = "data";

    static final String KEY_NAME = "name";
    static final String KEY_BEERID = "id";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_STYLE = "style";
    static final String KEY_STYLE_NAME = "name";
    static final String KEY_STYLE_DESCRIPTION = "description";

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
        try {
            numberOfPages = beerListJsonObj.getInt(KEY_NUMBEROFPAGES);
        }catch (JSONException e){
            e.printStackTrace();
            numberOfPages = 0;
        }
        totalResults = beerListJsonObj.getInt(KEY_TOTALRESULTS);

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
                    labels = RESPONSE_HAS_LABELS;

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
                    labels = RESPONSE_NO_LABELS;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(Columns.SearchedBeerColumns.NAME, name);
                contentValues.put(Columns.SearchedBeerColumns.BEERID, id);
                contentValues.put(Columns.SearchedBeerColumns.DESCRIPTION, description);
                contentValues.put(Columns.SearchedBeerColumns.LABELS, labels);

                if (labels.equals(RESPONSE_HAS_LABELS)) {
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
        if(TextUtils.isEmpty(jsonResponse)) return null;

        JSONObject beerJsonObj = new JSONObject(jsonResponse);

        String name;
        String id;
        String description = "";
        String styleName = "";
        String styleDescription = "";
        String isOrganic;
        String foodPairings = "";
        String abv = null;
        int year = 0;
        String imageUrlIcon = null;
        String imageUrlMedium = null;
        String imageUrlLarge = null;

        boolean hasImages = false;

        if(beerJsonObj.has(KEY_STATUS)){
            if(beerJsonObj.getString(KEY_STATUS).equals(STATUS_FAILURE)){
                throw new JSONException(beerJsonObj.getString(KEY_ERR_MSG));
            }
        }

        JSONObject dataJsonObj = beerJsonObj.getJSONObject(KEY_DATA);

        name = dataJsonObj.getString(KEY_NAME);
        id = dataJsonObj.getString(KEY_BEERID);
        isOrganic = dataJsonObj.getString(KEY_IS_ORGANIC);

        JSONObject styleJSONObj = dataJsonObj.getJSONObject(KEY_STYLE);
        styleName = styleJSONObj.getString(KEY_STYLE_NAME);
        styleDescription = styleJSONObj.getString(KEY_STYLE_DESCRIPTION);

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
        beer.setStyleName(styleName);
        beer.setStyleDescription(styleDescription);
        if(hasImages) {
            beer.setUrl_icon(imageUrlIcon);
            beer.setUrl_medium(imageUrlMedium);
            beer.setUrl_large(imageUrlLarge);
        }

        return beer;
    }

    public static Beer extractBeerFromCursor(Cursor c){
        boolean hasLabels = false;
        int year = 0;
        c.moveToFirst();

        if(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.LABELS)).equals(RESPONSE_HAS_LABELS)){
            hasLabels = true;
        }

        if(!TextUtils.isEmpty(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.YEAR)))){
            year = Integer.parseInt(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.YEAR)));
        }

        Beer beer = new Beer(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.NAME)),
                c.getString(c.getColumnIndex(Columns.SavedBeerColumns.BEERID)),
                c.getString(c.getColumnIndex(Columns.SavedBeerColumns.DESCRIPTION)),
                c.getString(c.getColumnIndex(Columns.SavedBeerColumns.ABV)),
                hasLabels,
                year);

        beer.setIsOrganic(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.ISORGANIC)));
        beer.setFoodPairings(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.FOOD_PARINGS)));
        beer.setStyleName(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.STYLE_NAME)));
        beer.setStyleDescription(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.STYLE_DESCRIPTION)));
        if(hasLabels) {
            beer.setUrl_icon(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLICON)));
            beer.setUrl_medium(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLMEDIUM)));
            beer.setUrl_large(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLLARGE)));
        }

        c.close();
        return beer;
    }

    public static void logBeers(Context context, Vector<ContentValues> contentValuesVector){
        int inserted = 0;

        context.getContentResolver().delete(BeerProvider.SearchedBeers.CONTENT_URI, null, null);

        if(contentValuesVector.size() > 0){
            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);
            inserted = context.getContentResolver().bulkInsert(BeerProvider.SearchedBeers.CONTENT_URI, contentValuesArray);
        }
    }

    public static void logBeers(Context context, ContentValues contentValues) {
        context.getContentResolver().insert(BeerProvider.SavedBeers.CONTENT_URI, contentValues);
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

    public static double getTimePassed(Context context){
        Calendar calendar = Calendar.getInstance();

        long startedDrinkingTime = context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .getLong(context.getString(R.string.start_drinking_time_key),
                        Utils.timeInMillis(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

        long curHourInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
        long curMinInMillis =  TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE));

        long elapsedTime = (curHourInMillis + curMinInMillis) - startedDrinkingTime;

        double elapsedHours =  TimeUnit.MILLISECONDS.toHours(elapsedTime);
        double elapsedMins = (TimeUnit.MILLISECONDS.toMinutes(elapsedTime)%60)/60.0;

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
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(mL /  29.5735296875));
    }

    public static String doubleToString(double d){
        return Double.toString(d);
    }

    public static String integerToString(int i){
        return Integer.toString(i);
    }

    public static double getBac(Context context){
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE);


        String gender = defaultPreferences.getString(context.getString(R.string.pref_gender_key),
                context.getString(R.string.pref_gender_default));

        double bodyWeight = Double.parseDouble(defaultPreferences.getString(context.getString(R.string.pref_body_weight_key),
                context.getString(R.string.pref_default_body_weight)));

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
                new String[]{context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE)
                        .getString(context.getString(R.string.preferred_beer_key),
                        context.getString(R.string.default_preferred_beer))},
                null);

        if(c.moveToFirst()) {
            abv = Double.parseDouble(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.ABV)));
        }


        drinkSize = Utils.mLToOz(context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .getInt(context.getString(R.string.beer_volume_key),
                        Integer.parseInt(context.getString(R.string.default_volume))));


        return calculateBAC(numOfBeers, abv, drinkSize, gender, bodyWeight, timePassed);
    }

    public static void storeBAC(Context context, double bac){
        context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putLong(context.getString(R.string.bac_key), Double.doubleToLongBits(bac))
                .apply();
    }

    public static long timeInMillis(int hourOfDay, int minute){
        return (TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute));
    }

    public static int[] convertTimeMillisToHourAndMins(long millis){
        int[] timeArray = new int[2];
        timeArray[0] = (int) TimeUnit.MILLISECONDS.toHours(millis);
        timeArray[1] = (int) TimeUnit.MILLISECONDS.toMinutes(millis)%60;

        return timeArray;
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

    public static boolean checkIfBeerExists(Context context, String beerId){
        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
        };

        Cursor c = context.getContentResolver().query(
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{beerId},
                null);

        if(c.getCount() > 0){
            return true;
        }

        return false;
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
