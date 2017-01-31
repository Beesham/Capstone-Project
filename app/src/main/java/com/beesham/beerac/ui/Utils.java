package com.beesham.beerac.ui;

import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;

import com.beesham.beerac.data.Columns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

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
    static final String KEY_FOODPARINGS = "foodParings";
    static final String KEY_ABV = "abv";
    static final String KEY_ISORGANIC = "isOrganic";
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

        JSONArray beerListJsonArray = beerListJsonObj.getJSONArray(KEY_DATA);

        Vector<ContentValues> contentValuesVector = new Vector<>(beerListJsonArray.length());

        for(int i = 0; i < beerListJsonArray.length(); i++){
            String name;
            String id;
            String description = null;
            String labels;
            String imageUrlIcon = null;
            String imageUrlMedium = null;
            String imageUrlLarge = null;

            name =  beerListJsonArray.getJSONObject(i).getString(KEY_NAME);
            id =  beerListJsonArray.getJSONObject(i).getString(KEY_BEERID);


            if(beerListJsonArray.getJSONObject(i).has(KEY_DESCRIPTION)) {
                description = beerListJsonArray.getJSONObject(i)
                        .getString(KEY_DESCRIPTION);
            }

            if(beerListJsonArray.getJSONObject(i).has(KEY_LABELS)) {
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
            }else{
                labels = "N";
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(Columns.SearchedBeerColumns.NAME, name);
            contentValues.put(Columns.SearchedBeerColumns.BEERID, id);
            contentValues.put(Columns.SearchedBeerColumns.DESCRIPTION, description);
            contentValues.put(Columns.SearchedBeerColumns.LABELS, labels);

            if(labels.equals("Y")){
                contentValues.put(Columns.SearchedBeerColumns.IMAGEURLICON, imageUrlIcon);
                contentValues.put(Columns.SearchedBeerColumns.IMAGEURLLARGE, imageUrlMedium);
                contentValues.put(Columns.SearchedBeerColumns.IMAGEURLMEDIUM, imageUrlLarge);
            }

            contentValuesVector.add(contentValues);
        }

        return contentValuesVector;
    }

    public static Beer extractBeerDetails(String jsonResponse) throws JSONException {
        JSONObject beerJsonObj = new JSONObject(jsonResponse);

        String name;
        String id;
        String description = "";
        String imageUrlLarge = null;

        boolean hasImages = false;

        if(!beerJsonObj.getString("status").equals("success")) return null;

        JSONObject dataJsonObj = beerJsonObj.getJSONObject(KEY_DATA);

        name = dataJsonObj.getString(KEY_NAME);

        if(dataJsonObj.has(KEY_DESCRIPTION)) {
            description = dataJsonObj.getString(KEY_DESCRIPTION);
        }

        if(dataJsonObj.has(KEY_LABELS)){
            hasImages = true;
            JSONObject imagesJsonObj = dataJsonObj.getJSONObject(KEY_LABELS);
            imageUrlLarge = imagesJsonObj.getString(KEY_IMAGEURL_LARGE);
        }

        Beer beer = new Beer(name, null, description, null, hasImages, 0);
        if(hasImages) beer.setUrl_large(imageUrlLarge);

        return beer;
    }
}
