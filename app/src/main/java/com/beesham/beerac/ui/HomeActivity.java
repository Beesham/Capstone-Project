package com.beesham.beerac.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beesham.beerac.R;

import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

public class HomeActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        TimePickerFragment.TimeSetter{

    public static final String LOG_TAG = HomeActivity.class.getSimpleName();
    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String FRAG_FROM_HOME_ACT_KEY = "detail_started_from_home_act";
    public static boolean mTwoPane;

    public static final String DETAIL_ACTIVITY_FRAG_TAG = "DETAIL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Looks for the details container, if found, then a tablet layout is used
        if (findViewById(R.id.beer_detail_container) != null) {
            mTwoPane = true;

            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle args = new Bundle();

            args.putString(getString(R.string.beer_details_uri_key),
                    buildBeerByIdUri(Utils.getBeerIdFromPrefs(this)));
            args.putString(FRAG_FROM_HOME_ACT_KEY, TAG);
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

    @Override
    public void setTime(int hourOfDay, int minute) {
        ((HomeFragment ) getSupportFragmentManager().findFragmentById(R.id.fragment_home))
        .setTime(hourOfDay, minute);
    }
}
