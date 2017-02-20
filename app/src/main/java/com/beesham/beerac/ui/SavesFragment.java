package com.beesham.beerac.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;

/**
 *
 */
public class SavesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = SavesFragment.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.empty_view) TextView mEmptyView;

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

    public SavesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saves, container, false);

        ButterKnife.bind(this, view);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBeerRecyclerViewAdapter = new BeerRecyclerViewAdapter(getActivity(), new BeerRecyclerViewAdapter.BeerRecyclerViewAdapterOnClickHandler() {
            @Override
            public void onClick(Bundle bundle, BeerRecyclerViewAdapter.BeerViewHolder beerViewHolder) {
                mListener.onFragmentInteraction(bundle);
                mPosition = beerViewHolder.getAdapterPosition();
            }
        }, mChoiceMode);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.beer_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mBeerRecyclerViewAdapter);
       // getActivity().getSupportLoaderManager().initLoader(BEERS_LOADER, null, this);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SELECTED_KEY)){
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            mBeerRecyclerViewAdapter.onRestoreInstanceState(savedInstanceState);
        }

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(BEERS_LOADER, null, this);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Bundle bundle) {
        if (mListener != null) {
            mListener.onFragmentInteraction(bundle);
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
    public void onResume() {
        super.onResume();

        mTracker.setScreenName(getString(R.string.title_activity_saves));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Bundle bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
                Columns.SavedBeerColumns.NAME,
                Columns.SavedBeerColumns.DESCRIPTION,
                Columns.SavedBeerColumns.LABELS,
                Columns.SavedBeerColumns.IMAGEURLICON,
                Columns.SavedBeerColumns.IMAGEURLLARGE,
                Columns.SavedBeerColumns.IMAGEURLMEDIUM
        };

        return new CursorLoader(
                getActivity(),
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;

        mBeerRecyclerViewAdapter.swapCursor(mCursor);
        if(mPosition != ListView.INVALID_POSITION)  mRecyclerView.smoothScrollToPosition(mPosition);

        if(mBeerRecyclerViewAdapter.getItemCount() == 0){
            mEmptyView.setText(getString(R.string.empty_beer_list));
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
