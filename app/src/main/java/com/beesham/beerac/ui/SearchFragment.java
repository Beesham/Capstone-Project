package com.beesham.beerac.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEERS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //@BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.adView) AdView mAdView;
    @BindView(R.id.empty_view) TextView mEmptyView;

    private static final String LOG_TAG = SearchFragment.class.getSimpleName();
    private static final int BEERS_LOADER = 0;

    private static final String SELECTED_KEY = "selected_position";

    private BeerRecyclerViewAdapter mBeerRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private Cursor mCursor;

    private int mPosition = RecyclerView.NO_POSITION;
    private int mChoiceMode;
    private boolean mAutoSelectView;


    private Tracker mTracker;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getActivity().getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            launchBeerACIntentService(query);
        }

        mBeerRecyclerViewAdapter = new BeerRecyclerViewAdapter(getActivity(),
                new BeerRecyclerViewAdapter.BeerRecyclerViewAdapterOnClickHandler() {
                    @Override
                    public void onClick(Bundle bundle, BeerRecyclerViewAdapter.BeerViewHolder beerViewHolder) {
                        mListener.onFragmentInteraction(bundle);
                        mPosition = beerViewHolder.getAdapterPosition();
                    }
                },
                mChoiceMode);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.beer_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mBeerRecyclerViewAdapter);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SELECTED_KEY)){
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            mBeerRecyclerViewAdapter.onRestoreInstanceState(savedInstanceState);
        }

        //TODO: remove testdevice before launch
        //MobileAds.initialize(getApplicationContext(), "ca-app-pub-9835470545063758~1394766028");
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("22D3A58CFBB4E012B9BDABD394696C04")  // An example device ID
                .build();
        mAdView.loadAd(request);

        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();

        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.SearchFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.SearchFragment_android_choiceMode, false);
        a.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(BEERS_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTracker.setScreenName(getString(R.string.title_activity_search));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchBeerACIntentService(String queryString){
        Intent intent = new Intent(getActivity(), BeerACIntentService.class);
        intent.setAction(ACTION_GET_BEERS);
        intent.putExtra(BeerACIntentService.EXTRA_QUERY, queryString);
        BeerACIntentService.startBeerQueryService(getActivity(), intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to RecyclerView.NO_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        mBeerRecyclerViewAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
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
        void onFragmentInteraction(Bundle bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String[] projections = {
                Columns.SearchedBeerColumns.BEERID,
                Columns.SearchedBeerColumns.NAME,
                Columns.SearchedBeerColumns.DESCRIPTION,
                Columns.SearchedBeerColumns.LABELS,
                Columns.SearchedBeerColumns.IMAGEURLICON,
                Columns.SearchedBeerColumns.IMAGEURLLARGE,
                Columns.SearchedBeerColumns.IMAGEURLMEDIUM
        };

        return new CursorLoader(
                getActivity(),
                BeerProvider.SearchedBeers.CONTENT_URI,
                projections,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        if(!mCursor.moveToFirst()) {return;}

        mBeerRecyclerViewAdapter.swapCursor(mCursor);

        if(mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }

        if(mBeerRecyclerViewAdapter.getItemCount() == 0){
            if(!Utils.isOnline(getActivity())){
                mEmptyView.setText(getString(R.string.empty_beer_list) + "\n" + getString(R.string.no_connectivity));
            }
        }

        if(data.getCount() > 0){
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = mBeerRecyclerViewAdapter.getSelectedItemPosition();
                        if ( RecyclerView.NO_POSITION == itemPosition ) itemPosition = 0;
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if ( null != vh && mAutoSelectView ) {
                            mBeerRecyclerViewAdapter.selectView( vh );
                        }
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mBeerRecyclerViewAdapter.swapCursor(null);
    }
}
