package com.beesham.beerac.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    private static final String ACTION_GET_BEERS = "com.beesham.beerac.service.action.GET_BEERS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.beesham.beerac.service.extra.PARAM1";


    private final String BREWERY_BASE_URL = "http://api.brewerydb.com/v2";
    private final String KEY = "b0c0eceef49f7ecd827331cde0912036";
    private final String PATH_SEARCH = "search";
    private final String PATH_BEER = "beer";


    private final String PARAM_KEY = "key";
    private final String PARMA_BEER_MULTI = "beers";
    //private final String PARMA_BEER_SINGLE = "beer";
    private final String PARAM_QUERY = "q";
    private final String PARAM_TYPE = "type";


    String type = "beer";


    public BeerACIntentService() {
        super("BeerACIntentService");
    }


    public static void startBeerQueryService(Context context, String queryString) {
        Intent intent = new Intent(context, BeerACIntentService.class);
        intent.setAction(ACTION_GET_BEERS);
        intent.putExtra(EXTRA_PARAM1, queryString);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_BEERS.equals(action)) {
                final String queryString = intent.getStringExtra(EXTRA_PARAM1);
                getBeerByName(queryString);
            }
        }
    }

    private void getBeerByName(String queryString){
        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_SEARCH)
                .appendQueryParameter(PARAM_QUERY, queryString)
                .appendQueryParameter(PARAM_TYPE, type)
                .appendQueryParameter(PARAM_KEY, KEY);

        Log.v(LOG_TAG, "beerUri: " + builder.build().toString());

        run(builder.build().toString());
    }

    private void getBeerById(String queryString){
        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_BEER)
                .appendPath(queryString)
                .appendQueryParameter(PARAM_KEY, KEY);

        Log.v(LOG_TAG, "beerUri: " + builder.build().toString());

        run(builder.build().toString());
    }

    private void run(String url){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            //Log.v(LOG_TAG, "response: " + response.body().string());
            String responseStr = response.body().string();
            extractBeers(responseStr);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractBeers(String jsonResponse) throws JSONException {
        final String KEY_CURRENTPAGE = "currentPage";
        final String KEY_NUMBEROFPAGES = "numberOfPages";
        final String KEY_TOTALRESULTS = "totalResults";
        final String KEY_DATA = "data";

        final String KEY_NAME = "name";
        final String KEY_BEERID = "id";
        final String KEY_DESCRIPTION = "description";
        final String KEY_FOODPARINGS = "foodParings";
        final String KEY_ISORGANIC = "isOrganic";
        final String KEY_LABELS = "labels";
        final String KEY_IMAGEURL_ICON = "icon";
        final String KEY_IMAGEURL_MEDIUM = "medium";
        final String KEY_IMAGEURL_LARGE = "large";
        final String KEY_YEAR = "year";


        int currentPage;
        int numberOfPages;
        int totalResults;

        JSONObject beerListJsonObj = new JSONObject(jsonResponse);

        currentPage = beerListJsonObj.getInt(KEY_CURRENTPAGE);
        numberOfPages = beerListJsonObj.getInt(KEY_NUMBEROFPAGES);
        totalResults = beerListJsonObj.getInt(KEY_TOTALRESULTS);

        JSONArray beerListJsonArray = beerListJsonObj.getJSONArray(KEY_DATA);

        for(int i = 0; i < beerListJsonArray.length(); i++){
            Log.v(LOG_TAG, "Beer name: " + beerListJsonArray.getJSONObject(i).getString(KEY_NAME));

            if(beerListJsonArray.getJSONObject(i).has(KEY_DESCRIPTION)) {
                Log.v(LOG_TAG, "Beer description: " + beerListJsonArray.getJSONObject(i).getString(KEY_DESCRIPTION));
            }

        }
    }

}
