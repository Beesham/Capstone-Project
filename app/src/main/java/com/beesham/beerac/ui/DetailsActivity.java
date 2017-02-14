package com.beesham.beerac.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.beesham.beerac.R;

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle bundle = getIntent().getExtras();

        DetailsActivityFragment fragment = new DetailsActivityFragment();
        fragment.setArguments(bundle);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.beer_detail_container, fragment)
                    .commit();
        }

    }

}
