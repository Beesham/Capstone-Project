package com.beesham.beerac.ui;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerDetailsAsyncTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment{

    private static final String LOG_TAG = DetailsActivityFragment.class.getSimpleName();

    Uri mUri;
    String mResponseStr = null;

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
        if(bundle.containsKey("uri")){
            mUri = Uri.parse(bundle.getString("uri"));
            Log.v(LOG_TAG, mUri.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);

        new BeerDetailsAsyncTask(new BeerDetailsAsyncTask.AsyncResponse() {
            @Override
            public void processFinish(String results) {
                mResponseStr = results;
                progressBar.setVisibility(View.GONE);

                Beer beer = null;

                try {
                    beer = Utils.extractBeerDetails(mResponseStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                beerNameTextView.setText(beer.getName());
                descriptionTextView.setText(beer.getDescription());

                if(!TextUtils.isEmpty(beer.getUrl_large())){
                    Picasso.with(getContext())
                            .load(beer.getUrl_large())
                            .into(beerImageView);
                }
            }
        }).execute(mUri);

        return view;
    }
}
