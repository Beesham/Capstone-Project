package com.beesham.beerac.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.service.BeerACIntentService;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v(LOG_TAG, "query: " + query);

            launchBeerACIntentService(query);
        }
    }

    private void launchBeerACIntentService(String queryString){
        Log.v(LOG_TAG, "launching intent service");
        BeerACIntentService.startBeerQueryService(this, queryString);
    }
}
