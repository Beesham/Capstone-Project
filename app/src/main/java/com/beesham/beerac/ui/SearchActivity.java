package com.beesham.beerac.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEERS;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    //@BindView(R.id.beers_recycler_view) RecyclerView mRecyclerView;

    private static final int BEERS_LOADER = 0;

    private BeerRecyclerViewAdapter mBeerRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private Cursor mCursor;

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

            launchBeerACIntentService(query);

        }

        mBeerRecyclerViewAdapter = new BeerRecyclerViewAdapter(this, new BeerRecyclerViewAdapter.BeerRecyclerViewAdapterOnClickHandler() {
            @Override
            public void onClick(BeerRecyclerViewAdapter.BeerViewHolder beerViewHolder) {
                Uri uri = BeerProvider.SearchedBeers.withName(beerViewHolder.beer_name_textView.getText().toString());

                Log.v(LOG_TAG, "I was clicked: " + uri.toString());

                Bundle args = new Bundle();
                args.putString("uri", uri.toString());
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.beer_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mBeerRecyclerViewAdapter);
        getSupportLoaderManager().initLoader(BEERS_LOADER, null, this);
    }

    private void launchBeerACIntentService(String queryString){
        Log.v(LOG_TAG, "launching intent service");
        Intent intent = new Intent(this, BeerACIntentService.class);
        intent.setAction(ACTION_GET_BEERS);
        intent.putExtra(BeerACIntentService.EXTRA_QUERY, queryString);
        BeerACIntentService.startBeerQueryService(this, intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String[] projections = {
                Columns.SearchedBeerColumns.BEERID,
                Columns.SearchedBeerColumns.NAME,
                Columns.SearchedBeerColumns.DESCRIPTION,
                Columns.SearchedBeerColumns.LABELS,
                Columns.SearchedBeerColumns.IMAGEURLICON,
                Columns.SearchedBeerColumns.IMAGEURLLARGE,
                Columns.SearchedBeerColumns.IMAGEURLMEDIUM
        };

        return new CursorLoader(
                this,
                BeerProvider.SearchedBeers.CONTENT_URI,
                projections,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        if(!mCursor.moveToFirst()) {return;}

        mBeerRecyclerViewAdapter.swapCursor(mCursor);

        Log.v(LOG_TAG, "size of cursor: " + mCursor.getCount());
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mBeerRecyclerViewAdapter.swapCursor(null);
    }
}
