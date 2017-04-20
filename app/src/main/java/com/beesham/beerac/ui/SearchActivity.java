package com.beesham.beerac.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beesham.beerac.R;
import com.beesham.beerac.utils.BeerUtils;
import com.beesham.beerac.utils.Utils;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class SearchActivity extends AppCompatActivity implements SearchFragment.OnFragmentInteractionListener{ //implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String FRAG_FROM_SEARCH_ACT_KEY = "detail_started_from_search_act";

    public static boolean mTwoPane;
    private final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;

            DetailsFragment fragment = new DetailsFragment();
            Bundle args = new Bundle();

            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(BeerUtils.getBeerIdFromPrefs(this)));
            args.putString(FRAG_FROM_SEARCH_ACT_KEY, TAG); //Determines whether or not to inflate the image view in details
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
        DetailsFragment fragment = new DetailsFragment();
        bundle.putString(FRAG_FROM_SEARCH_ACT_KEY, TAG);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                .commit();
    }

}
