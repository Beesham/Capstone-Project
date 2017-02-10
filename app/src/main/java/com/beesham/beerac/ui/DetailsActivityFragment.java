package com.beesham.beerac.ui;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.beesham.beerac.service.BeerDetailsAsyncTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

/**
 * Details fragment showing beer details and allows for user to save/fav a beer
 */
public class DetailsActivityFragment extends Fragment{

    private static final String LOG_TAG = DetailsActivityFragment.class.getSimpleName();

    Uri mUri;
    String mResponseStr = null;

    private Tracker mTracker;
    private Beer beer;

    @BindView(R.id.description_text_view) TextView descriptionTextView;
    @BindView(R.id.beer_name_text_view) TextView beerNameTextView;
    @BindView(R.id.photo) ImageView beerImageView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

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
            new BeerDetailsAsyncTask(new BeerDetailsAsyncTask.AsyncResponse() {
                @Override
                public void processFinish(String results) {
                    mResponseStr = results;
                    progressBar.setVisibility(View.GONE);

                    try {
                        beer = Utils.extractBeerDetails(mResponseStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    beerNameTextView.setText(beer.getName());
                    descriptionTextView.setText(beer.getDescription());

                    if (!TextUtils.isEmpty(beer.getUrl_large())) {
                        Picasso.with(getContext())
                                .load(beer.getUrl_large())
                                .into(beerImageView);
                    }
                }
            }).execute(mUri);
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String beerId =  beer.getId();
                Intent intent = new Intent(getActivity(), BeerACIntentService.class);
                intent.setAction(ACTION_GET_BEER_DETAILS);
                intent.putExtra(BeerACIntentService.EXTRA_QUERY, beerId);
                BeerACIntentService.startBeerQueryService(getActivity(), intent);
            }
        });

        return view;
    }
}
