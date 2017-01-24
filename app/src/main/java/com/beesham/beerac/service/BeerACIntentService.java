package com.beesham.beerac.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

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

    private final String PARAM_KEY = "key";
    private final String PARMA_BEER_MULTI = "beers";
    //private final String PARMA_BEER_SINGLE = "beer";
    private final String PARAM_QUERY = "q";
    private final String PARAM_TYPE = "type";

    private final String PARMA_BEER_SINGLE = "search?q=&type=beer";

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
                getSingleBeer(queryString);
            }
        }
    }

    private void getSingleBeer(String queryString){

        Uri.Builder builder = Uri.parse(BREWERY_BASE_URL).buildUpon()
                .appendPath(PATH_SEARCH)
                .appendQueryParameter(PARAM_QUERY, queryString)
                .appendQueryParameter(PARAM_TYPE, type)
                .appendQueryParameter(PARAM_KEY, KEY);

        Log.v(LOG_TAG, "beerUri: " + builder.build().toString());

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
