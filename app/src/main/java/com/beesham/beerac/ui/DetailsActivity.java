package com.beesham.beerac.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.model.Beer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener{

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.title_text_view) TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(getIntent().getExtras());

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.beer_detail_container, fragment)
                    .commit();
        }

        //If we are not in two pane mode, setup the toolbar so the beer title can be set
        //In two pane mode, the home activity toolbar spans across the detail fragment so
        //there is no need for the fragment to setup a toolbar unless in single pane mode
        if(!HomeActivity.mTwoPane) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_navigate_home));
        }
    }

    @Override
    public void onFragmentInteraction(Beer beer) {
        mTitle.setText(beer.getName());
    }
}
