package com.beesham.beerac.ui;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEERS;
import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class SearchActivity extends AppCompatActivity implements SearchFragment.OnFragmentInteractionListener{ //implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    private boolean mTwoPane;
    private final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";


/*    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.adView) AdView mAdView;
    //@BindView(R.id.beers_recycler_view) RecyclerView mRecyclerView;

    private static final int BEERS_LOADER = 0;


    private BeerRecyclerViewAdapter mBeerRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private Cursor mCursor;


    private Tracker mTracker;*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;

            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle args = new Bundle();

            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(Utils.getBeerIdFromPrefs(this)));
            fragment.setArguments(args);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

/*        ButterKnife.bind(this);

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

        if(findViewById(R.id.beer_detail_container) != null){
            mTwoPane = true;

            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle args = new Bundle();

            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(Utils.getBeerIdFromPrefs(this)));
            fragment.setArguments(args);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }

        }else{
            mTwoPane = false;
        }

        //TODO: remove testdevice before launch
        //MobileAds.initialize(getApplicationContext(), "ca-app-pub-9835470545063758~1394766028");
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("22D3A58CFBB4E012B9BDABD394696C04")  // An example device ID
                .build();
        mAdView.loadAd(request);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();*/
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

/*    private void launchBeerACIntentService(String queryString){
        Log.v(LOG_TAG, "launching intent service");
        Intent intent = new Intent(this, BeerACIntentService.class);
        intent.setAction(ACTION_GET_BEERS);
        intent.putExtra(BeerACIntentService.EXTRA_QUERY, queryString);
        BeerACIntentService.startBeerQueryService(this, intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("Search");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }*/

    /*@Override
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
    }*/
}
