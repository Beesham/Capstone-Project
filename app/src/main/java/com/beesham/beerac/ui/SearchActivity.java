package com.beesham.beerac.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.service.BeerACIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.beers_recycler_view) RecyclerView mRecyclerView;

    private BeerRecyclerViewAdapter mBeerRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v(LOG_TAG, "query: " + query);

            launchBeerACIntentService(query);
        }

        //mBeerRecyclerViewAdapter = new BeerRecyclerViewAdapter();
        mRecyclerView.setAdapter(mBeerRecyclerViewAdapter);
    }

    private void launchBeerACIntentService(String queryString){
        Log.v(LOG_TAG, "launching intent service");
        BeerACIntentService.startBeerQueryService(this, queryString);
    }
}
