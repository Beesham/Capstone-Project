package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.beesham.beerac.widget.BeerACWidgetProvider;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.defaultValue;
import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.R.layout.beer_acwidget;
import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface to handle interaction events.
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
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

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();
    private static String PREF_FILE;
    private static final String TIME_PICKER_FRAG_TAG = "com.beesham.beerac.TIMEPICKER";

    private int LOADER_FIRST_LAUNCH_ID = 0;
    private int LOADER_ID = 1;

    private String mBeerId;
    private double mABV;
    private int mBeerCount = 0;
    private double mBAC = 0;
    private int start_end_time_selector = 0;
    private long mStartTime;

    private SharedPreferences mPreferences;

    private Tracker mTracker;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        PREF_FILE = getString(R.string.pref_file);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        mBeerId = Utils.getBeerIdFromPrefs(getContext());

        setupViews();

        if(Utils.checkForFirstLaunch(getActivity())){
            initializeFirstLaunchVariables();
            getActivity().getSupportLoaderManager().initLoader(LOADER_FIRST_LAUNCH_ID, null, this);
        }else{
            if(mBeerId != null) {
                getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            }
        }

        if(savedInstanceState == null){
            mBeerCount = getActivity().getSharedPreferences(getString(R.string.pref_file),
                    MODE_PRIVATE)
                    .getInt(getString(R.string.beer_count_key), mBeerCount);

            mBAC = Double.longBitsToDouble(getActivity().getSharedPreferences(getString(R.string.pref_file),
                    MODE_PRIVATE)
                    .getLong(getString(R.string.bac_key), Double.doubleToLongBits(0f)));

            mTotalBeersTextView.setText(Integer.toString(mBeerCount));
            mBACTextView.setText(Double.toString(mBAC));
        }

        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();

        //set soft keyboard hidden when app launches
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.preferred_beer_key))) {
            if(mBeerId == null) {
                mBeerId = prefs.getString(getString(R.string.preferred_beer_key), null);
                getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }

        mTracker.setScreenName(getString(R.string.home_screen_title));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.home, menu);

        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);
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

    private void initializeFirstLaunchVariables(){
        mBeerId = getString(R.string.default_preferred_beer); //Naughty 90 Beer
        getActivity().getSharedPreferences(getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putString(getString(R.string.preferred_beer_key), mBeerId)
                .putLong(getString(R.string.start_drinking_time_key), mStartTime)
                .putInt(getString(R.string.beer_count_key), mBeerCount)
                .apply();

        Intent intent = new Intent(getActivity(), BeerACIntentService.class);
        intent.setAction(ACTION_GET_BEER_DETAILS);
        intent.putExtra(BeerACIntentService.EXTRA_QUERY, mBeerId);
        BeerACIntentService.startBeerQueryService(getActivity(), intent);
    }

    private void setupViews(){

        setupVolumeUnitsSpinner();

        mBeerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = (new Bundle());
                if (mBeerId != null) {
                    if(!HomeActivity.mTwoPane) {
                        args.putString(getString(R.string.beer_details_uri_key), BeerACIntentService.buildBeerByIdUri(mBeerId));
                        startActivity((new Intent(getActivity(), DetailsActivity.class))
                                .putExtras(args));
                    }
                }
            }
        });

        mIncrementBeerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseBeerCount();
            }
        });

        mDecrementBeerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseBeerCount();
            }
        });

        mStartDrinkTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getActivity().getSupportFragmentManager(), TIME_PICKER_FRAG_TAG);
                start_end_time_selector = 0;
            }
        });

        mVolumeEditText.setText(getString(R.string.default_volume));

        Calendar calendar = Calendar.getInstance();
        setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private void setupVolumeUnitsSpinner(){
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
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
    }

    private void launchMyBeersActivity(){
        Intent i = new Intent(getActivity(), SavesActivity.class);
        startActivity(i);
    }

    private void launchSettingsActivity(){
        Intent i = new Intent(getActivity(), SettingsActivity.class);
        startActivity(i);
    }

    public void increaseBeerCount(){
        mBeerCount++;
        mTotalBeersTextView.setText(getString(R.string.beers_had, mBeerCount));
        storeBeerCount();
        updateBAC();
    }

    public void decreaseBeerCount(){
        if(mBeerCount != 0)
            mBeerCount--;

        mTotalBeersTextView.setText(getString(R.string.beers_had, mBeerCount));
        storeBeerCount();
        updateBAC();
    }

    private void storeBeerCount(){
        getActivity().getSharedPreferences(getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putInt(getString(R.string.beer_count_key), mBeerCount)
                .apply();

       updateWidget();
    }

    private void updateWidget(){
        int widgetIds[] = AppWidgetManager.getInstance(getActivity())
                .getAppWidgetIds(new ComponentName(getActivity(), BeerACWidgetProvider.class));

        Intent intent = new Intent(getActivity(), BeerACWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        getActivity().sendBroadcast(intent);
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

        storeBAC();
    }

    private void storeBAC(){
        getActivity().getSharedPreferences(getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putLong(getString(R.string.bac_key), Double.doubleToLongBits(mBAC))
                .apply();
    }

    @Override
    public void setTime(int hourOfDay, int minute) {
        if(start_end_time_selector == 0) {
            mStartDrinkTimeTextView.setText(String.format("%d : %02d", hourOfDay, minute));
            mStartTime = (TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute));
        }
    }

    public void setUriInHomeActivity(String uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String uri);
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
                        getContext(),
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projections,
                        null,
                        null,
                        null
                );
            }

            case 1 :{
                return new CursorLoader(
                        getContext(),
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
            Picasso.with(getActivity())
                    .load(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLLARGE)))
                    .into(mBeerImage);
        }

        mABV = Double.parseDouble(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.ABV)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
