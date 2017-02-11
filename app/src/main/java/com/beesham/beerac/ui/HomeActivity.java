package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        TimePickerFragment.TimeSetter{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.photo) ImageView mBeerImage;
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.bac_text_view) TextView mBACTextView;
    @BindView(R.id.total_beers_text_view) TextView mTotalBeersTextView;
    @BindView(R.id.increment_beers_button) TextView mIncrementBeerTextView;
    @BindView(R.id.decrement_beers_button) TextView mDecrementBeerTextView;
    @BindView(R.id.units_spinner) Spinner mUnitsSpinner;
    @BindView(R.id.drinking_time_start_text_view) TextView mStartDrinkTimeTextView;
    @BindView(R.id.volume_edit_text) TextView mVolumeEditText;


    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static String PREF_FILE;
    private static final String TIME_PICKER_FRAG_TAG = "com.beesham.beerac.TIMEPICKER";

    private int LOADER_INIT_ID = 0;
    private int LOADER_ID = 1;

    private String mBeerId;
    private double mABV;
    private int mBeerCount = 0;
    private double mBAC = 0;
    private int start_end_time_selector = 0;
    private long mStartTime;
    private int mEndTime;

    private SharedPreferences mPreferences;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        PREF_FILE = getString(R.string.pref_file);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setSupportActionBar(mToolbar);
        mBeerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                if(mBeerId != null) {
                    args.putString(getString(R.string.beer_details_uri_key), BeerACIntentService.buildBeerByIdUri(mBeerId));

                    Intent i = new Intent(HomeActivity.this, DetailsActivity.class);
                    i.putExtras(args);

                    startActivity(i);
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.preferred_beer_key))) {
            mBeerId = prefs.getString(getString(R.string.preferred_beer_key), null);
            Log.v(LOG_TAG, mBeerId);
        }

        if(checkForFirstLaunch()){
            mBeerId =getString(R.string.default_preferred_beer); //Naughty 90 Beer
            prefs.edit().putString(getString(R.string.preferred_beer_key), mBeerId).apply();

            Intent intent = new Intent(this, BeerACIntentService.class);
            intent.setAction(ACTION_GET_BEER_DETAILS);
            intent.putExtra(BeerACIntentService.EXTRA_QUERY, mBeerId);
            BeerACIntentService.startBeerQueryService(this, intent);

            getSupportLoaderManager().initLoader(LOADER_INIT_ID, null, this);
        }else{
            if(mBeerId != null)
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.units, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mUnitsSpinner.setAdapter(spinnerAdapter);
        mUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                mPreferences.edit()
                        .putString(getString(R.string.pref_units_key), parent.getItemAtPosition(position).toString())
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mStartDrinkTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), TIME_PICKER_FRAG_TAG);
                start_end_time_selector = 0;
            }
        });


        Calendar calendar = Calendar.getInstance();
        setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        mVolumeEditText.setText(getString(R.string.default_volume));

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.preferred_beer_key))) {
            if(mBeerId != null) {
                mBeerId = prefs.getString(getString(R.string.preferred_beer_key), null);
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }

        mTracker.setScreenName(getString(R.string.home_screen_title));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                launchSettingsActivity();
                return true;

            case R.id.mybeers:
                launchMyBeersActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchMyBeersActivity(){
        Intent i = new Intent(HomeActivity.this, SavesActivity.class);
        startActivity(i);
    }

    private void launchSettingsActivity(){
        Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private boolean checkForFirstLaunch(){
        final String PREF_VERSION_CODE_KEY = getString(R.string.pref_version_code_key);
        final int NONE_EXIST = -1;
        int currentVersionCode = 0;
        boolean status = true;

        try{
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        int savedVersionCode = preferences.getInt(PREF_VERSION_CODE_KEY, NONE_EXIST);

        if(currentVersionCode == savedVersionCode){
            status = false;
        }else if(currentVersionCode == NONE_EXIST){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            editor.apply();
            status = true;    //New install, first launch, user cleared app data
        }else if(currentVersionCode > savedVersionCode){
            //Place upgrade code here
            status = true;
        }

        preferences.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode)
                .commit();
        return status;
    }


    public void increaseBeerCount(View v){
        mBeerCount++;
        mTotalBeersTextView.setText(getString(R.string.beers_had, mBeerCount));
        updateBAC();
    }

    public void decreaseBeerCount(View v){
        if(mBeerCount != 0)
            mBeerCount--;

        mTotalBeersTextView.setText(getString(R.string.beers_had, mBeerCount));
        updateBAC();
    }

    private void updateBAC(){
        getBAC();
        mBACTextView.setText(getString(R.string.bac_format, mBAC));
    }

    private void getBAC(){
        String gender = mPreferences.getString(getString(R.string.pref_gender_key),
                getString(R.string.pref_gender_default));

        double bodyWeight = Double.parseDouble(mPreferences.getString(getString(R.string.pref_body_weight_key),
                getString(R.string.pref_default_body_weight)));

        double timePassed = Utils.getTimePassed(mStartTime);
        double drinkSize;

        if(mPreferences.getString(getString(R.string.pref_units_key), null).equals("mL")){
            drinkSize = Utils.mLToOz(Integer.parseInt(mVolumeEditText.getText().toString()));
        }else{
            drinkSize = Double.parseDouble(mVolumeEditText.getText().toString());
        }

        mBAC = Utils.calculateBAC(mBeerCount,
                mABV,
                drinkSize,
                gender,
                bodyWeight,
                timePassed);
    }

    @Override
    public void setTime(int hourOfDay, int minute) {
        if(start_end_time_selector == 0) {
            mStartDrinkTimeTextView.setText(String.format("%d : %02d", hourOfDay, minute));
            mStartTime = (TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute));
        }else{
            //mEndDrinkTimeTextView.setText(String.format("%d : %02d", hourOfDay, minute));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
                Columns.SavedBeerColumns.NAME,
                Columns.SavedBeerColumns.DESCRIPTION,
                Columns.SavedBeerColumns.ABV,
                Columns.SavedBeerColumns.LABELS,
                Columns.SavedBeerColumns.IMAGEURLICON,
                Columns.SavedBeerColumns.IMAGEURLLARGE,
                Columns.SavedBeerColumns.IMAGEURLMEDIUM
        };

        switch (id) {
            case 0 :{
                return new CursorLoader(
                        this,
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projections,
                        null,
                        null,
                        null
                );
            }

            case 1 :{
                return new CursorLoader(
                        this,
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projections,
                        Columns.SavedBeerColumns.BEERID + "=?",
                        new String[]{mBeerId},
                        null
                );
            }
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) return;

        data.moveToFirst();
        if(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.LABELS)).equals("Y")){
            Picasso.with(this)
                    .load(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLLARGE)))
                    .into(mBeerImage);
        }

        mABV = Double.parseDouble(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.ABV)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
