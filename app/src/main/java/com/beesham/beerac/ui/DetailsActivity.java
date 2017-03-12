package com.beesham.beerac.ui;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.beesham.beerac.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.System.load;

public class DetailsActivity extends AppCompatActivity implements DetailsActivityFragment.OnFragmentInteractionListener{

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.title_scrim_view) View mTitleScrimView;
    @BindView(R.id.photo) ImageView beerImageView;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();

        DetailsActivityFragment fragment = new DetailsActivityFragment();
        fragment.setArguments(bundle);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.beer_detail_container, fragment)
                    .commit();
        }

        if(!HomeActivity.mTwoPane) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_navigate_home));
        }
    }

    @Override
    public void onFragmentInteraction(Beer beer) {
        mCollapsingToolbar.setTitle(beer.getName());

        if (!TextUtils.isEmpty(beer.getUrl_large())) {
            Picasso.with(this)
                    .load(beer.getUrl_large())
                    .placeholder(R.drawable.stockbeer)
                    .error(R.drawable.stockbeer)
                    .into(beerImageView);
        }
    }
}
