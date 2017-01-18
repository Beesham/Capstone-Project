package com.beesham.beerac.data;

import android.graphics.Path;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import static android.R.attr.path;

/**
 * Created by Beesham on 1/17/2017.
 */

@ContentProvider(authority = BeerProvider.AUTHORITY, database = BeerDatabase.class)
public class BeerProvider {
    public static final String AUTHORITY = "com.beesham.beerac.data.BeerProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

    interface Path{
        String BEERS = "beers";
    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for(String path:paths){
            builder.appendPath(path);
        }

        return builder.build();
    }

    @TableEndpoint(table = BeerDatabase.BEERS)
    public static class Beers{
        @ContentUri(
                path = Path.BEERS,
                type = "vnd.android.cursor.dir/beers"
        )
        public static final Uri CONTENT_URI = buildUri(Path.BEERS);

        @InexactContentUri(
                name = "BEER_ID",
                path = Path.BEERS + "/*",
                type = "vnd.android.cursor.item/beers",
                whereColumn = BeerColumns.NAME,
                pathSegment = 1
        )

        public static Uri withName(String beer_name){
            return buildUri(Path.BEERS, beer_name);
        }
    }
}
