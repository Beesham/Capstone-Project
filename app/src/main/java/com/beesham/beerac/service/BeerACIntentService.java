package com.beesham.beerac.service;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import com.beesham.beerac.BuildConfig;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.model.Beer;
import com.beesham.beerac.utils.BeerUtils;
import com.beesham.beerac.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BeerACIntentService extends IntentService {

    private static final String LOG_TAG = BeerACIntentService.class.getSimpleName();
    public static final String ACTION_GET_BEERS = "com.beesham.beerac.service.action.GET_BEERS";
    public static final String ACTION_GET_BEER_DETAILS = "com.beesham.beerac.service.action.GET_BEER_DETAILS";

    public static final String EXTRA_QUERY = "com.beesham.beerac.service.extra.QUERY";

    public static final String RESPONSE_HAS_LABELS = "Y";
    public static final String RESPONSE_NO_LABELS = "N";

    private static final String BREWERY_BASE_URL = "http://api.brewerydb.com/v2";
    private static final String KEY = BuildConfig.BREWERYDB_API_KEY;
    private final String PATH_SEARCH = "search";
    private static final String PATH_BEER = "beer";


    private static final String PARAM_KEY = "key";
    private final String PARMA_BEER_MULTI = "beers";
    private final String PARAM_QUERY = "q";
    private final String PARAM_TYPE = "type";
    private final static String PARAM_WITH_BREWERIES = "withBreweries"; //To be implemented in later versions, maybe

    private Tracker mTracker;

    String type = "beer";


    public BeerACIntentService() {
        super("BeerACIntentService");
    }


    public static void startBeerQueryService(Context context, Intent intent) {
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AnalyticsApplication application = (AnalyticsApplication) this.getApplication();
        mTracker = application.getDefaultTracker();

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
            getContentResolver().delete(BeerProvider.SearchedBeers.CONTENT_URI, null, null);
            BeerUtils.logBeers(getApplicationContext(), BeerUtils.extractBeers(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBeerById(String queryString){
        String response = run(buildBeerByIdUri(queryString));
        Beer beer = null;

        try {
            beer = BeerUtils.extractBeerDetails(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BeerUtils.logBeers(getApplicationContext(), beer.toContentValues());
    }

    public static String buildBeerByIdUri(String queryString){
        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_BEER)
                .appendPath(queryString)
                //.appendQueryParameter(PARAM_WITH_BREWERIES, "Y")
                .appendQueryParameter(PARAM_KEY, KEY);

        return builder.build().toString();
    }

    private String run(String url){
        String responseStr = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        OkHttpClient client = builder.connectTimeout(5, TimeUnit.MINUTES)   //Set 5 min timeouts because of API latency, any lower and err
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Search")
                .setAction("Query")
                .build());

        return responseStr;
    }
}
