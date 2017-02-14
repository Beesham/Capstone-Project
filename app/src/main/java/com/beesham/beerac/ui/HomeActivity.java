package com.beesham.beerac.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beesham.beerac.R;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class HomeActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener{

    public static final String LOG_TAG = HomeActivity.class.getSimpleName();
    public static boolean mTwoPane;

    public static final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
    public void onFragmentInteraction(String uri) {

    }


}
