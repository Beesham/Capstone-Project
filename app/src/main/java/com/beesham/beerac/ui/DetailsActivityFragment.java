package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.analytics.AnalyticsApplication;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

/**
 * Details fragment showing beer details and allows for user to save/fav a beer
 */
public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Beer>{

    private static final String LOG_TAG = DetailsActivityFragment.class.getSimpleName();

    Uri mUri;

    private Tracker mTracker;
    private Beer beer;

    @BindView(R.id.description_text_view) TextView descriptionTextView;
    @BindView(R.id.beer_name_text_view) TextView beerNameTextView;
    @BindView(R.id.photo) ImageView beerImageView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.fab) FloatingActionButton mFab;


    public DetailsActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null) {
            if (bundle.containsKey(getString(R.string.beer_details_uri_key))) {
                mUri = Uri.parse(bundle.getString(getString(R.string.beer_details_uri_key)));
                Log.v(LOG_TAG, mUri.toString());
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

        if(mUri != null) {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkIfBeerExists()) {
                    String beerId = beer.getId();
                    Intent intent = new Intent(getActivity(), BeerACIntentService.class);
                    intent.setAction(ACTION_GET_BEER_DETAILS);
                    intent.putExtra(BeerACIntentService.EXTRA_QUERY, beerId);
                    BeerACIntentService.startBeerQueryService(getActivity(), intent);

                    mFab.setImageResource(R.drawable.ic_favourite_fill);
                }
                else {
                    removeBeer();
                    mFab.setImageResource(R.drawable.ic_favourite_outline);
                }
            }
        });

        return view;
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

        getActivity().finish();
    }

    private boolean checkIfBeerExists(){
        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
        };

        Cursor c = getActivity().getContentResolver().query(
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{beer.getId()},
                null);

        if(c.getCount() > 0){
            return true;
        }

        return false;
    }

    @Override
    public Loader<Beer> onCreateLoader(int id, Bundle args) {
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

    @Override
    public void onLoadFinished(Loader<Beer> loader, Beer data) {
        beer = data;

        if(checkIfBeerExists())
            mFab.setImageResource(R.drawable.ic_favourite_fill);

        progressBar.setVisibility(View.GONE);
        beerNameTextView.setText(beer.getName());
        descriptionTextView.setText(beer.getDescription());

        if (!TextUtils.isEmpty(beer.getUrl_large())) {
            Picasso.with(getContext())
                    .load(beer.getUrl_large())
                    .into(beerImageView);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
