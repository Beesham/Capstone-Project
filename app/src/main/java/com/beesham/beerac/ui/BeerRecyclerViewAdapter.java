package com.beesham.beerac.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beesham.beerac.R;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by beesham on 24/01/17.
 */

public class BeerRecyclerViewAdapter extends RecyclerView.Adapter<BeerRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Beer> mBeerList;

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

    public BeerRecyclerViewAdapter(ArrayList beerList) {
        mBeerList = beerList;
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
        Beer beer = mBeerList.get(position);
        //holder.drinkBeer_icon_imageView.setImageResource(beer.getUrl_icon());
        holder.beer_name_textView.setText(beer.getName());
        holder.drinkBeer_icon_imageView.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public int getItemCount() {
        return mBeerList.size();
    }
}
