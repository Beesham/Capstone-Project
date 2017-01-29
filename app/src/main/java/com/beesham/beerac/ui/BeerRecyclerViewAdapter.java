package com.beesham.beerac.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.data.Columns;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by beesham on 24/01/17.
 */

public class BeerRecyclerViewAdapter extends RecyclerView.Adapter<BeerRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Beer> mBeerList;
    private final Context mContext;
    private Cursor mCursor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView beer_icon_imageView;
        public ImageView drinkBeer_icon_imageView;
        public TextView beer_name_textView;
        public ViewHolder(View v) {
            super(v);
            beer_icon_imageView = (ImageView) v.findViewById(R.id.beer_image_image_view);
            drinkBeer_icon_imageView = (ImageView) v.findViewById(R.id.drink_beer_image_view);
            beer_name_textView = (TextView) v.findViewById(R.id.beer_name_text_view);
        }
    }

    public BeerRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beer_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        //holder.drinkBeer_icon_imageView.setImageResource(beer.getUrl_icon());
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
