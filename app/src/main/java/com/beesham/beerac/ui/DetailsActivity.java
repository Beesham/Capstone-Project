package com.beesham.beerac.ui;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.model.Beer;
import com.beesham.beerac.service.BeerACIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;
import static com.beesham.beerac.ui.HomeActivity.FRAG_FROM_HOME_ACT_KEY;


public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private final int PATH_SEGMENT_BEER_ID = 2;
    private final int LOADER_ID_SEARCHED_BEER = 0;
    private final int LOADER_ID_SAVED_BEER = 1;

    @BindView(R.id.title_text_view) TextView mTitle;
    @BindView(R.id.pager) ViewPager mViewPager;

    private Cursor mCursor;
    private Bundle mBundle;
    private BeerDetailsPagerAdapter mPagerAdapter;

    private String mStartId;
    private String mSelectedItemId;

    //Todo: fix return animation shows next beer image in list
    //Todo: fix does not return to item that was being viewed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        mBundle = getIntent().getExtras();
        mStartId = Uri.parse(mBundle.getString(getString(R.string.beer_details_uri_key))).getPathSegments().get(PATH_SEGMENT_BEER_ID);

        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(mBundle);

        // Loads data based on if the beer is selected from Home or Search
        if(mBundle.containsKey(FRAG_FROM_HOME_ACT_KEY)) {
            if (mBundle.getString(FRAG_FROM_HOME_ACT_KEY).equals(HomeFragment.TAG)) {
                getSupportLoaderManager().initLoader(LOADER_ID_SEARCHED_BEER, null, this);
            }
        }else{
            getSupportLoaderManager().initLoader(LOADER_ID_SAVED_BEER, null, this);
        }

        if(savedInstanceState == null){
            mSelectedItemId = mStartId;
        }

        //If we are not in two pane mode, setup the toolbar so the beer title can be set
        //In two pane mode, the home activity toolbar spans across the detail fragment so
        //there is no need for the fragment to setup a toolbar unless in single pane mode
        if(!HomeActivity.mTwoPane) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_navigate_home));

            setupViewPager();
        }else{
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.beer_detail_container, fragment)
                    .commit();
        }
    }

    private void setupViewPager(){
        mPagerAdapter = new BeerDetailsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mViewPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID));
                mTitle.setText(mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.NAME)));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Beer beer) {
        //we use to set actionbar title here but not with the view pager
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projectionsSearchedBeer = {
                Columns.SearchedBeerColumns.BEERID,
                Columns.SearchedBeerColumns.NAME,
                Columns.SearchedBeerColumns.DESCRIPTION,
                Columns.SearchedBeerColumns.LABELS,
                Columns.SearchedBeerColumns.IMAGEURLICON,
                Columns.SearchedBeerColumns.IMAGEURLLARGE,
                Columns.SearchedBeerColumns.IMAGEURLMEDIUM
        };

        final String[] projectionsSavedBeer = {
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

        switch (id) {
            case 0:
                return new CursorLoader(
                        this,
                        BeerProvider.SearchedBeers.CONTENT_URI,
                        projectionsSearchedBeer,
                        null,
                        null,
                        null
                );
            case 1:
                return new CursorLoader(
                        this,
                        BeerProvider.SavedBeers.CONTENT_URI,
                        projectionsSavedBeer,
                        null,
                        null,
                        null
                );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (!TextUtils.isEmpty(mStartId)) {
            if(mCursor.moveToFirst()) {
                mTitle.setText(mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.NAME)));
                // TODO: optimize
                while (!mCursor.isAfterLast()) {
                    if (mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID)).equals(mStartId)) {
                        final int position = mCursor.getPosition();
                        mViewPager.setCurrentItem(position, false);
                        break;
                    }
                    mCursor.moveToNext();
                }
                mStartId = "";
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class BeerDetailsPagerAdapter extends FragmentPagerAdapter{

        public BeerDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            DetailsFragment detailsFragment = (DetailsFragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);

            Bundle args = new Bundle();
            args.putString(getApplication().getString(R.string.beer_details_uri_key),
                    BeerACIntentService.buildBeerByIdUri(
                            mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID))
                    ));

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

}
