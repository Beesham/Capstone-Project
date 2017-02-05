package com.beesham.beerac.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEERS;
import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.photo) ImageView mBeerImage;
    //@BindView(R.id.search_view) SearchView mSearchView;
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private String mBeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mBeerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity.this, DetailsActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences prefs = getSharedPreferences("com.drink_beer", Context.MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.contains("drink_beer")) {
            mBeerId = prefs.getString("drink_beer", null);
            Log.v(LOG_TAG, mBeerId);
        }

        /*if(savedInstanceState == null){
            mBeerId = "oeGSxs";
            Log.v(LOG_TAG, "launching intent service");
            Intent intent = new Intent(this, BeerACIntentService.class);
            intent.setAction(ACTION_GET_BEER_DETAILS);
            intent.putExtra(BeerACIntentService.EXTRA_QUERY, mBeerId);
            BeerACIntentService.startBeerQueryService(this, intent);
        }*/

        if(checkForFirstLaunch()){
            mBeerId = "oeGSxs";
            Log.v(LOG_TAG, "launching intent servicefor first launch");
            Intent intent = new Intent(this, BeerACIntentService.class);
            intent.setAction(ACTION_GET_BEER_DETAILS);
            intent.putExtra(BeerACIntentService.EXTRA_QUERY, mBeerId);
            BeerACIntentService.startBeerQueryService(this, intent);
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private boolean checkForFirstLaunch(){
        final String PREF_NAME = "com.beesham.beerac.PREF_FILE";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int NONE_EXIST = -1;
        int currentVersionCode = 0;

        try{
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int savedVersionCode = preferences.getInt(PREF_VERSION_CODE_KEY, NONE_EXIST);

        if(currentVersionCode == savedVersionCode){
            return false;
        }else if(currentVersionCode == NONE_EXIST){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            editor.apply();
            return true;    //New install, first launch, user cleared app data
        }else if(currentVersionCode == savedVersionCode){
            //Place upgrade code here
            Log.v(LOG_TAG, "upgrading");
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
        SharedPreferences prefs = getSharedPreferences("com.drink_beer", Context.MODE_PRIVATE);
        if(prefs.contains("drink_beer")) {
            mBeerId = prefs.getString("drink_beer", null);
            Log.v(LOG_TAG, mBeerId);
        }
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                launchSettingsActivity();
                return true;

            case R.id.mybeers:
                launchMyBeersActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchMyBeersActivity(){
        Intent i = new Intent(HomeActivity.this, SavesActivity.class);
        startActivity(i);
    }

    private void launchSettingsActivity(){
        Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(i);
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
                this,
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{mBeerId},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) return;

        data.moveToFirst();
        if(data.getString(data.getColumnIndex(Columns.SearchedBeerColumns.LABELS)).equals("Y")){
            Picasso.with(this)
                    .load(data.getString(data.getColumnIndex(Columns.SearchedBeerColumns.IMAGEURLLARGE)))
                    .into(mBeerImage);
        }



        Log.v(LOG_TAG, "cursor size: " + data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
