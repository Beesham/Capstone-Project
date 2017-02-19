package com.beesham.beerac.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.beesham.beerac.R;
import com.beesham.beerac.data.BeerProvider;
import com.beesham.beerac.data.Columns;
import com.beesham.beerac.ui.HomeActivity;
import com.beesham.beerac.ui.HomeFragment;
import com.beesham.beerac.ui.Utils;
import com.squareup.picasso.Picasso;

/**
 * Implementation of App Widget functionality.
 */
public class BeerACWidgetProvider extends AppWidgetProvider {

    public static final String INC_BEER_COUNT_ACTION = "com.beesham.beerac.beerac_widget.INC_BEER_COUNT_ACTION";
    public static final String DEC_BEER_COUNT_ACTION = "com.beesham.beerac.beerac_widget.DEC_BEER_COUNT_ACTION";

    private RemoteViews mRemoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.beer_acwidget);

        for (int appWidgetId : appWidgetIds) {

            mRemoteViews.setTextViewText(R.id.total_beers_text_view,
                    context.getString(R.string.beers_had,
                    context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE)
                            .getInt(context.getString(R.string.beer_count_key), 0)));

            mRemoteViews.setTextViewText(R.id.bac_text_view,
                    context.getString(R.string.bac_format,
                    Double.longBitsToDouble(
                            context.getSharedPreferences(context.getString(R.string.pref_file),
                                    Context.MODE_PRIVATE)
                            .getLong(context.getString(R.string.bac_key), 0))));



            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            mRemoteViews.setOnClickPendingIntent(R.id.photo, pendingIntent);

            //Inc beer count button
            Intent incBeerCountIntent = new Intent(context, BeerACWidgetProvider.class);
            incBeerCountIntent.setAction(BeerACWidgetProvider.INC_BEER_COUNT_ACTION);

            PendingIntent incBeerPendingIntent = PendingIntent.getBroadcast(context, 0, incBeerCountIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mRemoteViews.setOnClickPendingIntent(R.id.increment_beers_button, incBeerPendingIntent);

            //Dec beer count button
            Intent decBeerCountIntent = new Intent(context, BeerACWidgetProvider.class);
            decBeerCountIntent.setAction(BeerACWidgetProvider.DEC_BEER_COUNT_ACTION);

            PendingIntent decBeerPendingIntent = PendingIntent.getBroadcast(context, 0, decBeerCountIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mRemoteViews.setOnClickPendingIntent(R.id.decrement_beers_button, decBeerPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
        }

        loadImage(context, R.id.photo, appWidgetIds);
    }

    private void loadImage(Context context, int imageId, int[] appWidgetIds){

        final String[] projections = {
                Columns.SavedBeerColumns.BEERID,
                Columns.SavedBeerColumns.LABELS,
                Columns.SavedBeerColumns.IMAGEURLMEDIUM
        };

        Cursor c = context.getContentResolver().query(
                BeerProvider.SavedBeers.CONTENT_URI,
                projections,
                Columns.SavedBeerColumns.BEERID + "=?",
                new String[]{context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE)
                .getString(context.getString(R.string.preferred_beer_key),
                        context.getString(R.string.default_preferred_beer))},
                null);

        if(c.moveToFirst()) {
            if (c.getString(c.getColumnIndex(Columns.SavedBeerColumns.LABELS)).equals("Y")) {
                Picasso.with(context)
                        .load(c.getString(c.getColumnIndex(Columns.SavedBeerColumns.IMAGEURLMEDIUM)))
                        .into(mRemoteViews, imageId, appWidgetIds);
            }
        }
        else{
            Picasso.with(context)
                    .load(R.mipmap.ic_launcher)
                    .into(mRemoteViews, imageId, appWidgetIds);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        switch (intent.getAction()){
            case INC_BEER_COUNT_ACTION:
                Utils.adjustBeerCount(context, HomeFragment.INC_BEER,
                        context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE)
                        .getInt(context.getString(R.string.beer_count_key), 0));

                Utils.storeBAC(context, Utils.getBac(context));
                Utils.updateWidget(context);
                break;

            case DEC_BEER_COUNT_ACTION:
                Utils.adjustBeerCount(context, HomeFragment.DEC_BEER,
                        context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE)
                                .getInt(context.getString(R.string.beer_count_key), 0));

                Utils.storeBAC(context, Utils.getBac(context));
                Utils.updateWidget(context);
                break;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

