package com.beesham.beerac.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.beesham.beerac.R;

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle bundle = getIntent().getExtras();
        /*if(bundle != null) {
            if (bundle.containsKey(getString(R.string.beer_details_uri_key))) {
                mUri = Uri.parse(bundle.getString(getString(R.string.beer_details_uri_key)));
                Log.v(LOG_TAG, mUri.toString());
            }
        }
*/
        DetailsActivityFragment fragment = new DetailsActivityFragment();
        fragment.setArguments(bundle);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.beer_detail_container, fragment)
                    .commit();
        }

    }

}
