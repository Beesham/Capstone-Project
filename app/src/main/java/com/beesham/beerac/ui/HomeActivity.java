package com.beesham.beerac.ui;

import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.utils.BeerUtils;
import com.beesham.beerac.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class HomeActivity extends AppCompatActivity implements TimePickerFragment.TimeSetter{

    public static final String LOG_TAG = HomeActivity.class.getSimpleName();
    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String FRAG_FROM_HOME_ACT_KEY = "detail_started_from_home_act";
    public static boolean mTwoPane;

    public static final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //Looks for the details container, if found, then a tablet layout is used
        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;

            Bundle args = new Bundle();
            args.putString(getString(R.string.beer_details_uri_key), buildBeerByIdUri(BeerUtils.getBeerIdFromPrefs(this)));
            args.putString(FRAG_FROM_HOME_ACT_KEY, TAG); //Determines whether or not to inflate the image view in details

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        setSupportActionBar(mToolbar);
    }

    @Override
    public void setTime(int hourOfDay, int minute) {
        ((HomeFragment ) getSupportFragmentManager().findFragmentById(R.id.fragment_home))
        .setTimeTextView(hourOfDay, minute);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Remove all searched data from cache so as to not show old data if connectivity is lost and a query is made
        int rowsdeleted = getContentResolver().delete(BeerProvider.SearchedBeers.CONTENT_URI, null, null);
    }
}
