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
import static com.beesham.beerac.service.BeerACIntentService.buildBeerByIdUri;

/**
 * Created by beesham on 24/01/17.
 */

public class BeerRecyclerViewAdapter extends RecyclerView.Adapter<BeerRecyclerViewAdapter.BeerViewHolder> {

    private static final String LOG_TAG = BeerRecyclerViewAdapter.class.getSimpleName();

    private final Context mContext;
    private final BeerRecyclerViewAdapterOnClickHandler mOnClickHandler;
    private final ItemChoiceManager mICM;

    private Cursor mCursor;
    private static String PREF_FILE;


    public interface BeerRecyclerViewAdapterOnClickHandler{
        void onClick(Bundle bundle, BeerViewHolder beerViewHolder);
    }

    public class BeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.beer_image_image_view) ImageView beer_icon_imageView;
        @BindView(R.id.drink_beer_image_container) FrameLayout drinkBeer_icon_container;
        @BindView(R.id.beer_name_text_view) TextView beer_name_textView;

        public BeerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);

            drinkBeer_icon_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(getPosition());

                    final SharedPreferences prefs =
                            mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(mContext.getString(R.string.dialog_title));
                    builder.setMessage(mContext.getString(R.string.dialog_confirmation_message));
                    builder.setPositiveButton(mContext.getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String beerId =  mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID));
                            prefs.edit()
                                    .putString(mContext.getString(R.string.preferred_beer_key), beerId)
                                    .commit();

                            Intent intent = new Intent(mContext, BeerACIntentService.class);
                            intent.setAction(ACTION_GET_BEER_DETAILS);
                            intent.putExtra(BeerACIntentService.EXTRA_QUERY, beerId);
                            BeerACIntentService.startBeerQueryService(mContext, intent);
                        }
                    });
                    builder.setNegativeButton(mContext.getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            mCursor.moveToPosition(getAdapterPosition());
            Bundle args = new Bundle();

            //If we are in twoPane mode return a bundle with the beer uri else start details activity
            //with a beer uri
            if(SearchActivity.mTwoPane || SavesActivity.mTwoPane){
                args.putString(mContext.getString(R.string.beer_details_uri_key), BeerACIntentService.buildBeerByIdUri(
                        mCursor.getString(
                                mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID))));
                mOnClickHandler.onClick(args, BeerViewHolder.this);
                mICM.onClick(BeerViewHolder.this);
            }else{
                args.putString(mContext.getString(R.string.beer_details_uri_key),
                        BeerACIntentService.buildBeerByIdUri(
                                mCursor.getString(mCursor.getColumnIndex(Columns.SearchedBeerColumns.BEERID))
                        ));

                mContext.startActivity(new Intent(mContext, DetailsActivity.class)
                        .putExtras(args));
            }
        }
    }

    public BeerRecyclerViewAdapter(Context context,
                                   BeerRecyclerViewAdapterOnClickHandler handler,
                                   int choiceMode) {
        mContext = context;
        mOnClickHandler = handler;
        PREF_FILE = mContext.getString(R.string.pref_file);

        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    @Override
    public BeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.beer_list_item, parent, false);
            view.setFocusable(true);

            return new BeerViewHolder(view);
        }else{
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
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
                    .resize(50, 50)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.beer_icon_imageView);
        }

        holder.beer_name_textView.setText(mCursor.getString(
                mCursor.getColumnIndex(Columns.SearchedBeerColumns.NAME)));

        mICM.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
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

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof BeerViewHolder ) {
            BeerViewHolder beerViewHolder = (BeerViewHolder) viewHolder;
            beerViewHolder.onClick(beerViewHolder.itemView);
        }
    }

}
