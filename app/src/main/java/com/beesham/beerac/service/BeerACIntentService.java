package com.beesham.beerac.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.ui.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BeerACIntentService extends IntentService {

    private static final String LOG_TAG = BeerACIntentService.class.getSimpleName();
    public static final String ACTION_GET_BEERS = "com.beesham.beerac.service.action.GET_BEERS";
    public static final String ACTION_GET_BEER_DETAILS = "com.beesham.beerac.service.action.GET_BEER_DETAILS";

    public static final String EXTRA_QUERY = "com.beesham.beerac.service.extra.QUERY";


    private static final String BREWERY_BASE_URL = "http://api.brewerydb.com/v2";
    private static final String KEY = "b0c0eceef49f7ecd827331cde0912036";
    private final String PATH_SEARCH = "search";
    private static final String PATH_BEER = "beer";


    private static final String PARAM_KEY = "key";
    private final String PARMA_BEER_MULTI = "beers";
    //private final String PARMA_BEER_SINGLE = "beer";
    private final String PARAM_QUERY = "q";
    private final String PARAM_TYPE = "type";


    String type = "beer";


    public BeerACIntentService() {
        super("BeerACIntentService");
    }


    public static void startBeerQueryService(Context context, Intent intent) {
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_BEERS.equals(action)) {
                final String queryString = intent.getStringExtra(EXTRA_QUERY);
                getBeerByName(queryString);
            }
            if(ACTION_GET_BEER_DETAILS.equals(action)){
                final String queryString = intent.getStringExtra(EXTRA_QUERY);
                getBeerById(queryString);
            }
        }
    }

    private void getBeerByName(String queryString){
        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_SEARCH)
                .appendQueryParameter(PARAM_QUERY, queryString)
                .appendQueryParameter(PARAM_TYPE, type)
                .appendQueryParameter(PARAM_KEY, KEY);

        String response = run(builder.build().toString());
        try {
            logBeers(Utils.extractBeers(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBeerById(String queryString){
        String response = run(buildBeerByIdUri(queryString));
        Log.v(LOG_TAG, "response: " + response);

        /*try {
            extractBeers(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public static String buildBeerByIdUri(String queryString){
        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_BEER)
                .appendPath(queryString)
                .appendQueryParameter(PARAM_KEY, KEY);

        return builder.build().toString();
    }

    private String run(String url){
        String responseStr = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        OkHttpClient client = builder.connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            //Log.v(LOG_TAG, "response: " + response.body().string());
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseStr;
    }


    private void logBeers(Vector<ContentValues> contentValuesVector){
        int inserted = 0;
        if(contentValuesVector.size() > 0){
            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);

            getContentResolver().delete(BeerProvider.SearchedBeers.CONTENT_URI, null, null);
            Log.v(LOG_TAG, "Content URI " + BeerProvider.SearchedBeers.CONTENT_URI);

            inserted = getContentResolver().bulkInsert(BeerProvider.SearchedBeers.CONTENT_URI, contentValuesArray);

            Log.v(LOG_TAG, "Rows inserted: " + inserted);
        }
    }

}
