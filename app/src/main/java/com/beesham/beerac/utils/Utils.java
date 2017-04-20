package com.beesham.beerac.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.beesham.beerac.R;
import com.beesham.beerac.ui.HomeFragment;
import com.beesham.beerac.widget.BeerACWidgetProvider;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static com.beesham.beerac.utils.MathUtils.timeInMillis;

/**
 * Created by beesham on 21/01/17.
 * Utils class contains functions that are referenced multiple times
 * across the app
 */

public class Utils {

    public static String getCalculationsAsString(){



        return null;
    }

    public static double getTimePassed(Context context){
        Calendar calendar = Calendar.getInstance();
        long elapsedTime = 0;

        long startedDrinkingTime = context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .getLong(context.getString(R.string.start_drinking_time_key),
                        timeInMillis(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

        long curHourInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
        long curMinInMillis =  TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE));
        long curTimeInMillis = curHourInMillis + curMinInMillis;

        if(startedDrinkingTime > curTimeInMillis){
            elapsedTime = curTimeInMillis + (TimeUnit.HOURS.toMillis(24) - startedDrinkingTime);
        }else {
            elapsedTime = curTimeInMillis - startedDrinkingTime;
        }

        double elapsedHours =  TimeUnit.MILLISECONDS.toHours(elapsedTime);
        double elapsedMins = (TimeUnit.MILLISECONDS.toMinutes(elapsedTime)%60)/60.0;

        return (elapsedHours + elapsedMins);
    }

    /**
     * Stores the BAC in sharedPrefs
     * @param context
     * @param bac
     */
    public static void storeBAC(Context context, double bac){
        context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putLong(context.getString(R.string.bac_key), Double.doubleToLongBits(bac))
                .apply();
    }

    public static int adjustBeerCount(Context context, int incDecFlag, int beerCount){

        switch (incDecFlag){
            case HomeFragment.INC_BEER_FLAG:
                beerCount++;
                break;

            case HomeFragment.DEC_BEER_FLAG:
                if(beerCount != 0) {
                    beerCount--;
                }
                break;
        }

        context.getSharedPreferences(context.getString(R.string.pref_file),
                MODE_PRIVATE)
                .edit()
                .putInt(context.getString(R.string.beer_count_key), beerCount)
                .apply();

        return beerCount;
    }

    public static void updateWidget(Context context){
        int widgetIds[] = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, BeerACWidgetProvider.class));

        Intent intent = new Intent(context, BeerACWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(intent);
    }

    /**
     * Checks if the weight preferences is empty
     * @param context
     * @return true if weight is empty, else false
     */
    public static boolean checkForEmptyWeightPref(Context context){
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return TextUtils.isEmpty(defaultPreferences.getString(context.getString(R.string.pref_body_weight_key),
                null));
    }

    /**
     * checks if app is launched for first time or a consequent
     * launch
     * @param context
     * @return
     */
    public static boolean checkForFirstLaunch(Context context){
        final String PREF_VERSION_CODE_KEY = context.getString(R.string.pref_version_code_key);
        final int NONE_EXIST = -1;
        int currentVersionCode = 0;
        boolean status = true;

        try{
            currentVersionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.pref_file),
                MODE_PRIVATE);
        int savedVersionCode = preferences.getInt(PREF_VERSION_CODE_KEY, NONE_EXIST);

        if(currentVersionCode == savedVersionCode){
            status = false;
        }else if(currentVersionCode == NONE_EXIST){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            editor.apply();
            status = true;    //New install, first launch, user cleared app data
        }else if(currentVersionCode > savedVersionCode){
            //Place upgrade code here
            status = true;
        }

        preferences.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode)
                .commit();
        return status;
    }

    /**
     * Checks for network availability
     * @param context to get ConnectivityManager
     * @returns true is network is available
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
