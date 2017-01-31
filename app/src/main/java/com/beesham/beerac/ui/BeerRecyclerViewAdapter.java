package com.beesham.beerac.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.service.BeerACIntentService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.beesham.beerac.service.BeerACIntentService.ACTION_GET_BEERS;
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

    public class BeerViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener{
        @BindView(R.id.beer_image_image_view) ImageView beer_icon_imageView;
        @BindView(R.id.drink_beer_image_view) ImageView drinkBeer_icon_imageView;
        @BindView(R.id.beer_name_text_view) TextView beer_name_textView;

        public BeerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
        }

        @Override
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
        }
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
        holder.drinkBeer_icon_imageView.setImageResource(R.mipmap.ic_launcher);
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
