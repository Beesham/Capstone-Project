package com.beesham.beerac.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beesham.beerac.R;
import com.beesham.beerac.utils.MathUtils;
import com.beesham.beerac.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by beesham on 23/02/17.
 */

public class SpinnerAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<String> mVolumeArray;
    String[] mUnitsArray;

    @BindView(R.id.volume_text_view) TextView mVolumeTextView;
    @BindView(R.id.units_text_view) TextView mUnitsTextView;

    public SpinnerAdapter(Context context, String[] volumeArray, String[] unitsArray) {
        mContext = context;
        mVolumeArray = new ArrayList<String>();
        mVolumeArray.addAll(Arrays.asList(volumeArray));
        mUnitsArray = unitsArray;
    }

    @Override
    public int getCount() {
        return mVolumeArray.size();
    }

    @Override
    public Object getItem(int i) {
        return mVolumeArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = View.inflate(mContext, R.layout.volume_spinner_item, null);
        ButterKnife.bind(this, view);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);


        if(preferences.getString(mContext.getString(R.string.pref_units_key),
                mContext.getString(R.string.pref_units_default)).equals(mContext.getString(R.string.pref_units_default))) {
            mUnitsTextView.setText(mUnitsArray[0]);
            mVolumeTextView.setText((String) Double.toString(MathUtils.mLToOz(Integer.parseInt(mVolumeArray.get(position)))));
        }else{
            mUnitsTextView.setText(mUnitsArray[1]);
            mVolumeTextView.setText((String) mVolumeArray.get(position));

        }
        return view;
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(@Nullable String item) {
        return mVolumeArray.indexOf(item);
    }

}
