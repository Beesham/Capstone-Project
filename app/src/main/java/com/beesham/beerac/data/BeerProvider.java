package com.beesham.beerac.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Beesham on 1/17/2017.
 */

@ContentProvider(authority = BeerProvider.AUTHORITY, database = BeerDatabase.class)
public class BeerProvider {
    public static final String AUTHORITY = "com.beesham.beerac.data.BeerProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

    interface Path{
        String SAVED_BEERS = "saved_beers";
        String SEARCHED_BEERS = "searched_beers";

    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for(String path:paths){
            builder.appendPath(path);
        }

        return builder.build();
    }

    @TableEndpoint(table = BeerDatabase.SAVED_BEERS)
    public static class SavedBeers{
        @ContentUri(
                path = Path.SAVED_BEERS,
                type = "vnd.android.cursor.dir/beers"
        )
        public static final Uri CONTENT_URI = buildUri(Path.SAVED_BEERS);

        @InexactContentUri(
                name = "BEER_ID",
                path = Path.SAVED_BEERS + "/*",
                type = "vnd.android.cursor.item/beers",
                whereColumn = Columns.SavedBeerColumns.NAME,
                pathSegment = 1
        )

        public static Uri withName(String beer_name){
            return buildUri(Path.SAVED_BEERS, beer_name);
        }
    }

    @TableEndpoint(table = BeerDatabase.SEARCHED_BEERS)
    public static class SearchedBeers{
        @ContentUri(
                path = Path.SEARCHED_BEERS,
                type = "vnd.android.cursor.dir/beers"
        )
        public static final Uri CONTENT_URI = buildUri(Path.SEARCHED_BEERS);

        @InexactContentUri(
                name = "BEER_ID",
                path = Path.SEARCHED_BEERS + "/*",
                type = "vnd.android.cursor.item/beers",
                whereColumn = Columns.SearchedBeerColumns.NAME,
                pathSegment = 1
        )

        public static Uri withName(String beer_name){
            return buildUri(Path.SEARCHED_BEERS, beer_name);
        }
    }
}
