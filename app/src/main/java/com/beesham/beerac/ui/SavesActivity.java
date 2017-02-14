package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class SavesActivity extends AppCompatActivity implements SavesFragment.OnFragmentInteractionListener{//  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SavesActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;

    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saves);

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
                        .replace(R.id.beer_detail_container, fragment, HomeActivity.DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {
        DetailsActivityFragment fragment = new DetailsActivityFragment();
        bundle.putString("act_started_frag", LOG_TAG);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.beer_detail_container, fragment, HomeActivity.DETAIL_ACTIVITY_FRAG_TAG)
                .commit();
    }

}
