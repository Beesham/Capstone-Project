package com.beesham.beerac.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.beesham.beerac.R;
import com.beesham.beerac.ui.HomeActivity;
import com.beesham.beerac.ui.Utils;

/**
 * Implementation of App Widget functionality.
 */
public class BeerACWidgetProvider extends AppWidgetProvider {

    public static final String INC_BEER_COUNT_ACTION = "com.beesham.beerac.beerac_widget.INC_BEER_COUNT_ACTION";
    public static final String DEC_BEER_COUNT_ACTION = "com.beesham.beerac.beerac_widget.DEC_BEER_COUNT_ACTION";

    private RemoteViews mRemoteViews;
    private int mBeerCount;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.beer_acwidget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.beer_acwidget);

        for (int appWidgetId : appWidgetIds) {

            mRemoteViews.setTextViewText(R.id.total_beers_text_view,
                    Integer.toString(context.getSharedPreferences(
                            context.getString(R.string.pref_file),
                            Context.MODE_PRIVATE)
                            .getInt(context.getString(R.string.beer_count_key), 0)));

            mRemoteViews.setTextViewText(R.id.bac_text_view,
                    Double.toString(Double.longBitsToDouble(context.getSharedPreferences(
                            context.getString(R.string.pref_file),
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
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        switch (intent.getAction()){
            case INC_BEER_COUNT_ACTION:

                break;

            case DEC_BEER_COUNT_ACTION:

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

