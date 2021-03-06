package com.beesham.beerac.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;


import com.beesham.beerac.R;
import com.beesham.beerac.utils.BeerUtils;
import com.beesham.beerac.utils.Utils;

import butterknife.BindView;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class SavesActivity extends AppCompatActivity implements SavesFragment.OnFragmentInteractionListener{//  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SavesActivity.class.getSimpleName();
    public static final String TAG = SavesActivity.class.getSimpleName();
    public static final String FRAG_FROM_SAVE_ACT_KEY = "detail_started_from_save_act";

    @BindView(R.id.toolbar) Toolbar mToolbar;

    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saves);

        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;

            DetailsFragment fragment = new DetailsFragment();
            Bundle args = new Bundle();

            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(BeerUtils.getBeerIdFromPrefs(this)));
            args.putString(getString(R.string.activity_breadcrumb_key), TAG); //Determines whether or not to inflate the image view in details
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
        DetailsFragment fragment = new DetailsFragment();
        bundle.putString(getString(R.string.activity_breadcrumb_key), TAG);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.beer_detail_container, fragment, HomeActivity.DETAIL_ACTIVITY_FRAG_TAG)
                .commit();
    }

}
