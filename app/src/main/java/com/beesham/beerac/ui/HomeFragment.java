package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;
import static com.beesham.beerac.service.BeerACIntentService.RESPONSE_HAS_LABELS;
import static java.lang.reflect.Array.getInt;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface to handle interaction events.
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.photo) ImageView mBeerImage;
    @BindView(R.id.beer_name_text_view) TextView mBeerNameTextView;
    @BindView(R.id.bac_text_view) TextView mBACTextView;
    @BindView(R.id.total_beers_text_view) TextView mTotalBeersTextView;
    @BindView(R.id.increment_beers_button) ImageButton mIncrementBeerButton;
    @BindView(R.id.decrement_beers_button) ImageButton mDecrementBeerButton;
    @BindView(R.id.volume_spinner) Spinner mVolumeSpinner;
    @BindView(R.id.drinking_time_start_text_view) TextView mStartDrinkTimeTextView;

    public static final int INC_BEER_FLAG = 1;
    public static final int DEC_BEER_FLAG = 0;

    private static final int LOADER_FIRST_LAUNCH_ID = 0;
    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = HomeFragment.class.getSimpleName();
    private static final String TIME_PICKER_FRAG_TAG = "com.beesham.beerac.TIMEPICKER";

    private static String PREF_FILE;

    private String mBeerId;
    private int mBeerCount = 0;
    private double mBAC = 0;
    private long mStartTime;
    private int mVolumeSpinnerPosition;

    private SpinnerAdapter spinnerAdapter;

    private SharedPreferences mSharedPreferences;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PREF_FILE = getString(R.string.pref_file);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        mSharedPreferences = getActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

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

        getActivity().getSupportLoaderManager().initLoader(3, null, this);

        if(savedInstanceState == null){
            mBeerCount = mSharedPreferences.getInt(getString(R.string.beer_count_key), mBeerCount);

            mBAC = Double.longBitsToDouble(mSharedPreferences.getLong(getString(R.string.bac_key), Double.doubleToLongBits(0f)));

            updateBeerCountTextView();
            mBACTextView.setText(getString(R.string.bac_format, mBAC));

            Log.v(LOG_TAG, "savedInstance null");
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

        if(mBeerId == null) {
            mBeerId = Utils.getBeerIdFromPrefs(getContext());
            if(mBeerId != null)
                getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        if(mBeerId == null){
            mBeerImage.setImageResource(R.drawable.stockbeer);
            mBeerNameTextView.setText(R.string.stock_beer_name);
            mBeerNameTextView.setContentDescription(getString(R.string.stock_beer_name));
        }

        if(mSharedPreferences.contains(getString(R.string.beer_count_key))) {
            mBeerCount = mSharedPreferences.getInt(getString(R.string.beer_count_key), mBeerCount);
            updateBeerCountTextView();
        }

        restoreTime();
        updateBAC();

        spinnerAdapter.notifyDataSetChanged();
        mVolumeSpinner.setSelection(mVolumeSpinnerPosition);

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

        if(Utils.isOnline(getActivity())) {
            BeerACIntentService.startBeerQueryService(getActivity(), intent);
        }else{
            Toast.makeText(getActivity(), R.string.no_connectivity, Toast.LENGTH_LONG).show();
        }
    }

    private void setupViews(){

        mBeerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = (new Bundle());
                Intent detailsActivityIntent = new Intent(getActivity(), DetailsActivity.class);

                if (mBeerId != null) {
                    if(!HomeActivity.mTwoPane) {
                        args.putString(getString(R.string.beer_details_uri_key), BeerACIntentService.buildBeerByIdUri(mBeerId));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Pair<View, String> beerImagePair = Pair.create((View)mBeerImage, mBeerImage.getTransitionName());

                            Bundle transitionsBundle = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(
                                            getActivity(),
                                            beerImagePair
                                    ).toBundle();
                            startActivity(detailsActivityIntent.putExtras(args), transitionsBundle);
                        }else{
                            startActivity(detailsActivityIntent.putExtras(args));
                        }

                    }
                }
            }
        });

        mStartDrinkTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getActivity().getSupportFragmentManager(), TIME_PICKER_FRAG_TAG);
            }
        });

        setupVolumeUnitsSpinner();

        mIncrementBeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBeerCount = Utils.adjustBeerCount(getActivity(), INC_BEER_FLAG, mBeerCount);
                updateBeerCountTextView();
                updateBAC();
            }
        });

        mDecrementBeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBeerCount != 0) {
                    mBeerCount = Utils.adjustBeerCount(getActivity(), DEC_BEER_FLAG, mBeerCount);
                }

                updateBeerCountTextView();
                updateBAC();
            }
        });
    }

    private void setupVolumeUnitsSpinner(){
        spinnerAdapter = new SpinnerAdapter(getActivity(),
                getResources().getStringArray(R.array.beer_volumes_array),
                        getResources().getStringArray(R.array.units));

        mVolumeSpinner.setAdapter(spinnerAdapter);
        mVolumeSpinnerPosition = spinnerAdapter.getPosition(Integer.toString(mSharedPreferences.getInt(getString(R.string.beer_volume_key),
                Integer.parseInt(getString(R.string.default_volume)))));

        mVolumeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                mSharedPreferences.edit()
                        .putInt(getString(R.string.beer_volume_key), Integer.parseInt(parent.getItemAtPosition(position).toString()))
                        .commit();

                mVolumeSpinnerPosition = position;
                updateBAC();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateBeerCountTextView(){
        mTotalBeersTextView.setText(getString(R.string.beers_had, mBeerCount));
    }

    private void launchMyBeersActivity(){
        Intent i = new Intent(getActivity(), SavesActivity.class);
        startActivity(i);
    }

    private void launchSettingsActivity(){
        Intent i = new Intent(getActivity(), SettingsActivity.class);
        startActivity(i);
    }

    private void restoreTime(){
        //Restore mStartTime val from prefs
        Calendar calendar = Calendar.getInstance();
        mStartTime = getActivity().getSharedPreferences(getString(R.string.pref_file),
                MODE_PRIVATE)
                .getLong(getString(R.string.start_drinking_time_key),
                        Utils.timeInMillis(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

        int[] timeArray = Utils.convertTimeMillisToHourAndMins(mStartTime);
        setTime(timeArray[0], timeArray[1]);
    }

    private void updateBAC(){
        getBAC();
        mBACTextView.setText(getString(R.string.bac_format, mBAC));
        Utils.updateWidget(getActivity());
    }

    private void getBAC(){
        mBAC = Utils.getBac(getActivity());
        Utils.storeBAC(getContext(), mBAC);
    }

    public void setTime(int hourOfDay, int minute) {
        mStartDrinkTimeTextView.setText(String.format("%d : %02d", hourOfDay, minute));
        mStartTime = Utils.timeInMillis(hourOfDay, minute);

        mSharedPreferences.edit()
                .putLong( getActivity().getString(R.string.start_drinking_time_key), mStartTime)
                .apply();

        updateBAC();
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
            case LOADER_FIRST_LAUNCH_ID :{
                return new CursorLoader(
                        getContext(),
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projections,
                        null,
                        null,
                        null
                );
            }

            case LOADER_ID :{
                return new CursorLoader(
                        getContext(),
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projections,
                        Columns.SavedBeerColumns.BEERID + "=?",
                        new String[]{mBeerId},
                        null
                );
            }

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()){
            return;
        }

        data.moveToFirst();
        if(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.LABELS)).equals(RESPONSE_HAS_LABELS)){
            Picasso.with(getActivity())
                    .load(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLLARGE)))
                    .placeholder(R.drawable.stockbeer)
                    .error(R.drawable.stockbeer)
                    .into(mBeerImage);
        }

        mBeerNameTextView.setText(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.NAME)));
        mBeerNameTextView.setContentDescription(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.NAME)));
        mBeerImage.setContentDescription(data.getString(data.getColumnIndex(Columns.SavedBeerColumns.NAME)) + getString(R.string.image_content_description));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
