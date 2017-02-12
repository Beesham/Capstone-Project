package com.beesham.beerac.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beesham.beerac.R;
import com.beesham.beerac.service.BeerACIntentService;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;
import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class HomeActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener{ //implements LoaderManager.LoaderCallbacks<Cursor>,        TimePickerFragment.TimeSetter{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    public static int FIRST_LAUNCH_CODE = 0;
    public static int CONCEQUENT_LAUNCH_CODE = 1;

    private int launchCode;

    public static boolean mTwoPane;
    private String mUri;


    private final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;
            Log.v(LOG_TAG, "twoPane");

            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle args = new Bundle();

            Log.v(LOG_TAG, "uri: " + buildBeerByIdUri(Utils.getBeerIdFromPrefs(this)));


            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(Utils.getBeerIdFromPrefs(this)));
            args.putInt(getString(R.string.launch_key), launchCode);
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
        //mUri = uri;
        /*if(mTwoPane){
            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle args = new Bundle();
            args.putString(getString(R.string.beer_details_uri_key), uri);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.beer_detail_container, fragment, DETAIL_ACTIVITY_FRAG_TAG)
                    .commit();
        }*/
    }


}
