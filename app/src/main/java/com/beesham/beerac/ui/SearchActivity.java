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
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

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

    public static boolean mTwoPane;
    private final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

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
            args.putString("act_started_frag", LOG_TAG);
            fragment.setArguments(args);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {
        Log.v(LOG_TAG, "itemCLicked");

        DetailsActivityFragment fragment = new DetailsActivityFragment();
        bundle.putString("act_started_frag", LOG_TAG);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                .commit();
    }

}
