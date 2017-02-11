package com.beesham.beerac.ui;

import android.net.Uri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beesham.beerac.R;


public class HomeActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener{ //implements LoaderManager.LoaderCallbacks<Cursor>,        TimePickerFragment.TimeSetter{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private boolean mTwoPane;

    private final String DETAIL_ACTIVITY_FRAG_TAG = "detail_frag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.beer_detail_container, new DetailsActivityFragment(), DETAIL_ACTIVITY_FRAG_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO
    }


}
