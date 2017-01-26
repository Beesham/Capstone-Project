package com.beesham.beerac.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Beesham on 1/17/2017.
 */

@Database(version = BeerDatabase.VERSION)
public class BeerDatabase {
    public static final int VERSION = 1;

    @Table(Columns.SavedBeerColumns.class) public static final String SAVED_BEERS = "savedBeers";
    @Table(Columns.SavedBeerColumns.class) public static final String SEARCHED_BEERS = "searchedBeers";

}
