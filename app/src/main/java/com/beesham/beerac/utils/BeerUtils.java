package com.beesham.beerac.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.model.Beer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import static com.beesham.beerac.service.BeerACIntentService.RESPONSE_HAS_LABELS;
import static com.beesham.beerac.service.BeerACIntentService.RESPONSE_NO_LABELS;

/**
 * Created by Beesham on 4/19/2017.
 */

public class BeerUtils {
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

    /**
     * Extracts beers from a json response by parsing for key-value pairs
     *
     * @param jsonResponse of the list of beers to be parsed
     * @return a vector of content values to be inserted in a database
     * @throws JSONException
     */
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

    /**
     * Extracts the details of a beer from a json response
     * @param jsonResponse of a specific beer
     * @return a beer object that contains relevant details of a beer
     * @throws JSONException
     */
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

    /**
     * Extracts the details of a beer from a cursor
     * @param c a cursor that holds details of a beer queried from a local db
     * @return a beer object that contains relevant details of a beer
     */
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

    /**
     * Inserts a vector of beers in a local db as cache
     * @param context
     * @param contentValuesVector of beer
     */
    public static void logBeers(Context context, Vector<ContentValues> contentValuesVector){
        int inserted = 0;

        context.getContentResolver().delete(BeerProvider.SearchedBeers.CONTENT_URI, null, null);

        if(contentValuesVector.size() > 0){
            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);
            inserted = context.getContentResolver().bulkInsert(BeerProvider.SearchedBeers.CONTENT_URI, contentValuesArray);
        }
    }

    /**
     * Inserts a single beers in a local db for offline viewing and as favorites
     * @param context
     * @param contentValues
     */
    public static void logBeers(Context context, ContentValues contentValues) {
        context.getContentResolver().insert(BeerProvider.SavedBeers.CONTENT_URI, contentValues);
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
}
