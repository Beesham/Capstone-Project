package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.ui.HomeActivity.FRAG_FROM_HOME_ACT_KEY;

/**
 * Details fragment showing beer details and allows for user to save/fav a beer
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Beer>{

    @BindView(R.id.description_text_view) TextView descriptionTextView;
    @BindView(R.id.abv_text_view) TextView mAbvTextView;
    @BindView(R.id.style_name_text_view) TextView mBeerStyleNameTextView;
    @BindView(R.id.style_description_text_view) TextView mBeerStyleDescriptionTextView;
    @BindView(R.id.food_pairings_text_view) TextView mFoodPairingsTextView;
    @BindView(R.id.photo) ImageView beerImageView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.empty_view) TextView mEmptyView;

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    private static final int LOADER_BEER_EXISTS_ID = 0;
    private static final int LOADER_SEARCHED_BEER_ID = 1;

    Uri mUri;

    private Tracker mTracker;
    private Beer beer;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Beer beer);
    }

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if(bundle != null) {
            if (bundle.containsKey(getString(R.string.beer_details_uri_key))) {
                mUri = Uri.parse(bundle.getString(getString(R.string.beer_details_uri_key)));
            }
        }

        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onResume() {
        super.onResume();

        mTracker.setScreenName(getString(R.string.details_screen_title));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        if(bundle != null){
            if(bundle.containsKey(FRAG_FROM_HOME_ACT_KEY)) {
                if (bundle.getString(FRAG_FROM_HOME_ACT_KEY).equals(HomeActivity.TAG)) {
                    beerImageView.setVisibility(View.GONE);
                }else{
                    beerImageView.setVisibility(View.VISIBLE);
                }
            }
        }

        if(mUri != null) {
            if(Utils.checkIfBeerExists(getContext(), mUri.getPathSegments().get(2))){
                getLoaderManager().initLoader(LOADER_BEER_EXISTS_ID, null, this).forceLoad();
            }else {
                getLoaderManager().initLoader(LOADER_SEARCHED_BEER_ID, null, this).forceLoad();
            }
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!Utils.checkIfBeerExists(getContext(), beer.getId())) {
                    Utils.logBeers(getContext(), beer.toContentValues());
                    mFab.setImageResource(R.drawable.ic_favourite_fill);
                }
                else {
                    removeBeer();
                    mFab.setImageResource(R.drawable.ic_favourite_outline);
                }
            }
        });

        animateDescription();
        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateDescription(){
        Transition transition = TransitionInflater.from(getContext())
                .inflateTransition(R.transition.description_transition);

        transition.addTarget(R.id.abv_text_view);
        transition.addTarget(R.id.style_name_text_view);
        transition.addTarget(R.id.description_text_view);
        transition.addTarget(R.id.style_description_text_view);
        transition.addTarget(R.id.food_pairings_text_view);
        transition.addTarget(R.id.fab);

        getActivity().getWindow().setEnterTransition(transition);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
           e.printStackTrace();
        }
    }

    private void removeBeer(){
        int result = getActivity().getContentResolver().delete(
                BeerProvider.SavedBeers.CONTENT_URI,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{beer.getId()}
        );

        if(result > 0){
            getActivity().getSharedPreferences(getString(R.string.pref_file), MODE_PRIVATE)
                    .edit()
                    .putString(getString(R.string.preferred_beer_key), null)
                    .commit();
        }
    }

    @Override
    public Loader<Beer> onCreateLoader(int id, Bundle args) {
        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
                Columns.SavedBeerColumns.NAME,
                Columns.SavedBeerColumns.DESCRIPTION,
                Columns.SavedBeerColumns.STYLE_NAME,
                Columns.SavedBeerColumns.STYLE_DESCRIPTION,
                Columns.SavedBeerColumns.FOOD_PARINGS,
                Columns.SavedBeerColumns.ISORGANIC,
                Columns.SavedBeerColumns.YEAR,
                Columns.SavedBeerColumns.ABV,
                Columns.SavedBeerColumns.LABELS,
                Columns.SavedBeerColumns.IMAGEURLICON,
                Columns.SavedBeerColumns.IMAGEURLLARGE,
                Columns.SavedBeerColumns.IMAGEURLMEDIUM
        };

        switch (id){
            case LOADER_BEER_EXISTS_ID:
                return new android.support.v4.content.AsyncTaskLoader<Beer>(getActivity()) {
                    @Override
                    public Beer loadInBackground() {
                        Cursor c = getContext().getContentResolver().query(
                                BeerProvider.SavedBeers.CONTENT_URI,
                                projections,
                                Columns.SavedBeerColumns.BEERID + "=?",
                                new String[]{mUri.getPathSegments().get(2)},
                                null);

                        return Utils.extractBeerFromCursor(c);
                    }
                };


            case LOADER_SEARCHED_BEER_ID:
                return new android.support.v4.content.AsyncTaskLoader<Beer>(getActivity()) {
                    @Override
                    public Beer loadInBackground() {
                        String responseStr = null;
                        OkHttpClient client = new OkHttpClient();

                        Request request = new Request.Builder()
                                .url(mUri.toString())
                                .build();

                        try {
                            Response response = client.newCall(request).execute();
                            responseStr = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            return Utils.extractBeerDetails(responseStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Beer> loader, Beer data) {
        beer = data;
        if(beer == null) {
            beerImageView.setImageResource(R.drawable.stockbeer);
            progressBar.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);

            if(Utils.isOnline(getContext())) {
                mEmptyView.setVisibility(View.GONE);
                mAbvTextView.setText(getString(R.string.abv_format, getString(R.string.stock_beer_abv)));
                descriptionTextView.setText(getString(R.string.stock_beer_description));
            }
            return;
        }

        if(Utils.checkIfBeerExists(getActivity(), beer.getId()))
            mFab.setImageResource(R.drawable.ic_favourite_fill);

        progressBar.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mCollapsingToolbar.setTitle(beer.getName());
        descriptionTextView.setText(beer.getDescription());
        mBeerStyleNameTextView.setText(beer.getStyleName());
        mAbvTextView.setText(getString(R.string.abv_format, beer.getAbv()));
        mBeerStyleDescriptionTextView.setText(beer.getStyleDescription());
        mFoodPairingsTextView.setText(beer.getFoodParings());

        if(!HomeActivity.mTwoPane)
            mListener.onFragmentInteraction(beer);

        if(beerImageView != null) {
            if (!TextUtils.isEmpty(beer.getUrl_large())) {
                Picasso.with(getContext())
                        .load(beer.getUrl_large())
                        .placeholder(R.drawable.stockbeer)
                        .error(R.drawable.stockbeer)
                        .into(beerImageView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
