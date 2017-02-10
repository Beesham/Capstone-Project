package com.beesham.beerac.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEER_DETAILS;

/**
 * Created by beesham on 24/01/17.
 */

public class BeerRecyclerViewAdapter extends RecyclerView.Adapter<BeerRecyclerViewAdapter.BeerViewHolder> {

    private static final String LOG_TAG = BeerRecyclerViewAdapter.class.getSimpleName();

    private final Context mContext;
    private final BeerRecyclerViewAdapterOnClickHandler mOnClickHandler;

    private Cursor mCursor;

    public static interface BeerRecyclerViewAdapterOnClickHandler{
        void onClick(BeerViewHolder beerViewHolder);
    }

    public class BeerViewHolder extends RecyclerView.ViewHolder{
                                    //implements View.OnClickListener{
        @BindView(R.id.beer_image_image_view) ImageView beer_icon_imageView;
        @BindView(R.id.drink_beer_image_container) FrameLayout drinkBeer_icon_container;
        @BindView(R.id.beer_name_text_view) TextView beer_name_textView;

        public BeerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(LOG_TAG, "I was clicked " + beer_name_textView.getText());
                    //mOnClickHandler.onClick(this);

                    mCursor.moveToPosition(getPosition());

                    Bundle args = new Bundle();
                    args.putString(mContext.getString(R.string.beer_details_uri_key), BeerACIntentService.buildBeerByIdUri(
                            mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID))
                    ));

                    mContext.startActivity(new Intent(mContext, DetailsActivity.class)
                            .putExtras(args));
                }
            });

            drinkBeer_icon_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(getPosition());

                    final SharedPreferences prefs = mContext.getSharedPreferences("com.drink_beer", Context.MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(mContext);

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Drink this beer?");
                    builder.setMessage("This beer will be saved under \"My beers\"");
                    builder.setPositiveButton("0k", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String beerId =  mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID));
                            prefs.edit()
                                    .putString("drink_beer", beerId)
                                    .commit();

                            Intent intent = new Intent(mContext, BeerACIntentService.class);
                            intent.setAction(ACTION_GET_BEER_DETAILS);
                            intent.putExtra(BeerACIntentService.EXTRA_QUERY, beerId);
                            BeerACIntentService.startBeerQueryService(mContext, intent);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Log.v(LOG_TAG, "I want to drink this beer: " + mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID)));
                }
            });
        }

        /*@Override
        public void onClick(View view) {
            Log.v(LOG_TAG, "I was clicked " + beer_name_textView.getText());
            mOnClickHandler.onClick(this);

            mCursor.moveToPosition(getPosition());

            Bundle args = new Bundle();
            args.putString("uri", BeerACIntentService.buildBeerByIdUri(
                    mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID))
            ));

            mContext.startActivity(new Intent(mContext, DetailsActivity.class)
                    .putExtras(args));
        }*/
    }

    public BeerRecyclerViewAdapter(Context context,
                                   BeerRecyclerViewAdapterOnClickHandler handler) {
        mContext = context;
        mOnClickHandler = handler;
    }

    @Override
    public BeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beer_list_item, parent, false);

        BeerViewHolder beerViewHolder = new BeerViewHolder(view);
        return beerViewHolder;
    }

    @Override
    public void onBindViewHolder(BeerViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        if(mCursor.getString(
                mCursor.getColumnIndex(Columns.SearchedBeerColumns.LABELS))
                .equals("Y")) {

            String imagePath = mCursor.getString(
                    mCursor.getColumnIndex(
                            Columns.SearchedBeerColumns.IMAGEURLMEDIUM)
            );

            Picasso.with(mContext)
                    .load(imagePath)
                    .resize(150, 150)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.beer_icon_imageView);
        }
        holder.beer_name_textView.setText(mCursor.getString(
                mCursor.getColumnIndex(Columns.SearchedBeerColumns.NAME)));


    }

    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

}
